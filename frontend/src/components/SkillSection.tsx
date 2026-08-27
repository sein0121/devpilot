import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { SkillCategoryItem, SkillItem, SkillStatus } from '../api/types';

export function SkillSection() {
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [categoryId, setCategoryId] = useState<string>('');
  const [status, setStatus] = useState<SkillStatus>('LEARNING');
  const [proficiency, setProficiency] = useState(3);
  const [editingId, setEditingId] = useState<number | null>(null);

  const { data: skills } = useQuery({
    queryKey: ['skills'],
    queryFn: () => api.get<SkillItem[]>('/api/skills'),
  });

  const { data: categories } = useQuery({
    queryKey: ['skill-categories'],
    queryFn: () => api.get<SkillCategoryItem[]>('/api/skill-categories'),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['skills'] });

  const createMutation = useMutation({
    mutationFn: () =>
      api.post('/api/skills', {
        name,
        categoryId: categoryId ? Number(categoryId) : null,
        status,
        proficiency,
      }),
    onSuccess: () => {
      setName('');
      setCategoryId('');
      setProficiency(3);
      invalidate();
    },
  });

  const updateMutation = useMutation({
    mutationFn: (payload: {
      id: number;
      name: string;
      categoryId: string;
      status: SkillStatus;
      proficiency: number;
    }) =>
      api.put(`/api/skills/${payload.id}`, {
        name: payload.name,
        categoryId: payload.categoryId ? Number(payload.categoryId) : null,
        status: payload.status,
        proficiency: payload.proficiency,
      }),
    onSuccess: () => {
      setEditingId(null);
      invalidate();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/skills/${id}`),
    onSuccess: invalidate,
  });

  const moveMutation = useMutation({
    mutationFn: (orderedSkillIds: number[]) =>
      api.put('/api/skills/reorder', { orderedSkillIds }),
    onSuccess: invalidate,
  });

  function moveSkill(index: number, direction: -1 | 1) {
    if (!skills) return;
    const target = index + direction;
    if (target < 0 || target >= skills.length) return;

    const ids = skills.map((s) => s.id);
    [ids[index], ids[target]] = [ids[target], ids[index]];
    moveMutation.mutate(ids);
  }

  // 최상위 카테고리 밑에 자식만 선택 옵션으로 노출 (부모 카테고리 자체는 선택 대상에서 제외)
  const selectableCategories = categories?.filter((c) => c.parentId === null) ?? [];

  return (
    <div className="card">
      <p className="card-title">Skills</p>

      <div className="skill-form">
        <input
          className="skill-input"
          placeholder="기술 이름 (예: Spring)"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <select className="skill-select" value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
          <option value="">카테고리 없음</option>
          {selectableCategories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <select className="skill-select" value={status} onChange={(e) => setStatus(e.target.value as SkillStatus)}>
          <option value="PROFICIENT">보유 기술</option>
          <option value="LEARNING">학습 중</option>
        </select>
        <select
          className="skill-select"
          value={proficiency}
          onChange={(e) => setProficiency(Number(e.target.value))}
        >
          {[1, 2, 3, 4, 5].map((n) => (
            <option key={n} value={n}>{'★'.repeat(n)}</option>
          ))}
        </select>
        <button
          className="btn btn-primary"
          onClick={() => name.trim() && createMutation.mutate()}
          disabled={createMutation.isPending || !name.trim()}
        >
          추가
        </button>
      </div>

      <ul className="skill-list">
        {skills?.map((skill, index) =>
          editingId === skill.id ? (
            <SkillEditRow
              key={skill.id}
              skill={skill}
              categories={selectableCategories}
              isSaving={updateMutation.isPending}
              onCancel={() => setEditingId(null)}
              onSave={(payload) => updateMutation.mutate({ id: skill.id, ...payload })}
            />
          ) : (
            <li key={skill.id} className="skill-item" onClick={() => setEditingId(skill.id)}>
              <span className={`skill-badge ${skill.status === 'PROFICIENT' ? 'proficient' : 'learning'}`}>
                {skill.status === 'PROFICIENT' ? '보유' : '학습중'}
              </span>
              <span className="skill-name">{skill.name}</span>
              {skill.categoryName && <span className="tag">{skill.categoryName}</span>}
              <span className="skill-stars">{'★'.repeat(skill.proficiency)}</span>
              <div className="skill-actions" onClick={(e) => e.stopPropagation()}>
                <button className="btn-icon" onClick={() => moveSkill(index, -1)} disabled={index === 0}>▲</button>
                <button className="btn-icon" onClick={() => moveSkill(index, 1)} disabled={index === (skills.length - 1)}>▼</button>
                <button
                  className="btn-icon danger"
                  onClick={() => {
                    if (window.confirm(`"${skill.name}" 스킬을 삭제할까요?`)) {
                      deleteMutation.mutate(skill.id);
                    }
                  }}
                >
                  ✕
                </button>
              </div>
            </li>
          )
        )}
        {skills?.length === 0 && <div className="empty-state">아직 등록한 스킬이 없어요.</div>}
      </ul>
    </div>
  );
}

function SkillEditRow({
  skill,
  categories,
  isSaving,
  onCancel,
  onSave,
}: {
  skill: SkillItem;
  categories: SkillCategoryItem[];
  isSaving: boolean;
  onCancel: () => void;
  onSave: (payload: { name: string; categoryId: string; status: SkillStatus; proficiency: number }) => void;
}) {
  const initialCategoryId = categories.find((c) => c.name === skill.categoryName)?.id;
  const [name, setName] = useState(skill.name);
  const [categoryId, setCategoryId] = useState(initialCategoryId ? String(initialCategoryId) : '');
  const [status, setStatus] = useState<SkillStatus>(skill.status);
  const [proficiency, setProficiency] = useState(skill.proficiency);

  return (
    <li className="skill-item skill-item-editing" onClick={(e) => e.stopPropagation()}>
      <div className="skill-edit-row">
        <input
          className="skill-input"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <select className="skill-select" value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
          <option value="">카테고리 없음</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <select className="skill-select" value={status} onChange={(e) => setStatus(e.target.value as SkillStatus)}>
          <option value="PROFICIENT">보유 기술</option>
          <option value="LEARNING">학습 중</option>
        </select>
        <select
          className="skill-select"
          value={proficiency}
          onChange={(e) => setProficiency(Number(e.target.value))}
        >
          {[1, 2, 3, 4, 5].map((n) => (
            <option key={n} value={n}>{'★'.repeat(n)}</option>
          ))}
        </select>
      </div>
      <div className="studylog-today-actions">
        <button
          className="btn btn-primary"
          disabled={!name.trim() || isSaving}
          onClick={() => onSave({ name, categoryId, status, proficiency })}
        >
          저장
        </button>
        <button className="btn" onClick={onCancel}>
          취소
        </button>
      </div>
    </li>
  );
}

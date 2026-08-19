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
        {skills?.map((skill, index) => (
          <li key={skill.id} className="skill-item">
            <span className={`skill-badge ${skill.status === 'PROFICIENT' ? 'proficient' : 'learning'}`}>
              {skill.status === 'PROFICIENT' ? '보유' : '학습중'}
            </span>
            <span className="skill-name">{skill.name}</span>
            {skill.categoryName && <span className="tag">{skill.categoryName}</span>}
            <span className="skill-stars">{'★'.repeat(skill.proficiency)}</span>
            <div className="skill-actions">
              <button className="btn-icon" onClick={() => moveSkill(index, -1)} disabled={index === 0}>▲</button>
              <button className="btn-icon" onClick={() => moveSkill(index, 1)} disabled={index === (skills.length - 1)}>▼</button>
              <button className="btn-icon danger" onClick={() => deleteMutation.mutate(skill.id)}>✕</button>
            </div>
          </li>
        ))}
        {skills?.length === 0 && <div className="empty-state">아직 등록한 스킬이 없어요.</div>}
      </ul>
    </div>
  );
}
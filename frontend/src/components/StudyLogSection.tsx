import { useState, useMemo, type MouseEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import ReactMarkdown from 'react-markdown';
import { api } from '../api/client';
import type { SkillItem, StudyLogItem } from '../api/types';

const PAGE_SIZE = 5;

function todayISO() {
  const d = new Date();
  const offset = d.getTimezoneOffset();
  return new Date(d.getTime() - offset * 60000).toISOString().slice(0, 10);
}

export function StudyLogSection() {
  const queryClient = useQueryClient();

  const [editingDate, setEditingDate] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [selectedSkillIds, setSelectedSkillIds] = useState<number[]>([]);
  const [newDate, setNewDate] = useState(todayISO());
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');

  const { data: skills } = useQuery({
    queryKey: ['skills'],
    queryFn: () => api.get<SkillItem[]>('/api/skills'),
  });

  const { data: logs } = useQuery({
    queryKey: ['study-logs'],
    queryFn: () => api.get<StudyLogItem[]>('/api/study-logs'),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['study-logs'] });

  const saveMutation = useMutation({
    mutationFn: (date: string) =>
      api.put(`/api/study-logs/${date}`, { title, content, skillIds: selectedSkillIds }),
    onSuccess: () => {
      invalidate();
      closeForm();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (date: string) => api.delete(`/api/study-logs/${date}`),
    onSuccess: () => {
      invalidate();
      closeForm();
    },
  });

  function closeForm() {
    setEditingDate(null);
    setIsCreating(false);
    setTitle('');
    setContent('');
    setSelectedSkillIds([]);
  }

  function loadExisting(date: string) {
    const existing = logs?.find((l) => l.date === date);
    if (existing) {
      setTitle(existing.title);
      setContent(existing.content);
      const ids =
        skills?.filter((s) => existing.skillNames.includes(s.name)).map((s) => s.id) ?? [];
      setSelectedSkillIds(ids);
    } else {
      setTitle('');
      setContent('');
      setSelectedSkillIds([]);
    }
  }

  function startCreate() {
    closeForm();
    setIsCreating(true);
    const today = todayISO();
    setNewDate(today);
    loadExisting(today);
  }

  function handleDateChange(date: string) {
    setNewDate(date);
    loadExisting(date);
  }

  function startEdit(log: StudyLogItem) {
    closeForm();
    setEditingDate(log.date);
    setTitle(log.title);
    setContent(log.content);
    const ids = skills?.filter((s) => log.skillNames.includes(s.name)).map((s) => s.id) ?? [];
    setSelectedSkillIds(ids);
  }

  function toggleSkill(id: number) {
    setSelectedSkillIds((prev) =>
      prev.includes(id) ? prev.filter((s) => s !== id) : [...prev, id]
    );
  }

  function handleQuickDelete(e: MouseEvent, date: string) {
    e.stopPropagation();
    if (window.confirm(`"${date}" 기록을 삭제할까요?`)) {
      deleteMutation.mutate(date);
    }
  }

  function handleKeywordChange(value: string) {
    setKeyword(value);
    setPage(0); // 검색어 바뀌면 항상 1페이지로 리셋
  }

  const sorted = useMemo(
    () => [...(logs ?? [])].sort((a, b) => b.date.localeCompare(a.date)),
    [logs]
  );

  const filtered = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    if (!q) return sorted;
    return sorted.filter(
      (log) => log.title.toLowerCase().includes(q) || log.date.includes(q)
    );
  }, [sorted, keyword]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages - 1);
  const pageItems = filtered.slice(currentPage * PAGE_SIZE, currentPage * PAGE_SIZE + PAGE_SIZE);

  const formOpen = isCreating || editingDate !== null;
  const isOverwriting = isCreating && logs?.some((l) => l.date === newDate);
  const canSave = title.trim() && content.trim();

  return (
    <div className="card">
      <div className="card-header">
        <p className="card-title">Study Log</p>
        {!formOpen && (
          <button className="btn btn-primary" onClick={startCreate}>
            + 새 기록
          </button>
        )}
      </div>

      {formOpen ? (
        <div className="studylog-form">
          <div className="studylog-form-topbar">
            <button className="studylog-back-arrow" onClick={closeForm} aria-label="목록으로">
              ←
            </button>
            {isCreating && (
              <input
                type="date"
                className="studylog-date-input"
                value={newDate}
                max={todayISO()}
                onChange={(e) => handleDateChange(e.target.value)}
              />
            )}
            {editingDate && <div className="studylog-today-date">{editingDate}</div>}
          </div>

          {isOverwriting && (
            <div className="studylog-existing-notice">
              이 날짜엔 이미 기록이 있어요. 저장하면 기존 내용이 수정됩니다.
            </div>
          )}

          <input
            className="studylog-title-input"
            placeholder="제목"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />

          <textarea
            className="studylog-textarea"
            placeholder="무엇을 공부했나요? (마크다운 지원: **굵게**, - 목록, # 제목, `코드` 등)"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={5}
          />

          {skills && skills.length > 0 && (
            <div className="skill-chip-row">
              {skills.map((s) => (
                <button
                  key={s.id}
                  className={`skill-chip ${selectedSkillIds.includes(s.id) ? 'active' : ''}`}
                  onClick={() => toggleSkill(s.id)}
                >
                  {s.name}
                </button>
              ))}
            </div>
          )}

          <div className="studylog-today-actions">
            <button
              className="btn btn-primary"
              disabled={!canSave || saveMutation.isPending}
              onClick={() => saveMutation.mutate(isCreating ? newDate : editingDate!)}
            >
              저장
            </button>
            {editingDate && (
              <button
                className="btn btn-logout"
                onClick={() => deleteMutation.mutate(editingDate)}
                disabled={deleteMutation.isPending}
              >
                삭제
              </button>
            )}
            <button className="btn" onClick={closeForm}>
              취소
            </button>
          </div>

          {content.trim() && (
            <div className="studylog-preview">
              <div className="studylog-preview-label">미리보기</div>
              <div className="markdown-body">
                <ReactMarkdown>{content}</ReactMarkdown>
              </div>
            </div>
          )}
        </div>
      ) : (
        <>
          {sorted.length > 0 && (
            <input
              className="studylog-search-input"
              placeholder="제목 또는 날짜(예: 2026-08)로 검색"
              value={keyword}
              onChange={(e) => handleKeywordChange(e.target.value)}
            />
          )}

          {sorted.length === 0 && <div className="empty-state">아직 학습 기록이 없어요.</div>}

          {sorted.length > 0 && filtered.length === 0 && (
            <div className="empty-state">검색 결과가 없어요.</div>
          )}

          {pageItems.length > 0 && (
            <>
              <ul className="studylog-row-list">
                {pageItems.map((log) => (
                  <li
                    key={log.id}
                    className="studylog-row clickable"
                    onClick={() => startEdit(log)}
                  >
                    <span className="studylog-row-title">{log.title}</span>
                    <span className="studylog-row-date">{log.date}</span>
                    <button
                      className="btn-icon danger"
                      onClick={(e) => handleQuickDelete(e, log.date)}
                      title="삭제"
                    >
                      ✕
                    </button>
                  </li>
                ))}
              </ul>

              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    className="btn-icon"
                    disabled={currentPage === 0}
                    onClick={() => setPage(currentPage - 1)}
                  >
                    ‹
                  </button>
                  {Array.from({ length: totalPages }, (_, i) => i).map((p) => (
                    <button
                      key={p}
                      className={`page-num-btn ${p === currentPage ? 'active' : ''}`}
                      onClick={() => setPage(p)}
                    >
                      {p + 1}
                    </button>
                  ))}
                  <button
                    className="btn-icon"
                    disabled={currentPage >= totalPages - 1}
                    onClick={() => setPage(currentPage + 1)}
                  >
                    ›
                  </button>
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
}

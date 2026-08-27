import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type {
  AiRoadmapDraftResponse,
  AiRoadmapStepDraft,
  RoadmapDetail,
  RoadmapLinkItem,
  RoadmapStepItem,
  RoadmapStepStatus,
  RoadmapSummary,
  SkillItem,
} from '../api/types';

const STATUS_LABEL: Record<RoadmapStepStatus, string> = {
  TODO: '예정',
  IN_PROGRESS: '진행중',
  DONE: '완료',
};

export function RoadmapSection() {
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showAiFlow, setShowAiFlow] = useState(false);

  const { data: roadmaps } = useQuery({
    queryKey: ['roadmaps'],
    queryFn: () => api.get<RoadmapSummary[]>('/api/roadmaps'),
  });

  const invalidateList = () => queryClient.invalidateQueries({ queryKey: ['roadmaps'] });

  if (selectedId !== null) {
    return (
      <RoadmapDetailView
        roadmapId={selectedId}
        onBack={() => {
          setSelectedId(null);
          invalidateList();
        }}
        onDeleted={() => {
          setSelectedId(null);
          invalidateList();
        }}
      />
    );
  }

  const anyFormOpen = showCreateForm || showAiFlow;

  return (
    <div className="card">
      <div className="card-header">
        <p className="card-title">Roadmap</p>
        {!anyFormOpen && (
          <div className="roadmap-header-actions">
            <button className="btn" onClick={() => setShowAiFlow(true)}>
              ✨ AI로 만들기
            </button>
            <button className="btn btn-primary" onClick={() => setShowCreateForm(true)}>
              + 새 로드맵
            </button>
          </div>
        )}
      </div>

      {showCreateForm && (
        <CreateRoadmapForm
          onCancel={() => setShowCreateForm(false)}
          onCreated={(id) => {
            setShowCreateForm(false);
            invalidateList();
            setSelectedId(id);
          }}
        />
      )}

      {showAiFlow && (
        <AiDraftFlow
          onCancel={() => setShowAiFlow(false)}
          onCreated={(id) => {
            setShowAiFlow(false);
            invalidateList();
            setSelectedId(id);
          }}
        />
      )}

      {!anyFormOpen && (!roadmaps || roadmaps.length === 0) && (
        <div className="empty-state">아직 만든 로드맵이 없어요.</div>
      )}

      {!anyFormOpen && roadmaps && roadmaps.length > 0 && (
        <ul className="roadmap-list">
          {roadmaps.map((r) => (
            <li key={r.id} className="roadmap-card" onClick={() => setSelectedId(r.id)}>
              <div className="roadmap-card-top">
                <span className="roadmap-card-title">{r.title}</span>
                {r.targetDate && <span className="roadmap-card-date">~{r.targetDate}</span>}
              </div>
              <div className="progress-bar-track">
                <div className="progress-bar-fill" style={{ width: `${r.progress}%` }} />
              </div>
              <div className="roadmap-card-meta">
                {r.doneSteps} / {r.totalSteps} 완료 · {r.progress}%
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

interface EditableDraftStep extends AiRoadmapStepDraft {
  skillId: string; // select value로 쓰기 위해 string, '' = 미선택
}

function AiDraftFlow({
  onCancel,
  onCreated,
}: {
  onCancel: () => void;
  onCreated: (id: number) => void;
}) {
  const [phase, setPhase] = useState<'input' | 'review'>('input');
  const [goal, setGoal] = useState('');
  const [targetDate, setTargetDate] = useState('');
  const [roadmapTitle, setRoadmapTitle] = useState('');
  const [draftSteps, setDraftSteps] = useState<EditableDraftStep[]>([]);

  const { data: skills } = useQuery({
    queryKey: ['skills'],
    queryFn: () => api.get<SkillItem[]>('/api/skills'),
  });

  const generateMutation = useMutation({
    mutationFn: () =>
      api.post<AiRoadmapDraftResponse>('/api/ai/roadmap-drafts', {
        goal,
        targetDate: targetDate || null,
      }),
    onSuccess: (res) => {
      setDraftSteps(
        res.steps.map((s) => ({
          ...s,
          skillId: s.matchedSkillId ? String(s.matchedSkillId) : '',
        }))
      );
      setRoadmapTitle(goal.length > 40 ? goal.slice(0, 40) + '...' : goal);
      setPhase('review');
    },
  });

  const confirmMutation = useMutation({
    mutationFn: async () => {
      const roadmap = await api.post<RoadmapSummary>('/api/roadmaps', {
        title: roadmapTitle,
        description: goal,
        targetDate: targetDate || null,
      });

      for (const step of draftSteps) {
        await api.post(`/api/roadmaps/${roadmap.id}/steps`, {
          skillId: Number(step.skillId),
          title: step.title,
          description: step.description,
          targetDate: null,
          link: null,
        });
      }

      return roadmap;
    },
    onSuccess: (roadmap) => onCreated(roadmap.id),
  });

  function updateStep(index: number, patch: Partial<EditableDraftStep>) {
    setDraftSteps((prev) => prev.map((s, i) => (i === index ? { ...s, ...patch } : s)));
  }

  function removeStep(index: number) {
    setDraftSteps((prev) => prev.filter((_, i) => i !== index));
  }

  function addStep() {
    setDraftSteps((prev) => [
      ...prev,
      { title: '', description: '', suggestedSkillName: '', matchedSkillId: null, skillId: '' },
    ]);
  }

  const allStepsValid =
    draftSteps.length > 0 && draftSteps.every((s) => s.skillId && s.title.trim());
  const canConfirm = roadmapTitle.trim() && allStepsValid;

  if (phase === 'input') {
    return (
      <div className="studylog-form">
        <div className="card-header" style={{ marginBottom: '0.75rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <button className="btn-icon" onClick={onCancel} aria-label="취소">
              ←
            </button>
            <p className="card-title" style={{ margin: 0 }}>AI로 로드맵 만들기</p>
          </div>
        </div>

        <label className="field-label">목표</label>
        <textarea
          className="studylog-textarea"
          placeholder="예: 백엔드 주니어에서 시니어로 성장하고 싶어요"
          rows={3}
          value={goal}
          onChange={(e) => setGoal(e.target.value)}
        />

        <label className="field-label">목표 마감일 (선택)</label>
        <input
          type="date"
          className="studylog-date-input"
          value={targetDate}
          onChange={(e) => setTargetDate(e.target.value)}
        />

        {generateMutation.isError && (
          <div className="ai-error-notice">{(generateMutation.error as Error).message}</div>
        )}

        <div className="studylog-today-actions">
          <button
            className="btn btn-primary"
            disabled={!goal.trim() || generateMutation.isPending}
            onClick={() => generateMutation.mutate()}
          >
            {generateMutation.isPending ? 'AI가 생각하는 중... (최대 30초)' : '초안 생성'}
          </button>
          <button className="btn" onClick={onCancel}>
            취소
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="studylog-form">
      <div className="card-header" style={{ marginBottom: '0.75rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button className="btn-icon" onClick={onCancel} aria-label="취소">
            ←
          </button>
          <p className="card-title" style={{ margin: 0 }}>AI 초안 검토</p>
        </div>
      </div>

      <div className="ai-review-notice">AI가 만든 초안이에요. 검토하고 자유롭게 수정해주세요.</div>

      <label className="field-label">로드맵 제목</label>
      <input
        className="studylog-title-input"
        value={roadmapTitle}
        onChange={(e) => setRoadmapTitle(e.target.value)}
      />

      <div className="card-header" style={{ marginBottom: '0.4rem' }}>
        <label className="field-label" style={{ marginBottom: 0 }}>단계 ({draftSteps.length}개)</label>
        <button className="btn" onClick={addStep}>
          + 단계 추가
        </button>
      </div>
      <ul className="ai-draft-list">
        {draftSteps.map((step, index) => (
          <li key={index} className="ai-draft-item">
            <div className="ai-draft-item-header">
              <input
                className="ai-draft-title-input"
                placeholder="단계 제목"
                value={step.title}
                onChange={(e) => updateStep(index, { title: e.target.value })}
              />
              <button
                className="btn-icon danger"
                onClick={() => removeStep(index)}
                aria-label="이 단계 삭제"
              >
                ✕
              </button>
            </div>
            <textarea
              className="studylog-textarea"
              rows={2}
              placeholder="설명 (선택)"
              value={step.description ?? ''}
              onChange={(e) => updateStep(index, { description: e.target.value })}
            />
            <select
              className={`skill-select ${!step.skillId ? 'unselected' : ''}`}
              value={step.skillId}
              onChange={(e) => updateStep(index, { skillId: e.target.value })}
            >
              <option value="">
                {step.suggestedSkillName
                  ? step.matchedSkillId
                    ? '기술 선택'
                    : `기술 선택 (AI 추천: ${step.suggestedSkillName})`
                  : '기술 선택 (필수)'}
              </option>
              {skills?.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
            {!step.skillId && step.suggestedSkillName && (
              <div className="ai-draft-skill-hint">
                “{step.suggestedSkillName}”이(가) 아직 Skill 목록에 없어요. 비슷한 기술을 선택하거나
                Skills 페이지에서 먼저 추가해주세요.
              </div>
            )}
          </li>
        ))}
      </ul>

      {draftSteps.length === 0 && (
        <div className="empty-state">모든 단계를 삭제했어요. 다시 생성하거나 취소해주세요.</div>
      )}

      <div className="studylog-today-actions">
        <button
          className="btn btn-primary"
          disabled={!canConfirm || confirmMutation.isPending}
          onClick={() => confirmMutation.mutate()}
        >
          {confirmMutation.isPending ? '만드는 중...' : '로드맵 만들기'}
        </button>
        <button className="btn" onClick={onCancel}>
          취소
        </button>
      </div>
    </div>
  );
}

function CreateRoadmapForm({
  onCancel,
  onCreated,
}: {
  onCancel: () => void;
  onCreated: (id: number) => void;
}) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [targetDate, setTargetDate] = useState('');

  const createMutation = useMutation({
    mutationFn: () =>
      api.post<RoadmapSummary>('/api/roadmaps', {
        title,
        description: description || null,
        targetDate: targetDate || null,
      }),
    onSuccess: (created) => onCreated(created.id),
  });

  return (
    <div className="studylog-form">
      <label className="field-label">로드맵 제목</label>
      <input
        className="studylog-title-input"
        placeholder="예: 백엔드 주니어 → 시니어"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
      />

      <label className="field-label">설명 (선택)</label>
      <textarea
        className="studylog-textarea"
        placeholder="이 로드맵의 목표를 간단히 적어주세요"
        rows={2}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <label className="field-label">목표 마감일 (선택)</label>
      <input
        type="date"
        className="studylog-date-input"
        value={targetDate}
        onChange={(e) => setTargetDate(e.target.value)}
      />

      <div className="studylog-today-actions">
        <button
          className="btn btn-primary"
          disabled={!title.trim() || createMutation.isPending}
          onClick={() => createMutation.mutate()}
        >
          만들기
        </button>
        <button className="btn" onClick={onCancel}>
          취소
        </button>
      </div>
    </div>
  );
}

function EditRoadmapForm({
  roadmap,
  onCancel,
  onSaved,
}: {
  roadmap: RoadmapDetail;
  onCancel: () => void;
  onSaved: () => void;
}) {
  const [title, setTitle] = useState(roadmap.title);
  const [description, setDescription] = useState(roadmap.description ?? '');
  const [targetDate, setTargetDate] = useState(roadmap.targetDate ?? '');

  const updateMutation = useMutation({
    mutationFn: () =>
      api.put(`/api/roadmaps/${roadmap.id}`, {
        title,
        description: description || null,
        targetDate: targetDate || null,
      }),
    onSuccess: onSaved,
  });

  return (
    <div className="studylog-form">
      <label className="field-label">로드맵 제목</label>
      <input
        className="studylog-title-input"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
      />

      <label className="field-label">설명 (선택)</label>
      <textarea
        className="studylog-textarea"
        rows={2}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <label className="field-label">목표 마감일 (선택)</label>
      <input
        type="date"
        className="studylog-date-input"
        value={targetDate}
        onChange={(e) => setTargetDate(e.target.value)}
      />

      <div className="studylog-today-actions">
        <button
          className="btn btn-primary"
          disabled={!title.trim() || updateMutation.isPending}
          onClick={() => updateMutation.mutate()}
        >
          저장
        </button>
        <button className="btn" onClick={onCancel}>
          취소
        </button>
      </div>
    </div>
  );
}

function RoadmapDetailView({
  roadmapId,
  onBack,
  onDeleted,
}: {
  roadmapId: number;
  onBack: () => void;
  onDeleted: () => void;
}) {
  const queryClient = useQueryClient();
  const [showStepForm, setShowStepForm] = useState(false);
  const [editingRoadmap, setEditingRoadmap] = useState(false);
  const [editingStepId, setEditingStepId] = useState<number | null>(null);

  const { data: roadmap } = useQuery({
    queryKey: ['roadmaps', roadmapId],
    queryFn: () => api.get<RoadmapDetail>(`/api/roadmaps/${roadmapId}`),
  });

  const { data: skills } = useQuery({
    queryKey: ['skills'],
    queryFn: () => api.get<SkillItem[]>('/api/skills'),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['roadmaps', roadmapId] });

  const deleteRoadmapMutation = useMutation({
    mutationFn: () => api.delete(`/api/roadmaps/${roadmapId}`),
    onSuccess: onDeleted,
  });

  const addLinkMutation = useMutation({
    mutationFn: (payload: { url: string; label: string | null }) =>
      api.post(`/api/roadmaps/${roadmapId}/links`, payload),
    onSuccess: invalidate,
  });

  const deleteLinkMutation = useMutation({
    mutationFn: (linkId: number) => api.delete(`/api/roadmaps/${roadmapId}/links/${linkId}`),
    onSuccess: invalidate,
  });

  const changeStatusMutation = useMutation({
    mutationFn: ({ stepId, status }: { stepId: number; status: RoadmapStepStatus }) =>
      api.patch(`/api/roadmaps/${roadmapId}/steps/${stepId}/status`, { status }),
    onSuccess: invalidate,
  });

  const deleteStepMutation = useMutation({
    mutationFn: (stepId: number) => api.delete(`/api/roadmaps/${roadmapId}/steps/${stepId}`),
    onSuccess: invalidate,
  });

  const moveMutation = useMutation({
    mutationFn: (orderedStepIds: number[]) =>
      api.put(`/api/roadmaps/${roadmapId}/steps/reorder`, { orderedStepIds }),
    onSuccess: invalidate,
  });

  function moveStep(index: number, direction: -1 | 1) {
    if (!roadmap) return;
    const target = index + direction;
    if (target < 0 || target >= roadmap.steps.length) return;

    const ids = roadmap.steps.map((s) => s.id);
    [ids[index], ids[target]] = [ids[target], ids[index]];
    moveMutation.mutate(ids);
  }

  if (!roadmap) return <div className="card">불러오는 중...</div>;

  return (
    <div className="card">
      <div className="studylog-form-topbar">
        <button className="studylog-back-arrow" onClick={onBack} aria-label="목록으로">
          ←
        </button>
        {!editingRoadmap && <div className="roadmap-detail-title">{roadmap.title}</div>}
      </div>

      {editingRoadmap ? (
        <EditRoadmapForm
          roadmap={roadmap}
          onCancel={() => setEditingRoadmap(false)}
          onSaved={() => {
            setEditingRoadmap(false);
            invalidate();
          }}
        />
      ) : (
        <>
          {roadmap.description && <p className="roadmap-detail-desc">{roadmap.description}</p>}

          <div className="progress-bar-track">
            <div className="progress-bar-fill" style={{ width: `${roadmap.progress}%` }} />
          </div>
          <div className="roadmap-card-meta">
            {roadmap.progress}% 완료
            {roadmap.targetDate && ` · 목표 마감일 ${roadmap.targetDate}`}
          </div>

          <div className="studylog-today-actions" style={{ marginTop: '0.75rem' }}>
            <button className="btn" onClick={() => setEditingRoadmap(true)}>
              로드맵 수정
            </button>
            <button
              className="btn btn-logout"
              onClick={() => {
                if (window.confirm('이 로드맵을 삭제할까요? 안의 모든 단계도 함께 삭제됩니다.')) {
                  deleteRoadmapMutation.mutate();
                }
              }}
            >
              로드맵 삭제
            </button>
          </div>
        </>
      )}

      {!editingRoadmap && (
        <>
          <hr style={{ margin: '1rem 0', border: 'none', borderTop: '1px solid var(--border)' }} />
          <RoadmapLinksSection
            links={roadmap.links}
            onAdd={(url, label) => addLinkMutation.mutate({ url, label })}
            onDelete={(linkId) => deleteLinkMutation.mutate(linkId)}
          />
        </>
      )}

      <hr style={{ margin: '1rem 0', border: 'none', borderTop: '1px solid var(--border)' }} />

      <div className="card-header">
        <p className="card-title">Steps</p>
        {!showStepForm && (
          <button className="btn btn-primary" onClick={() => setShowStepForm(true)}>
            + 단계 추가
          </button>
        )}
      </div>

      {showStepForm && (
        <StepForm
          roadmapId={roadmapId}
          skills={skills ?? []}
          onCancel={() => setShowStepForm(false)}
          onSaved={() => {
            setShowStepForm(false);
            invalidate();
          }}
        />
      )}

      {roadmap.steps.length === 0 && !showStepForm && (
        <div className="empty-state">아직 등록한 단계가 없어요.</div>
      )}

      {roadmap.steps.length > 0 && (
        <ul className="roadmap-step-list">
          {roadmap.steps.map((step, index) =>
            editingStepId === step.id ? (
              <li key={step.id} className="roadmap-step-item editing">
                <StepForm
                  roadmapId={roadmapId}
                  skills={skills ?? []}
                  initial={step}
                  onCancel={() => setEditingStepId(null)}
                  onSaved={() => {
                    setEditingStepId(null);
                    invalidate();
                  }}
                />
              </li>
            ) : (
              <li key={step.id} className="roadmap-step-item">
                <select
                  className={`step-status-select ${step.status.toLowerCase()}`}
                  value={step.status}
                  onChange={(e) =>
                    changeStatusMutation.mutate({
                      stepId: step.id,
                      status: e.target.value as RoadmapStepStatus,
                    })
                  }
                >
                  <option value="TODO">{STATUS_LABEL.TODO}</option>
                  <option value="IN_PROGRESS">{STATUS_LABEL.IN_PROGRESS}</option>
                  <option value="DONE">{STATUS_LABEL.DONE}</option>
                </select>

                <div className="roadmap-step-body" onClick={() => setEditingStepId(step.id)}>
                  <div className="roadmap-step-title">{step.title}</div>
                  <div className="roadmap-step-meta">
                    {step.skillName && <span className="tag">{step.skillName}</span>}
                    {step.targetDate && <span className="roadmap-step-date">~{step.targetDate}</span>}
                    {step.link && (
                      <a
                        className="roadmap-step-link"
                        href={step.link}
                        target="_blank"
                        rel="noreferrer"
                        onClick={(e) => e.stopPropagation()}
                      >
                        🔗 링크
                      </a>
                    )}
                  </div>
                </div>

                <div className="skill-actions">
                  <button className="btn-icon" onClick={() => moveStep(index, -1)} disabled={index === 0}>
                    ▲
                  </button>
                  <button
                    className="btn-icon"
                    onClick={() => moveStep(index, 1)}
                    disabled={index === roadmap.steps.length - 1}
                  >
                    ▼
                  </button>
                  <button
                    className="btn-icon danger"
                    onClick={() => {
                      if (window.confirm('이 단계를 삭제할까요?')) {
                        deleteStepMutation.mutate(step.id);
                      }
                    }}
                  >
                    ✕
                  </button>
                </div>
              </li>
            )
          )}
        </ul>
      )}
    </div>
  );
}

function StepForm({
  roadmapId,
  skills,
  initial,
  onCancel,
  onSaved,
}: {
  roadmapId: number;
  skills: SkillItem[];
  initial?: RoadmapStepItem;
  onCancel: () => void;
  onSaved: () => void;
}) {
  const isEditing = !!initial;
  const [skillId, setSkillId] = useState(
    initial?.skillName ? String(skills.find((s) => s.name === initial.skillName)?.id ?? '') : ''
  );
  const [title, setTitle] = useState(initial?.title ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [targetDate, setTargetDate] = useState(initial?.targetDate ?? '');
  const [link, setLink] = useState(initial?.link ?? '');

  const saveMutation = useMutation({
    mutationFn: () => {
      const body = {
        skillId: skillId ? Number(skillId) : null,
        title,
        description: description || null,
        targetDate: targetDate || null,
        link: link || null,
      };
      return isEditing
        ? api.put(`/api/roadmaps/${roadmapId}/steps/${initial!.id}`, body)
        : api.post(`/api/roadmaps/${roadmapId}/steps`, body);
    },
    onSuccess: onSaved,
  });

  const canSave = title.trim() && (isEditing || skillId); // 생성 시엔 skill 필수

  return (
    <div className="studylog-form">
      <label className="field-label">기술 {isEditing ? '' : '(필수)'}</label>
      <select className="skill-select" value={skillId} onChange={(e) => setSkillId(e.target.value)}>
        <option value="">선택 안 함</option>
        {skills.map((s) => (
          <option key={s.id} value={s.id}>
            {s.name}
          </option>
        ))}
      </select>

      <label className="field-label">단계 제목</label>
      <input
        className="studylog-title-input"
        placeholder="예: JPA 심화 학습"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
      />

      <label className="field-label">설명 (선택)</label>
      <textarea
        className="studylog-textarea"
        rows={2}
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <label className="field-label">목표 마감일 (선택)</label>
      <input
        type="date"
        className="studylog-date-input"
        value={targetDate}
        onChange={(e) => setTargetDate(e.target.value)}
      />

      <label className="field-label">참고 링크 (선택)</label>
      <input
        className="studylog-title-input"
        placeholder="https://..."
        value={link}
        onChange={(e) => setLink(e.target.value)}
      />

      <div className="studylog-today-actions">
        <button
          className="btn btn-primary"
          disabled={!canSave || saveMutation.isPending}
          onClick={() => saveMutation.mutate()}
        >
          {isEditing ? '저장' : '추가'}
        </button>
        <button className="btn" onClick={onCancel}>
          취소
        </button>
      </div>
    </div>
  );
}

function RoadmapLinksSection({
  links,
  onAdd,
  onDelete,
}: {
  links: RoadmapLinkItem[];
  onAdd: (url: string, label: string | null) => void;
  onDelete: (linkId: number) => void;
}) {
  const [showForm, setShowForm] = useState(false);
  const [url, setUrl] = useState('');
  const [label, setLabel] = useState('');

  function handleAdd() {
    if (!url.trim()) return;
    onAdd(url.trim(), label.trim() || null);
    setUrl('');
    setLabel('');
    setShowForm(false);
  }

  return (
    <div>
      <div className="card-header">
        <p className="card-title">참고 링크</p>
        {!showForm && (
          <button className="btn" onClick={() => setShowForm(true)}>
            + 링크 추가
          </button>
        )}
      </div>

      {showForm && (
        <div className="roadmap-link-form">
          <input
            className="studylog-title-input"
            placeholder="https://..."
            value={url}
            onChange={(e) => setUrl(e.target.value)}
          />
          <input
            className="studylog-title-input"
            placeholder="설명 (선택, 예: 공식 문서)"
            value={label}
            onChange={(e) => setLabel(e.target.value)}
          />
          <div className="studylog-today-actions">
            <button className="btn btn-primary" disabled={!url.trim()} onClick={handleAdd}>
              추가
            </button>
            <button className="btn" onClick={() => setShowForm(false)}>
              취소
            </button>
          </div>
        </div>
      )}

      {links.length === 0 && !showForm && (
        <div className="empty-state">등록된 링크가 없어요.</div>
      )}

      {links.length > 0 && (
        <ul className="roadmap-link-list">
          {links.map((link) => (
            <li key={link.id} className="roadmap-link-item">
              <a href={link.url} target="_blank" rel="noreferrer" className="roadmap-link-anchor">
                🔗 {link.label || link.url}
              </a>
              <button className="btn-icon danger" onClick={() => onDelete(link.id)}>
                ✕
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

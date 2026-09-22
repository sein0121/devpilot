import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../api/client';
import type { CareerAnalysisResponse, CareerGapAnalysisResult, CareerGapPriority } from '../api/types';

const PRIORITY_LABEL: Record<CareerGapPriority, string> = {
  HIGH: '높음',
  MEDIUM: '보통',
  LOW: '낮음',
};

const IN_PROGRESS_STATUSES = ['PENDING', 'RUNNING'];

export function CareerAnalysisSection() {
  const queryClient = useQueryClient();
  const [analysisId, setAnalysisId] = useState<number | null>(null);

  const startMutation = useMutation({
    mutationFn: () =>
      api.post<CareerAnalysisResponse>('/api/career-analysis', undefined, {
        headers: { 'Idempotency-Key': crypto.randomUUID() },
      }),
    onSuccess: (res) => {
      setAnalysisId(res.id);
      queryClient.setQueryData(['career-analysis', res.id], res);
    },
  });

  const { data: analysis, error: pollError } = useQuery({
    queryKey: ['career-analysis', analysisId],
    queryFn: () => api.get<CareerAnalysisResponse>(`/api/career-analysis/${analysisId}`),
    enabled: analysisId !== null,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status && IN_PROGRESS_STATUSES.includes(status) ? 2000 : false;
    },
  });

  const githubRequired = startMutation.error instanceof ApiError && startMutation.error.status === 428;
  const result: CareerGapAnalysisResult | null =
    analysis?.status === 'COMPLETED' && analysis.result ? JSON.parse(analysis.result) : null;

  return (
    <div className="card">
      <div className="card-header">
        <p className="card-title">Career Gap Analysis</p>
        <button
          className="btn btn-primary"
          onClick={() => startMutation.mutate()}
          disabled={startMutation.isPending || (analysis ? IN_PROGRESS_STATUSES.includes(analysis.status) : false)}
        >
          {startMutation.isPending || (analysis && IN_PROGRESS_STATUSES.includes(analysis.status))
            ? '분석 중...'
            : '분석 시작'}
        </button>
      </div>

      {githubRequired && (
        <div className="empty-state">GitHub 계정을 먼저 연동해주세요.</div>
      )}
      {!githubRequired && startMutation.isError && (
        <div className="empty-state">분석 요청에 실패했습니다: {(startMutation.error as Error).message}</div>
      )}
      {pollError && (
        <div className="empty-state">상태 조회에 실패했습니다: {(pollError as Error).message}</div>
      )}

      {analysis && IN_PROGRESS_STATUSES.includes(analysis.status) && (
        <div className="empty-state">분석 진행 중입니다... ({analysis.status})</div>
      )}

      {analysis?.status === 'FAILED' && (
        <div className="empty-state">분석에 실패했습니다: {analysis.errorMessage}</div>
      )}

      {result && (
        <div className="career-analysis-result">
          <p className="career-analysis-summary">{result.summary}</p>

          {result.strengths.length > 0 && (
            <section>
              <h4>강점</h4>
              <ul>
                {result.strengths.map((s, i) => (
                  <li key={i}>
                    <strong>{s.title}</strong> — {s.evidence}
                  </li>
                ))}
              </ul>
            </section>
          )}

          {result.gaps.length > 0 && (
            <section>
              <h4>부족한 부분</h4>
              <ul>
                {result.gaps.map((g, i) => (
                  <li key={i}>
                    <strong>{g.skill}</strong> ({PRIORITY_LABEL[g.priority]}) — {g.reason}
                  </li>
                ))}
              </ul>
            </section>
          )}

          {result.recommendations.length > 0 && (
            <section>
              <h4>추천 행동</h4>
              <ul>
                {result.recommendations.map((r, i) => (
                  <li key={i}>
                    <strong>{r.title}</strong> ({PRIORITY_LABEL[r.priority]}) — {r.description}
                  </li>
                ))}
              </ul>
            </section>
          )}
        </div>
      )}
    </div>
  );
}
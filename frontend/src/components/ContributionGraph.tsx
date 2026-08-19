import { useState } from 'react';
import type { GithubContributionItem } from '../api/types';

function buildWeeks(contributions: GithubContributionItem[]) {
  const countMap = new Map(contributions.map((c) => [c.date, c.count]));

  const today = new Date();
  const start = new Date(today);
  start.setDate(start.getDate() - 371);
  start.setDate(start.getDate() - start.getDay());

  const weeks: { date: string; count: number }[][] = [];
  const cursor = new Date(start);

  while (cursor <= today) {
    const week: { date: string; count: number }[] = [];
    for (let i = 0; i < 7; i++) {
      const iso = cursor.toISOString().slice(0, 10);
      week.push({ date: iso, count: countMap.get(iso) ?? 0 });
      cursor.setDate(cursor.getDate() + 1);
    }
    weeks.push(week);
  }
  return weeks;
}

function plantOf(count: number) {
  if (count === 0) return null;
  if (count <= 2) return '🌱';
  if (count <= 5) return '🌳';
  if (count <= 8) return '🌴';
  if (count <= 10) return '🌻';
  return '🌷';
}

function formatDate(iso: string) {
  const d = new Date(iso);
  return `${d.getMonth() + 1}월 ${d.getDate()}일`;
}

export function ContributionGraph({ contributions }: { contributions: GithubContributionItem[] }) {
  const weeks = buildWeeks(contributions);
  const [hovered, setHovered] = useState<{ date: string; count: number; x: number; y: number } | null>(null);

  return (
    <div className="contrib-graph">
      <div className="contrib-grid">
        {weeks.map((week, wi) => (
          <div className="contrib-col" key={wi}>
            {week.map((day) => (
              <div
                key={day.date}
                className="contrib-cell"
                onMouseEnter={(e) => {
                  const rect = e.currentTarget.getBoundingClientRect();
                  setHovered({ date: day.date, count: day.count, x: rect.left + rect.width / 2, y: rect.top });
                }}
                onMouseLeave={() => setHovered(null)}
              >
                {plantOf(day.count)}
              </div>
            ))}
          </div>
        ))}
      </div>

      {hovered && (
        <div
          className="contrib-tooltip"
          style={{ left: hovered.x, top: hovered.y }}
        >
          <strong>{formatDate(hovered.date)}</strong>
          <span>{hovered.count > 0 ? `커밋 ${hovered.count}회` : '커밋 없음'}</span>
        </div>
      )}

      <div className="contrib-legend">
        <span>🌱 1-2</span>
        <span>🌳 3-5</span>
        <span>🌴 6-8</span>
        <span>🌻 9-10</span>
        <span>🌷 11+</span>
      </div>
    </div>
  );
}
import { Link } from 'react-router-dom';

const TILES = [
  { to: '/github', icon: '/icons/github.png', title: 'GitHub', desc: '커밋, 레포, 기여 그래프를 한눈에 확인하세요.' },
  { to: '/skills', icon: '/icons/skills.png', title: 'Skills', desc: '보유 기술과 학습 중인 스택을 정리하세요.' },
  { to: '/study-log', icon: '/icons/studylog.png', title: 'Study', desc: '오늘 배운 내용을 짧게 기록해 두세요.' },
  { to: '/roadmap', icon: '/icons/roadmap.png', title: 'Roadmap', desc: '목표를 단계로 나누고 진행률을 따라가세요.' },
];

export function HomePage() {
  return (
    <div className="home-hub">
      <p className="home-hub-kicker">오늘의 성장</p>
      <p className="home-hub-lead">흩어진 개발 활동을 모아서, 다음에 무엇을 할지 바로 고를 수 있어요.</p>
      <div className="home-grid">
        {TILES.map((tile) => (
          <Link key={tile.to} to={tile.to} className="home-tile">
            <img src={tile.icon} alt="" className="home-tile-icon" />
            <div>
              <div className="home-tile-title">{tile.title}</div>
              <div className="home-tile-desc">{tile.desc}</div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}

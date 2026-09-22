import { NavLink } from 'react-router-dom';

const NAV_ITEMS = [
  { to: '/github', icon: '/icons/github.png', label: 'GitHub' },
  { to: '/skills', icon: '/icons/skills.png', label: 'Skills' },
  { to: '/study-log', icon: '/icons/studylog.png', label: 'Study' },
  { to: '/roadmap', icon: '/icons/roadmap.png', label: 'Roadmap' },
  { to: '/career-analysis', icon: '/icons/career-analysis.png', label: 'Career' },
];

export function BottomNav() {
  return (
    <nav className="bottom-nav">
      {NAV_ITEMS.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) => `bottom-nav-item ${isActive ? 'active' : ''}`}
        >
          <img src={item.icon} alt={item.label} className="bottom-nav-icon" />
          <span className="bottom-nav-label">{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}

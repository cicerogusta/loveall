import { useLocation, useNavigate } from 'react-router-dom';
import { Heart, MessageCircle, User, Shield } from 'lucide-react';

const tabs = [
  { path: '/discover', icon: Heart, label: 'Descobrir' },
  { path: '/matches', icon: MessageCircle, label: 'Matches' },
  { path: '/safety', icon: Shield, label: 'Seguranca' },
  { path: '/profile', icon: User, label: 'Perfil' },
];

export default function BottomNav() {
  const location = useLocation();
  const navigate = useNavigate();

  // Esconder nav no chat
  if (location.pathname.startsWith('/chat/')) return null;

  return (
    <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-[480px] glass border-t border-white/10 z-50">
      <div className="flex items-center justify-around py-2 px-4">
        {tabs.map(({ path, icon: Icon, label }) => {
          const active = location.pathname === path;
          return (
            <button
              key={path}
              onClick={() => navigate(path)}
              className={`flex flex-col items-center gap-1 py-1 px-3 rounded-xl transition-all ${
                active ? 'text-pink-500' : 'text-white/40 hover:text-white/70'
              }`}
            >
              <Icon size={22} fill={active ? 'currentColor' : 'none'} />
              <span className="text-[10px] font-medium">{label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}

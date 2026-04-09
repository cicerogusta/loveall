import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GENDERS } from '../utils/constants';
import { Settings, Edit3, CreditCard, Shield, LogOut, BadgeCheck, MapPin, ChevronRight, Heart } from 'lucide-react';

export default function Profile() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const genderInfo = GENDERS.find(g => g.value === user?.gender);
  const photo = user?.photos?.[0];

  const handleLogout = () => {
    if (confirm('Sair da sua conta?')) {
      logout();
      navigate('/welcome');
    }
  };

  return (
    <div className="min-h-screen pt-6 px-4">
      <h1 className="text-2xl font-bold gradient-text mb-6">Perfil</h1>

      {/* Profile Card */}
      <div className="glass rounded-3xl p-6 mb-6">
        <div className="flex items-center gap-4">
          <div className="w-20 h-20 rounded-2xl overflow-hidden border-2 border-pink-500/30 shrink-0">
            {photo ? (
              <img src={photo} alt={user.name} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full bg-gradient-to-br from-pink-600/30 to-purple-600/30 flex items-center justify-center">
                <span className="text-2xl font-bold text-white/30">{user?.name?.[0]}</span>
              </div>
            )}
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold">{user?.name}</h2>
              {user?.is_verified ? <BadgeCheck size={18} className="text-blue-400" /> : null}
            </div>
            <p className="text-white/50 text-sm flex items-center gap-1">
              {genderInfo?.emoji} {genderInfo?.label || user?.gender}
              {user?.city && <><span className="mx-1">·</span><MapPin size={12} /> {user.city}</>}
            </p>
            {user?.bio && <p className="text-white/40 text-xs mt-1 line-clamp-2">{user.bio}</p>}
          </div>
        </div>

        <button
          onClick={() => navigate('/edit-profile')}
          className="w-full mt-4 py-2.5 rounded-xl glass text-sm font-medium text-pink-400 hover:bg-pink-500/10 transition flex items-center justify-center gap-2"
        >
          <Edit3 size={14} /> Editar perfil
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-3 mb-6">
        <StatCard icon={<Heart size={18} className="text-pink-400" />} label="Likes" value="Ilimitados" />
        <StatCard
          icon={<BadgeCheck size={18} className="text-blue-400" />}
          label="Status"
          value={user?.is_verified ? 'Verificado' : 'Pendente'}
        />
        <StatCard
          icon={<CreditCard size={18} className="text-purple-400" />}
          label="Plano"
          value={user?.is_premium ? 'Premium' : 'Free'}
        />
      </div>

      {/* Menu Items */}
      <div className="space-y-2">
        <MenuItem
          icon={<CreditCard size={18} className="text-purple-400" />}
          label="Planos de Chamada"
          desc="Audio e video com matches"
          onClick={() => navigate('/plans')}
        />
        <MenuItem
          icon={<Shield size={18} className="text-green-400" />}
          label="Seguranca"
          desc="Dicas e configuracoes"
          onClick={() => navigate('/safety')}
        />
        <MenuItem
          icon={<LogOut size={18} className="text-red-400" />}
          label="Sair"
          desc="Encerrar sessao"
          onClick={handleLogout}
          danger
        />
      </div>
    </div>
  );
}

function StatCard({ icon, label, value }) {
  return (
    <div className="glass rounded-2xl p-3 text-center">
      <div className="flex justify-center mb-2">{icon}</div>
      <p className="text-xs font-semibold">{value}</p>
      <p className="text-white/30 text-[10px]">{label}</p>
    </div>
  );
}

function MenuItem({ icon, label, desc, onClick, danger }) {
  return (
    <button
      onClick={onClick}
      className={`w-full flex items-center gap-3 glass rounded-2xl p-4 hover:bg-white/10 transition text-left active:scale-[0.98] ${danger ? 'hover:bg-red-500/10' : ''}`}
    >
      <div className="shrink-0">{icon}</div>
      <div className="flex-1">
        <p className={`text-sm font-medium ${danger ? 'text-red-400' : ''}`}>{label}</p>
        <p className="text-white/30 text-xs">{desc}</p>
      </div>
      <ChevronRight size={16} className="text-white/20" />
    </button>
  );
}

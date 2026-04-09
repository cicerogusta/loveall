import { useNavigate } from 'react-router-dom';
import { Heart, Infinity, Shield, Video } from 'lucide-react';

export default function Welcome() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-6 py-12">
      {/* Logo */}
      <div className="float mb-8">
        <div className="w-24 h-24 gradient-primary rounded-3xl flex items-center justify-center pulse-glow rotate-12">
          <Heart size={48} className="text-white -rotate-12" fill="white" />
        </div>
      </div>

      <h1 className="text-4xl font-extrabold mb-2">
        <span className="gradient-text">LoveAll</span>
      </h1>
      <p className="text-white/50 text-lg mb-12">Amor para todos</p>

      {/* Features */}
      <div className="w-full space-y-4 mb-12">
        <Feature
          icon={<Infinity size={20} />}
          title="Likes Ilimitados"
          desc="Curta quantas pessoas quiser, sem limites!"
          color="text-pink-400"
        />
        <Feature
          icon={<Heart size={20} />}
          title="Todos os Generos"
          desc="Inclusivo para todas as identidades de genero"
          color="text-purple-400"
        />
        <Feature
          icon={<Shield size={20} />}
          title="100% Seguro"
          desc="Verificacao, denuncia e bloqueio de usuarios"
          color="text-blue-400"
        />
        <Feature
          icon={<Video size={20} />}
          title="Chamadas Premium"
          desc="Audio e video com seus matches"
          color="text-green-400"
        />
      </div>

      {/* Buttons */}
      <div className="w-full space-y-3">
        <button
          onClick={() => navigate('/register')}
          className="w-full py-4 rounded-2xl gradient-primary text-white font-semibold text-lg shadow-lg shadow-pink-500/30 active:scale-[0.98] transition-transform"
        >
          Criar Conta Gratis
        </button>
        <button
          onClick={() => navigate('/login')}
          className="w-full py-4 rounded-2xl glass text-white font-semibold text-lg active:scale-[0.98] transition-transform"
        >
          Ja tenho conta
        </button>
      </div>

      <p className="text-white/30 text-xs mt-8 text-center">
        Ao continuar, voce concorda com nossos Termos de Uso e Politica de Privacidade
      </p>
    </div>
  );
}

function Feature({ icon, title, desc, color }) {
  return (
    <div className="flex items-center gap-4 glass rounded-2xl p-4">
      <div className={`${color} shrink-0`}>{icon}</div>
      <div>
        <p className="font-semibold text-sm">{title}</p>
        <p className="text-white/40 text-xs">{desc}</p>
      </div>
    </div>
  );
}

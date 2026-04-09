import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Shield, AlertTriangle, Users, Lock, Eye, Heart, Phone, BookOpen } from 'lucide-react';
import { api } from '../utils/api';

export default function Safety() {
  const navigate = useNavigate();
  const [tips, setTips] = useState([]);
  const [blocked, setBlocked] = useState([]);
  const [showBlocked, setShowBlocked] = useState(false);

  useEffect(() => {
    api.getTips().then(data => setTips(data.tips)).catch(console.error);
    api.getBlocked().then(data => setBlocked(data.blocked)).catch(console.error);
  }, []);

  const handleUnblock = async (id, name) => {
    if (confirm(`Desbloquear ${name}?`)) {
      try {
        await api.unblock(id);
        setBlocked(b => b.filter(u => u.id !== id));
      } catch (err) {
        alert(err.message);
      }
    }
  };

  return (
    <div className="min-h-screen pt-6 px-4 pb-24">
      <div className="flex items-center gap-3 mb-6">
        <h1 className="text-2xl font-bold">
          <span className="gradient-text">Seguranca</span>
        </h1>
      </div>

      {/* Safety Header */}
      <div className="glass rounded-2xl p-5 mb-6 border border-green-500/20">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-12 h-12 bg-green-500/20 rounded-xl flex items-center justify-center">
            <Shield size={24} className="text-green-400" />
          </div>
          <div>
            <h2 className="font-bold">Sua seguranca importa</h2>
            <p className="text-white/50 text-xs">LoveAll prioriza sua protecao</p>
          </div>
        </div>
        <p className="text-white/60 text-sm leading-relaxed">
          Nosso app possui verificacao de perfis, filtro de conteudo, sistema de denuncias
          e bloqueio de usuarios para garantir uma experiencia segura e respeitosa.
        </p>
      </div>

      {/* Safety Features */}
      <div className="mb-6">
        <h3 className="text-sm font-semibold text-white/60 mb-3">Recursos de protecao</h3>
        <div className="space-y-2">
          <SafetyFeature
            icon={<Eye size={18} className="text-blue-400" />}
            title="Verificacao de perfil"
            desc="Perfis verificados possuem selo azul de autenticidade"
          />
          <SafetyFeature
            icon={<AlertTriangle size={18} className="text-yellow-400" />}
            title="Denuncia rapida"
            desc="Denuncie comportamentos inadequados a qualquer momento"
          />
          <SafetyFeature
            icon={<Lock size={18} className="text-red-400" />}
            title="Bloqueio instantaneo"
            desc="Bloqueie usuarios e eles nao poderao mais te ver ou contatar"
          />
          <SafetyFeature
            icon={<Shield size={18} className="text-green-400" />}
            title="Filtro de conteudo"
            desc="Mensagens com conteudo sensivel sao automaticamente filtradas"
          />
          <SafetyFeature
            icon={<Users size={18} className="text-purple-400" />}
            title="Ban automatico"
            desc="Usuarios com multiplas denuncias sao banidos automaticamente"
          />
          <SafetyFeature
            icon={<Phone size={18} className="text-pink-400" />}
            title="Chamadas seguras"
            desc="Chamadas apenas entre matches e sem compartilhar dados pessoais"
          />
        </div>
      </div>

      {/* Safety Tips */}
      <div className="mb-6">
        <h3 className="text-sm font-semibold text-white/60 mb-3 flex items-center gap-2">
          <BookOpen size={14} /> Dicas de seguranca
        </h3>
        <div className="space-y-2">
          {tips.map((tip, i) => (
            <div key={i} className="glass rounded-xl p-4">
              <div className="flex items-start gap-3">
                <span className="text-2xl shrink-0">{tip.icon}</span>
                <div>
                  <p className="font-semibold text-sm mb-0.5">{tip.title}</p>
                  <p className="text-white/50 text-xs leading-relaxed">{tip.text}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Blocked Users */}
      <div>
        <button
          onClick={() => setShowBlocked(!showBlocked)}
          className="w-full glass rounded-xl p-4 flex items-center justify-between hover:bg-white/10 transition"
        >
          <div className="flex items-center gap-2">
            <Lock size={16} className="text-red-400" />
            <span className="text-sm font-medium">Usuarios bloqueados</span>
          </div>
          <span className="text-white/30 text-xs">{blocked.length}</span>
        </button>

        {showBlocked && (
          <div className="mt-2 space-y-2">
            {blocked.length === 0 ? (
              <p className="text-white/30 text-xs text-center py-4">Nenhum usuario bloqueado</p>
            ) : (
              blocked.map(user => (
                <div key={user.id} className="glass rounded-xl p-3 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full overflow-hidden bg-white/5">
                      {user.photos?.[0] ? (
                        <img src={user.photos[0]} alt="" className="w-full h-full object-cover" />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-xs text-white/20">{user.name?.[0]}</div>
                      )}
                    </div>
                    <span className="text-sm">{user.name}</span>
                  </div>
                  <button
                    onClick={() => handleUnblock(user.id, user.name)}
                    className="text-xs text-red-400 hover:text-red-300 px-3 py-1 glass rounded-lg"
                  >
                    Desbloquear
                  </button>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {/* Emergency */}
      <div className="mt-6 glass rounded-2xl p-5 border border-red-500/20">
        <h3 className="font-bold text-red-400 text-sm mb-2 flex items-center gap-2">
          <AlertTriangle size={16} /> Em caso de emergencia
        </h3>
        <p className="text-white/50 text-xs leading-relaxed">
          Se voce esta em perigo imediato, ligue para a policia (190) ou SAMU (192).
          Central de Atendimento a Mulher: 180.
          CVV (apoio emocional): 188.
        </p>
      </div>
    </div>
  );
}

function SafetyFeature({ icon, title, desc }) {
  return (
    <div className="glass rounded-xl p-3 flex items-start gap-3">
      <div className="shrink-0 mt-0.5">{icon}</div>
      <div>
        <p className="text-sm font-medium">{title}</p>
        <p className="text-white/40 text-xs">{desc}</p>
      </div>
    </div>
  );
}

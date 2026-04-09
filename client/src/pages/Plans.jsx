import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Phone, Video, Check, Crown, Zap, Star } from 'lucide-react';
import { api } from '../utils/api';
import { CALL_PLANS } from '../utils/constants';

export default function Plans() {
  const navigate = useNavigate();
  const [currentPlan, setCurrentPlan] = useState(null);
  const [loading, setLoading] = useState(true);
  const [subscribing, setSubscribing] = useState('');

  useEffect(() => {
    api.getMyPlan()
      .then(data => setCurrentPlan(data.subscription))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleSubscribe = async (plan) => {
    setSubscribing(plan);
    try {
      const data = await api.subscribe(plan);
      if (data.payment_url) {
        // Redirecionar para Mercado Pago
        window.location.href = data.payment_url;
      } else {
        alert('Erro ao iniciar pagamento. Tente novamente.');
      }
    } catch (err) {
      alert(err.message || 'Falha ao iniciar pagamento');
      setSubscribing('');
    }
  };

  return (
    <div className="min-h-screen pt-6 px-4 pb-24">
      <div className="flex items-center gap-3 mb-6">
        <button onClick={() => navigate(-1)} className="text-white/60 hover:text-white">
          <ArrowLeft size={22} />
        </button>
        <h1 className="text-xl font-bold">Planos de Chamada</h1>
      </div>

      {/* Info */}
      <div className="glass rounded-2xl p-5 mb-6">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-12 h-12 gradient-primary rounded-xl flex items-center justify-center">
            <Crown size={24} className="text-white" />
          </div>
          <div>
            <h2 className="font-bold">Chamadas Premium</h2>
            <p className="text-white/50 text-xs">Audio e video com seus matches</p>
          </div>
        </div>
        <p className="text-white/60 text-sm leading-relaxed">
          No LoveAll, likes sao <span className="text-pink-400 font-semibold">sempre gratuitos e ilimitados</span>!
          Os planos sao apenas para chamadas de audio e video.
        </p>
      </div>

      {/* Current plan */}
      {currentPlan && currentPlan.plan !== 'free' && (
        <div className="glass rounded-2xl p-4 mb-6 border border-pink-500/30">
          <p className="text-white/60 text-xs mb-1">Seu plano atual</p>
          <p className="text-lg font-bold gradient-text capitalize">{currentPlan.plan}</p>
          <p className="text-white/50 text-sm">
            {currentPlan.call_minutes_remaining >= 999999
              ? 'Minutos ilimitados'
              : `${currentPlan.call_minutes_remaining} minutos restantes`
            }
          </p>
        </div>
      )}

      {/* Free features */}
      <div className="mb-6">
        <h3 className="text-sm font-semibold text-white/60 mb-3">Gratis para sempre</h3>
        <div className="glass rounded-2xl p-4 space-y-2">
          <FreeFeature text="Likes ilimitados" />
          <FreeFeature text="Matches ilimitados" />
          <FreeFeature text="Chat por texto ilimitado" />
          <FreeFeature text="Ver quem curtiu voce" />
          <FreeFeature text="Todos os filtros de genero" />
        </div>
      </div>

      {/* Plans */}
      <h3 className="text-sm font-semibold text-white/60 mb-3">Planos para chamadas</h3>
      <div className="space-y-3">
        {Object.entries(CALL_PLANS).map(([key, plan]) => (
          <div key={key} className={`glass rounded-2xl p-5 relative overflow-hidden ${key === 'premium' ? 'border border-pink-500/30' : ''}`}>
            {key === 'premium' && (
              <div className="absolute top-3 right-3 bg-pink-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full">
                POPULAR
              </div>
            )}

            <div className="flex items-center gap-3 mb-3">
              <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${plan.color} flex items-center justify-center`}>
                {key === 'basic' && <Phone size={18} className="text-white" />}
                {key === 'plus' && <Zap size={18} className="text-white" />}
                {key === 'premium' && <Star size={18} className="text-white" fill="white" />}
              </div>
              <div>
                <h4 className="font-bold">{plan.name}</h4>
                <p className="text-white/40 text-xs">{plan.minutes} de chamada/mes</p>
              </div>
            </div>

            <div className="flex items-end justify-between">
              <div>
                <span className="text-2xl font-extrabold">{plan.price}</span>
                <span className="text-white/40 text-sm">/mes</span>
              </div>
              <button
                onClick={() => handleSubscribe(key)}
                disabled={subscribing === key}
                className={`py-2.5 px-6 rounded-xl font-semibold text-sm transition-all active:scale-95 ${
                  currentPlan?.plan === key
                    ? 'glass text-white/40 cursor-default'
                    : 'gradient-primary text-white shadow-lg shadow-pink-500/20'
                }`}
              >
                {subscribing === key ? 'Ativando...' : currentPlan?.plan === key ? 'Ativo' : 'Assinar'}
              </button>
            </div>
          </div>
        ))}
      </div>

      <p className="text-white/20 text-xs text-center mt-6">
        Pagamentos processados com segurança via Mercado Pago
      </p>
    </div>
  );
}

function FreeFeature({ text }) {
  return (
    <div className="flex items-center gap-2">
      <Check size={14} className="text-green-400 shrink-0" />
      <span className="text-sm text-white/70">{text}</span>
    </div>
  );
}

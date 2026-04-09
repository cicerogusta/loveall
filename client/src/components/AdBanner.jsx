import { useState, useEffect } from 'react';

// Componente de banner de anuncio para web
// Em mobile nativo, o AdMob cuida dos banners automaticamente
const AD_BANNERS = [
  { id: 1, title: 'LoveAll Premium', desc: 'Chamadas ilimitadas! Assine agora', color: 'from-pink-500 to-purple-600', cta: 'Ver planos', link: 'plans' },
  { id: 2, title: 'Perfil verificado', desc: 'Aumente suas chances de match', color: 'from-blue-500 to-cyan-500', cta: 'Verificar', link: 'profile' },
  { id: 3, title: 'Seguranca', desc: 'Conhca nossas dicas de seguranca', color: 'from-green-500 to-emerald-500', cta: 'Ver dicas', link: 'safety' },
];

export default function AdBanner({ onNavigate, variant = 'small' }) {
  const [currentAd, setCurrentAd] = useState(0);
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentAd(i => (i + 1) % AD_BANNERS.length);
    }, 8000);
    return () => clearInterval(interval);
  }, []);

  if (!visible) return null;

  const ad = AD_BANNERS[currentAd];

  if (variant === 'small') {
    return (
      <div className="mx-4 mb-3">
        <div className={`bg-gradient-to-r ${ad.color} rounded-xl p-3 flex items-center justify-between relative`}>
          <div className="flex-1 min-w-0">
            <p className="text-white font-semibold text-xs">{ad.title}</p>
            <p className="text-white/80 text-[10px]">{ad.desc}</p>
          </div>
          <button
            onClick={() => onNavigate?.(ad.link)}
            className="bg-white/20 text-white text-[10px] font-bold px-3 py-1.5 rounded-lg ml-2 shrink-0"
          >
            {ad.cta}
          </button>
          <button
            onClick={() => setVisible(false)}
            className="absolute -top-1 -right-1 w-4 h-4 bg-black/40 rounded-full text-white text-[8px] flex items-center justify-center"
          >
            x
          </button>
          <span className="absolute bottom-1 right-2 text-white/30 text-[8px]">Ad</span>
        </div>
      </div>
    );
  }

  return null;
}

// Interstitial web (tela cheia entre acoes)
export function AdInterstitial({ onClose, onNavigate }) {
  const [countdown, setCountdown] = useState(3);

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  return (
    <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-[100] flex items-center justify-center px-6">
      <div className="text-center max-w-sm w-full">
        <div className="bg-gradient-to-br from-pink-500 to-purple-600 rounded-3xl p-8 mb-6 relative">
          <span className="absolute top-3 left-3 text-white/40 text-[10px] bg-black/20 px-2 py-0.5 rounded">Anuncio</span>
          <div className="text-5xl mb-4">👑</div>
          <h2 className="text-xl font-bold text-white mb-2">LoveAll Premium</h2>
          <p className="text-white/80 text-sm mb-4">
            Faca chamadas de audio e video ilimitadas com seus matches!
          </p>
          <div className="flex justify-center gap-4 mb-4">
            <div className="text-center">
              <p className="text-2xl">📞</p>
              <p className="text-white/70 text-xs">Audio</p>
            </div>
            <div className="text-center">
              <p className="text-2xl">📹</p>
              <p className="text-white/70 text-xs">Video</p>
            </div>
            <div className="text-center">
              <p className="text-2xl">∞</p>
              <p className="text-white/70 text-xs">Ilimitado</p>
            </div>
          </div>
          <p className="text-white font-bold text-lg">A partir de R$ 9,90/mes</p>
        </div>

        <div className="space-y-3">
          <button
            onClick={() => { onClose(); onNavigate?.('plans'); }}
            className="w-full py-3 rounded-2xl gradient-primary text-white font-semibold"
          >
            Ver planos
          </button>
          <button
            onClick={onClose}
            disabled={countdown > 0}
            className="w-full py-3 rounded-2xl glass text-white/60 font-medium disabled:opacity-30"
          >
            {countdown > 0 ? `Fechar em ${countdown}s` : 'Fechar'}
          </button>
        </div>
      </div>
    </div>
  );
}

// Rewarded ad web (assistir para ganhar recompensa)
export function AdRewarded({ onClose, onReward }) {
  const [watching, setWatching] = useState(true);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    if (!watching) return;
    const interval = setInterval(() => {
      setProgress(p => {
        if (p >= 100) {
          setWatching(false);
          clearInterval(interval);
          return 100;
        }
        return p + 4;
      });
    }, 200);
    return () => clearInterval(interval);
  }, [watching]);

  return (
    <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-[100] flex items-center justify-center px-6">
      <div className="text-center max-w-sm w-full">
        {watching ? (
          <div>
            <div className="bg-gradient-to-br from-amber-500 to-orange-600 rounded-3xl p-8 mb-4">
              <div className="text-5xl mb-4">🎬</div>
              <h2 className="text-xl font-bold text-white mb-2">Assistindo anuncio...</h2>
              <p className="text-white/80 text-sm mb-4">Assista para ganhar um Super Like gratis!</p>
              <div className="w-full bg-white/20 rounded-full h-2">
                <div className="bg-white rounded-full h-2 transition-all" style={{ width: `${progress}%` }} />
              </div>
              <p className="text-white/60 text-xs mt-2">{Math.ceil((100 - progress) / 20)}s restantes</p>
            </div>
          </div>
        ) : (
          <div>
            <div className="bg-gradient-to-br from-green-500 to-emerald-600 rounded-3xl p-8 mb-6">
              <div className="text-5xl mb-4">⭐</div>
              <h2 className="text-xl font-bold text-white mb-2">Recompensa!</h2>
              <p className="text-white/80 text-sm">Voce ganhou 1 Super Like gratis!</p>
            </div>
            <button
              onClick={() => { onReward?.(); onClose(); }}
              className="w-full py-3 rounded-2xl gradient-primary text-white font-semibold"
            >
              Coletar recompensa
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

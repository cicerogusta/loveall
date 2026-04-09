import { useState, useEffect, useCallback, useRef } from 'react';
import { motion, useMotionValue, useTransform, AnimatePresence } from 'framer-motion';
import { Heart, X, Star, MapPin, BadgeCheck, RefreshCw, Sparkles, Film } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { api } from '../utils/api';
import { GENDERS } from '../utils/constants';
import { hapticFeedback } from '../utils/platform';
import { showInterstitial, getSwipeCount } from '../utils/ads';
import AdBanner, { AdInterstitial, AdRewarded } from '../components/AdBanner';

export default function Discover() {
  const navigate = useNavigate();
  const [profiles, setProfiles] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [matchPopup, setMatchPopup] = useState(null);
  const [direction, setDirection] = useState(null);
  const [showInterstitialAd, setShowInterstitialAd] = useState(false);
  const [showRewardedAd, setShowRewardedAd] = useState(false);
  const [superLikes, setSuperLikes] = useState(0);

  const loadProfiles = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.discover();
      setProfiles(data.profiles);
      setCurrentIndex(0);
    } catch {
      // Fallback: se API nao disponivel, usar dados demo
      setProfiles([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadProfiles(); }, [loadProfiles]);

  const advanceCard = () => {
    setDirection(null);
    setCurrentIndex(i => i + 1);
  };

  const handleLike = async (profileId, isSuper = false) => {
    hapticFeedback(isSuper ? 'heavy' : 'light');
    try {
      const result = await api.like(profileId, isSuper);
      if (result.match) setMatchPopup(profiles[currentIndex]);
    } catch {}

    setDirection('right');
    setTimeout(async () => {
      advanceCard();
      // Mostrar interstitial a cada N swipes
      const shouldShow = await showInterstitial();
      if (shouldShow) setShowInterstitialAd(true);
    }, 300);
  };

  const handleDislike = async (profileId) => {
    hapticFeedback('light');
    try { await api.dislike(profileId); } catch {}
    setDirection('left');
    setTimeout(async () => {
      advanceCard();
      const shouldShow = await showInterstitial();
      if (shouldShow) setShowInterstitialAd(true);
    }, 300);
  };

  const handleRewardedAd = () => {
    setSuperLikes(s => s + 1);
  };

  const currentProfile = profiles[currentIndex];
  const nextProfile = profiles[currentIndex + 1];

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <RefreshCw size={32} className="text-pink-500 animate-spin" />
      </div>
    );
  }

  if (!currentProfile) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center px-6 pb-20">
        <div className="w-20 h-20 glass rounded-full flex items-center justify-center mb-6">
          <Sparkles size={36} className="text-pink-400" />
        </div>
        <h2 className="text-xl font-bold mb-2 text-center">Voce viu todos!</h2>
        <p className="text-white/50 text-center mb-6">Volte mais tarde para ver novos perfis</p>
        <button onClick={loadProfiles} className="gradient-primary text-white py-3 px-8 rounded-2xl font-medium flex items-center gap-2">
          <RefreshCw size={18} /> Atualizar
        </button>
      </div>
    );
  }

  const genderLabel = GENDERS.find(g => g.value === currentProfile.gender)?.label || currentProfile.gender;

  return (
    <div className="min-h-screen pt-4 px-4 pb-24 relative">
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-2xl font-bold gradient-text">LoveAll</h1>
        <div className="flex items-center gap-3">
          {superLikes > 0 && (
            <span className="text-xs bg-blue-500/20 text-blue-400 px-2 py-0.5 rounded-full">
              ⭐ {superLikes}
            </span>
          )}
          <div className="flex items-center gap-1 text-white/40 text-xs">
            <Heart size={12} className="text-pink-400" fill="currentColor" />
            Likes ilimitados
          </div>
        </div>
      </div>

      {/* Ad Banner */}
      <AdBanner onNavigate={(s) => navigate('/' + s)} />

      {/* Card Stack */}
      <div className="relative h-[500px]">
        {nextProfile && (
          <div className="absolute inset-0 rounded-3xl overflow-hidden scale-[0.95] opacity-60">
            <ProfileImage photos={nextProfile.photos} name={nextProfile.name} />
          </div>
        )}

        <AnimatePresence mode="wait">
          <SwipeCard
            key={currentProfile.id}
            profile={currentProfile}
            genderLabel={genderLabel}
            direction={direction}
            onLike={() => handleLike(currentProfile.id)}
            onDislike={() => handleDislike(currentProfile.id)}
          />
        </AnimatePresence>
      </div>

      {/* Action Buttons */}
      <div className="flex items-center justify-center gap-4 mt-4">
        <button
          onClick={() => handleDislike(currentProfile.id)}
          className="w-16 h-16 rounded-full glass flex items-center justify-center text-red-400 hover:bg-red-500/20 active:scale-90 transition-all shadow-lg"
        >
          <X size={30} />
        </button>

        {/* Watch ad for super like */}
        <button
          onClick={() => setShowRewardedAd(true)}
          className="w-12 h-12 rounded-full glass flex items-center justify-center text-amber-400 hover:bg-amber-500/20 active:scale-90 transition-all"
          title="Assistir anuncio para Super Like gratis"
        >
          <Film size={20} />
        </button>

        <button
          onClick={() => handleLike(currentProfile.id, true)}
          className="w-14 h-14 rounded-full glass flex items-center justify-center text-blue-400 hover:bg-blue-500/20 active:scale-90 transition-all"
        >
          <Star size={24} fill="currentColor" />
        </button>
        <button
          onClick={() => handleLike(currentProfile.id)}
          className="w-16 h-16 rounded-full gradient-primary flex items-center justify-center text-white active:scale-90 transition-all shadow-lg shadow-pink-500/40"
        >
          <Heart size={30} fill="white" />
        </button>
      </div>

      {/* Swipe counter */}
      <p className="text-center text-white/20 text-[10px] mt-2">
        {getSwipeCount()} swipes hoje
      </p>

      {/* Match Popup */}
      <AnimatePresence>
        {matchPopup && (
          <MatchPopup profile={matchPopup} onClose={() => setMatchPopup(null)} />
        )}
      </AnimatePresence>

      {/* Ad Interstitial */}
      {showInterstitialAd && (
        <AdInterstitial
          onClose={() => setShowInterstitialAd(false)}
          onNavigate={(s) => { setShowInterstitialAd(false); navigate('/' + s); }}
        />
      )}

      {/* Rewarded Ad */}
      {showRewardedAd && (
        <AdRewarded
          onClose={() => setShowRewardedAd(false)}
          onReward={handleRewardedAd}
        />
      )}
    </div>
  );
}

function SwipeCard({ profile, genderLabel, direction, onLike, onDislike }) {
  const x = useMotionValue(0);
  const rotate = useTransform(x, [-200, 200], [-15, 15]);
  const likeOpacity = useTransform(x, [0, 100], [0, 1]);
  const nopeOpacity = useTransform(x, [-100, 0], [1, 0]);
  const [photoIndex, setPhotoIndex] = useState(0);

  const photos = profile.photos || [];

  const handleDragEnd = (_, info) => {
    if (info.offset.x > 100) onLike();
    else if (info.offset.x < -100) onDislike();
  };

  return (
    <motion.div
      style={{ x, rotate }}
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      dragElastic={0.8}
      onDragEnd={handleDragEnd}
      initial={{ scale: 0.95, opacity: 0 }}
      animate={{
        scale: 1, opacity: 1,
        x: direction === 'right' ? 400 : direction === 'left' ? -400 : 0,
      }}
      exit={{ opacity: 0 }}
      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
      className="absolute inset-0 rounded-3xl overflow-hidden cursor-grab active:cursor-grabbing card-shadow"
    >
      <ProfileImage photos={photos} name={profile.name} index={photoIndex} />

      {photos.length > 1 && (
        <div className="absolute top-4 left-0 right-0 flex justify-center gap-1.5 z-10">
          {photos.map((_, i) => (
            <button key={i} onClick={() => setPhotoIndex(i)} className={`h-1 rounded-full transition-all ${i === photoIndex ? 'w-6 bg-white' : 'w-4 bg-white/40'}`} />
          ))}
        </div>
      )}

      <motion.div style={{ opacity: likeOpacity }} className="absolute top-8 left-6 z-10 rotate-[-20deg]">
        <div className="border-4 border-green-400 text-green-400 font-extrabold text-3xl px-4 py-1 rounded-lg">LIKE</div>
      </motion.div>
      <motion.div style={{ opacity: nopeOpacity }} className="absolute top-8 right-6 z-10 rotate-[20deg]">
        <div className="border-4 border-red-400 text-red-400 font-extrabold text-3xl px-4 py-1 rounded-lg">NOPE</div>
      </motion.div>

      <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/90 via-black/50 to-transparent p-6 pt-20">
        <div className="flex items-center gap-2 mb-1">
          <h2 className="text-2xl font-bold">{profile.name}, {profile.age}</h2>
          {profile.is_verified === 1 && <BadgeCheck size={22} className="text-blue-400" />}
        </div>
        <p className="text-white/60 text-sm mb-1 flex items-center gap-1">
          {genderLabel}
          {profile.city && <><span className="mx-1">·</span><MapPin size={12} /> {profile.city}</>}
        </p>
        {profile.bio && <p className="text-white/80 text-sm line-clamp-2 mt-2">{profile.bio}</p>}
      </div>
    </motion.div>
  );
}

function ProfileImage({ photos, name, index = 0 }) {
  const src = photos?.[index] || photos?.[0];
  if (src) return <img src={src} alt={name} className="w-full h-full object-cover" />;
  return (
    <div className="w-full h-full bg-gradient-to-br from-pink-600/30 to-purple-600/30 flex items-center justify-center">
      <span className="text-6xl font-bold text-white/20">{name?.[0] || '?'}</span>
    </div>
  );
}

function MatchPopup({ profile, onClose }) {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 bg-black/80 backdrop-blur-md z-50 flex items-center justify-center px-6"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.5, y: 50 }}
        animate={{ scale: 1, y: 0 }}
        className="text-center"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex justify-center mb-6">
          <div className="w-24 h-24 rounded-full overflow-hidden border-4 border-pink-500 pulse-glow">
            <ProfileImage photos={profile.photos} name={profile.name} />
          </div>
        </div>
        <h2 className="text-4xl font-extrabold gradient-text mb-2">Match!</h2>
        <p className="text-white/60 mb-8">Voce e {profile.name} se curtiram!</p>
        <div className="space-y-3">
          <button onClick={onClose} className="w-full py-3 rounded-2xl gradient-primary text-white font-semibold">
            Enviar mensagem
          </button>
          <button onClick={onClose} className="w-full py-3 rounded-2xl glass text-white/60 font-medium">
            Continuar explorando
          </button>
        </div>
      </motion.div>
    </motion.div>
  );
}

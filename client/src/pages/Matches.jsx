import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MessageCircle, Heart, Phone, Video, Clock } from 'lucide-react';
import { api } from '../utils/api';

export default function Matches() {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    api.getMatches()
      .then(data => setMatches(data.matches))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-pulse text-white/40">Carregando matches...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen pt-6 px-4">
      <h1 className="text-2xl font-bold mb-6">
        <span className="gradient-text">Matches</span>
        {matches.length > 0 && (
          <span className="ml-2 text-sm bg-pink-500/20 text-pink-400 px-2 py-0.5 rounded-full font-normal">
            {matches.length}
          </span>
        )}
      </h1>

      {matches.length === 0 ? (
        <EmptyState />
      ) : (
        <div className="space-y-3">
          {matches.map(match => (
            <MatchCard
              key={match.match_id}
              match={match}
              onClick={() => navigate(`/chat/${match.match_id}`, { state: { partner: match.partner } })}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function MatchCard({ match, onClick }) {
  const { partner, last_message, unread_count } = match;
  if (!partner) return null;

  const photo = partner.photos?.[0];
  const timeAgo = last_message ? formatTimeAgo(last_message.created_at) : formatTimeAgo(match.matched_at);

  return (
    <button
      onClick={onClick}
      className="w-full flex items-center gap-3 glass rounded-2xl p-3 hover:bg-white/10 transition-all active:scale-[0.98] text-left"
    >
      <div className="relative shrink-0">
        <div className="w-14 h-14 rounded-full overflow-hidden border-2 border-pink-500/30">
          {photo ? (
            <img src={photo} alt={partner.name} className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-pink-600/30 to-purple-600/30 flex items-center justify-center">
              <span className="text-lg font-bold text-white/30">{partner.name?.[0]}</span>
            </div>
          )}
        </div>
        {unread_count > 0 && (
          <div className="absolute -top-1 -right-1 w-5 h-5 bg-pink-500 rounded-full flex items-center justify-center">
            <span className="text-[10px] font-bold">{unread_count}</span>
          </div>
        )}
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between">
          <h3 className="font-semibold text-sm truncate">{partner.name}, {partner.age}</h3>
          <span className="text-white/30 text-xs flex items-center gap-1">
            <Clock size={10} /> {timeAgo}
          </span>
        </div>
        <p className="text-white/40 text-xs truncate mt-0.5">
          {last_message
            ? last_message.content
            : 'Novo match! Diga oi!'
          }
        </p>
      </div>

      <MessageCircle size={18} className="text-white/20 shrink-0" />
    </button>
  );
}

function EmptyState() {
  return (
    <div className="flex flex-col items-center justify-center mt-20 px-6">
      <div className="w-20 h-20 glass rounded-full flex items-center justify-center mb-6">
        <Heart size={36} className="text-pink-400/40" />
      </div>
      <h2 className="text-lg font-bold mb-2 text-center">Sem matches ainda</h2>
      <p className="text-white/40 text-sm text-center">Continue explorando perfis e curtindo! Quando alguem curtir voce de volta, aparecera aqui.</p>
    </div>
  );
}

function formatTimeAgo(dateStr) {
  if (!dateStr) return '';
  const now = new Date();
  const date = new Date(dateStr);
  const diff = Math.floor((now - date) / 1000);

  if (diff < 60) return 'agora';
  if (diff < 3600) return `${Math.floor(diff / 60)}m`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h`;
  if (diff < 604800) return `${Math.floor(diff / 86400)}d`;
  return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
}

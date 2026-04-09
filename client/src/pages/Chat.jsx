import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, Send, Phone, Video, MoreVertical, Shield, Ban, Flag, X } from 'lucide-react';
import { api } from '../utils/api';
import { useAuth } from '../contexts/AuthContext';

export default function Chat() {
  const { matchId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [partner, setPartner] = useState(location.state?.partner || null);
  const [loading, setLoading] = useState(true);
  const [showMenu, setShowMenu] = useState(false);
  const [sendError, setSendError] = useState('');
  const bottomRef = useRef(null);

  useEffect(() => {
    api.getMessages(matchId)
      .then(data => setMessages(data.messages))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [matchId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    setSendError('');

    const tempMsg = {
      id: `temp-${Date.now()}`,
      sender_id: user.id,
      content: input.trim(),
      msg_type: 'text',
      created_at: new Date().toISOString(),
    };

    setMessages(prev => [...prev, tempMsg]);
    setInput('');

    try {
      const data = await api.sendMessage(matchId, input.trim());
      setMessages(prev => prev.map(m => m.id === tempMsg.id ? data.message : m));
    } catch (err) {
      setSendError(err.message);
      setMessages(prev => prev.filter(m => m.id !== tempMsg.id));
    }
  };

  const handleCall = async (type) => {
    try {
      const result = await api.checkCall({
        receiver_id: partner?.id,
        call_type: type,
      });
      alert(`Chamada de ${type === 'audio' ? 'audio' : 'video'} iniciada! Minutos restantes: ${result.minutes_remaining}`);
    } catch (err) {
      if (err.message.includes('Plano')) {
        navigate('/plans');
      } else {
        alert(err.message);
      }
    }
  };

  const handleBlock = async () => {
    if (!partner) return;
    if (confirm(`Bloquear ${partner.name}? Voces nao poderao mais interagir.`)) {
      try {
        await api.block(partner.id);
        navigate('/matches');
      } catch (err) {
        alert(err.message);
      }
    }
  };

  const handleReport = async () => {
    if (!partner) return;
    const reason = prompt('Motivo da denuncia:\n1. Perfil falso\n2. Assedio\n3. Spam\n4. Golpe\n5. Outro');
    const reasons = { '1': 'fake_profile', '2': 'harassment', '3': 'spam', '4': 'scam', '5': 'other' };
    if (reason && reasons[reason]) {
      try {
        await api.report({ reported_id: partner.id, reason: reasons[reason] });
        alert('Denuncia enviada. Obrigado por ajudar a manter a comunidade segura!');
      } catch (err) {
        alert(err.message);
      }
    }
  };

  return (
    <div className="flex flex-col h-screen max-h-screen">
      {/* Header */}
      <div className="glass border-b border-white/10 px-4 py-3 flex items-center gap-3 shrink-0">
        <button onClick={() => navigate('/matches')} className="text-white/60 hover:text-white">
          <ArrowLeft size={22} />
        </button>

        <div className="w-10 h-10 rounded-full overflow-hidden border-2 border-pink-500/30 shrink-0">
          {partner?.photos?.[0] ? (
            <img src={partner.photos[0]} alt="" className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-pink-600/30 to-purple-600/30 flex items-center justify-center">
              <span className="text-sm font-bold text-white/30">{partner?.name?.[0]}</span>
            </div>
          )}
        </div>

        <div className="flex-1 min-w-0">
          <h2 className="font-semibold text-sm truncate">{partner?.name || 'Chat'}</h2>
          <p className="text-white/40 text-xs">{partner?.age ? `${partner.age} anos` : ''}</p>
        </div>

        <div className="flex items-center gap-2">
          <button onClick={() => handleCall('audio')} className="w-9 h-9 rounded-full glass flex items-center justify-center text-green-400 hover:bg-green-500/20">
            <Phone size={16} />
          </button>
          <button onClick={() => handleCall('video')} className="w-9 h-9 rounded-full glass flex items-center justify-center text-blue-400 hover:bg-blue-500/20">
            <Video size={16} />
          </button>
          <div className="relative">
            <button onClick={() => setShowMenu(!showMenu)} className="w-9 h-9 rounded-full glass flex items-center justify-center text-white/40 hover:text-white">
              <MoreVertical size={16} />
            </button>

            {showMenu && (
              <>
                <div className="fixed inset-0 z-40" onClick={() => setShowMenu(false)} />
                <div className="absolute right-0 top-12 w-48 glass rounded-xl overflow-hidden z-50 slide-up">
                  <button onClick={() => { setShowMenu(false); handleReport(); }} className="w-full px-4 py-3 text-left text-sm hover:bg-white/10 flex items-center gap-2 text-yellow-400">
                    <Flag size={14} /> Denunciar
                  </button>
                  <button onClick={() => { setShowMenu(false); handleBlock(); }} className="w-full px-4 py-3 text-left text-sm hover:bg-white/10 flex items-center gap-2 text-red-400">
                    <Ban size={14} /> Bloquear
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3">
        {loading ? (
          <div className="flex justify-center pt-8">
            <p className="text-white/30 text-sm">Carregando mensagens...</p>
          </div>
        ) : messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center pt-12">
            <div className="w-16 h-16 glass rounded-full flex items-center justify-center mb-4">
              <Shield size={28} className="text-pink-400/60" />
            </div>
            <p className="text-white/40 text-sm text-center">Novo match! Envie uma mensagem.</p>
            <p className="text-white/20 text-xs text-center mt-2">Lembre-se: respeito sempre!</p>
          </div>
        ) : (
          messages.map(msg => (
            <MessageBubble key={msg.id} message={msg} isMe={msg.sender_id === user.id} />
          ))
        )}
        <div ref={bottomRef} />
      </div>

      {/* Error */}
      {sendError && (
        <div className="mx-4 mb-2 bg-red-500/10 border border-red-500/20 rounded-xl p-3 flex items-center justify-between">
          <p className="text-red-400 text-xs">{sendError}</p>
          <button onClick={() => setSendError('')}><X size={14} className="text-red-400" /></button>
        </div>
      )}

      {/* Input */}
      <form onSubmit={handleSend} className="glass border-t border-white/10 p-3 flex items-center gap-2 shrink-0">
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Digite uma mensagem..."
          maxLength={2000}
          className="flex-1 bg-white/5 rounded-xl py-3 px-4 text-sm text-white placeholder:text-white/30 focus:outline-none focus:bg-white/10 transition"
        />
        <button
          type="submit"
          disabled={!input.trim()}
          className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center disabled:opacity-30 active:scale-90 transition-all shrink-0"
        >
          <Send size={18} className="text-white" />
        </button>
      </form>
    </div>
  );
}

function MessageBubble({ message, isMe }) {
  const time = new Date(message.created_at).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

  return (
    <div className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
      <div className={`max-w-[75%] rounded-2xl px-4 py-2.5 ${
        isMe
          ? 'gradient-primary text-white rounded-br-md'
          : 'glass text-white/90 rounded-bl-md'
      }`}>
        <p className="text-sm leading-relaxed break-words">{message.content}</p>
        <p className={`text-[10px] mt-1 ${isMe ? 'text-white/60' : 'text-white/30'} text-right`}>
          {time}
        </p>
      </div>
    </div>
  );
}

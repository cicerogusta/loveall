import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { ArrowLeft, Mail, Lock, Eye, EyeOff } from 'lucide-react';

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/discover');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen px-6 py-8">
      <button onClick={() => navigate('/welcome')} className="text-white/60 hover:text-white mb-8">
        <ArrowLeft size={24} />
      </button>

      <h1 className="text-3xl font-bold mb-2">Bem-vindo de volta</h1>
      <p className="text-white/50 mb-8">Entre na sua conta LoveAll</p>

      {error && (
        <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-3 mb-6">
          <p className="text-red-400 text-sm">{error}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="relative">
          <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-white/30" />
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="w-full bg-white/5 border border-white/10 rounded-xl py-4 pl-12 pr-4 text-white placeholder:text-white/30 focus:outline-none focus:border-pink-500/50 transition"
          />
        </div>

        <div className="relative">
          <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-white/30" />
          <input
            type={showPw ? 'text' : 'password'}
            placeholder="Senha"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="w-full bg-white/5 border border-white/10 rounded-xl py-4 pl-12 pr-12 text-white placeholder:text-white/30 focus:outline-none focus:border-pink-500/50 transition"
          />
          <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-4 top-1/2 -translate-y-1/2 text-white/30">
            {showPw ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full py-4 rounded-2xl gradient-primary text-white font-semibold text-lg shadow-lg shadow-pink-500/30 disabled:opacity-50 active:scale-[0.98] transition-transform mt-6"
        >
          {loading ? 'Entrando...' : 'Entrar'}
        </button>
      </form>

      <p className="text-center text-white/40 text-sm mt-8">
        Nao tem conta?{' '}
        <button onClick={() => navigate('/register')} className="text-pink-400 font-medium">
          Cadastre-se
        </button>
      </p>
    </div>
  );
}

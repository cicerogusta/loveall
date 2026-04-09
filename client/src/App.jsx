import { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import Welcome from './pages/Welcome';
import Login from './pages/Login';
import Register from './pages/Register';
import Discover from './pages/Discover';
import Matches from './pages/Matches';
import Chat from './pages/Chat';
import Profile from './pages/Profile';
import EditProfile from './pages/EditProfile';
import Plans from './pages/Plans';
import Safety from './pages/Safety';
import PaymentSuccess from './pages/PaymentSuccess';
import PaymentFailed from './pages/PaymentFailed';
import BottomNav from './components/BottomNav';

function PrivateRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <LoadingScreen />;
  return user ? children : <Navigate to="/welcome" />;
}

function PublicRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <LoadingScreen />;
  return !user ? children : <Navigate to="/discover" />;
}

// Coracoes flutuantes
const HEARTS = Array.from({ length: 12 }, (_, i) => ({
  id: i,
  size: Math.random() * 14 + 8,
  left: Math.random() * 100,
  delay: Math.random() * 3,
  duration: Math.random() * 3 + 4,
  opacity: Math.random() * 0.3 + 0.1,
}));

function LoadingScreen() {
  const [progress, setProgress] = useState(0);
  const [fadeOut, setFadeOut] = useState(false);

  useEffect(() => {
    const interval = setInterval(() => {
      setProgress(prev => {
        if (prev >= 100) {
          clearInterval(interval);
          setTimeout(() => setFadeOut(true), 300);
          return 100;
        }
        // Progresso mais lento para durar ~4s
        return prev + Math.random() * 5 + 2;
      });
    }, 120);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className={`splash-screen ${fadeOut ? 'splash-fade-out' : ''}`}>
      {/* Fundo com gradiente animado */}
      <div className="splash-bg" />

      {/* Particulas de coracao flutuantes */}
      <div className="splash-particles">
        {HEARTS.map(h => (
          <div
            key={h.id}
            className="splash-heart-particle"
            style={{
              left: `${h.left}%`,
              fontSize: `${h.size}px`,
              animationDelay: `${h.delay}s`,
              animationDuration: `${h.duration}s`,
              opacity: h.opacity,
            }}
          />
        ))}
      </div>

      {/* Conteudo central */}
      <div className="splash-content">
        {/* Anel giratorio */}
        <div className="splash-ring" />
        <div className="splash-ring splash-ring-2" />

        {/* Coracao principal */}
        <div className="splash-heart-wrapper">
          <div className="splash-heart-glow" />
          <div className="splash-heart-main">
            <svg viewBox="0 0 24 24" fill="currentColor" className="w-14 h-14 text-white drop-shadow-lg">
              <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z"/>
            </svg>
          </div>
        </div>

        {/* Titulo */}
        <h1 className="splash-title">LoveAll</h1>
        <p className="splash-subtitle">Amor para Todos</p>

        {/* Barra de progresso */}
        <div className="splash-progress-track">
          <div className="splash-progress-bar" style={{ width: `${Math.min(progress, 100)}%` }} />
        </div>
      </div>
    </div>
  );
}

export default function App() {
  const { user, loading } = useAuth();
  const [showSplash, setShowSplash] = useState(true);

  useEffect(() => {
    // Mostra splash por no minimo 4s
    const minTimer = setTimeout(() => {
      if (!loading) {
        setShowSplash(false);
      }
    }, 4000);
    return () => clearTimeout(minTimer);
  }, [loading]);

  // Se o auth terminar depois do timer minimo
  useEffect(() => {
    if (!loading && showSplash) {
      const timer = setTimeout(() => setShowSplash(false), 4000);
      return () => clearTimeout(timer);
    }
  }, [loading, showSplash]);

  if (showSplash) return <LoadingScreen />;

  return (
    <div className="min-h-screen relative pb-20">
      <Routes>
        <Route path="/welcome" element={<PublicRoute><Welcome /></PublicRoute>} />
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
        <Route path="/discover" element={<PrivateRoute><Discover /></PrivateRoute>} />
        <Route path="/matches" element={<PrivateRoute><Matches /></PrivateRoute>} />
        <Route path="/chat/:matchId" element={<PrivateRoute><Chat /></PrivateRoute>} />
        <Route path="/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
        <Route path="/edit-profile" element={<PrivateRoute><EditProfile /></PrivateRoute>} />
        <Route path="/plans" element={<PrivateRoute><Plans /></PrivateRoute>} />
        <Route path="/safety" element={<PrivateRoute><Safety /></PrivateRoute>} />
        <Route path="/payment-success" element={<PaymentSuccess />} />
        <Route path="/payment-failed" element={<PaymentFailed />} />
        <Route path="*" element={<Navigate to={user ? "/discover" : "/welcome"} />} />
      </Routes>
      {user && <BottomNav />}
    </div>
  );
}

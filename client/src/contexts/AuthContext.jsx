import { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../utils/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('loveall_token');
    if (token) {
      api.getMe()
        .then(data => setUser(data.user))
        .catch(() => localStorage.removeItem('loveall_token'))
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (email, password) => {
    const data = await api.login({ email, password });
    localStorage.setItem('loveall_token', data.token);
    setUser(data.user);
    return data;
  };

  const register = async (formData) => {
    try {
      const data = await api.register(formData);
      localStorage.setItem('loveall_token', data.token);

      // Buscar dados completos do usuário
      try {
        const userData = await api.getMe();
        setUser(userData.user);
      } catch (err) {
        setUser(data.user);
      }

      return data;
    } catch (err) {
      throw err;
    }
  };

  const logout = () => {
    localStorage.removeItem('loveall_token');
    setUser(null);
  };

  const updateUser = (updates) => {
    setUser(prev => ({ ...prev, ...updates }));
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);

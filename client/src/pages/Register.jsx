import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GENDERS } from '../utils/constants';
import { ArrowLeft, ArrowRight, User, Mail, Lock, Calendar, Heart, AlertCircle, Eye, EyeOff } from 'lucide-react';

const validators = {
  name: (v) => {
    if (!v.trim()) return 'Nome e obrigatorio';
    if (v.trim().length < 2) return 'Nome deve ter pelo menos 2 caracteres';
    if (v.trim().length > 50) return 'Nome deve ter no maximo 50 caracteres';
    if (/\d/.test(v)) return 'Nome nao pode conter numeros';
    return '';
  },
  email: (v) => {
    if (!v.trim()) return 'Email e obrigatorio';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) return 'Email invalido';
    return '';
  },
  password: (v) => {
    if (!v) return 'Senha e obrigatoria';
    if (v.length < 8) return 'Minimo 8 caracteres';
    if (!/[a-zA-Z]/.test(v)) return 'Deve conter pelo menos 1 letra';
    if (!/\d/.test(v)) return 'Deve conter pelo menos 1 numero';
    return '';
  },
  birthdate: (v) => {
    if (!v) return 'Data de nascimento e obrigatoria';
    const birth = new Date(v);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
    if (age < 18) return 'Voce deve ter pelo menos 18 anos';
    if (age > 120) return 'Data de nascimento invalida';
    return '';
  },
  gender: (v) => {
    if (!v) return 'Selecione seu genero';
    return '';
  }
};

function getPasswordStrength(pw) {
  if (!pw) return { level: 0, label: '', color: '' };
  let score = 0;
  if (pw.length >= 8) score++;
  if (pw.length >= 12) score++;
  if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) score++;
  if (/\d/.test(pw)) score++;
  if (/[^a-zA-Z0-9]/.test(pw)) score++;

  if (score <= 1) return { level: 1, label: 'Fraca', color: 'bg-red-500' };
  if (score <= 2) return { level: 2, label: 'Razoavel', color: 'bg-orange-500' };
  if (score <= 3) return { level: 3, label: 'Boa', color: 'bg-yellow-500' };
  if (score <= 4) return { level: 4, label: 'Forte', color: 'bg-green-400' };
  return { level: 5, label: 'Excelente', color: 'bg-green-500' };
}

export default function Register() {
  const navigate = useNavigate();
  const { register, user } = useAuth();
  const [step, setStep] = useState(1);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [registrationComplete, setRegistrationComplete] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [form, setForm] = useState({
    name: '', email: '', password: '', birthdate: '',
    gender: '', gender_custom: '', looking_for: []
  });

  // Quando o registro for bem-sucedido e o user estiver setado, navega
  useEffect(() => {
    if (registrationComplete && user) {
      navigate('/edit-profile');
    }
  }, [user, registrationComplete, navigate]);

  const update = (key, value) => {
    setForm(f => ({ ...f, [key]: value }));
    // Validar em tempo real se o campo ja foi tocado
    if (touched[key] && validators[key]) {
      setFieldErrors(prev => ({ ...prev, [key]: validators[key](value) }));
    }
  };

  const handleBlur = (key) => {
    setTouched(prev => ({ ...prev, [key]: true }));
    if (validators[key]) {
      setFieldErrors(prev => ({ ...prev, [key]: validators[key](form[key]) }));
    }
  };

  const toggleLookingFor = (gender) => {
    setForm(f => ({
      ...f,
      looking_for: f.looking_for.includes(gender)
        ? f.looking_for.filter(g => g !== gender)
        : [...f.looking_for, gender]
    }));
  };

  const validateStep = (s) => {
    const errors = {};
    if (s === 1) {
      errors.name = validators.name(form.name);
      errors.email = validators.email(form.email);
      errors.password = validators.password(form.password);
    } else if (s === 2) {
      errors.birthdate = validators.birthdate(form.birthdate);
      errors.gender = validators.gender(form.gender);
      if (form.gender === 'other' && !form.gender_custom.trim()) {
        errors.gender_custom = 'Informe como voce se identifica';
      }
    }
    return errors;
  };

  const handleNext = () => {
    const errors = validateStep(step);
    const hasErrors = Object.values(errors).some(e => e);

    if (hasErrors) {
      setFieldErrors(prev => ({ ...prev, ...errors }));
      // Marcar todos os campos como tocados
      const touchAll = {};
      Object.keys(errors).forEach(k => touchAll[k] = true);
      setTouched(prev => ({ ...prev, ...touchAll }));
      return;
    }

    setError('');
    setStep(step + 1);
  };

  const handleSubmit = async () => {
    setError('');
    setLoading(true);
    try {
      await register(form);
      setRegistrationComplete(true);
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  const canNext = () => {
    if (step === 1) {
      return form.name.trim().length >= 2 && form.email.includes('@') && form.password.length >= 8;
    }
    if (step === 2) {
      return form.birthdate && form.gender && (form.gender !== 'other' || form.gender_custom.trim());
    }
    if (step === 3) return true;
    return false;
  };

  const pwStrength = getPasswordStrength(form.password);

  return (
    <div className="min-h-screen px-6 py-8">
      <div className="flex items-center justify-between mb-8">
        <button onClick={() => step > 1 ? setStep(step - 1) : navigate('/welcome')} className="text-white/60 hover:text-white">
          <ArrowLeft size={24} />
        </button>
        <div className="flex gap-2">
          {[1, 2, 3].map(s => (
            <div key={s} className={`h-1.5 rounded-full transition-all ${s === step ? 'w-8 bg-pink-500' : s < step ? 'w-8 bg-pink-500/50' : 'w-8 bg-white/10'}`} />
          ))}
        </div>
        <div className="w-6" />
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-3 mb-6 flex items-start gap-2">
          <AlertCircle size={16} className="text-red-400 mt-0.5 shrink-0" />
          <p className="text-red-400 text-sm">{error}</p>
        </div>
      )}

      {/* Step 1: Dados basicos */}
      {step === 1 && (
        <div className="slide-up">
          <h1 className="text-3xl font-bold mb-2">Crie sua conta</h1>
          <p className="text-white/50 mb-8">Vamos comecar com o basico</p>

          <div className="space-y-4">
            <div>
              <InputField
                icon={<User size={18} />}
                type="text"
                placeholder="Seu nome"
                value={form.name}
                onChange={e => update('name', e.target.value)}
                onBlur={() => handleBlur('name')}
                hasError={touched.name && fieldErrors.name}
              />
              {touched.name && fieldErrors.name && (
                <p className="text-red-400 text-xs mt-1 ml-1">{fieldErrors.name}</p>
              )}
            </div>

            <div>
              <InputField
                icon={<Mail size={18} />}
                type="email"
                placeholder="Email"
                value={form.email}
                onChange={e => update('email', e.target.value)}
                onBlur={() => handleBlur('email')}
                hasError={touched.email && fieldErrors.email}
              />
              {touched.email && fieldErrors.email && (
                <p className="text-red-400 text-xs mt-1 ml-1">{fieldErrors.email}</p>
              )}
            </div>

            <div>
              <div className="relative">
                <InputField
                  icon={<Lock size={18} />}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Senha (min. 8 caracteres)"
                  value={form.password}
                  onChange={e => update('password', e.target.value)}
                  onBlur={() => handleBlur('password')}
                  hasError={touched.password && fieldErrors.password}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-white/30 hover:text-white/60"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {touched.password && fieldErrors.password && (
                <p className="text-red-400 text-xs mt-1 ml-1">{fieldErrors.password}</p>
              )}
              {form.password && (
                <div className="mt-2">
                  <div className="flex gap-1 mb-1">
                    {[1, 2, 3, 4, 5].map(i => (
                      <div key={i} className={`h-1 flex-1 rounded-full transition-all ${i <= pwStrength.level ? pwStrength.color : 'bg-white/10'}`} />
                    ))}
                  </div>
                  <p className={`text-xs ${pwStrength.level <= 2 ? 'text-red-400' : pwStrength.level <= 3 ? 'text-yellow-400' : 'text-green-400'}`}>
                    Senha: {pwStrength.label}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Step 2: Genero e idade */}
      {step === 2 && (
        <div className="slide-up">
          <h1 className="text-3xl font-bold mb-2">Sobre voce</h1>
          <p className="text-white/50 mb-6">Como voce se identifica?</p>

          <div className="mb-6">
            <label className="text-white/60 text-sm mb-2 flex items-center gap-2">
              <Calendar size={14} /> Data de nascimento
            </label>
            <input
              type="date"
              value={form.birthdate}
              onChange={e => update('birthdate', e.target.value)}
              onBlur={() => handleBlur('birthdate')}
              max={new Date(new Date().setFullYear(new Date().getFullYear() - 18)).toISOString().split('T')[0]}
              className={`w-full bg-white/5 border rounded-xl py-3 px-4 text-white focus:outline-none transition ${
                touched.birthdate && fieldErrors.birthdate ? 'border-red-500/50' : 'border-white/10 focus:border-pink-500/50'
              }`}
            />
            {touched.birthdate && fieldErrors.birthdate && (
              <p className="text-red-400 text-xs mt-1 ml-1">{fieldErrors.birthdate}</p>
            )}
          </div>

          <label className="text-white/60 text-sm mb-3 block">Seu genero</label>
          <div className="grid grid-cols-2 gap-2 max-h-[300px] overflow-y-auto pr-1">
            {GENDERS.map(g => (
              <button
                key={g.value}
                onClick={() => {
                  update('gender', g.value);
                  setTouched(prev => ({ ...prev, gender: true }));
                  setFieldErrors(prev => ({ ...prev, gender: '' }));
                }}
                className={`p-3 rounded-xl text-left text-sm transition-all ${
                  form.gender === g.value
                    ? 'gradient-primary text-white shadow-lg shadow-pink-500/20'
                    : 'glass text-white/70 hover:text-white'
                }`}
              >
                <span className="mr-2">{g.emoji}</span>{g.label}
              </button>
            ))}
          </div>
          {touched.gender && fieldErrors.gender && (
            <p className="text-red-400 text-xs mt-2 ml-1">{fieldErrors.gender}</p>
          )}

          {form.gender === 'other' && (
            <div>
              <input
                type="text"
                placeholder="Como voce se identifica?"
                value={form.gender_custom}
                onChange={e => update('gender_custom', e.target.value)}
                onBlur={() => handleBlur('gender_custom')}
                className={`w-full mt-3 bg-white/5 border rounded-xl py-3 px-4 text-white placeholder:text-white/30 focus:outline-none transition ${
                  touched.gender_custom && fieldErrors.gender_custom ? 'border-red-500/50' : 'border-white/10 focus:border-pink-500/50'
                }`}
              />
              {touched.gender_custom && fieldErrors.gender_custom && (
                <p className="text-red-400 text-xs mt-1 ml-1">{fieldErrors.gender_custom}</p>
              )}
            </div>
          )}
        </div>
      )}

      {/* Step 3: Preferencias */}
      {step === 3 && (
        <div className="slide-up">
          <h1 className="text-3xl font-bold mb-2">Seus interesses</h1>
          <p className="text-white/50 mb-6">Quem voce gostaria de conhecer?</p>

          <div className="flex items-center gap-2 mb-4">
            <Heart size={16} className="text-pink-400" />
            <span className="text-white/60 text-sm">Selecione um ou mais (ou deixe vazio para ver todos)</span>
          </div>

          <div className="grid grid-cols-2 gap-2">
            {GENDERS.map(g => (
              <button
                key={g.value}
                onClick={() => toggleLookingFor(g.value)}
                className={`p-3 rounded-xl text-left text-sm transition-all ${
                  form.looking_for.includes(g.value)
                    ? 'gradient-primary text-white shadow-lg shadow-pink-500/20'
                    : 'glass text-white/70 hover:text-white'
                }`}
              >
                <span className="mr-2">{g.emoji}</span>{g.label}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Navigation */}
      <div className="mt-8">
        {step < 3 ? (
          <button
            onClick={handleNext}
            disabled={!canNext()}
            className="w-full py-4 rounded-2xl gradient-primary text-white font-semibold text-lg shadow-lg shadow-pink-500/30 disabled:opacity-30 flex items-center justify-center gap-2 active:scale-[0.98] transition-transform"
          >
            Continuar <ArrowRight size={20} />
          </button>
        ) : (
          <button
            onClick={handleSubmit}
            disabled={loading}
            className="w-full py-4 rounded-2xl gradient-primary text-white font-semibold text-lg shadow-lg shadow-pink-500/30 disabled:opacity-50 active:scale-[0.98] transition-transform"
          >
            {loading ? 'Criando conta...' : 'Comecar a explorar!'}
          </button>
        )}
      </div>
    </div>
  );
}

function InputField({ icon, hasError, ...props }) {
  return (
    <div className="relative">
      <span className={`absolute left-4 top-1/2 -translate-y-1/2 ${hasError ? 'text-red-400' : 'text-white/30'}`}>{icon}</span>
      <input
        {...props}
        className={`w-full bg-white/5 border rounded-xl py-4 pl-12 pr-4 text-white placeholder:text-white/30 focus:outline-none transition ${
          hasError ? 'border-red-500/50' : 'border-white/10 focus:border-pink-500/50'
        }`}
      />
    </div>
  );
}

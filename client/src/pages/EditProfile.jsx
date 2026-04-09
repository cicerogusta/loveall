import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GENDERS } from '../utils/constants';
import { ArrowLeft, Camera, Save, Plus, X } from 'lucide-react';
import { api } from '../utils/api';

export default function EditProfile() {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState({
    name: user?.name || '',
    bio: user?.bio || '',
    gender: user?.gender || '',
    gender_custom: user?.gender_custom || '',
    city: user?.city || '',
    photos: user?.photos || [],
    looking_for: user?.looking_for || [],
    age_min: user?.age_min || 18,
    age_max: user?.age_max || 99,
    max_distance: user?.max_distance || 50,
  });

  const [photoUrl, setPhotoUrl] = useState('');

  const update = (key, value) => setForm(f => ({ ...f, [key]: value }));

  const addPhoto = () => {
    if (photoUrl.trim() && form.photos.length < 6) {
      update('photos', [...form.photos, photoUrl.trim()]);
      setPhotoUrl('');
    }
  };

  const removePhoto = (index) => {
    update('photos', form.photos.filter((_, i) => i !== index));
  };

  const toggleLookingFor = (g) => {
    update('looking_for',
      form.looking_for.includes(g)
        ? form.looking_for.filter(v => v !== g)
        : [...form.looking_for, g]
    );
  };

  const handleSave = async () => {
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const data = await api.updateProfile(form);
      updateUser(data.user);
      setSuccess('Perfil atualizado!');
      setTimeout(() => navigate('/profile'), 1000);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen pt-6 px-4 pb-24">
      <div className="flex items-center gap-3 mb-6">
        <button onClick={() => navigate('/profile')} className="text-white/60 hover:text-white">
          <ArrowLeft size={22} />
        </button>
        <h1 className="text-xl font-bold">Editar Perfil</h1>
      </div>

      {error && <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-3 mb-4"><p className="text-red-400 text-sm">{error}</p></div>}
      {success && <div className="bg-green-500/10 border border-green-500/20 rounded-xl p-3 mb-4"><p className="text-green-400 text-sm">{success}</p></div>}

      {/* Photos */}
      <section className="mb-6">
        <label className="text-white/60 text-sm mb-3 flex items-center gap-2">
          <Camera size={14} /> Fotos ({form.photos.length}/6)
        </label>
        <div className="grid grid-cols-3 gap-2 mb-3">
          {form.photos.map((photo, i) => (
            <div key={i} className="aspect-square rounded-xl overflow-hidden relative group">
              <img src={photo} alt="" className="w-full h-full object-cover" />
              <button
                onClick={() => removePhoto(i)}
                className="absolute top-1 right-1 w-6 h-6 bg-black/60 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition"
              >
                <X size={12} />
              </button>
            </div>
          ))}
          {form.photos.length < 6 && (
            <div className="aspect-square rounded-xl glass flex items-center justify-center">
              <Plus size={24} className="text-white/20" />
            </div>
          )}
        </div>
        <div className="flex gap-2">
          <input
            type="url"
            placeholder="URL da foto"
            value={photoUrl}
            onChange={e => setPhotoUrl(e.target.value)}
            className="flex-1 bg-white/5 border border-white/10 rounded-xl py-2.5 px-4 text-sm text-white placeholder:text-white/30 focus:outline-none focus:border-pink-500/50"
          />
          <button onClick={addPhoto} disabled={!photoUrl.trim()} className="px-4 rounded-xl gradient-primary text-white text-sm font-medium disabled:opacity-30">
            Adicionar
          </button>
        </div>
      </section>

      {/* Basic info */}
      <section className="space-y-4 mb-6">
        <div>
          <label className="text-white/60 text-xs mb-1 block">Nome</label>
          <input value={form.name} onChange={e => update('name', e.target.value)} className="w-full bg-white/5 border border-white/10 rounded-xl py-3 px-4 text-white focus:outline-none focus:border-pink-500/50 text-sm" />
        </div>
        <div>
          <label className="text-white/60 text-xs mb-1 block">Bio</label>
          <textarea
            value={form.bio}
            onChange={e => update('bio', e.target.value)}
            maxLength={500}
            rows={3}
            placeholder="Conte um pouco sobre voce..."
            className="w-full bg-white/5 border border-white/10 rounded-xl py-3 px-4 text-white placeholder:text-white/30 focus:outline-none focus:border-pink-500/50 text-sm resize-none"
          />
          <p className="text-white/20 text-xs text-right">{form.bio.length}/500</p>
        </div>
        <div>
          <label className="text-white/60 text-xs mb-1 block">Cidade</label>
          <input value={form.city} onChange={e => update('city', e.target.value)} placeholder="Sua cidade" className="w-full bg-white/5 border border-white/10 rounded-xl py-3 px-4 text-white placeholder:text-white/30 focus:outline-none focus:border-pink-500/50 text-sm" />
        </div>
      </section>

      {/* Gender */}
      <section className="mb-6">
        <label className="text-white/60 text-xs mb-3 block">Seu genero</label>
        <div className="grid grid-cols-2 gap-2">
          {GENDERS.map(g => (
            <button
              key={g.value}
              onClick={() => update('gender', g.value)}
              className={`p-2.5 rounded-xl text-xs transition-all ${
                form.gender === g.value
                  ? 'gradient-primary text-white'
                  : 'glass text-white/60 hover:text-white'
              }`}
            >
              {g.emoji} {g.label}
            </button>
          ))}
        </div>
      </section>

      {/* Looking for */}
      <section className="mb-6">
        <label className="text-white/60 text-xs mb-3 block">Interesse em (deixe vazio para todos)</label>
        <div className="grid grid-cols-2 gap-2">
          {GENDERS.map(g => (
            <button
              key={g.value}
              onClick={() => toggleLookingFor(g.value)}
              className={`p-2.5 rounded-xl text-xs transition-all ${
                form.looking_for.includes(g.value)
                  ? 'gradient-primary text-white'
                  : 'glass text-white/60 hover:text-white'
              }`}
            >
              {g.emoji} {g.label}
            </button>
          ))}
        </div>
      </section>

      {/* Distance & Age */}
      <section className="mb-8 space-y-4">
        <div>
          <label className="text-white/60 text-xs mb-1 flex justify-between">
            <span>Distancia maxima</span>
            <span className="text-pink-400">{form.max_distance} km</span>
          </label>
          <input type="range" min="5" max="200" value={form.max_distance} onChange={e => update('max_distance', parseInt(e.target.value))} className="w-full accent-pink-500" />
        </div>
        <div className="flex gap-4">
          <div className="flex-1">
            <label className="text-white/60 text-xs mb-1 block">Idade min</label>
            <input type="number" min="18" max="99" value={form.age_min} onChange={e => update('age_min', parseInt(e.target.value))} className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 px-3 text-white text-sm focus:outline-none focus:border-pink-500/50" />
          </div>
          <div className="flex-1">
            <label className="text-white/60 text-xs mb-1 block">Idade max</label>
            <input type="number" min="18" max="99" value={form.age_max} onChange={e => update('age_max', parseInt(e.target.value))} className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 px-3 text-white text-sm focus:outline-none focus:border-pink-500/50" />
          </div>
        </div>
      </section>

      {/* Save */}
      <button
        onClick={handleSave}
        disabled={saving}
        className="w-full py-4 rounded-2xl gradient-primary text-white font-semibold text-lg shadow-lg shadow-pink-500/30 disabled:opacity-50 flex items-center justify-center gap-2 active:scale-[0.98] transition-transform"
      >
        <Save size={20} /> {saving ? 'Salvando...' : 'Salvar Perfil'}
      </button>
    </div>
  );
}

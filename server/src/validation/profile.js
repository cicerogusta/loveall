import db from '../database.js';

export const genders = new Set([
  'woman',
  'man',
  'non_binary',
  'transgender_woman',
  'transgender_man',
  'genderqueer',
  'genderfluid',
  'agender',
  'bigender',
  'two_spirit',
  'other',
  'prefer_not_say',
]);
export const legalVersion = '2026-11-20';
const text = (value, max, min = 0) =>
  typeof value === 'string' && value.trim().length >= min && value.length <= max;
const integer = (value, min, max) => Number.isInteger(value) && value >= min && value <= max;
export const validName = (value) => text(value, 50, 2) && !/\p{N}/u.test(value);
// bcrypt considera no máximo 72 bytes: não aceita cadastros com sufixos ignorados.
export const validPassword = (value) => text(value, 72, 8) && Buffer.byteLength(value, 'utf8') <= 72 && /[a-zA-Z]/.test(value) && /[0-9]/.test(value);
export const validPreferences = (value) =>
  Array.isArray(value) &&
  value.length <= genders.size &&
  value.every((item) => genders.has(item)) &&
  new Set(value).size === value.length;

export function validBirthdate(value, today = new Date()) {
  if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const birth = new Date(`${value}T00:00:00Z`);
  if (Number.isNaN(birth.getTime()) || birth.toISOString().slice(0, 10) !== value) return false;
  const todayIso = today.toISOString().slice(0, 10);
  let age = Number(todayIso.slice(0, 4)) - Number(value.slice(0, 4));
  if (todayIso.slice(5) < value.slice(5)) age--;
  return age >= 18 && age <= 120;
}

async function ownsPhoto(reference, user) {
  if (typeof reference !== 'string') return false;
  // Referências externas antigas podem ser preservadas, mas não introduzidas por outro cliente.
  if (!reference.startsWith('/api/photos/') && user.photos.includes(reference)) return true;
  return Boolean(
    await db
      .prepare(
        'SELECT 1 FROM uploaded_photos WHERE reference = ? AND user_id = ? AND pending_delete = 0 AND ready = 1'
      )
      .get(reference, user.id)
  );
}

export async function validateProfilePatch(patch, user) {
  const validators = {
    name: validName,
    bio: (v) => text(v, 500),
    city: (v) => text(v, 100),
    gender: (v) => genders.has(v),
    gender_custom: (v) => text(v, 100),
    looking_for: validPreferences,
    photos: (v) => Array.isArray(v) && v.length <= 6 && new Set(v).size === v.length,
    age_min: (v) => integer(v, 18, 99),
    age_max: (v) => integer(v, 18, 99),
    max_distance: (v) => integer(v, 5, 200),
    location_lat: (v) => v === null || (typeof v === 'number' && Number.isFinite(v) && Math.abs(v) <= 90),
    location_lng: (v) => v === null || (typeof v === 'number' && Number.isFinite(v) && Math.abs(v) <= 180),
  };
  if (!patch || typeof patch !== 'object' || Array.isArray(patch) || Object.keys(patch).length === 0)
    return false;
  if (
    !Object.entries(patch).every(([key, value]) => Object.hasOwn(validators, key) && validators[key](value))
  )
    return false;
  if (patch.photos)
    for (const photo of patch.photos) {
      if (!(await ownsPhoto(photo, user))) return false;
    }
  const next = { ...user, ...patch };
  return next.age_min <= next.age_max && (next.gender !== 'other' || text(next.gender_custom, 100, 1));
}

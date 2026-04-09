export const GENDERS = [
  { value: 'woman', label: 'Mulher', emoji: '👩' },
  { value: 'man', label: 'Homem', emoji: '👨' },
  { value: 'non_binary', label: 'Nao-binario', emoji: '🧑' },
  { value: 'transgender_woman', label: 'Mulher trans', emoji: '🏳️‍⚧️' },
  { value: 'transgender_man', label: 'Homem trans', emoji: '🏳️‍⚧️' },
  { value: 'genderqueer', label: 'Genderqueer', emoji: '🌈' },
  { value: 'genderfluid', label: 'Genderfluid', emoji: '🌊' },
  { value: 'agender', label: 'Agênero', emoji: '⭐' },
  { value: 'bigender', label: 'Bigênero', emoji: '✨' },
  { value: 'two_spirit', label: 'Two-Spirit', emoji: '🪶' },
  { value: 'other', label: 'Outro', emoji: '💜' },
  { value: 'prefer_not_say', label: 'Prefiro nao dizer', emoji: '🤫' },
];

export const REPORT_REASONS = [
  { value: 'fake_profile', label: 'Perfil falso' },
  { value: 'inappropriate_content', label: 'Conteudo inapropriado' },
  { value: 'harassment', label: 'Assédio' },
  { value: 'spam', label: 'Spam' },
  { value: 'underage', label: 'Menor de idade' },
  { value: 'scam', label: 'Golpe / Scam' },
  { value: 'hate_speech', label: 'Discurso de ódio' },
  { value: 'violence', label: 'Violência' },
  { value: 'other', label: 'Outro' },
];

export const CALL_PLANS = {
  basic: { price: 'R$ 9,90', minutes: '60 min', name: 'Básico', color: 'from-blue-500 to-blue-600' },
  plus: { price: 'R$ 19,90', minutes: '180 min', name: 'Plus', color: 'from-purple-500 to-purple-600' },
  premium: { price: 'R$ 29,90', minutes: 'Ilimitado', name: 'Premium', color: 'from-pink-500 to-rose-600' },
};

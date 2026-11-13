import { legalVersion } from '../validation/profile.js';

export function requireAcceptedTerms(req, res, next) {
  if (req.user.terms_version !== legalVersion) return res.status(403).json({ error: 'Leia e aceite os termos atuais antes de publicar conteúdo.' });
  next();
}

// Um único proxy é aceito somente na implantação em que a API não publica porta externa.
export function proxyHops(value = '0') {
  if (value !== '0' && value !== '1') throw new Error('TRUST_PROXY_HOPS deve ser 0 ou 1.');
  return Number(value);
}

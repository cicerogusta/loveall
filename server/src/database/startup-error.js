const hints = {
  '28P01':
    'Usuário ou senha do banco inválidos. Confira o usuário completo do Session pooler e a senha do banco codificada para URL.',
  28000: 'Autenticação recusada. Confira o usuário completo e o projeto na conexão do Session pooler.',
  '3D000': 'Banco inexistente. Copie novamente a conexão do projeto no Supabase.',
  42501: 'Sem permissão para inicializar o schema loveall. Confira o usuário do banco.',
  ENOTFOUND: 'Host do banco não encontrado. Copie novamente o endereço do Session pooler.',
  EAI_AGAIN: 'Falha temporária de DNS ao localizar o banco. Tente novamente.',
  ENETUNREACH: 'Rede do banco inacessível. No Render, use Session pooler na porta 5432, compatível com IPv4.',
  EHOSTUNREACH: 'Host do banco inacessível. Confira o Session pooler e as restrições de rede do projeto.',
  ECONNREFUSED: 'Conexão recusada. Confira host, porta 5432 e se o projeto Supabase está ativo.',
  ETIMEDOUT: 'Tempo de conexão esgotado. Confira o projeto ativo e as restrições de rede do Supabase.',
  SELF_SIGNED_CERT_IN_CHAIN:
    'Certificado do banco não confiável. Cadastre a CA do projeto como Secret File e configure PG_CA_CERT_PATH. Mantenha a validação TLS.',
  DEPTH_ZERO_SELF_SIGNED_CERT:
    'Certificado autoassinado. Configure a CA correta do projeto em PG_CA_CERT_PATH.',
  UNABLE_TO_VERIFY_LEAF_SIGNATURE:
    'Não foi possível validar o certificado. Configure a CA do projeto em PG_CA_CERT_PATH.',
  UNABLE_TO_GET_ISSUER_CERT_LOCALLY:
    'CA do banco ausente. Configure o certificado do projeto em PG_CA_CERT_PATH.',
  CERT_HAS_EXPIRED: 'Certificado do banco expirado. Confira o certificado e o projeto no Supabase.',
  ERR_TLS_CERT_ALTNAME_INVALID:
    'O certificado não corresponde ao host. Copie o host oficial do Session pooler.',
  ENOENT:
    'Arquivo necessário à inicialização ausente. Confira PG_CA_CERT_PATH e os arquivos de migração no deploy.',
};

// Não copia message, detail, stack, input ou cause do driver: podem conter a
// connection string, dados de linhas ou credenciais. Somente códigos conhecidos.
export function databaseStartupError(error, stage) {
  let code = Object.hasOwn(hints, error?.code) ? error.code : 'POSTGRES_STARTUP_FAILED';
  let hint = hints[code] || 'Confira a conexão do Session pooler, o certificado e as permissões de migração.';
  if (stage === 'configuration' && code === 'POSTGRES_STARTUP_FAILED') {
    code = 'DATABASE_CONFIGURATION_INVALID';
    hint =
      'DATABASE_URL inválida. Use a conexão postgresql:// do Session pooler, sem marcadores ou aspas; codifique a senha para URL.';
  }
  const result = new Error(`[Database:${code}] Não foi possível inicializar PostgreSQL. ${hint}`);
  result.name = 'DatabaseStartupError';
  result.code = code;
  return result;
}

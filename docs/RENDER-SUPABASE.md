# API gratuita: Render + Supabase

Opção escolhida em 12/11/2026 para cadastro pessoal: **Render Free** hospeda a API Node; **Supabase Free** mantém PostgreSQL e fotos. O aplicativo continua Android Kotlin/Compose. O HTTPS usa o endereço `onrender.com`, sem comprar domínio.

**Estado atual:** API publicada pelo usuário em `https://loveall-api.onrender.com/api/`. O health check público respondeu HTTP 200 com `status: ok`; o endpoint de sessão exige autenticação (HTTP 401 sem token). O Android já usa essa URL por padrão em `gradle.properties`. Cadastro, login, envio de fotos, persistência após redeploy e demais fluxos com contas cloud ainda precisam ser verificados. A Oracle ficou como alternativa histórica, pois o cadastro brasileiro apresentado só oferecia Corporate.

## 1. Criar as duas contas

- [Criar conta Render](https://dashboard.render.com/register).
- [Criar conta Supabase](https://supabase.com/dashboard/sign-up).

Use seu e-mail pessoal. Se a interface exigir contratação, cartão ou upgrade, confira o plano antes de continuar: esta configuração prevê somente recursos Free. Não é necessário inventar nome de empresa nem CNPJ.

## 2. Preparar o Supabase Free

1. Crie um projeto **Free**, preferindo São Paulo se disponível. Guarde a senha do banco em gerenciador de senhas.
2. Em **Connect**, copie a conexão **Session pooler**, porta **5432**. Esse modo funciona com IPv4. A conexão direta pode exigir IPv6. Não use o transaction pooler neste procedimento.
3. Substitua o marcador da senha pela senha real codificada para URL; caracteres como `@`, `#`, `/` e `%` precisam de percent-encoding. Não envie a conexão no chat nem a salve no Git.
4. Em Storage, crie o bucket **`loveall-photos`**, com **Public desativado**, tipo permitido `image/jpeg` e limite de arquivo de 4 MB. Não crie políticas de acesso público ou para usuários anônimos.
5. Copie a **Project URL** e a chave de servidor **secret/service_role**. A chave `anon`/publishable não serve para essa integração. Guarde a chave somente no Environment do Render.
6. Mantenha o schema `loveall` fora dos schemas expostos da Data API. A API cria esse schema privado e suas tabelas automaticamente. O login continua sendo o da API existente, sem migrar para Supabase Auth.

O driver valida TLS e hostname. Se o projeto disponibilizar uma CA específica, cadastre o certificado como Secret File no Render e configure `PG_CA_CERT_PATH` com o caminho fornecido pelo painel. Não use `rejectUnauthorized=false` nem desative TLS para contornar erro de certificado.

## 3. Preparar o Render Free

O deploy por Blueprint precisa que a versão atual do projeto esteja em um repositório Git acessível ao Render. Nenhum commit, push ou autorização de repositório é feito automaticamente por este preparo.

1. Conecte o repositório e crie um **Blueprint**, apontando para o `render.yaml` da raiz.
2. Confira: serviço `loveall-api`, runtime Docker, plano **Free**, região Virginia. O banco pode ficar em outra região; haverá latência entre os serviços.
3. Preencha as variáveis solicitadas. O modelo sem credenciais está em `deploy/render/environment.env.example`.

| Variável | Conteúdo no painel |
| --- | --- |
| `DATABASE_URL` | Conexão Session pooler do Supabase com senha codificada |
| `SUPABASE_URL` | URL HTTPS do projeto |
| `SUPABASE_SERVICE_ROLE_KEY` | Chave secret/service_role, exclusivamente de servidor |
| `SUPABASE_PHOTO_BUCKET` | `loveall-photos` |
| `PHOTO_STORAGE` | `supabase` |
| `TRUST_PROXY_HOPS` | `1` para o proxy gerenciado do Render |
| `JWT_SECRET` | Gerado pelo Blueprint; preservar em novos deploys |
| `PLAY_BILLING_ENABLED` | `false` até configurar e homologar Google Play |

O Blueprint usa a porta dinâmica `PORT` do Render e o health check `/api/health`. Não adicione disco persistente: ele não faz parte do plano Free. A API recusa iniciar no ambiente Render sem PostgreSQL e armazenamento remoto, e também recusa bucket público.

O deploy automático está desativado. Revise e inicie o primeiro deploy pelo painel. O pacote opcional gerado por `scripts/New-RenderDeploymentPackage.ps1` serve para revisão/transporte do código; não substitui a conexão Git exigida pelo Blueprint.

## 4. Conferir antes de configurar o Android

- Abra `https://SEU-SERVICO.onrender.com/api/health`: deve responder `status: ok`.
- Cadastre uma conta de teste, entre, envie uma foto, salve o perfil e confirme que a foto é exibida.
- Reinicie/republique a API e confirme que conta, perfil e foto permanecem disponíveis.
- Teste duas contas: curtida mútua, match, mensagens, bloqueio e exclusão de conta. A foto de uma conta excluída deve deixar de ser servida imediatamente.
- Confira no Supabase que o bucket está privado e que o schema `loveall` não foi exposto pela Data API.

O Android está configurado com `loveall.apiBaseUrl=https://loveall-api.onrender.com/api/` em `gradle.properties`; veja `docs/ANDROID.md` para sobrescrever esse endereço. Nenhuma senha, connection string ou chave Supabase entra no APK. O cliente tolera até 90 s de leitura e 120 s totais para acomodar o despertar da instância, mas ainda pode apresentar indisponibilidade.

## 5. Dados locais, manutenção e backup

Sem `DATABASE_URL`, o desenvolvimento continua usando o SQLite e as fotos locais existentes. Esses arquivos **não são apagados nem enviados** ao Supabase. O primeiro projeto cloud começa vazio. Se precisar levar contas reais existentes, faça uma migração explícita, com backup, mapeamento e validação das fotos; copiar apenas o SQLite não publica os dados no PostgreSQL.

As consultas são assíncronas e usam parâmetros. Transações PostgreSQL mantêm a mesma conexão; as locais usam uma fila para impedir intercalamento de requisições durante `await`. Reservas de upload entram no banco antes da transferência. Fotos indisponíveis não podem ser anexadas; remoções que falham ficam em fila para nova tentativa.

Rascunhos abandonados são limpos após 24 h, na próxima execução da manutenção. Ela roda ao iniciar, ao enviar foto e de hora em hora enquanto a API está acordada. O host suspenso não executa timers; portanto, 24 h é um prazo mínimo de elegibilidade, não uma garantia de exclusão naquele instante.

O plano Free não fornece o mesmo backup gerenciado do plano Pro. Antes de receber usuários reais, defina e teste exportação separada do PostgreSQL **e dos arquivos Storage**, em destino privado fora dos provedores. Guarde também JWT_SECRET e, quando billing for ativado, a chave de criptografia dos tokens. Perder a chave de billing impede reconciliar os tokens gravados. Não use o backup SQLite da pasta Oracle para este banco PostgreSQL.

## 6. Limites e publicação comercial

- Render Free: suspende após 15 minutos sem tráfego; o despertar pode levar cerca de um minuto. O disco é efêmero e as horas mensais são compartilhadas pelo workspace. O próprio Render orienta não usar Free em aplicações de produção.
- Supabase Free: atualmente 500 MB de banco, 1 GB de arquivos e 5 GB de saída, além da cota separada de cache. Projetos podem ser pausados após uma semana de inatividade. Fotos são entregues via API, sem depender de cache público; monitore saída nos dois provedores.
- Não há garantia de disponibilidade ou escala comercial nessa combinação. Ela atende início e homologação gratuita; revise infraestrutura e backup antes de habilitar assinaturas com usuários reais.
- Anúncios e compras continuam dependendo dos registros, credenciais e homologação descritos em `docs/MONETIZACAO.md`. Assinaturas continuam desligadas por padrão.

Fontes oficiais verificadas em 12/11/2026: [Render Free](https://render.com/docs/free), [Blueprint](https://render.com/docs/blueprint-spec), [Supabase Free](https://supabase.com/pricing), [conexão PostgreSQL](https://supabase.com/docs/guides/database/connecting-to-postgres) e [criação de buckets](https://supabase.com/docs/guides/storage/buckets/creating-buckets).

## 7. Validação técnica

`cd server; npm ci; npm test` executa os contratos locais, o PostgreSQL embarcado PGlite pelo protocolo `pg`, transações, billing e o adaptador Storage com respostas simuladas. O workflow da API também inclui um job PostgreSQL 16 real em ambiente descartável. A execução remota desse workflow depende de enviar o código ao repositório.

Testes locais não substituem conferir Supabase, TLS do projeto real, proxy Render, suspensão/despertar e persistência após um deploy. Essas verificações ficam pendentes até criar os recursos.
## Diagnóstico de falha ao iniciar PostgreSQL

Se o log mostrar apenas `Não foi possível inicializar PostgreSQL; confira a conexão e o certificado.`, o deploy ainda usa o tratamento antigo, que ocultava a causa do driver. Envie as alterações locais ao repositório conectado ao Render e faça um deploy desse commit. As mensagens novas começam com `[Database:CODIGO]`, sem imprimir a URL ou a senha.

| Código | O que conferir |
| --- | --- |
| `DATABASE_CONFIGURATION_INVALID` | Connection string completa, sem aspas ou marcadores; senha codificada para URL |
| `28P01` / `28000` | Senha do banco e usuário completo `postgres.PROJECT_REF` do Session pooler |
| `ENETUNREACH` / `ENOTFOUND` | Host copiado do Session pooler, porta 5432, compatível com IPv4 |
| `ETIMEDOUT` / `ECONNREFUSED` | Projeto ativo, host, porta e restrições de rede |
| `SELF_SIGNED_CERT_IN_CHAIN` / `UNABLE_TO_VERIFY_LEAF_SIGNATURE` | CA do projeto como Secret File; `PG_CA_CERT_PATH` apontando para esse arquivo |
| `42501` | Usuário com permissão para inicializar as tabelas no schema privado `loveall` |
| `ENOENT` | Caminho do certificado e arquivos de migração presentes no deploy |

Para a CA, consulte Database Settings → SSL Configuration no Supabase. No Render, cadastre o certificado em Secret Files e use o caminho real fornecido pelo painel. Não desative a validação TLS para contornar a falha. A configuração correta depende do código registrado; a mensagem genérica antiga não comprova problema de certificado.

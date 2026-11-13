# Assinatura sem anúncios e AdMob

Implementação preparada em 12/11/2026 para o Android Kotlin/Compose. O produto escolhido é **assinatura para remover anúncios**. Chamadas e checkout externo continuam indisponíveis. O código não cria contas, produtos, credenciais ou anúncios nas plataformas.

**Configuração atual:** anúncios habilitados em `gradle.properties`, com os IDs AdMob fornecidos pelo proprietário. Debug usa banner de teste; release usa a unidade real. A assinatura Play permanece desligada. O código compila e possui testes locais, mas mensagens de privacidade, verificação app-ads.txt, aprovação AdMob e homologação real ainda não foram confirmadas. Não foram realizadas compras reais.

## O que foi implementado

- Play Billing Library 9.1.0: conexão/reconexão com a loja, consulta de planos-base recorrentes, preços/moeda/período da Play, tela nativa de compra, compras pendentes, restauração e tratamento de item já adquirido. Operações têm limite de tempo.
- Tela **Perfil → Sem anúncios**, com renovação automática informada antes da compra, restauração e link para gerenciar assinaturas na Google Play. Não há preço fixo ou produto comercial inventado no app.
- Validação no servidor pela Google Play Developer API, vinculando a compra à conta LoveAll pelo identificador ofuscado. O cliente não decide conceder o benefício pelo resultado local do SDK.
- Tokens cifrados com AES-256-GCM e identificados por SHA-256 no banco. Registro idempotente, confirmação da compra no servidor, expiração, graça, suspensão, cancelamento, substituição de token e revogação pela consulta ao estado atual na Google.
- Notificações RTDN com autenticação OIDC, verificação de audiência e email da conta de serviço. Eventos repetidos ou antigos não são tomados como estado financeiro: o servidor consulta novamente a Google. Uma compra concluída com o Android fechado pode ser localizada pelo vínculo ofuscado criado antes da compra.
- Reconciliação a cada 15 minutos enquanto o servidor está acordado e atualização no app ao voltar ao primeiro plano, enquanto estiver aberto, a cada cinco minutos, e depois de compra/restauração. Direitos sem verificação recente deixam de ser considerados confirmados pelo servidor.
- GMA Next-Gen 1.6.0 e UMP 4.0.0: banner na descoberta, inicialização depois da autorização do UMP, preferências de privacidade quando exigidas, destruição da view ao sair da tela ou ao ativar a assinatura. Nenhum banner em conversas ou telas de autenticação.
- Sem requisição de anúncio quando o direito da conta é desconhecido, há assinatura ativa ou uma compra está pendente. Falhas de consentimento não liberam novas requisições. Nome, email, gênero, preferências e mensagens não são enviados como parâmetros de segmentação.
- Textos de privacidade e exclusão atualizados. **Excluir a conta LoveAll não cancela a assinatura na Google Play**; esse aviso aparece antes da confirmação de exclusão. Os registros locais da compra e do vínculo são removidos com a conta.

O lançamento atual oferece somente planos-base auto-renováveis com uma fase de preço recorrente. Ofertas promocionais, testes grátis, planos pré-pagos, pacotes de minutos e troca de plano por uma assinatura já ativa não são apresentados pela UI. Isso evita escolher automaticamente uma condição comercial diferente daquela exibida.

## 1. Registros que você fará na Google Play

1. Criar/configurar a conta de desenvolvedor e o perfil de pagamentos.
2. Cadastrar o app com o pacote final **`com.loveall.app`** e enviar uma versão assinada ao teste interno.
3. Criar a assinatura sem anúncios, seus planos-base recorrentes e preços/países. Escolher os identificadores; depois informá-los em `PLAY_AD_FREE_PRODUCT_IDS`. É possível cadastrar produtos mensais e anuais. O exemplo de produto usado nos testes não é uma configuração comercial.
4. Ativar a Google Play Developer API em um projeto Google Cloud e preparar a conta de serviço do servidor com acesso ao aplicativo e as permissões necessárias para verificar e confirmar compras. Guardar o JSON somente no servidor, como arquivo secreto protegido.
5. Configurar RTDN no Play Console e um tópico Pub/Sub. Dar permissão de publicação ao principal indicado pela Play.
6. Criar uma assinatura Pub/Sub de **push autenticado**, apontando para `https://HOST_REAL/api/billing/notifications`. Configurar a conta de serviço do push, permissões de emissão de tokens e a audiência esperada. O endpoint não aceita uma notificação apenas porque ela possui o formato do Pub/Sub.

Documentação: [preparação do Billing](https://developer.android.com/google/play/billing/getting-ready), [integração com backend](https://developer.android.com/google/play/billing/backend) e [autenticação do push](https://docs.cloud.google.com/pubsub/docs/authenticate-push-subscriptions).

## 2. Configuração da API no Render

A hospedagem escolhida agora é [Render + Supabase](RENDER-SUPABASE.md). A assinatura permanece desligada no Blueprint. Antes de habilitá-la, valide persistência e disponibilidade da API, backup externo e os limites do plano gratuito; o Render desaconselha Free em produção comercial.

Cadastre o JSON da conta de serviço como **Secret File** `play-service-account.json` no painel Render. Configure `GOOGLE_APPLICATION_CREDENTIALS=/etc/secrets/play-service-account.json`, conferindo o caminho fornecido pelo painel. Nunca inclua a chave no APK, no Git, no chat ou no pacote de implantação.

Cadastre as variáveis abaixo em **Environment** do serviço:

| Variável | Valor |
| --- | --- |
| `PLAY_BILLING_ENABLED` | `true` somente após completar a configuração |
| `PLAY_PACKAGE_NAME` | `com.loveall.app`, igual ao pacote na Play |
| `PLAY_AD_FREE_PRODUCT_IDS` | IDs das assinaturas criadas, separados por vírgula |
| `PLAY_TOKEN_ENCRYPTION_KEY` | Base64 de 32 bytes aleatórios, gerados uma vez com `openssl rand -base64 32` |
| `PLAY_RTDN_AUDIENCE` | Audiência HTTPS configurada no push; pode ser a URL completa do endpoint |
| `PLAY_RTDN_SERVICE_ACCOUNT_EMAIL` | Email exato da conta de serviço escolhida para autenticar o push |

No Render, use o caminho do Secret File. A alternativa histórica de VM/Oracle usa o Compose, que monta `deploy/oracle/.secrets` somente para leitura em `/run/secrets`; esse caminho não se aplica ao Render.

Não trocar `PLAY_TOKEN_ENCRYPTION_KEY` a cada deploy: os tokens existentes dependem dela. Guardar a chave e credenciais com proteção e backup fora do serviço, separados das cópias do banco e do Storage. Uma rotação exige um procedimento de migração. Preserve os valores existentes ao reimplantar o serviço.

Após configurar, siga o procedimento de [Render + Supabase](RENDER-SUPABASE.md). Configuração incompleta com Billing explicitamente habilitado faz a API recusar a inicialização; credenciais sem acesso correto à Play ainda precisam ser identificadas na homologação de uma compra de teste.

### Contratos HTTP

| Rota | Autenticação e resultado |
| --- | --- |
| `GET /api/billing/config` | Sessão LoveAll; disponibilidade, IDs permitidos e identificador ofuscado da conta |
| `GET /api/billing/entitlements` | Sessão LoveAll; `ad_free` e `expires_at` confirmados pelo servidor |
| `POST /api/billing/verify` | Sessão LoveAll e aceite das regras; recebe `product_id` e `purchase_token`, consulta a Google e confirma a compra |
| `POST /api/billing/notifications` | Token OIDC do Pub/Sub; consulta o estado atual da compra; retorna erro para permitir nova entrega quando não consegue processar |

As antigas rotas financeiras em `/api/calls` permanecem bloqueadas; a implementação nova não reativa o webhook Mercado Pago nem os planos de chamadas.

## 3. Registros que você fará no AdMob

1. Cadastrar o aplicativo Android e obter o **App ID**.
2. Criar uma unidade de **banner** e obter o **Ad Unit ID**.
3. Configurar Privacy & messaging, mensagens e regiões aplicáveis no AdMob para o UMP.
4. Concluir a associação/verificação do aplicativo e os requisitos de prontidão da sua conta. Configurar `app-ads.txt` e o domínio de desenvolvedor conforme o fluxo do AdMob, quando aplicável.
5. Atualizar política de privacidade publicada e a declaração de Segurança dos dados da Play para corresponder aos SDKs ativados e às suas configurações. O manifesto final incorpora permissões técnicas dos SDKs; não reutilizar a antiga declaração de app sem anúncios sem revisá-la.

Documentação: [GMA Next-Gen](https://developers.google.com/admob/android/next-gen/quick-start), [banner](https://developers.google.com/admob/android/next-gen/banner) e [UMP](https://developers.google.com/admob/android/next-gen/privacy).

## 4. Habilitar o build Android

As configurações ficam em propriedades Gradle. Os identificadores AdMob são públicos, mas não precisam ficar espalhados no código:

```properties
loveall.adsEnabled=true
loveall.playBillingEnabled=false
loveall.admobAppId=APP_ID_REAL_DO_ADMOB
loveall.admobBannerId=AD_UNIT_ID_REAL_DO_BANNER
loveall.apiBaseUrl=https://loveall-api.onrender.com/api/
```

O exemplo acima habilita somente anúncios. A assinatura continua desligada até concluir os registros Play e a homologação; só então habilitar `loveall.playBillingEnabled=true`. Usar propriedades locais ou do ambiente de build. Não colocar o JSON da conta de serviço nem a chave de cifragem nessas propriedades Android.

Em **debug**, o banner sempre usa a unidade de teste oficial da Google, mesmo que haja uma unidade real configurada. O App ID real pode ser usado para validar suas mensagens UMP; sem ele, há apenas o App ID de demonstração e não se pode comprovar suas mensagens de consentimento.

Em **release**, anúncios habilitados exigem IDs com formato válido e rejeitam o publicador de demonstração. Billing habilitado exige anúncios habilitados para as contas gratuitas, pois o produto vendido é removê-los. Essa verificação local não comprova que os IDs pertencem à sua conta ou que a conta foi aprovada.

Uma build de teste interna da Play deve usar o pacote e a assinatura adequados. O debug tem sufixo `.native.debug` e não substitui a homologação do produto registrado em `com.loveall.app`.

## 5. Homologação obrigatória depois dos registros

- Configurar testadores de licença e usar instrumentos de pagamento de teste da Google Play.
- Verificar compra, cancelamento da tela, pagamento pendente e conclusão posterior com app aberto e fechado, confirmação, restauração após reinstalação e acesso pelo mesmo usuário LoveAll.
- Verificar que outra conta LoveAll não recebe a assinatura ao reutilizar um token.
- Validar cancelamento de renovação com acesso até o vencimento, graça, suspensão, expiração e reembolso/revogação, observando os eventos na API.
- Validar notificações autenticadas reais, reentrega, falha temporária da Google/API e reconciliação. Monitorar falhas de confirmação e de entrega do Pub/Sub; configurar retenção/reentrega e tratamento operacional conforme o projeto.
- Validar consentimento e mudança de preferências, anúncios de teste, ausência de anúncio para assinante e ausência de novas requisições com direito desconhecido.
- Testar em aparelho real, release minificado e Google Play instalada. Usar unidades/dispositivos de teste; não clicar em anúncios reais para testar.
- Exercitar exclusão de conta e compreender que cancelar a renovação é uma operação separada na Play.

Os testes automatizados usam respostas Google simuladas e interfaces de loja falsas. Eles não acessam suas contas nem demonstram que permissões, catálogo, impostos, preços, repasses, anúncios ou notificações reais estão aprovados. A publicação permanece bloqueada até essa homologação.

## 6. Resultados locais

A rodada final passou com **142 testes JVM, 26 instrumentados Android e 28 testes da API**. Build de bundle release com R8 e lint passaram, sem erros de lint. A auditoria npm de produção apontou zero vulnerabilidades. A tentativa de habilitar anúncios no release sem IDs reais foi recusada conforme esperado.

Os resultados e os limites estão em [qa/monetization/results.json](../qa/monetization/results.json). A [captura da tela](../qa/monetization/ad-free-offer.png) usa uma oferta e um preço fictícios de teste, sem configurar uma venda real.

O bundle de verificação continua sem assinatura e usa uma URL `.invalid` apenas para compilar. Monetização está desligada nesse artefato. Ele não deve ser publicado. O debug foi recompilado para a API local depois da verificação de release.


## 7. Configuração AdMob em andamento

A API pública já está em `https://loveall-api.onrender.com/api/`. A integração Android de banner e UMP está preparada. Os IDs da conta AdMob foram fornecidos pelo proprietário e configurados no projeto; compilar com esses IDs não comprova aprovação da conta ou exibição de anúncios reais.

### Cadastrar o app e o banner

1. Em [AdMob](https://admob.google.com/), abra Aplicativos → Adicionar aplicativo → Android. Se ainda não está na Google Play, informe que não está publicado. A API estar online não significa que o aplicativo esteja publicado na loja.
2. Cadastre o nome LoveAll. O pacote de distribuição é `com.loveall.app`; o debug local tem sufixo `.native.debug`.
3. No app LoveAll, abra Unidades/Blocos de anúncios → Adicionar unidade → **Banner**. Nome sugerido: **Banner Descoberta**. Esse é o formato implementado no app.
4. Copie o **App ID** (`ca-app-pub-...~...`) e o **ID da unidade** (`ca-app-pub-.../...`). São identificadores públicos. Não é necessário fornecer login, senha ou credenciais de pagamento ao código.
5. Configure Privacidade e mensagens no AdMob e publique as mensagens aplicáveis às regiões de distribuição. A URL da política de privacidade publicada ainda precisa ser definida para esse cadastro.

Os anúncios no debug sempre usam a unidade de teste da Google. Com seu App ID real, será possível testar também a configuração UMP da sua conta. Não clicar em anúncios reais para testar.

### Publicar app-ads.txt

A API agora prepara a rota pública **`/app-ads.txt`** na raiz do host, fora de `/api/`. Para ativá-la após enviar o código ao repositório e fazer deploy, adicione no Render:

```text
ADMOB_PUBLISHER_ID=pub-SEUS_16_DIGITOS
```

O trecho acima é somente formato, não um valor válido. Copie o Publisher ID real da conta AdMob. O identificador contém os mesmos 16 dígitos de publicador presentes no App ID; não inclui o sufixo do app ou do banner. Confira a linha personalizada fornecida pelo AdMob.

Com a configuração real, `https://loveall-api.onrender.com/app-ads.txt` deve responder HTTP 200 e texto simples com a linha `google.com, pub-<id real>, DIRECT, f08c47fec0942fa0`. Sem configuração, responde 404 e não declara um vendedor fictício. Nome, conteúdo ou retorno HTML de uma página de erro não substituem esse arquivo.

Cadastre o site de desenvolvedor na ficha da Google Play correspondente ao host que efetivamente hospedar o arquivo; não cadastre a URL da API `/api/` como se fosse a política de privacidade. A verificação depende de vincular o app à loja, o Google rastrear o arquivo e aprovar a prontidão do aplicativo. Não está concluída apenas porque a rota existe no código.

O Render gratuito suspende a API por inatividade e pode atrasar o acesso do rastreador. Para hospedar os arquivos de suporte em serviço estático, Firebase Hosting é uma alternativa indicada pelo Google; exige configuração própria. Ainda não foi criado um site de desenvolvedor separado nem publicada uma política de privacidade por estes ajustes.

Fontes: [cadastro do app](https://support.google.com/admob/answer/9989980), [verificação](https://support.google.com/admob/answer/14538460), [app-ads.txt](https://support.google.com/admob/answer/9363762) e [formato Google](https://developers.google.com/admob/android/next-gen/app-ads).


## 8. Identificadores configurados do LoveAll

| Configuração | Valor público |
| --- | --- |
| App ID | `ca-app-pub-7902597376494373~8119920674` |
| Banner Descoberta | `ca-app-pub-7902597376494373/6446766419` |
| Publisher ID | `pub-7902597376494373` |
| API Android | `https://loveall-api.onrender.com/api/` |

`gradle.properties` habilita anúncios e registra os dois IDs; mantém `loveall.playBillingEnabled=false`. O App ID real é usado no manifesto e na inicialização GMA/UMP. A unidade de banner do debug continua sendo a demonstração oficial da Google. O release usa o banner acima e passa pelas validações de IDs reais do build. Esses valores são públicos e não são credenciais para entrar na conta.

O arquivo exato para publicação está em [deploy/admob/app-ads.txt](../deploy/admob/app-ads.txt). O `render.yaml` e o exemplo de Environment já incluem `ADMOB_PUBLISHER_ID=pub-7902597376494373`. No serviço Render existente, cadastre essa variável em Environment se o Blueprint não a sincronizar; publique também o código da rota `/app-ads.txt`. Ao verificar nesta etapa, a URL pública ainda retornou HTTP 404: não foi confirmado um deploy dessa rota/configuração.

Linha a ser exibida na URL pública:

```text
google.com, pub-7902597376494373, DIRECT, f08c47fec0942fa0
```

Antes de liberar anúncios reais, configurar e conferir Privacidade e mensagens no AdMob, publicar a política de privacidade em URL acessível, vincular o app à ficha da Play com o site de desenvolvedor correto e concluir verificação/prontidão no AdMob. Esses passos de conta não são feitos pela compilação e continuam pendentes.

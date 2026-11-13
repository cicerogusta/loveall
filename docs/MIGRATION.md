# Reescrita do cliente Android

Inspeção e referências: 12/11/2026. O aplicativo ativo foi reescrito em Kotlin/Compose e consolidado no módulo `app/`, com projeto Gradle na raiz. Por decisão explícita do produto, a API Node/Express em `server/` foi mantida. O cliente React/Capacitor foi arquivado em `legacy/web-client/`, sem participação no build Android ou na imagem Docker atual. Os scripts npm, instruções e ferramentas de web/iOS anteriores estão em `legacy/`.

## Inventário e correspondência

| Fluxo anterior | Implementação nativa | Contrato preservado |
| --- | --- | --- |
| Welcome | `WelcomeScreen` | Identidade LoveAll, inclusão e acesso às contas |
| Login / AuthContext | `LoginScreen`, `SessionViewModel`, `DefaultAuthRepository` | `POST auth/login`, `GET auth/me`, JWT Bearer |
| Register | `RegisterScreen`, `Registration` | `POST auth/register`; maioridade, identidades e preferências |
| Discover | `ExploreScreen`, `ExploreViewModel` | `GET profiles/discover`, `POST likes/like`, `POST likes/dislike` |
| Curtidas recebidas | `ExploreScreen` em modo curtidas | `GET likes/who-liked-me` |
| Matches | `MatchesScreen`, `MatchesViewModel` | `GET matches`; último texto e não lidas |
| Chat | `ChatScreen`, `ChatViewModel` | GET paginado/POST `messages/:matchId`; não mostra envio fictício |
| Perfil / EditProfile | `ProfileScreen`, `EditProfileScreen`, `ProfileViewModel` | `GET auth/me`, `PUT profiles/me`, `POST profiles/me/photos`; seletor de fotos Android, identidade, bio, cidade, preferências |
| Perfil público | `DetailScreen`, `DetailViewModel` | `GET profiles/:userId`, bloqueio e denúncia |
| Safety | `SafetyScreen`, `SafetyViewModel` | Dicas, bloqueados, desbloqueio, todos os motivos de denúncia |
| Plans | `PlansScreen`, `PlansViewModel` | Consulta do saldo existente; novas compras e ofertas indisponíveis |
| PaymentSuccess / PaymentFailed | Estado nativo de pagamento em Planos | Infraestrutura de estado preservada no Android; rotas financeiras indisponíveis nesta versão |
| Chamadas | Ações nativas no chat | `POST calls/check`; a autorização nunca é apresentada como chamada conectada |
| Banners internos | Cards nativos de promoção de planos | Sem instalação de SDK ou IDs fictícios |

Não há Activities/Fragments/layouts XML legados para portar: a origem é uma SPA React. A nova Activity é exclusivamente Compose. Os XML novos são recursos Android, manifesto, tema, ícone e configuração de rede.

## Comportamentos importantes

- Todas as chamadas autenticadas enviam o token no header. Uma resposta 401 das APIs privadas invalida a navegação autenticada. Falhas temporárias na restauração permitem tentar novamente sem apagar o token.
- Credenciais não entram em logs, BuildConfig nem estado salvo de senha. Somente o token cifrado é persistido; backup do app está desabilitado. A chave fica no Android Keystore. Logout remove o ciphertext local; o servidor atual não possui revogação remota de JWT.
- Cadastro preserva identificadores usados pelo cliente implantado (`non_binary`, `transgender_woman`, etc.). A constante `VALID_GENDERS` do servidor contém outros nomes, mas não é utilizada para validar a rota atual. Unificar esses identificadores exige migração de dados/contrato e não foi feito silenciosamente.
- Planos, preços e saldo são carregados da API. O app não transforma `is_premium` ou preferências locais em autorização de chamadas.
- O código Android de acompanhamento de pagamentos permanece separado, mas o checkout está desabilitado em todos os builds e as rotas financeiras retornam indisponibilidade. Nenhuma WebView é empacotada.
- Mensagens usam HTTP e atualização periódica apenas com a tela em STARTED; o cliente web atual também persiste via HTTP. Não há notificações push ou recebimento em background implementados. Paginação preserva mensagens anteriores e deduplica por ID.
- Fotos são escolhidas pelo seletor Android (`PickVisualMedia`, com fallback do AndroidX), sem permissão de acesso à galeria inteira. O cliente corrige a orientação, reduz a maior dimensão a 1600 px e reencoda em JPEG sem EXIF. A API recebe até 4 MB por foto em `POST profiles/me/photos`, salva em `data/photos/` (ou `PHOTO_UPLOAD_DIR`) e retorna uma referência relativa `/api/photos/<uuid>.jpg`; `PUT profiles/me` associa as fotos ao perfil ao salvar. Preserve esse diretório no volume de dados do servidor. URLs HTTPS antigas continuam compatíveis; novas fotos não exigem digitar links. Arquivos enviados e ainda não associados a um perfil não têm limpeza automática. Idade/distância são salvas como no app atual; a consulta `discover` do servidor não aplica esses filtros atualmente.
- Em falha de curtir/passar, o perfil não é descartado. Em falha de envio, o rascunho fica disponível para nova tentativa. Operações repetidas são bloqueadas durante requisições.
- Componentes Material têm alvos de toque apropriados; grupos de gênero/denúncia unem controle e rótulo para TalkBack. Layouts rolam, respeitam teclado/insets e limitam largura em tablets. Validação manual com TalkBack, fontes grandes, dobráveis e redes reais ainda deve preceder publicação.

## Situação após a revisão de segurança

Consulte o [relatório atualizado](REVISAO-PUBLICACAO.md) para os ajustes e pendências. O servidor passou a exigir segredo JWT, validar dados e propriedade das fotos, remover dados na exclusão e exigir aceite das regras. Eventos Socket.IO e o fluxo inseguro de pagamentos foram retirados. Chamadas e novas compras estão indisponíveis no Android e na API; o saldo existente continua consultável.

Ainda faltam infraestrutura HTTPS, assinatura, configuração da loja, páginas públicas, identificação e contatos do responsável e operação de moderação. Distribuição fica para a etapa final. Áudio/vídeo e planos de chamadas continuam indisponíveis. A [assinatura sem anúncios e o AdMob](MONETIZACAO.md) agora possuem código nativo e validação no servidor, desligados até configuração e homologação reais.

## Design e fontes

A inspeção visual do produto de origem preservou marca, rosa/roxo, proposta inclusiva, navegação para descoberta/matches/perfil e destaque de planos. A reescrita usa Material 3 com tema claro/escuro, contraste de papéis semânticos, cards e hierarquia tipográfica. Retirou a espera artificial de splash e a promessa absoluta “100% seguro”.

Referências externas consultadas: [opções de identidade do Bumble](https://bumble.com/en/help/gender-identity-options) e [segurança e denúncias do Tinder](https://www.help.tinder.com/hc/en-us/categories/360006058312-Safety-Reporting). Essas páginas documentam padrões de inclusão e controles de segurança; não houve teste comparativo dos apps instalados nem análise comprovada de reclamações. Não foram copiados marca, layout ou assets dessas empresas.

Referências técnicas primárias:

- [Recomendações de arquitetura Android](https://developer.android.com/topic/architecture/recommendations).
- [Material 3 em Compose](https://developer.android.com/develop/ui/compose/designsystems/material3).
- [Kotlin: releases e compatibilidade](https://kotlinlang.org/docs/releases.html) e [AGP 8.13 / compatibilidade](https://developer.android.com/build/releases/agp-8-13-0-release-notes).
- [Android Keystore](https://developer.android.com/privacy-and-security/keystore).
- [Target API Google Play](https://developer.android.com/google/play/requirements/target-sdk).
- [Política de pagamentos Google Play](https://support.google.com/googleplay/android-developer/answer/9858738).
- [JaCoCo e verificação de cobertura](https://docs.gradle.org/current/userguide/jacoco_plugin.html).

## Critério para liberação do aplicativo nativo

Exigir testes com backend isolado e depois homologação autenticada: cadastro adulto e recusa de menor, restauração/logout/401/offline, curtidas e match, paginação/envio/leitura de mensagens, bloqueio/desbloqueio/denúncia, persistência do perfil e preferências, plano existente, compra/restauração/expiração/reembolso no canal autorizado. Áudio/vídeo e publicidade não podem ser marcados como prontos sem integrações reais. Nenhum build por si só atende esses critérios.

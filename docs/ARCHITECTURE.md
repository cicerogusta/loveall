# Arquitetura do Android

O app usa MVVM, estado imutável e injeção por construtor. A organização segue as [recomendações de arquitetura Android](https://developer.android.com/topic/architecture/recommendations), com casos de uso quando há uma operação que combina responsabilidades. A API Node.js continua sendo o backend.

## Responsabilidades

- `domain/model`: entidades e valores do app, sem DTOs, Compose ou Android SDK.
- `domain/repository`: contratos consumidos pelos ViewModels e casos de uso; inclui `PhotoReader` como abstração da preparação de imagens.
- `domain/error`, `domain/validation`: falhas e validações independentes da plataforma.
- `domain/usecase`: `AddProfilePhoto` valida o limite e coordena leitura/upload; `MessageTimeline` reúne mensagens sem duplicar IDs e mantém ordem cronológica e confirmação de leitura.
- `data/remote/api`: interfaces Retrofit por funcionalidade. Cada repositório recebe apenas a interface necessária.
- `data/remote/dto`: um DTO por arquivo, mantendo os nomes JSON da API.
- `data/repository`: implementações dos contratos; a conversão de erros HTTP fica em `apiCall`.
- `data/local` e `ProfilePhotoReader`: armazenamento seguro e leitura Android de imagens. Dispatchers são fornecidos pelo ponto de composição.
- `presentation/state`: estados de sessão e tela, separados dos arquivos de ViewModel.
- Cada ViewModel herda diretamente de `androidx.lifecycle.ViewModel` e possui seu próprio `MutableStateFlow`. `ProfileState`, `ChatState` e os demais estados contêm diretamente os dados da tela, sem envelope genérico. Flags como `sending`, `refreshing`, `saving` e `uploadingPhoto` representam operações concretas; `busy`, quando necessário pela UI, é calculado a partir delas.
- `presentation`: ViewModels recebem contratos/casos de uso e expõem StateFlow somente para leitura. Não recebem Activity, ContentResolver, Retrofit, composables ou callbacks para alterar a View.
- `ui/screens`: uma tela por arquivo; recebe estado e callbacks de ações. Estado estritamente visual, como o texto ainda não enviado, permanece na UI.
- `ui/navigation`: rotas e descritores da navegação.
- A navegação de membros registra destinos por funcionalidade e delega a barra inferior a `MemberBottomBar`. O chat é montado por cabeçalho/menu, lista/bolhas, compositor e diálogos; o rascunho fica no compositor e a atualização por ciclo de vida fica em um efeito dedicado. O editor de perfil reúne seções independentes de dados, fotos, identidade, preferências e interesses. `ProfilePhotoReader.read` coordena métodos de leitura limitada, decodificação amostrada, orientação/redimensionamento e codificação JPEG, mantendo o descarte de bitmaps em `finally`.
- Planos delega saldo, ofertas, pagamento e abertura do checkout a componentes específicos. Descoberta separa cabeçalho, conteúdo, ações com resposta tátil e diálogo de match. O cartão de perfil é um componente em `ui/components/discovery`, composto por galeria, fundo, identificação, resumo e detalhes; o mesmo cartão atende descoberta e perfil detalhado.
- `AppContainer`: ponto de composição das implementações. Expõe repositórios pelos contratos e o fluxo de expiração somente para leitura.

## Comportamentos protegidos

O envio de mensagens publica a confirmação no estado; a UI limpa apenas o rascunho correspondente. A atualização periódica não ocupa o indicador de uma ação do usuário e não descarta mensagens enviadas durante a consulta. Timestamps ISO e SQLite são comparados como instantes UTC.

A expiração de sessão cancela autenticações pendentes. Resultados atrasados de uma operação cancelada não restauram a sessão. Exceções inesperadas viram erro recuperável na apresentação; cancelamento de coroutine continua sendo propagado.

Referências relativas das fotos usam a URL da API fornecida ao app, inclusive em testes, em vez de ignorar a configuração injetada. O seletor permanece nativo e o processamento de imagens usa AndroidX ExifInterface.

## Verificação

```powershell
.\gradlew.bat :app:assembleDebug :app:verifyBusinessCoverage :app:lintDebug :app:assembleDebugAndroidTest
```

`ArchitectureBoundaryTest` verifica imports proibidos entre camadas, impede estados/modelos adicionais nos arquivos de ViewModel e exige herança direta do ViewModel AndroidX. Esses testes protegem dependências estáticas; a avaliação de responsabilidades continua fazendo parte da revisão. Testes de regressão cobrem expiração durante login, erros inesperados, upload de fotos, consulta de chat simultânea ao envio, nova tentativa após falha inicial e ordenação/deduplicação de mensagens.

Os testes instrumentados de sessão guardam e restauram o token de desenvolvimento que existia antes da execução, sem apagar a conta local do usuário. A cobertura de negócio mantém o limite existente de 80% para `Rules`, `Registration` e `ProfileEdit`; ela não representa cobertura de todo o app.

O código Kotlin foi padronizado com ktfmt 0.64, estilo Kotlin de quatro espaços. Não foram adicionadas camadas de delegação para cada chamada simples de repositório.

## Strings e convenções de teste

Textos apresentados às pessoas ficam nos recursos Android (`strings.xml` e plurais). Valores técnicos ficam com seu responsável: `ApiPaths`, `ApiFields`, `ApiParameters`, `ApiHeaders` e `PhotoUploadContract` definem o contrato HTTP; `RouteArguments` e `Routes` definem navegação; `BrazilianLocale` define idioma/moeda; constantes locais nomeiam padrões, identificadores de animação e mensagens internas. `TextDefaults.EMPTY` representa texto vazio. `CallType` e `PaymentState` substituem strings livres nas decisões de chamada e pagamento; um status novo da API é mapeado para `UNKNOWN`, sem encerrar a transação indevidamente.

Os testes de ViewModel são separados por funcionalidade, usam fakes em arquivos próprios e `MainDispatcherRule` para restaurar `Dispatchers.Main` mesmo após falha. Cenários de validação e pagamento são parametrizados com nomes individuais; os casos usam Arrange/Act/Assert e `runTest`, sem esperas reais. Dados recorrentes são organizados por contexto em fixtures. Respostas HTTP ficam em JSON em `src/test/resources/api` e `src/androidTest/assets/api`; as expectativas de protocolo são independentes das constantes de produção, para detectar alterações acidentais do contrato. A simulação HTTP dos testes de navegação fica em `NativeApiDispatcher`, separada das interações de UI.

`StringDeclarationTest` faz uma verificação leve dos fontes Kotlin de produção e aponta literais fora de declarações de constantes e entradas de enum. O teste complementa o lint e a revisão dos recursos; não é um parser completo de Kotlin nem uma exigência de traduzir chaves técnicas da API.


## Revisão de release — 20/11/2026

`Rules` centraliza validações de senha (inclusive limite UTF-8 do bcrypt), identidade e preferências, usadas por `Registration` e `ProfileEdit`. A API aplica os mesmos limites no cadastro e na edição, e fixtures cobrem fronteiras e duplicatas.

`ChatViewModel.moderate` e `ExploreViewModel.removeProfile` concentram somente o ciclo de estado comum às ações daquela tela; as operações continuam injetadas por contratos. Não há superclasse de ViewModel nem serviço genérico alterando StateFlow alheio. Moderação só fecha a conversa após resposta confirmada; falha permite nova tentativa.

`KeystoreSessionStore.access` serializa as operações no armazenamento seguro com Mutex e dispatcher IO. Formulários, feedback e seletores/rótulos de identidade foram separados em arquivos específicos, preservando callbacks e recursos de texto.

As páginas públicas da API separam conteúdo/renderização (`public/pages.js`) das rotas e validações HTTP (`routes/public-pages.js`). O formulário de exclusão reutiliza o serviço transacional existente em vez de duplicar o apagamento de dependências.

A assinatura de upload é responsabilidade do build e scripts locais, sem credenciais no código Android ou no AppContainer. Consulte [a revisão de publicação](REVISAO-PUBLICACAO.md) para etapas externas, URLs e limites de validação.


## Comunidade PcD e Libras — 21/11/2026

`PcdCommunityRepository` é um contrato independente dos perfis públicos. A implementação API adapta endpoints específicos; `PcdCommunityViewModel` mantém participação e fila em `PcdCommunityState`, sem herdar de outro ViewModel. O consentimento fica inicialmente desmarcado e a API exige confirmação própria. Ao retornar à tela, o status é consultado novamente. Não há diagnóstico, laudo, inferência automática ou cadastro compulsório.

O servidor persiste participação, data e versão do consentimento. As consultas de perfis públicos usam listas explícitas de colunas e não incluem esses campos. A curtida PcD verifica ambos os participantes dentro da transação que bloqueia o par em ordem estável. Uma saída concorrente não pode contornar essa verificação. Os matches usam as tabelas e regras comuns. Sair remove a declaração e o consentimento da base ativa; não desfaz conexões anteriores nem retira o perfil da descoberta geral.

A migração PostgreSQL `002-pcd-community.sql` é aditiva e aplicada por uma lista ordenada, sob o bloqueio de `schema_migrations`. Contas existentes começam fora da comunidade. SQLite usa adições equivalentes. Testes verificam atualização de uma base versão 1, repetição sem perda de dados, consentimento, revogação e interação bloqueada.

`LibrasLauncher` isola clipboard e abertura do aplicativo oficial VLibras. A tradução é externa: copiar, abrir e colar são ações explícitas. Não existe envio automático de conversas, WebView, widget JavaScript ou avatar embutido no LoveAll. O texto não vai ao servidor do LoveAll; o clipboard é marcado como sensível no Android 13+. O fallback abre a ficha oficial na Google Play.

`GuideVideo` usa VideoView e controles Android, pausa ao sair do primeiro plano e libera reprodução ao remover o componente. O catálogo em `res/values/libras_videos.xml` só deve receber MP4 HTTPS autorizados, com Libras revisada e legendas em português. Os três endereços estão vazios porque os vídeos ainda não foram produzidos: a interface informa essa pendência. Playback real e qualidade linguística precisam de homologação. Referência: [VLibras oficial](https://www.gov.br/governodigital/pt-br/acessibilidade-e-usuario/vlibras).


## Tutoriais locais em português

`TutorialGuide` define os três assuntos e seus recursos MP4/transcrição. `TutorialGuides` permite selecionar um vídeo por vez e abrir o texto completo, sem serviço remoto ou repositório desnecessário para conteúdo estático. `GuideVideoSource` distingue explicitamente recursos incluídos no APK de endereços HTTPS aprovados. O player é recriado ao trocar a origem, evitando que o VideoView continue reproduzindo o assunto anterior.

Os tutoriais em português ficam separados do catálogo de Libras. A reprodução local não precisa de internet; a autenticação e os fluxos de relacionamentos mantêm a dependência da API. `GuideVideo` libera o player ao sair da composição e pausa no ON_PAUSE da Activity. Instruções de produção, formato e testes: [guia dos vídeos](../deploy/video-guides/README.md).

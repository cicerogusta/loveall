# Revisão para publicação na Google Play — 20/11/2026

**Complemento de 21/11/2026 — vídeos em português:** três MP4 originais foram incluídos em recursos Android, com reprodução local e transcrição acessível. Não têm áudio nem Libras, conforme a escolha do responsável. Os vídeos em Libras continuam pendentes; não anunciar os tutoriais em português como conteúdo sinalizado. Produção e evidências: `deploy/video-guides/README.md` e `qa/video-guides`.

**Complemento de 21/11/2026 — PcD e Libras:** o código agora inclui comunidade PcD voluntária, consentimento sensível separado, migração aditiva 002, descoberta restrita e curtidas com verificação transacional da participação. Políticas e fixtures passam à versão 2026-11-21; publicar API e Android compatíveis. A participação não deve ser usada em segmentação de anúncios. Revisar a declaração de dados da Play para a autodeclaração de deficiência, além dos dados já listados abaixo.

Libras usa abertura explícita do aplicativo externo VLibras, com cópia/colagem pelo usuário. **Não há avatar/tradutor embutido no LoveAll.** Os três guias de funções possuem player Android e catálogo preparado, mas os vídeos em Libras ainda não existem: produzir conteúdo autorizado, revisar com pessoa fluente, incluir legendas e homologar playback antes de anunciar esses vídeos como disponíveis. O catálogo fica em `app/src/main/res/values/libras_videos.xml`. Ver detalhes na seção 19 do guia do código. As evidências de 20/11 abaixo pertencem ao release anterior, não validam automaticamente esse complemento.

Validação do complemento: 162 testes JVM, 47 testes da API e 29 cenários únicos no emulador API 37 aprovados (28 na regressão completa, mais três verificações finais dirigidas, incluindo um cenário novo de consentimento após saída remota). Lint debug/release sem erros; permanecem avisos de dependências/compatibilidade e um falso positivo de ortografia em português. Resultados em `qa/pcd-libras-2026-11-21/results.json`. Testes de banco usam SQLite e PGlite via driver pg, sem alterações no Supabase de produção. Vídeos reais, tradução no aplicativo VLibras instalado e deploy cloud ainda exigem homologação.

O código foi revisado para o aplicativo Android Kotlin/Compose. O pacote de distribuição é **com.loveall.app**, versão **1.0.0**, versionCode **1**, minSdk 26 e targetSdk 36. Nenhuma versão foi enviada à Play nesta revisão.

## Alterações desta revisão

- Validações compartilhadas no domínio para senha, identidade e preferências. Cadastro e edição agora rejeitam cidade/descrição acima do limite, listas duplicadas e referências de foto repetidas, em acordo com a API.
- Senhas novas respeitam o limite de 72 bytes UTF-8 do bcrypt, evitando sufixos ignorados. Login de contas existentes continua com o contrato anterior.
- Ações repetidas de passar/bloquear e moderação no chat foram simplificadas dentro de seus próprios ViewModels. Não foi criada uma superclasse genérica de ViewModel. Uma consulta de mensagens atrasada não modifica o histórico depois de fechar a conversa.
- Armazenamento Keystore serializa leitura, gravação, limpeza e geração da chave. Isso evita uma leitura antiga limpar uma sessão recém-gravada ou duas operações concorrerem na criação da chave.
- Componentes de formulário, feedback e identidade foram separados do arquivo genérico de componentes, mantendo a API dos composables.
- Documentos locais receberam o responsável e contato fornecidos: Cicero Pinheiro, loveallappcp@gmail.com. A vírgula digitada no endereço foi normalizada para ponto. A versão legal passou a 2026-11-20 no Android, API e fixtures: usuários anteriores precisarão aceitar os documentos atualizados.
- Páginas públicas preparadas na API, incluindo exclusão funcional pelo navegador com reautenticação, confirmação explícita, limite de tentativas e verificação de origem. Senhas não são colocadas em URLs, respostas ou logs.
- Configuração de assinatura por variáveis de ambiente e tarefa preparePlayRelease, que verifica chave e assinatura no AAB. A chave de upload foi criada localmente, com senha protegida por DPAPI do Windows e arquivos excluídos do Git.

## Bundle e chave de upload

Para gerar novamente no mesmo usuário Windows:

```powershell
.\scripts\Build-PlayRelease.ps1 -VersionCode 1 -VersionName 1.0.0
```

O resultado é `app/build/outputs/bundle/release/app-release.aab`. O script carrega temporariamente as quatro variáveis LOVEALL_UPLOAD_* e as restaura ao terminar. Não passar senhas na linha de comando. Usar um versionCode maior em cada envio posterior.

A chave está em `.secrets/loveall-upload.jks`; a senha fica cifrada em `.secrets/upload.signing.xml`. Esse XML só pode ser aberto pelo mesmo usuário no mesmo Windows: **copiá-lo sozinho não é um backup portátil**. Preserve a chave, faça um backup recuperável fora deste computador antes de enviar o app e use Play App Signing no Console. Nunca envie arquivos de chave/senha ao Git, Render, chat ou pacote da API. Se a chave já existir, New-UploadKey.ps1 não a substitui.

A revisão automática de aprovação bloqueou o comando de exportação/teste de backup portátil com a indicação genérica `blocked by policy`. Nenhuma exportação foi realizada; o backup externo permanece manual. A geração da chave e a assinatura local foram concluídas separadamente.

## Páginas que precisam entrar no ar

| Uso no Console | URL preparada |
| --- | --- |
| Site do desenvolvedor | https://loveall-api.onrender.com/ |
| Política de privacidade | https://loveall-api.onrender.com/privacy |
| Termos | https://loveall-api.onrender.com/terms |
| Padrões de segurança infantil | https://loveall-api.onrender.com/child-safety |
| Exclusão fora do app | https://loveall-api.onrender.com/delete-account |
| Verificação AdMob | https://loveall-api.onrender.com/app-ads.txt |

Estas rotas novas dependem de publicar as alterações da API no Render. Faça isso **antes de testar o novo Android contra o serviço público**, porque a versão de aceite dos termos também mudou. Não basta inserir no Console uma URL que ainda retorna 404. Para outro host, configure PUBLIC_SITE_URL para a origem HTTPS efetivamente usada pelo formulário de exclusão.

A exclusão no navegador remove a conta e dependências da base ativa; não é só um formulário de contato. A fila de remoção de fotos mantém retentativas quando o Storage falha. O responsável ainda precisa definir e operar backup, retenções de infraestrutura e atendimento aos titulares. Os textos descrevem esses limites sem inventar um prazo de backup.

Na checagem externa desta revisão, `/api/health` e as URLs de suporte não responderam dentro de 95 segundos. Não foi possível validar a infraestrutura pública; verificar status/logs do Render e se o projeto Supabase está pausado. Não considerar a API homologada com base apenas no teste local.

## Passos externos ainda obrigatórios

1. Publicar e abrir as páginas acima; testar cadastro, login, foto, mensagem, bloqueio e exclusão contra a API real com contas de teste autorizadas. A revisão automatizada usa bancos e HTTP isolados e não cria perfis de produção.
2. Na Play Console, cadastrar LoveAll e com.loveall.app, enviar o AAB à faixa de teste, habilitar Play App Signing e guardar a chave de upload com backup recuperável.
3. Informar público exclusivamente adulto e aplicar o bloqueio de menores exigido para apps de encontros; não basta o campo de nascimento no app. Preencher classificação de conteúdo, declaração de anúncios, Segurança dos dados e os URLs de privacidade/exclusão.
4. Usar Cicero Pinheiro e loveallappcp@gmail.com como responsável/contato autorizados. Confirmar acesso à caixa de email e operar análise de denúncias, remoção de conteúdo, medidas de segurança infantil e comunicação às autoridades quando aplicável. Publicar um texto não cria uma equipe de moderação.
5. No AdMob, configurar/publicar as mensagens UMP pertinentes, conferir app-ads.txt e concluir verificação/prontidão. Debug continua com unidade de teste; release usa os IDs reais fornecidos. Assinaturas e chamadas permanecem desligadas.
6. Preparar ícone, capturas reais e gráfico de destaque, descrição e dados de acesso para revisão. Não declarar funcionamento de chamadas ou compras indisponíveis. Executar o relatório de pré-lançamento da Play.
7. Se a conta pessoal for abrangida pela regra de novos desenvolvedores, realizar teste fechado com pelo menos 12 testadores inscritos continuamente por 14 dias antes de solicitar produção.
8. Validar backup/restauração e monitoramento. Render Free pode dormir e não é recomendado pelo provedor para produção; os limites gratuitos não garantem disponibilidade comercial.

## Segurança dos dados: base para preencher o Console

Revise as respostas conforme os SDKs e configurações efetivamente distribuídos:

| Dados | Tratamento no app/serviço |
| --- | --- |
| Nome, email, nascimento, identificador da conta | Cadastro, autenticação e idade; armazenados no serviço |
| Gênero, preferências, biografia, cidade | Perfil/descoberta; informações pessoais potencialmente sensíveis e parte do perfil público |
| Fotos | Perfil público; Storage privado com entrega controlada pela API; metadados removidos |
| Mensagens, curtidas, conexões | Funcionalidades de conversa e descoberta; sem criptografia de ponta a ponta |
| Denúncias e bloqueios | Segurança e moderação |
| IP/localização aproximada inferida, IDs do dispositivo/publicidade, interações e diagnósticos | Coleta/compartilhamento pelo SDK AdMob para publicidade, análise e prevenção de fraude; considerar declarações da Google |
| Compras | A integração existe, mas venda de assinatura está desligada na configuração atual |

As comunicações usam TLS, a senha é hash, o token local é cifrado e há exclusão dentro e fora do app. Isso não significa ausência de coleta ou compartilhamento por prestadores. Não preencher “nenhum dado coletado”. Os SDKs de anúncios e permissões do manifesto final devem constar da avaliação. Não há permissão de contatos, câmera, microfone nem acesso amplo à galeria; fotos usam o seletor Android.

## Fontes oficiais verificadas em 20/11/2026

- [API alvo 36](https://developer.android.com/google/play/requirements/target-sdk) e [páginas de memória de 16 KB](https://developer.android.com/guide/practices/page-sizes).
- [Exclusão dentro e fora do app](https://support.google.com/googleplay/android-developer/answer/13327111).
- [Segurança infantil](https://support.google.com/googleplay/android-developer/answer/14747720?hl=pt-BR) e [bloqueio de menores](https://support.google.com/googleplay/android-developer/answer/16302250?hl=pt-BR).
- [Teste fechado para novas contas pessoais](https://support.google.com/googleplay/android-developer/answer/14151465?hl=pt-BR).
- [Dados tratados pelo AdMob](https://developers.google.com/admob/android/next-gen/privacy/play-data-disclosure) e [assinatura Android](https://developer.android.com/studio/publish/app-signing).

As evidências finais e limitações desta execução ficam em `qa/release-review-2026-11-20/`. O bundle compilado e testes locais não equivalem a aprovação da loja.


## Resultado final das verificações

- 155 testes JVM e 26 testes instrumentados aprovados na verificação final; houve um timeout de login na primeira rodada, seguido de aprovação do caso isolado e duas suítes completas.
- 43 testes da API aprovados, incluindo exclusão web, proteção de origem e fronteira de senha bcrypt.
- Lint debug/release: zero erros; 25/26 avisos, majoritariamente versões de dependências e recomendações de estilo/API. Não foi feita atualização ampla de SDKs/AGP apenas para eliminar avisos.
- Cobertura de linhas das regras selecionadas: 78/78 (100%). Não representa cobertura de todo o app.
- AAB de release 1.0.0 assinado, minificado com R8 e verificado por jarsigner. As duas bibliotecas nativas de 64 bits presentes têm segmentos ELF alinhados a 16 KB; a execução em dispositivo de páginas 16 KB ainda precisa integrar a matriz de testes da loja.
- Auditoria npm das dependências de produção: zero vulnerabilidades conhecidas.
- O APK debug atualizado foi instalado e iniciado no emulador. A checagem pública da infraestrutura permaneceu sem resposta nesta revisão.

Metadados, hash do AAB, logs e limitações: [qa/release-review-2026-11-20/results.json](../qa/release-review-2026-11-20/results.json).

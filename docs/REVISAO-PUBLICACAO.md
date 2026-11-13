# Revisão e ajustes para publicação

Atualizado em 12/11/2026. Destino previsto: Google Play. Conforme solicitado, a distribuição fica para a última etapa.

**Os ajustes possíveis no código foram implementados. A publicação continua pendente de infraestrutura, identificação do responsável, operação de segurança e configuração da loja.** Chamadas e compras não fazem parte da versão disponível; não foram declaradas como concluídas.

## Atualização de monetização

A [assinatura para remover anúncios](MONETIZACAO.md) recebeu integração de Play Billing, verificação no servidor, RTDN autenticado e AdMob/UMP. O código fica desligado por padrão e ainda depende dos registros, credenciais, catálogo e homologação com as contas reais. Isso não reativa o checkout externo nem as chamadas. A avaliação abaixo descreve a rodada anterior de correções; os resultados mais recentes da monetização ficam em `qa/monetization`.

## Problemas tratados

| Achado da revisão | Tratamento atual |
| --- | --- |
| JWT com segredo alternativo conhecido | Removido. A API recusa inicialização sem segredo exclusivo com pelo menos 32 caracteres. A verificação aceita somente HS256 e contas ativas não banidas. Compose exige a variável em vez de fornecer um padrão. |
| Eventos Socket.IO sem autorização | Removidos junto à dependência. O Android mantém o chat REST existente; o antigo canal não fica exposto. |
| Webhook sem comprovação do pagamento | O fluxo inseguro e o SDK foram retirados. Webhook, compra, elegibilidade e registro de chamada retornam indisponibilidade e não concedem saldo. A consulta de assinaturas existentes foi preservada. |
| Ofertas de chamadas ainda não entregues | Compras desabilitadas em debug e release; nenhum parâmetro Gradle as habilita. A tela informa indisponibilidade e esconde ofertas. Saldo anterior permanece consultável. |
| Status `failed` desconhecido | Mapeado como terminal no Android, com apresentação e teste correspondentes. |
| Atualização de perfil sem contrato validado | Tipos, enumerações, nomes, limites, preferências, faixa etária, distância, coordenadas e propriedade das fotos são conferidos no servidor antes de gravar. Campos desconhecidos são rejeitados. |
| Cadastro com validação permissiva | Datas ISO reais, idade entre 18 e 120, tipos, identidade, preferências e aceite da versão atual das regras são validados. Conta e assinatura inicial são criadas em uma transação. |
| Exclusão de conta ausente | Implementada na API e no perfil Android, exigindo senha e confirmação explícita. Remove dados associados da base ativa em transação e invalida tokens pela ausência da conta. |
| Fotos sem controle de propriedade e remoção | Inventário por usuário; referências novas pertencem ao próprio usuário. JPEG é decodificado e recodificado, com limite de pixels, redimensionamento e remoção de metadados. Há quota e limite de envio. |
| Fotos removidas ainda acessíveis | Remoção do perfil enfileira exclusão física e impede acesso pela API imediatamente. Downloads usam `no-store`. Falhas de remoção são repetidas na manutenção. Rascunhos abandonados e arquivos órfãos expiram após 24 horas. |
| Ausência de termos e privacidade acessíveis | Documentos locais acessíveis na entrada, no cadastro e no perfil. Cadastro exige aceite explícito. Contas antigas têm tela de revisão; a API exige aceite atual antes de publicar conteúdo. Exclusão, denúncia e bloqueio permanecem acessíveis sem novo aceite. |
| Dados e interações malformados | Entradas inválidas de mensagens, paginação, curtidas, bloqueios e denúncias são rejeitadas. Perfis bloqueados não podem ser consultados diretamente. Datas completas de nascimento não são devolvidas nos perfis públicos. |
| Dependências de produção sinalizadas | Dependências sem uso removidas, bibliotecas atualizadas e lockfile atualizado. Auditoria posterior: **zero vulnerabilidades sinalizadas pelo npm**. Isso não equivale a ausência de qualquer falha no produto. |

As referências externas de fotos já existentes podem ser preservadas para não quebrar perfis anteriores. Não são aceitas novas URLs externas arbitrárias pela atualização de perfil. Arquivos que já tenham sido baixados por outras pessoas não podem ser apagados dos aparelhos delas.

## Como funciona a exclusão

1. A pessoa abre o perfil, escolhe **Excluir minha conta**, lê o alcance da exclusão e informa sua senha.
2. `SessionViewModel` solicita a exclusão ao `AuthRepository`; a senha não é persistida no estado salvo da Activity.
3. `DELETE /api/auth/me` exige autenticação e confere a senha contra o hash da própria conta.
4. Uma transação remove mensagens das conversas compartilhadas, histórico de chamadas, curtidas, rejeições, bloqueios, denúncias, matches, pagamentos, assinatura e conta. Conversas sem relação com essa pessoa são preservadas.
5. Fotos são marcadas para remoção; a API deixa de servi-las e tenta apagar os arquivos. Uma fila persistente permite repetir falhas de disco.
6. Depois da resposta de sucesso, o aplicativo apaga a sessão e retorna à entrada. Senha incorreta permite corrigir e tentar novamente.

A regra atual remove registros de pagamentos da base ativa porque esta versão não realiza compras. Antes de reativar transações reais, definir e implementar eventuais retenções necessárias, conciliação, reembolsos e tratamento dos dados na exclusão. Backups de uma futura infraestrutura também precisam de política de retenção e de um procedimento que não restaure contas excluídas.

## Validação

- Builds debug e bundle release com R8, testes JVM, cobertura de regras, lint e testes Android executados com sucesso. Lint: zero erros; 24 avisos em debug e 26 em release, principalmente versões de ferramentas/dependências e recomendações. O bundle continua sem assinatura e com URL fictícia de verificação.
- [Resumo dos resultados](../qa/publication-review/results.json).
- **124 testes JVM**, sem falhas. Cobertura continua limitada às classes explicitamente configuradas; não representa cobertura integral.
- **9 testes de integração/segurança da API**, com processos e bancos temporários isolados: segredo JWT, token falso, datas e consentimento, validação e propriedade das fotos, remoção e manutenção, interações malformadas, rotas indisponíveis e exclusão preservando dados de outras pessoas.
- Auditoria original: [14 pacotes sinalizados](../qa/publication-review/npm-audit.json). Auditoria posterior: [zero sinalizações](../qa/publication-review/npm-audit-after.json).
- CI da API acrescentado em `.github/workflows/server.yml`, com testes e auditoria. A configuração foi escrita localmente; a execução no GitHub ainda depende do próximo envio ao repositório.
- **21 testes instrumentados Android** passaram no emulador: cadastro e aceite, revisão das regras em conta antiga, exclusão com erro de senha e nova tentativa, chat, denúncia, planos sem ofertas, seletor de fotos e armazenamento seguro.

Para repetir:

```powershell
.\gradlew.bat :app:assembleDebug :app:verifyBusinessCoverage :app:lintDebug :app:assembleDebugAndroidTest
cd server
npm ci
npm test
npm audit --omit=dev
```

A API local exige `JWT_SECRET`; `scripts/start-api.ps1` gera um segredo aleatório quando cria um novo `.env`. Um `.env` antigo inválido precisa ser corrigido; o servidor não troca silenciosamente seu segredo. Os testes não excluem contas reais: utilizam bancos temporários.

O reinício final da API local foi rejeitado pela revisão automática de aprovação com a indicação `blocked by policy`, sem justificativa adicional. Reiniciar manualmente o processo de desenvolvimento para carregar a última alteração. Os testes da API usaram processos novos com o código atualizado.

## O que ainda depende de definições reais

### Produto e operação

- Informar a identidade do responsável/controlador e o contato de suporte/privacidade/segurança infantil. Os textos locais descrevem a versão atual, mas ainda precisam dessas informações e da revisão correspondente ao serviço real. Não foram inventados contatos ou uma equipe de moderação.
- Definir atendimento e resolução de denúncias, inclusive de menores e material de abuso. Registrar denúncias e aplicar banimento automático não substitui esse processo.
- Validar os textos finais de privacidade e termos com as práticas reais, prestadores e retenções que serão adotados.
- Se houver desejo de chamadas ou planos pagos na primeira versão, concluir mídia, cobrança permitida, comprovação de pagamentos e testes financeiros antes de reativá-los. A versão atual pode ser homologada com os recursos gratuitos existentes.

### Distribuição — etapa final

A opção atual é [Render Free + Supabase Free](RENDER-SUPABASE.md), após o cadastro Oracle brasileiro só oferecer Corporate. A API foi adaptada para PostgreSQL e Storage privado; contas, credenciais e URL pública ainda não foram configuradas. O plano gratuito atende homologação; o Render desaconselha produção comercial em Free. Backup externo e homologação real continuam pendentes.

1. Provisionar API HTTPS real, persistência, backups e monitoramento; alinhar a imagem de execução ao Node 22 usado nos testes. Ainda não existe URL de produção, conforme informado.
2. Publicar páginas de privacidade, regras de segurança e uma página funcional de solicitação de exclusão. A exclusão dentro do Android já está implementada; o recurso web ainda precisa ser publicado e integrado ao atendimento ou ao fluxo correspondente.
3. Configurar chave de upload, assinatura e Play App Signing; confirmar pacote e versionamento no Console.
4. Preencher Segurança dos dados, classificação de conteúdo, restrição de menores, contato responsável, materiais da loja e acesso para revisão.
5. Homologar o release assinado contra a infraestrutura real, incluindo exclusão, privacidade, rede, fotos, bloqueios e restauração. Executar o teste interno e o relatório de pré-lançamento da Play.

O AAB da revisão inicial foi compilado com URL fictícia e sem assinatura: não deve ser distribuído. O `targetSdk 36` atende ao requisito consultado; isso não garante aprovação geral.

Referências oficiais consultadas:

- [Exclusão de conta, incluindo recurso web](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en).
- [Dados do usuário](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en) e [conteúdo gerado pelo usuário](https://support.google.com/googleplay/android-developer/answer/9876937?hl=en-GB).
- [Segurança infantil](https://support.google.com/googleplay/android-developer/answer/14747720?hl=en-CA-3) e [restrição de menores](https://support.google.com/googleplay/android-developer/answer/16302250?hl=en).
- [Pagamentos](https://support.google.com/googleplay/android-developer/answer/10281818?hl=en), [API alvo](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) e [assinatura](https://developer.android.com/studio/publish/app-signing).


### Preparação Render + Supabase — 12/11/2026

Código adaptado sem alterar o contrato do Android: PostgreSQL assíncrono, schema privado versionado, TLS com validação, fotos em bucket privado e limpeza recuperável. SQLite e arquivos locais preservados para desenvolvimento. Blueprint gratuito sem disco persistente, validado contra o schema oficial Render; assinaturas desativadas por padrão.

Nesta rodada: **35 testes da API e 142 testes JVM aprovados**, APK debug compilado e auditoria das dependências de produção sem vulnerabilidades conhecidas. Os testes PostgreSQL locais usam PGlite pelo protocolo `pg`; o workflow também prepara PostgreSQL 16 real, ainda não executado remotamente. Instrumentação anterior não foi repetida nesta mudança. Evidências em `qa/render/results.json`.

Não houve publicação. O usuário ainda vai criar as contas Render/Supabase. Permanecem pendentes configuração real, TLS do projeto, proxy, despertar, persistência após deploy e backup externo. Docker não está disponível neste computador para validar a imagem localmente. Consulte [o procedimento atual](RENDER-SUPABASE.md).

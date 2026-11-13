# Evidências de QA

O relatório mais recente, após o redesign completo, está em [redesign/README.md](redesign/README.md). As seções seguintes preservam o histórico da migração inicial.

Verificações em 12/11/2026 com JDK 17, Gradle 8.13, SDK 36 e emulador Android 17/API 37 (`Medium_Phone_API_37.0`).

- 51 testes JVM passaram: contratos HTTP, sessão, validações, ViewModels, horários e estados de pagamento.
- 8 testes instrumentados passaram: 5 fluxos nativos (boas-vindas/cadastro, cadastro adulto completo, erro de login com recuperação, login/matches/envio de mensagem, segurança/planos) e 3 testes de Android Keystore (persistência sem texto puro, IV aleatório, corrupção).
- A cobertura JaCoCo das classes locais selecionadas (`Rules`, `Registration`, `ProfileEdit`) foi de 26/26 linhas e 95/104 branches. A meta de 80% das linhas foi verificada. Não representa cobertura de todo o app.
- `assembleDebug`, Android Lint e `bundleRelease` com R8 executados. O Lint não encontrou erros; os avisos remanescentes incluem versões mais recentes, target 36 em ambiente com API 37 e sugestões KTX. Nenhuma verificação foi desativada para aprovar o build.
- O bundle de release foi validado com `https://build-check.invalid/api/`, endereço reservado e sem serviço. Esse artefato serve apenas à verificação de compilação/minificação e **não deve ser distribuído**. Não há assinatura/configuração de produção nem validação comercial real.

As capturas PNG desta pasta mostram o app nativo no emulador com respostas HTTP simuladas e perfis de teste. Não demonstram conectividade com o servidor de produção, chamadas reais, processamento de pagamentos ou aprovação da loja. Datas/horários seguem a configuração do dispositivo de teste.

O cliente web foi arquivado em `legacy/web-client/`; a API e seus dados foram preservados. O Android agora é o projeto Gradle da raiz. Antes da liberação para produção, cumprir os critérios de homologação descritos em [MIGRATION.md](../docs/MIGRATION.md).

## Validação após consolidar o Android na raiz

Na nova estrutura foram executados `:app:assembleDebug`, `:app:verifyBusinessCoverage`, `:app:lintDebug` e `:app:assembleDebugAndroidTest`: build aprovado, 51 testes JVM sem falhas e Lint com 0 erros e 22 avisos. A execução dos 8 testes instrumentados acima pertence à validação anterior do mesmo código Kotlin; nesta reorganização, os testes Android foram recompilados.

A inspeção do APK confirmou a ausência de arquivos JavaScript/HTML/CSS e de bundle web/Capacitor. Os 34 arquivos rastreados do cliente original foram comparados com seus blobs Git e preservados integralmente no arquivo de legado. `git diff -- server` permaneceu vazio. As alterações de Docker/Render preparam somente o empacotamento da API; não houve publicação nem validação de container em execução.

# LoveAll Android nativo

Aplicativo Android escrito em Kotlin e Jetpack Compose. Abra **a raiz do repositório**, acima desta pasta de documentação, no Android Studio. O módulo ativo é `app/`. Não há React, Capacitor ou WebView no APK. A API existente foi mantida em `server/` (Node/Express, SQLite local e PostgreSQL na nuvem), conforme a decisão de migrar somente o aplicativo.

## Executar

Requisitos: JDK 17, SDK Android 36 e acesso aos repositórios Google/Maven Central. O Gradle Wrapper 8.13 está incluído com checksum SHA-256. Versões estão em `gradle/libs.versions.toml`; AGP 8.13.2 e Kotlin/Compose Compiler 2.3.0. A BOM Compose 2025.09.01 é uma base estável fixada para esta migração, não uma afirmação de versão mais recente.

1. O endereço padrão em `gradle.properties` é `https://loveall-api.onrender.com/api/`. Não é necessário iniciar o backend local para usar a API publicada. Nenhuma credencial é criada pelo aplicativo.
2. Inicie um emulador ou conecte um dispositivo Android com acesso à internet.
3. Na raiz do repositório, execute `.\gradlew.bat :app:installDebug` no Windows (Linux/macOS: `bash gradlew :app:installDebug`). O pacote debug é `com.loveall.app.native.debug`.

Para outro endpoint: `bash gradlew :app:assembleDebug -Ploveall.apiBaseUrl=https://seu-host/api/` (Windows: substitua `bash gradlew` por `.\gradlew.bat`). A URL deve terminar em `/`. Para desenvolver com a API local, inicie o backend conforme [COMO-EXECUTAR.md](../COMO-EXECUTAR.md) e use `-Ploveall.apiBaseUrl=http://10.0.2.2:3001/api/` no emulador. HTTP é permitido somente em debug, para `10.0.2.2` e `localhost`. Em aparelho físico, use HTTPS ou `adb reverse tcp:3001 tcp:3001` com `-Ploveall.apiBaseUrl=http://localhost:3001/api/`.

## Verificações

```text
bash gradlew :app:assembleDebug :app:lintDebug :app:verifyBusinessCoverage
bash gradlew :app:connectedDebugAndroidTest
```

Testes JVM usam fakes e MockWebServer, sem internet real. Os testes instrumentados exercitam telas Compose e HTTP simulado no próprio emulador, incluindo armazenamento pelo Android Keystore. Capturas de QA ficam no diretório externo `files/qa` do pacote debug. Não são prova de integração com produção.

`businessCoverage` gera HTML/XML e `verifyBusinessCoverage` exige 80% das linhas das regras de cadastro, edição de perfil e validação de URLs (`Rules`, `Registration`, `ProfileEdit`). A seleção explícita representa a lógica de negócio local; não mede UI, DTOs, rede nem equivalência total do produto. Testes de repositórios e ViewModels complementam esse indicador. A CI executa build, testes JVM, lint, cobertura e compilação dos testes Android; a execução instrumentada requer dispositivo/emulador.

## Estrutura

Consulte [ARCHITECTURE.md](ARCHITECTURE.md) para as responsabilidades, limites de dependência e testes da refatoração.

- `domain`: modelos, validações e contratos sem dependência Android.
- `data`: Retrofit, mapeamento, repositórios e sessão AES-GCM com chave não exportável do Android Keystore.
- `presentation`: ViewModels por funcionalidade, estados imutáveis e StateFlow; sem acesso direto a Retrofit/SQLite na apresentação.
- `ui`: telas Compose Material 3, navegação oficial, recursos de texto, tema claro/escuro e tokens de dimensão.
- `AppContainer`: injeção de dependências por construtor; um módulo de app enquanto a escala não justificar outros.

## Escopo e transição

Veja [MIGRATION.md](MIGRATION.md) para o inventário, decisões, riscos herdados e critérios antes da liberação. O cliente anterior foi arquivado em `legacy/web-client/`; ele não integra o build Android nem a imagem Docker da API. A raiz agora contém o projeto Gradle como entrada para o aplicativo. **A reescrita nativa não equivale a liberação para produção.**

Build de release exige uma URL HTTPS configurada; `gradle.properties` já fornece a API publicada. Para sobrescrever: `./gradlew :app:bundleRelease -Ploveall.apiBaseUrl=https://seu-host/api/`. A assinatura de upload local pode ser carregada com `scripts/Build-PlayRelease.ps1`; use esse script para preparar o AAB assinado. O fluxo completo e o backup necessário estão em `REVISAO-PUBLICACAO.md`. O `applicationId` de release mantém `com.loveall.app`, mas versionCode, assinatura, pacote publicado e direitos existentes precisam ser conferidos na Play Console antes de qualquer atualização.

Compras e chamadas estão indisponíveis em debug e release. A API também bloqueia essas operações; a consulta do saldo anterior foi preservada. Consulte [a revisão de publicação](REVISAO-PUBLICACAO.md) antes de reativar integrações.

Para assinatura sem anúncios, configurações AdMob/UMP e testes com Play Billing, veja [MONETIZACAO.md](MONETIZACAO.md). O checkout externo e os planos de chamadas continuam bloqueados.

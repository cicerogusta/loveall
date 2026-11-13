# LoveAll — Android Kotlin

O aplicativo é **Android nativo, com código Kotlin e interface Jetpack Compose**. Abra **a raiz deste repositório** no Android Studio. O módulo `:app` não utiliza React, JavaScript, Capacitor, WebView nem build npm.

Para entender a implementação, leia o [guia explicativo do código](GUIA-DO-CODIGO.md), com a estrutura, os fluxos principais e orientações de manutenção.

**Publicação:** consulte a [revisão para a Google Play](docs/REVISAO-PUBLICACAO.md). Os ajustes de segurança e exclusão de conta estão documentados; infraestrutura, contatos responsáveis e configuração da loja ficam para a etapa final.

**API gratuita:** veja o [guia Render + Supabase](docs/RENDER-SUPABASE.md). A opção escolhida usa cadastro pessoal, PostgreSQL e fotos em bucket privado. Código preparado; contas e publicação real ainda pendentes. O plano gratuito serve para início e homologação.

**Monetização:** a [assinatura sem anúncios e o AdMob](docs/MONETIZACAO.md) possuem integração de código. Ficam desligados até configurar os registros e homologar compras, notificações e anúncios com as contas reais.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

No Linux/macOS, use `bash gradlew` no lugar de `.\gradlew.bat`.

- `app/`: aplicativo Android, ViewModels, domínio, repositórios, recursos e testes.
- `gradle/`, `build.gradle.kts`, `settings.gradle.kts`: build Android na raiz.
- `server/`: API Node/Express existente, mantida por escolha do produto. Ela roda no servidor, não dentro do aplicativo.
- `legacy/`: referência histórica do cliente React/Capacitor e ferramentas web. Fora do build Android e da imagem Docker atual da API.
- `docs/` e `qa/`: execução, inventário da migração e evidências de teste.

O [redesign visual](docs/DESIGN.md) usa tipografia editorial, ilustração original, cartões de perfil imersivos e temas claro/escuro, mantendo o aplicativo 100% nativo.

**Node.js não é necessário para compilar o Android.** Para login, matches e mensagens entre pessoas, execute a API existente ou configure o endereço do servidor. No emulador, o padrão é `http://10.0.2.2:3001/api/`.

Consulte [como executar](COMO-EXECUTAR.md), o [guia Android](docs/ANDROID.md) e o [inventário e pendências](docs/MIGRATION.md). O código do app ser nativo não significa que as integrações de mídia, publicidade e compras já estejam prontas para produção.

# LoveAll — Android Kotlin

O aplicativo é **Android nativo, com código Kotlin e interface Jetpack Compose**. Abra **a raiz deste repositório** no Android Studio. O módulo `:app` não utiliza React, JavaScript, Capacitor, WebView nem build npm.

Para entender a implementação, leia o [guia explicativo do código](GUIA-DO-CODIGO.md), com a estrutura, os fluxos principais e orientações de manutenção.

**Publicação:** consulte a [revisão para a Google Play](docs/REVISAO-PUBLICACAO.md), atualizada em 20/11/2026. O preparo inclui chave de upload local, build assinado e páginas de suporte/exclusão. Publicar as páginas na API, fazer backup recuperável da chave e concluir os registros/testes da loja ainda são necessários.

**API gratuita:** publicada em `https://loveall-api.onrender.com/api/`, com PostgreSQL e fotos em bucket privado no Supabase. O Android usa esse endereço por padrão. Veja o [guia Render + Supabase](docs/RENDER-SUPABASE.md) para configuração e limites do plano gratuito; a homologação dos fluxos com usuários reais continua pendente.

**Monetização:** [AdMob e assinatura sem anúncios](docs/MONETIZACAO.md) possuem integração de código. Anúncios estão configurados com os IDs do proprietário; debug usa banner de teste e release a unidade real. Aprovação AdMob, consentimento e verificação app-ads.txt ainda precisam ser confirmados. A assinatura Play continua desligada.

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

**Node.js não é necessário para compilar o Android.** Login, matches e mensagens usam a API publicada por padrão, em emulador e dispositivo físico. Para desenvolver com a API local, sobrescreva `loveall.apiBaseUrl` conforme [como executar](COMO-EXECUTAR.md).

Consulte [como executar](COMO-EXECUTAR.md), o [guia Android](docs/ANDROID.md) e o [inventário e pendências](docs/MIGRATION.md). O código do app ser nativo não significa que as integrações de mídia, publicidade e compras já estejam prontas para produção.

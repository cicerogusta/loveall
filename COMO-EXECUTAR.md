# Executar o LoveAll Android

Abra `C:\Projetos\loveall` no Android Studio, sincronize o Gradle e execute a configuração do módulo **app** em emulador ou dispositivo Android.

Requisitos: JDK 17, SDK Android 36 e Android API 26 ou superior no dispositivo. O app é Kotlin/Compose e seu build não depende de Node.js.

O Android Studio pode executar o Gradle com seu JBR 21. A compilação usa a toolchain Java 17; o resolvedor Foojay em `settings.gradle.kts` baixa esse JDK automaticamente quando não o encontra instalado (a primeira sincronização precisa de internet). Se o Studio ainda mostrar “Cannot find a Java installation… languageVersion=17”, execute **Sync Project with Gradle Files** para carregar essa configuração.

Na raiz, com JDK e SDK configurados:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
.\gradlew.bat :app:verifyBusinessCoverage :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

O APK está em `app/build/outputs/apk/debug/app-debug.apk`. O pacote debug é `com.loveall.app.native.debug`.

O app usa por padrão `https://loveall-api.onrender.com/api/`, configurado em `gradle.properties`, tanto no emulador quanto no celular. Não é necessário executar Node.js localmente para acessar essa API. Após inatividade, o servidor gratuito pode demorar cerca de um minuto para responder.

## API local para desenvolvimento (opcional)

Somente o servidor continua usando Node.js. Para executá-lo, use suas configurações de ambiente e credenciais autorizadas; os comandos abaixo rodam em outra janela:

```powershell
npm --prefix server ci
npm --prefix server run dev
```

No Windows, use **Node.js 22 LTS** para as dependências SQLite atuais. Como alternativa aos comandos acima, execute na raiz:

```powershell
.\scripts\start-api.ps1
```

Esse script instala as dependências ausentes e cria `server/.env` de desenvolvimento com uma chave aleatória se o arquivo ainda não existir. Mantenha a API em execução enquanto usa o emulador; instalar ou abrir o APK não inicia o servidor. Para verificar a API, abra `http://localhost:3001/api/health`.

O diretório de execução do servidor é `server/`; configure seu `.env` nesse diretório ou forneça as variáveis no ambiente. Não execute o seed sobre dados de produção.

Para usar a API local no emulador, sobrescreva o endereço no build:

```powershell
.\gradlew.bat :app:assembleDebug '-Ploveall.apiBaseUrl=http://10.0.2.2:3001/api/'
```

No dispositivo físico, use uma API HTTPS acessível ou `adb reverse tcp:3001 tcp:3001` e a URL debug `http://localhost:3001/api/`.

Consulte [docs/ANDROID.md](docs/ANDROID.md) para release, segurança da sessão e verificações. Não use os comandos Capacitor/npm dos arquivos em `legacy/` para gerar este aplicativo.

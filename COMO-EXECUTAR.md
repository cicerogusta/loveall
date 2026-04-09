# LoveAll - Guia de Execucao

## Pre-requisitos
- Node.js 18+
- Android Studio (para Android)
- Xcode (para iOS - apenas macOS)

---

## 1. Web (Navegador)

```bash
# Instalar dependencias (primeira vez)
cd LoveAll
npm run install:all

# Seed do banco de dados com usuarios demo
cd server && node src/seed.js && cd ..

# Rodar server + client juntos
npm run dev
```
- Frontend: http://localhost:5173
- Backend API: http://localhost:3001
- Login demo: `ana@demo.com` / `senha1234`

---

## 2. Android

### Primeiro build:
```bash
cd LoveAll/client

# Build web + sync com projeto nativo
npm run build:android

# Abrir no Android Studio
npx cap open android
```

### No Android Studio:
1. Aguarde o Gradle sincronizar
2. Conecte um dispositivo ou inicie um emulador
3. Clique em **Run** (botao play verde)

### Para AdMob em producao:
1. Crie uma conta no [Google AdMob](https://admob.google.com)
2. Crie um app Android e obtenha os IDs dos anuncios
3. Substitua `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY` no arquivo:
   - `android/app/src/main/AndroidManifest.xml`
4. Instale o plugin: `npm install @capacitor-community/admob`
5. Rebuild: `npm run build:android`

### Gerar APK para distribuicao:
No Android Studio: **Build > Generate Signed Bundle / APK**

---

## 3. iOS

### Primeiro build (requer macOS + Xcode):
```bash
cd LoveAll/client

# Build web + sync com projeto nativo
npm run build:ios

# Abrir no Xcode
npx cap open ios
```

### No Xcode:
1. Selecione um Team de desenvolvimento (Signing & Capabilities)
2. Conecte um iPhone ou selecione um simulador
3. Clique em **Run** (botao play)

### Para AdMob em producao:
1. Crie um app iOS no Google AdMob
2. Substitua o `GADApplicationIdentifier` em `ios/App/App/Info.plist`
3. Instale o plugin: `npm install @capacitor-community/admob`
4. Rebuild: `npm run build:ios`

### Publicar na App Store:
No Xcode: **Product > Archive** e depois envie via App Store Connect

---

## 4. PWA (Progressive Web App)

O app ja tem suporte a PWA. Em dispositivos moveis com Chrome:
1. Acesse http://localhost:5173
2. Menu do navegador > "Instalar app" ou "Adicionar a tela inicial"

---

## Estrutura do Projeto

```
LoveAll/
├── client/                 # Frontend React + Vite
│   ├── src/
│   │   ├── components/     # Componentes reutilizaveis (AdBanner, BottomNav)
│   │   ├── contexts/       # AuthContext
│   │   ├── pages/          # Telas (Welcome, Login, Register, Discover, etc)
│   │   ├── utils/          # API, constantes, ads, platform
│   │   └── App.jsx         # Rotas principais
│   ├── android/            # Projeto Android nativo (Capacitor)
│   ├── ios/                # Projeto iOS nativo (Capacitor)
│   └── capacitor.config.json
├── server/                 # Backend Express.js
│   ├── src/
│   │   ├── routes/         # Auth, Profiles, Likes, Matches, Messages, Safety, Calls
│   │   ├── middleware/     # Auth JWT, Safety filters, Rate limiter
│   │   ├── database.js     # SQLite schema
│   │   ├── seed.js         # Dados demo
│   │   └── index.js        # Server + Socket.IO
│   └── data/               # Banco SQLite
└── package.json            # Scripts do monorepo
```

## Modelo de Monetizacao

| Recurso | Preco |
|---------|-------|
| Likes | **Gratis e ilimitados** |
| Matches e Chat | **Gratis** |
| Banner ads | Exibidos no app (gera receita) |
| Interstitial ads | A cada 5 swipes |
| Rewarded ads | Assistir = ganhar Super Like |
| Chamada de audio/video Basico | R$ 9,90/mes (60 min) |
| Chamada de audio/video Plus | R$ 19,90/mes (180 min) |
| Chamada de audio/video Premium | R$ 29,90/mes (ilimitado) |

## Generos Suportados

Mulher, Homem, Nao-binario, Mulher Trans, Homem Trans, Genderqueer,
Genderfluid, Agenero, Bigenero, Two-Spirit, Outro, Prefiro nao dizer

## Recursos de Seguranca

- Verificacao de perfil (selo azul)
- Filtro de conteudo em mensagens (bloqueia dados pessoais/financeiros)
- Rate limiter (max 20 msgs/min)
- Denuncia com 7 categorias
- Ban automatico apos 5 denuncias
- Bloqueio instantaneo de usuarios
- Verificacao de idade (18+)
- Dicas de seguranca + numeros de emergencia

# Apenas a API existente. O Android é compilado separadamente pelo Gradle.
FROM node:22-bookworm-slim AS dependencies

RUN apt-get update && apt-get install -y --no-install-recommends python3 make g++ \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Instalar dependencias do server
COPY server/package.json server/package-lock.json* ./server/
RUN cd server && npm ci --omit=dev --no-audit --no-fund

FROM node:22-bookworm-slim AS production
WORKDIR /app
COPY --from=dependencies --chown=node:node /app/server/node_modules ./server/node_modules
COPY --chown=node:node server/package.json ./server/package.json

# Copiar codigo do server
COPY --chown=node:node server/src ./server/src

# Criar diretorio de dados
RUN mkdir -p /app/server/data /data && chown -R node:node /app /data

ENV NODE_ENV=production
ENV PORT=3001

EXPOSE 3001

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s \
  CMD node -e "fetch('http://127.0.0.1:'+(process.env.PORT||3001)+'/api/health').then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"

WORKDIR /app/server
USER node
CMD ["node", "src/index.js"]

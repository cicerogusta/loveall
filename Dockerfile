# ============================
# Stage 1: Build do Frontend
# ============================
FROM node:20-alpine AS frontend-build

WORKDIR /app/client
COPY client/package.json client/package-lock.json* ./
RUN npm ci --ignore-scripts
COPY client/ .
RUN npm run build

# ============================
# Stage 2: Server de Producao
# ============================
FROM node:20-alpine AS production

RUN apk add --no-cache python3 make g++

WORKDIR /app

# Instalar dependencias do server
COPY server/package.json server/package-lock.json* ./server/
RUN cd server && npm ci --omit=dev

# Copiar codigo do server
COPY server/src ./server/src

# Copiar build do frontend
COPY --from=frontend-build /app/client/dist ./client/dist

# Criar diretorio de dados
RUN mkdir -p /app/server/data

ENV NODE_ENV=production
ENV PORT=3001

EXPOSE 3001

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s \
  CMD wget -qO- http://localhost:3001/api/health || exit 1

WORKDIR /app/server
CMD ["node", "src/index.js"]

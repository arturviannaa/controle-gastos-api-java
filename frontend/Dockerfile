# build do bundle
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# o endereco da API entra no bundle em build time
ARG VITE_API_URL=http://localhost:8092/api
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# servir estatico: nginx faz isso melhor e mais leve que qualquer runtime node
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html

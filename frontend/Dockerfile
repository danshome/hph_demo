FROM node:20-alpine AS build
WORKDIR /app
COPY package.json tsconfig.json ./
RUN npm install
COPY src ./src
COPY index.html ./
RUN npx tsc && cp index.html dist/index.html

FROM nginx:1.25-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

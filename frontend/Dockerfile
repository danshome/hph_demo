# Build and serve the Angular demo using a simple static server
FROM node:20-alpine
WORKDIR /app
COPY package.json tsconfig.json index.html ./
COPY src ./src
RUN npm install
RUN npm run build
EXPOSE 4200
CMD ["npx", "http-server", "/app", "-p", "4200"]

# Stage 1: Build the application
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first for better layer caching
COPY package.json package-lock.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Accept VITE_API_BASE_URL as a build argument so Railway can inject it
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Build the production bundle
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy nginx template
COPY nginx.conf.template /etc/nginx/nginx.conf.template

# Copy built assets from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Railway injects PORT; default to 80 for docker-compose
ENV PORT=80
# Default backend URL for docker-compose; override for Railway
ENV BACKEND_URL=http://backend:8080

EXPOSE ${PORT}

# Use envsubst to replace PORT and BACKEND_URL in nginx template
CMD ["/bin/sh", "-c", "envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]

FROM nginx:stable-alpine3.17
RUN mkdir -p /usr/local/aurora-vue/blog/
RUN mkdir -p /usr/local/aurora-vue/admin/
RUN mkdir -p /usr/local/nginx/
COPY aurora-blog/dist/ /usr/local/aurora-vue/blog/
COPY aurora-admin/dist/ /usr/local/aurora-vue/admin/
COPY nginx.conf /usr/local/nginx/
CMD ["nginx", "-g", "daemon off;"]
EXPOSE 80
EXPOSE 443

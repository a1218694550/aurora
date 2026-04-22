# 基础镜像（稳定、轻量）
FROM nginx:stable-alpine3.17

# 强制清空镜像缓存，每次构建都全新复制前端
# 创建目录
RUN mkdir -p /usr/local/aurora-vue/blog/ \
    && mkdir -p /usr/local/aurora-vue/admin/

# 先删除容器自带干扰配置！必加！
RUN rm -rf /etc/nginx/conf.d/*

# 复制前端dist（顺序不变）
#COPY aurora-blog/dist/ /usr/local/aurora-vue/blog/
#COPY aurora-admin/dist/ /usr/local/aurora-vue/admin/

# 覆盖Nginx全局配置
COPY nginx.conf /etc/nginx/nginx.conf

# 权限修复
RUN chmod -R 755 /usr/local/aurora-vue/

# 声明端口
EXPOSE 80
EXPOSE 9010
EXPOSE 443

# 前台常驻运行
CMD ["nginx", "-g", "daemon off;"]
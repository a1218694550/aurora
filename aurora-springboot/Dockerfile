FROM openjdk:8
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone
# 删除COPY命令，不再将jar文件复制到镜像中
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-XX:+PrintGC","-XX:+PrintGCDetails","-XX:+PrintGCTimeStamps","-XX:+PrintGCDateStamps","-Xloggc:/app/logs/gc.log","-jar","/app/aurora-springboot-0.0.1.jar","--spring.config.location=/config/application-prod.yml"]
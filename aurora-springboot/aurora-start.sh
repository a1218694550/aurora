SOURCE_PATH=/usr/local/aurora-springboot
SERVER_NAME=aurora-springboot-0.0.1.jar
TAG=latest
SERVER_PORT=9080
CID=$(docker ps | grep "$SERVER_NAME" | awk '{print $1}')
IID=$(docker images | grep "$SERVER_NAME" | awk '{print $3}')
if [ -n "$CID" ]; then
  echo "存在容器$SERVER_NAME,CID-$CID"
  docker stop $CID
  echo "成功停止容器$SERVER_NAME,CID-$CID"
  docker rm $CID
  echo "成功删除容器$SERVER_NAME,CID-$CID"
fi
if [ -n "$IID" ]; then
  echo "存在镜像$SERVER_NAME:$TAG,IID=$IID"
  docker rmi $IID
  echo "成功删除镜像$SERVER_NAME:$TAG,IID=$IID"
fi
echo "开始构建镜像$SERVER_NAME:$TAG"
cd $SOURCE_PATH
docker build -t $SERVER_NAME:$TAG .
echo "成功构建镜像$SERVER_NAME:$TAG"
# 修改运行命令，使用卷挂载方式
docker run --restart=always \
  --name aurora-springboot-0.0.1.jar \
  -e JAVA_OPTS="-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/app/logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M" \  -d -p 9080:9080 \
  -v /usr/local/aurora-springboot:/app \
  -v /usr/local/aurora-springboot/config:/config \
  -v /usr/local/aurora-springboot/logs:/app/logs \
  $SERVER_NAME:$TAG
echo "成功创建并运行容器$SERVER_NAME"
#!/bin/bash
SOURCE_PATH="/usr/local/aurora-springboot"
# 镜像名称不建议包含.jar扩展名
SERVER_NAME="aurora-springboot"
VERSION="0.0.1"
TAG="${VERSION}-latest"
SERVER_PORT=9080

# 停止并删除现有容器
CID=$(docker ps | grep "$SERVER_NAME" | awk '{print $1}')
if [ -n "$CID" ]; then
  echo "存在容器$SERVER_NAME,CID-$CID"
  docker stop "$CID"
  echo "成功停止容器$SERVER_NAME,CID-$CID"
  docker rm "$CID"
  echo "成功删除容器$SERVER_NAME,CID-$CID"
fi

# 删除现有镜像
IID=$(docker images | grep "$SERVER_NAME" | awk '{print $3}')
if [ -n "$IID" ]; then
  echo "存在镜像$SERVER_NAME:$TAG,IID=$IID"
  docker rmi "$IID"
  echo "成功删除镜像$SERVER_NAME:$TAG,IID=$IID"
fi

# 构建新镜像
echo "开始构建镜像$SERVER_NAME:$TAG"
cd "$SOURCE_PATH" || { echo "无法进入目录$SOURCE_PATH"; exit 1; }
docker build -t "${SERVER_NAME}:${TAG}" . || { echo "镜像构建失败"; exit 1; }
echo "成功构建镜像$SERVER_NAME:$TAG"

# 运行新容器
echo "开始运行容器$SERVER_NAME:$TAG"
docker run --restart=always \
  --name "${SERVER_NAME}-${VERSION}" \
  -e JAVA_OPTS="-Xms64m -Xmx128m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/app/logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M" \
  -d -p "${SERVER_PORT}:${SERVER_PORT}" \
  -v "${SOURCE_PATH}:/app" \
  -v "${SOURCE_PATH}/config:/config" \
  -v "${SOURCE_PATH}/logs:/app/logs" \
  "${SERVER_NAME}:${TAG}" || { echo "容器启动失败"; exit 1; }

echo "成功创建并运行容器$SERVER_NAME:$TAG"

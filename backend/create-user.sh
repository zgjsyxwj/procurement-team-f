#!/bin/bash
# 创建内部用户的便捷脚本
# 用法: ./create-user.sh

JAR_FILE="target/ecosaas-procurement-0.0.1.snapshot.jar"

if [ ! -f "$JAR_FILE" ]; then
  echo "JAR 文件不存在，正在编译..."
  mvn package -DskipTests -q
fi

java -jar "$JAR_FILE" --spring.profiles.active=dev,create-user

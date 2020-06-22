#!/bin/bash

mvn clean package
docker build -t registry.cn-hangzhou.aliyuncs.com/sirm/aliyun-preliminary .
docker login --username=sirmsyp registry.cn-hangzhou.aliyuncs.com
docker push registry.cn-hangzhou.aliyuncs.com/sirm/aliyun-preliminary
docker rmi -f `docker images | grep  "<none>" | awk '{print $3}'`

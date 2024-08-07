#!/bin/bash

REDIS_HOST=localhost
REDIS_PORT=6379

# Redis가 준비될 때까지 대기
until nc -z -v -w30 $REDIS_HOST $REDIS_PORT; do
 echo "Waiting for Redis connection..."
 # 대기 시간 추가 (예: 5초)
 sleep 5
done

echo "Redis is up and running"
#!/bin/bash
#Sample approach to wait until a container is ready
#by looking for a string in container log
container=$1
target=$2

attempt=0
while [ $attempt -le 60 ]; do
    attempt=$(( $attempt + 1 ))
    echo "Waiting for container ready (attempt: $attempt)..."
    result=$(docker logs $container)
    if grep -q "$target" <<< $result ; then
      echo "Container is ready!"
      exit 0
    fi
    sleep 1
done
echo "ERROR: Container is not ready after maximum number of attempts"
exit 1
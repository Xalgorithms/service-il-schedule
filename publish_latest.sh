#!/bin/bash
echo "publishing $1 as latest"
docker tag xalgorithms/service-il-schedule:$1 xalgorithms/service-il-schedule:latest-development
docker push xalgorithms/service-il-schedule:latest-development

TAG="development"
if [[ -n "$TRAVIS_BRANCH" && "$TRAVIS_BRANCH" == "production" ]]; then
   TAG=$TRAVIS_BRANCH
fi

docker build -t xalgorithms/xadf-schedule-service:$TAG -f Dockerfile.$TAG .
docker login -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
docker push xalgorithms/xadf-schedule-service:$TAG

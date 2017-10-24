TAG="development"
if [[ -n "$TRAVIS_BRANCH" && "$TRAVIS_BRANCH" != "master" ]]; then
   TAG=$TRAVIS_BRANCH
fi

docker build -t xalgorithms/xadf-schedule-service:$TAG -f Dockerfile.$TAG .
docker push xalgorithms/xadf-schedule-service:$TAG

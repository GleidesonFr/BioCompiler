FROM alpine:3.20

RUN apk add --no-cache sqlite

WORKDIR /data
VOLUME ["/data"]

CMD ["sh", "-c", "mkdir -p /data && while true; do sleep 3600; done"]

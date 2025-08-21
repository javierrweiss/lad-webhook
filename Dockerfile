FROM clojure:latest

WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -s /bin/sh -m appuser

RUN mkdir -p lad_webhook/

RUN mkdir -p /home/appuser/lad_webhook

RUN chown -R appuser:appgroup lad_webhook/ && chmod 755 lad_webhook/

VOLUME [ "/app/lad_webhook" ]

COPY target/*jar app.jar

RUN echo '#!/bin/sh' > /entrypoint.sh && \
    echo 'chown -R appuser:appgroup /app/lad_webhook' >> /entrypoint.sh && \
    echo 'chmod 755 /app/lad_webhook' >> /entrypoint.sh && \
    echo 'if [ -f /run/secrets/secrets.edn ]; then' >> /entrypoint.sh && \
    echo 'cp /run/secrets/secrets.edn /home/appuser/lad_webhook/.secrets.edn' >> /entrypoint.sh && \
    echo 'chown appuser:appgroup /home/appuser/lad_webhook/.secrets.edn' >> /entrypoint.sh && \
    echo 'fi' >> /entrypoint.sh && \
    echo 'exec runuser -u appuser -- "$@"' >> /entrypoint.sh && \
    chmod +x /entrypoint.sh

EXPOSE 2000

ENTRYPOINT ["/entrypoint.sh"]
CMD [ "java", "-jar", "app.jar"]
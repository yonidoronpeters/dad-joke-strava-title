FROM gradle:jdk8-alpine

COPY . /app
WORKDIR /app

USER root
RUN chown -R gradle /app
USER gradle

ENTRYPOINT ["sh", "scripts/entrypoint.sh"]

FROM eclipse-temurin:17

RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y nodejs \
    npm 

ARG JAR_FILE=springdb/target/*.jar
ARG FRONT_END=springdb/frontend
ARG NODE_NPM=springdb/target/node
ARG ENTRY=start.sh
COPY ${JAR_FILE} app.jar
COPY ${FRONT_END} frontend
# COPY package*.json ./
# COPY ${NODE_NPM} node
COPY ${ENTRY} start.sh
ENTRYPOINT ["/start.sh"]
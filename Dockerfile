FROM openjdk:17-oracle
#FROM anapsix/alpine-java
#FROM alpine-java:base
#FROM openjdk:8-jdk-alpine
MAINTAINER maj.dagostino@gmail.com

#RUN mkdir /service
#RUN wget -c https://download.hazelcast.com/management-center/hazelcast-management-center-3.12.5.tar.gz -O - | tar -xz -C service
#RUN mv /service/hazelcast-management-center-3.12.5 /service/hazelcast-management-center
#RUN chmod +x /service/hazelcast-management-center/start.sh

#ARG BUILD_VERSION=SNAPSHOT
ARG SERVICE_PORT=8080
ARG METRICS_PORT=8081

ENV JAVA_OPTS "--add-modules java.se --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED ${JAVA_OPTS}"
ENV SERVICE_PORT=$SERVICE_PORT
ENV METRICS_PORT=$METRICS_PORT


COPY ./build/libs/graphql-pulse-1.0.0-SNAPSHOT-fat.jar /service/graphql-pulse-demo.jar
COPY ./src/main/resources/config.yml /service/config.yml
COPY ./src/main/resources/schema.graphql /service/schema.graphql
COPY docker/demo-entrypoint.sh /demo-entrypoint.sh

#CMD ["bash", "/service/hazelcast-management-center/start.sh", "5700", "/mancenter"]
ENTRYPOINT ["/demo-entrypoint.sh"]
#ENTRYPOINT ["java","-Xms1024m -Xmx2048m","-jar","/service/graphql-pulse-demo.jar"]
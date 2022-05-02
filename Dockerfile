FROM bellsoft/liberica-openjdk-alpine-musl:17.0.3

RUN apk add --no-cache fontconfig ttf-dejavu && rm -rf /var/cache/apk/*

WORKDIR /home/pdf4oereb-web-service
COPY build/libs/pdf4oereb-web-service-*.jar /home/pdf4oereb-web-service/pdf4oereb-web-service.jar
RUN cd /home/pdf4oereb-web-service && \
    chown -R 1001:0 /home/pdf4oereb-web-service && \
    chmod -R g+rw /home/pdf4oereb-web-service && \
    ls -la /home/pdf4oereb-web-service

USER 1001
EXPOSE 8080
ENV LOG4J_FORMAT_MSG_NO_LOOKUPS=true
CMD java -XX:MaxRAMPercentage=80.0 -jar pdf4oereb-web-service.jar \

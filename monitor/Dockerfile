FROM gradle:7.5.1-jdk11-alpine as buildJRE

RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime


FROM alpine:latest as prepareContainerJRE

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=buildJRE /javaruntime $JAVA_HOME


FROM gradle:7.5.1-jdk11-alpine as buildDeps

WORKDIR /app

COPY --chown=gradle:gradle *.gradle.kts ./

RUN gradle --no-daemon build


FROM buildDeps as buildApp

COPY --chown=gradle:gradle src .

RUN gradle --no-daemon installDist


FROM prepareContainerJRE

ARG USR=1000

RUN adduser --no-create-home -u $USR -D app
USER $USR

WORKDIR /app

COPY --from=buildApp --chown=$USR:$USR /app/build/install/monitor .

ENTRYPOINT ["./bin/monitor"]
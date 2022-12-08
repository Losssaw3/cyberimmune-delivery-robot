FROM gradle:7.6.0-jdk17-alpine as buildJRE

RUN apk add --no-cache binutils

RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.logging,java.net.http,java.prefs,java.se \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime


FROM alpine:latest as prepareContainerJRE

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=buildJRE /javaruntime $JAVA_HOME


FROM prepareContainerJRE

ARG USER_NAME=app
ARG GROUP_NAME=app
ARG USER_ID=1000
ARG GROUP_ID=1000

WORKDIR /app

RUN addgroup -g $GROUP_ID $GROUP_NAME &&\
    adduser -u $USER_ID -G $GROUP_NAME -D $USER_NAME -h /app

USER $USER_NAME:$GROUP_NAME

ARG PROJECT_NAME

COPY --chown=$USER_NAME:$GROUP_NAME package.zip .

RUN unzip package.zip && \
    rm package.zip && \
    mv package/bin/$PROJECT_NAME package/bin/start

ENTRYPOINT ["/app/package/bin/start"]
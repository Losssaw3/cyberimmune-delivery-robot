# Cyber-immune delivery robot

This system is a demo project for principles demonstration of cyber-immune applications development.
All documentation is in `/docs` (sorry, but in Russian only).

[Video demonstration](https://youtu.be/_nKHuNlXcNc)

## Setup

- Environment
    - Please use Linux.
    - Install `Java 16` (16 only, because containers have v16 and may fail with others)
    - Install `Gradle`. Tested with v7.5.1
    - Install `Docker` and `docker-compose`
    - If you want, install `tmux` and `tmuxinator` to view logs with alreadyprepared screens
- `git clone https://github.com/BardinPetr/cyberimmune-delivery-robot.git`
- `cd certs && ../keygen.sh` - generate certificates (demo certs included in `/certs`)
- `gradle build` - builds all services
- `gradle docker` - builds docker images
- `./start.sh` - starts all containers in docker-compose
- `./reload.sh` - reloads stateful containers in docker-compose
- `# ./compose.sh` - wrapper for docker-compose with configs set
- `# test.http` - file with main api requests needed

## Building

Gradle multi-project builds are used.
Packaging to docker is not classic. Do not try to use `docker build`.
All services are build with single Dockerfile located in /docker.
Firstly, build java code with gradle.
Then use `gradle docker`, which copies global dockerfile, inserts service-specific data
and puts application package near to dockerfile copy and then automatically builds image.
All images get names in form `delivery_${gradleProjectName}`.

## Running

```shell
gradle docker
./start.sh
tmuxinator start
```
When system is up, go to test.http file and create new robot-instance by sending request below comment (# Register robot by URL in FMS).

```shell
HTTP/1.1 200 OK
Date: Thu, 12 Jun 2025 14:35:37 GMT
Connection: close
Content-Type: text/plain
Content-Length: 0
```
The output will look similar to the example above.

Then send task to robot by sending request below (# Create new task and automatically assign to first available robot)
```shell
HTTP/1.1 200 OK
Date: Thu, 12 Jun 2025 14:35:37 GMT
Connection: close
Content-Type: text/plain
Content-Length: 0
```
The output will look similar to the example above.

After execute pin.sh 
```shell
server_auth-1  | 14:28:33.579 [WARN ] backend.authentication.services.messaging.SenderService - sending PIN code to user: 758373
```
Then you need to copy 6-digit PIN and 
paste it to the next request

```shell
GET http://0.0.0.0:9041/pin?code=PASTEYOURPINHERE
```

If you did everything correctly output will look similar to the example below.

```shell
HTTP/1.1 200 OK
Date: Thu, 12 Jun 2025 14:40:19 GMT
Content-length: 2

OK
```
This is expected behavior

## Testing

There are e2e tests provided in `./e2e` project on Junit5.

- Running these tests requires user to **manually** start all containers with `./start.sh`.
- Each before rerunning tests, it is recommended to restart all with `./compose.sh down && ./start.sh`
    - if you know how services work, you can use `reload.sh` to reload only stateful containers
      if others are unaffected by your previous actions
- Only after having containers started execute tests with `gradle test --info`
- Remember that tests requires kafka to be active, so initial delay of up to 1 minute is normal
- Tests **cannot** be started with any Kafka ACLs configured,
  because they need to subscribe to any topic and publish to any topic bypassing the monitor
- Tests should be run **only with demo certificates**.
- Sometimes messages could be lost. If testing fails on first steps, try restarting

## Structure info

### Containers

- Environment
    - robot and server
        - `*_zookeeper`
        - `*_broker`
        - `*_kafka_ui` - WEB panel for monitoring kafka messages
        - `*_monitor` - Monitor service
        - `*_comms` - Communication service (Kafka to HTTP bridge)
    - Robot
        - `robot_central` - main unit / business logic
        - `robot_hmi` - user interaction (PIN code enter)
        - `robot_motion` - motor controller
        - `robot_odom` - odometer positioning driver
        - `robot_location` - location aggregator
        - `robot_sensors` - environmental sensors unit - human detection
        - `robot_locker` - locker control
    - Server
        - `server_fms` - fleet management system / business logic
        - `server_auth` - PIN generation and delivery
        - `server_store` - service for submitting tasks to FMS

## Docker networks:

- robot_net
    - internal network for all robot services
- server_net
    - internal network for all server services
- web_net
    - network connecting FMS and Web store
- comms_net
    - this network is demo only
    - in real should be replaced with VPN connection
    - external network connection robot and server communication service

### Hostnames:

- robot-com - for robot communication service
- server-com - for server communication service
- server-fms - for FMS

### Ports:

- server
    - host:
        - 9010 - comms unit
        - 9020 - kafka ui
        - 9030 - kafka
        - 9040 - FMS HTTP API
        - 9044 - WEB store HTTP API
    - internal:
        - zookeeper:2181
        - broker:29000
- robot
    - host:
        - 9011 - comms unit
        - 9021 - kafka ui
        - 9031 - kafka
        - 9041 - HMI HTTP API
    - internal:
        - zookeeper:2181
        - broker:29000

### Troubleshooting
- if pin.sh does not executing (permission denied). Try _chmod 777 pin.sh_ or other ways to grand right to execute
- if you have issues with e2e tests (especially at PIN validation) try enter PIN manually by executing pin.sh and finish mission then start tests again


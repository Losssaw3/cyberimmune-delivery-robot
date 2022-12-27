# Cyber-immune delivery robot

This system is a demo project for principles demonstration of cyber-immune applications development.
All documentation is in `/docs` (sorry, but in Russian only).

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

## Testing

There are e2e tests provided in `./e2e` project on Junit5.

- Running these tests requires user to **manually** start all containers with `./start.sh`.
- Each before rerunning tests, it is recommended to restart all with `./compose.sh down && ./start.sh`
- Only after having containers started execute tests with `gradle test`
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

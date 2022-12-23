#!/bin/bash

docker-compose -f robot.docker-compose.yml -f server.docker-compose.yml $@

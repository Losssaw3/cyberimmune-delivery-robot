#!/bin/bash

docker network create comms_net
./compose.sh up -d robot_central server_fms robot_kafka_ui server_kafka_ui server_store
tmuxinator start
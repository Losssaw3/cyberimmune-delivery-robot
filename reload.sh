#!/bin/bash

./compose.sh stop robot_central robot_motion robot_location server_fms
./compose.sh up -d robot_central robot_motion robot_location server_fms

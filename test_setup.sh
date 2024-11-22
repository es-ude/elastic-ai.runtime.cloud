#!/usr/bin/env bash

if [[ -z $(command -v docker) ]]; then
  UNDEFINED_DEPENDENCY_FOUND=1
  echo "docker not found! Please install docker."
fi

# gradle doesn't need to be checked -> gradle wrapper provided!

if [[ -z $(command -v java) ]]; then
  UNDEFINED_DEPENDENCY_FOUND=1
  echo "Java not found! Please install JDK version 22."
elif [[ -z $(java --version | awk '/openjdk/ && ($2 == 22) {print "match"}') ]]; then
  UNDEFINED_DEPENDENCY_FOUND=1
  echo "Wrong version of Java found! Please install JDK version 22."
fi

if [[ -z $(command -v docker) ]]; then
  UNDEFINED_DEPENDENCY_FOUND=1
  echo "Docker not found! Pleas install Docker."
fi

if [[ $UNDEFINED_DEPENDENCY_FOUND -ne 1 ]]; then
  echo "All dependencies are satisfied!"
fi

if [[ -z "$HOST_IP" ]]; then
  echo ""
  echo "HOST_IP not defined. Please define a environment variable that stores your current hosts IP."
  echo "For example: Add 'export HOST_IP=\$(hostname -I)' to your .bashrc"
fi

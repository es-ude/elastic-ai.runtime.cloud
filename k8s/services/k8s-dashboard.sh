#! /bin/sh
microk8s kubectl apply -f ./dashboard/dashboard.yml

microk8s kubectl proxy --address 0.0.0.0 --accept-hosts '.*'

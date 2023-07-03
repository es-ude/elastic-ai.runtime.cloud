#! /bin/bash

microk8s kubectl apply -f ~/k8s/services/dashboard/dashboard.yml

kubectl proxy --address 0.0.0.0 --accept-hosts '.*'

echo "Dashboard is reachable at http://192.168.203.46:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/overview?namespace=default"

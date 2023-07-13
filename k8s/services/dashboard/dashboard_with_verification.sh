#!/bin/bash
showtoken=1
cmd="microk8s microk8s kubectl proxy --address=0.0.0.0 --accept-hosts '.*'"
count=`pgrep -cf "$cmd"`
dashboard_yaml="https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0/aio/deploy/recommended.yaml"
msgstarted="-e Kubernetes Dashboard \e[92mstarted\e[0m"
msgstopped="Kubernetes Dashboard stopped"

case $1 in
start)
   microk8s kubectl apply -f ~/k8s/services/dashboard/dashboard.yml >/dev/null 2>&1
   microk8s kubectl apply -f ~/k8s/services/dashboard/dashboard-admin.yaml >/dev/null 2>&1
   microk8s kubectl apply -f ~/k8s/services/dashboard/dashboard-read-only.yaml >/dev/null 2>&1

   if [ $count = 0 ]; then
      nohup $cmd >/dev/null 2>&1 &
      echo $msgstarted
   else
      echo "Kubernetes Dashboard already running"
   fi
   ;;

stop)
   showtoken=0
   if [ $count -gt 0 ]; then
      kill -9 $(pgrep -f "$cmd")
   fi
   microk8s kubectl delete -f ~/k8s/services/dashboard/dashboard.yml >/dev/null 2>&1
   microk8s kubectl delete -f ~/k8s/services/dashboard/dashboard-admin.yaml >/dev/null 2>&1
   microk8s kubectl delete -f ~/k8s/services/dashboard/dashboard-read-only.yaml >/dev/null 2>&1
   echo $msgstopped
   ;;

status)
   found=`microk8s kubectl get serviceaccount admin-user -n kubernetes-dashboard 2>/dev/null`
   if [[ $count = 0 ]] || [[ $found = "" ]]; then
      showtoken=0
      echo $msgstopped
   else
      found=`microk8s kubectl get clusterrolebinding admin-user -n kubernetes-dashboard 2>/dev/null`
      if [[ $found = "" ]]; then
         nopermission=" but user has no permissions."
         echo $msgstarted$nopermission
         echo 'Run "dashboard start" to fix it.'
      else
         echo $msgstarted
      fi
   fi
   ;;
esac

# Show full command line # ps -wfC "$cmd"
if [ $showtoken -gt 0 ]; then
   # Show token
   echo "Admin token:"
   microk8s kubectl get secret -n kubernetes-dashboard $(microk8s kubectl get serviceaccount admin-user -n kubernetes-dashboard -o jsonpath="{.secrets[0].name}") -o jsonpath="{.data.token}" | base64 --decode
   echo

   echo "User read-only token:"
   microk8s kubectl get secret -n kubernetes-dashboard $(microk8s kubectl get serviceaccount read-only-user -n kubernetes-dashboard -o jsonpath="{.secrets[0].name}") -o jsonpath="{.data.token}" | base64 --decode
   echo
fi

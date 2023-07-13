BOLD=$(tput bold)
GREEN='\033[0;32m'

microk8s kubectl kustomize ./ > k8s.yml

microk8s kubectl apply -f k8s.yml --dry-run=client

echo "\n"

read -p "${BOLD}Apply k8s config? (y/n)?" CONT
if [ "$CONT" = "y" ]; then
      echo -e "${GREEN}\n"
      microk8s kubectl apply -f k8s.yml
  else
      echo "exiting";
fi



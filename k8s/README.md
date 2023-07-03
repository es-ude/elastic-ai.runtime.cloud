# Elastic AI Runtime

This repository is mainly created for Wiki and Issues

## Kubernetes

This section explains how Kubernetes (k8s) can be used, to deploy and use the _Digital-Twin-Orchestration_. Please be aware, that this section does not explain how Kubernetes works in general. For that, please take a look [here](https://kubernetes.io/de/docs/concepts/overview/what-is-kubernetes/). 

The Digital Twin Orchestration contains the following services:
- Neo4J-Database: stores the mappings which describe the message flow
- URI Resolver: reads/writes mappings in the database / handles message flow
- R2F-Gateway: required for communication with the ElasticNode v4
- MQTT-Broker: required for communication between different twins and the resolver via MQTT
- (at least one) Digital Twin: digital representation of a physical device

Except the MQTT-Broker, at least one container image for each of the services above is stored in the [container-registry](https://git.uni-due.de/embedded-systems/artificial-intelligence/elastic-ai-runtime/container-images/container_registry) in the Digital-Twin-Orchestration group in GitLab. The MQTT-Broker container image is pulled from [Dockerhub](https://hub.docker.com/_/eclipse-mosquitto).

### Configuration files

The configuration files are located in the folder `services/<service-name>`. Configuration files differ between `deployment.yml` for stateless applications, `stateful_set.yml` for stateful applications, and `service.yml` for allowing access to applications inside containers via port mappings.

#### Stateless Applications

Each service, except the Neo4J-Database is a stateless application, which don't require persistent data storage. Stateless applications are deployed via a deployment file. This file contains attributes that specify how the container will be deployed, as well as the image name and the tag. Note that the developer needs to handle registry authentication, depending from which container-registry the image is pulled.

#### Stateful Applications

Stateful applications need to handle persistent data storage, and should be consistent with read/write access. This is handled by using _stateful sets_. The configuration file of a stateful set is similar to a deployment file, but allows to use Volume-Claim-Templates. A volume claim template requires an existing storage class, from which it can request the desired amount of storage. Each replica will therefore also require it's own storage. To avoid read/write inconsitencies, only on replica is allowed to write to the storage, while all replicas are allowed to read from the storage. Due to the persistent data storage, stateful sets are more complicated than deployments. More information about stateful sets can be found [here](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/)

#### Services

Services that offer access to a web interface also require a running service. This service is specified in the `service.yml` file in the respective service folder. This service file is linked via the _spec.selector.app_ attribute to the deployment or stateful set. In addition to that, it handles port mappings for accessing the web interface from the outside of the pod. This can be seen in `services/database/service.yml`. 

#### Kustomization

[Kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/) manages multiple k8s configuration files. It requires a `kustomization.yml` file, in which all files are listed, that are required for the deployment.

### Dashboard

Kubernetes comes with a dashboard that can be started via the shell script `services/k8s-dashboard.sh`. This applies a configuration to set up the dashboard and applies a proxy to access it. Please note, that the flag `--accept-hosts='.*'` is insecure and should not be used in production.

### Deployment

- requires: working (and running) installation of kubernetes, which is in our case micro-k8s

In order to validate the configuration files and start the deployment, we created the shell script `kube_test.sh` which will be explained below.

```bash
BOLD=$(tput bold)
GREEN='\033[0;32m'
```
handles colored and bold ouput

```bash
microk8s kubectl kustomize ./ > k8s.yml
```
Uses the `kustomization.yml` file to merge all configuration files into one _yaml_ file, lets call it _master configuration file_. 

```bash
microk8s kubectl apply -f k8s.yml --dry-run=client
```
Does a dry-run, which validates the content of the previously created master configuration file and gives the user a reasonable result message in both cases.

```bash
read -p "${BOLD}Apply k8s config? (y/n)?" CONT
if [ "$CONT" = "y" ]; then
      echo -e "${GREEN}\n"
      microk8s kubectl apply -f k8s.yml
  else
      echo "exiting";
fi
```
After the dry-run, the user is asked wheter the configuration should actually be deployed or not. If that is not the case, the script will exit. In case it should be deployed, the command only differs from the dry-run above, with the missing _--dry-run_ flag. The user the receives output from `kubectl` that states which containers are created or changed.  


### Troubleshooting

#### Find out what is running

```microk8s kubectl get <pods/deployments/services/statefulsets>```

#### Restarting a pod

```microk8s kubectl rollout restart deployment <deployment-name>```

#### Get log output

```microk8s kubectl logs -f <deployment-name>```
- `-f`: enables "following", which allows consecutive log output without the need to reuse the command.

#### Use StdIn in a Twin

```microk8s kubectl -i -t attach <deployment-name> -c <container-name>```
- `-i`: enables stdin
- `-t`: enables tty
- `-c`: specifies container name

**IMPORTANT**: attaching to a deployment should only be done via a `screen` or `tmux` session, since detaching via `Ctrl-Q` will most likely not work, and kubectl does not allow to specify a custom escape sequence.
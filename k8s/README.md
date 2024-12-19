## Kubernetes

This section explains how Kubernetes (k8s) can be used, to deploy and use the _elastic-AI.runtime.cloud_. Please be aware that this section does not explain how Kubernetes works in general. For that, please take a look [here](https://kubernetes.io/de/docs/concepts/overview/what-is-kubernetes/).

The elastic-ai.runtime.cloud contains the following services:

- elastic-ai.runtime: main application logic that also contains Digital Twins
  - (at least one) Digital Twin: digital representation of a physical device
- elastic-ai.monitor: Monitors Digital Twins throughout execution
- MQTT-Broker: required for communication between different twins and the resolver via MQTT
- elastic-ai.runtime.enV5: application containing various libraries for sensors/actuators of the Elastic Node v5

Except the MQTT-Broker, at least one container image for each of the services above is stored in the [container-registry](https://github.com/orgs/es-ude/packages?repo_name=elastic-ai.runtime). The MQTT-Broker container image is pulled from [Dockerhub](https://hub.docker.com/r/hivemq/hivemq-ce).

### Configuration files

The configuration files are located in the folder `services/<service-name>`. Configuration files differ between `deployment.yml` for stateless applications, `stateful_set.yml` for stateful applications, and `service.yml` for allowing access to applications inside containers via port mappings. Currently, there are no stateful applications. This was required for older runtime versions. However, we left the files here, in case this is helpful in the future.

#### Stateless Applications

Stateless applications are deployed via a deployment file. This file contains attributes that specify how the container will be deployed, as well as the image name and the tag. Note that the developer needs to handle registry authentication, depending from which container-registry the image is pulled.

#### Stateful Applications

\[OPTIONAL\]Stateful applications need to handle persistent data storage, and should be consistent with read/write access. This is handled by using _stateful sets_. The configuration file of a stateful set is similar to a deployment file, but allows to use Volume-Claim-Templates. A volume claim template requires an existing storage class, from which it can request the desired amount of storage. Each replica will therefore also require it's own storage. To avoid read/write inconsitencies, only on replica is allowed to write to the storage, while all replicas are allowed to read from the storage. Due to the persistent data storage, stateful sets are more complicated than deployments. More information about stateful sets can be found [here](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/)

#### Services

Services that offer access to a web interface also require a running service. This service is specified in the `service.yml` file in the respective service folder. This service file is linked via the _spec.selector.app_ attribute to the deployment or stateful set. In addition to that, it handles port mappings for accessing the web interface from the outside of the pod. This can be seen in `services/monitoring_service/service.yml`. Regarding port mapping: Kubernets does not allow a mapping of ports below 30000. Therefore it is not possible to i.e. map port 8080 to port 8080.

##### Port Mappings

| Name                 | Regular Port | K8s Port |
| -------------------- | ------------ | -------- |
| Monitoring Service   | 8081         | 30134    |
| HiveMQ (MQTT Broker) | 1883         | 30135    |

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

After the dry-run, the user is asked whether the configuration should actually be deployed or not. If that is not the case, the script will exit. In case it should be deployed, the command only differs from the dry-run above, with the missing _--dry-run_ flag. The user the receives output from `kubectl` that states which containers are created or changed.

### Known Issues

- Failed to restart if FPGA flashing was not successful:
  The twin drops all messages to enV5 that do not contain chunks of bitfiles. If the enV5 crashes during flashing, the twin does not receive the response that the flashing is done and therefore indefinitely blocks requests to the device

- Release Target of enV5 is the only target that reliable works thorugh a longer time
  The Debug Output of the Debug target causes the enV5 to crash under conditions that we are unable to reliably replicate. This also causes the monitor to stuck at a certain point.

### Troubleshooting

#### Stop complete deployment

In this case, deployment means all files, not a specific k8s deployment.

```bash
microk8s kubectl delete -f k8s.yml
```

#### Find out what is running

```bash
microk8s kubectl get <pods/deployments/services/statefulsets>
```

#### Restarting a pod

```bash
microk8s kubectl rollout restart deployment <deployment-name>
```

#### Get log output

```bash
microk8s kubectl logs -f <deployment-name>
```

- `-f`: enables "following", which allows consecutive log output without the need to reuse the command.

#### Use StdIn in a Twin

```bash
microk8s kubectl -i -t attach <deployment-name> -c <container-name>
```

- `-i`: enables stdin
- `-t`: enables tty
- `-c`: specifies container name

**IMPORTANT**: attaching to a deployment should only be done via a `screen` or `tmux` session, since detaching via `Ctrl-Q` will most likely not work, and kubectl does not allow to specify a custom escape sequence.

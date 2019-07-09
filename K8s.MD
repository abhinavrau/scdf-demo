## Overview

This section describes how to setup and run the demo using Spring Cloud Data Flow for Kubernetes.  

## Install SCDF on K8s

This section describes one possible way to setup SCDF for K8s.  You may need to tweak some settings
for your own environment.  Please refer to the [official docs for SCDF on K8S](https://dataflow.spring.io/docs/installation/kubernetes/)
for additional settings that can be applied.

### Install Helm

Use the following command to install Helm onto your K8s cluster if it's not already present: 
 
```bash
helm init
```

Update the helm repo:

```bash
helm repo update
```

### Install SCDF using Helm

```bash
helm install --name my-release stable/spring-cloud-data-flow --set kafka.enabled=true,rabbitmq.enabled=false,server.imagePullPolicy=Always,skipper.imagePullPolicy=Always
```

It will take a while for the deployment to complete.  Check in every few minutes to see if all pods are
ready.  The results of `kubectl get svc --namespace <your-namespace-here> my-release-data-flow-server -o jsonpath='{.status.loadBalancer.ingress[0].ip}'` should show you the IP
address to use for your SCDF server.

## Import the samples

Use the [official documentation](https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#shell) for running the 
SCDF shell and to connect to the server you just started.

Once in the shell, use the following commands to import the samples:

```bash
app import --uri https://dataflow.spring.io/kafka-docker-latest
app import --uri https://dataflow.spring.io/task-docker-latest
``` 

## Build and Publish the Task Apps to DockerHub (or other registry) 

### Tell the Project Your Docker Hub Id

Copy the `scdf.properties.sample` file in the root of the project to `scdf.properties`.  Place your Docker Hub Id as the
value of the `docker.image.prefix` property, replacing the sample value that's there.

### DB Delete

From the directory `db-delete`, run the following:

```bash
$ mvn clean install jib:build -Dimage=<your-docker-hub-id>/db-delete
```

Your Docker image should be build and pushed out to DockerHub

### DB Lowercase

From the directory `db-lowercase`, run the following:

```bash
$ mvn clean install jib:build -Dimage=<your-docker-hub-id>/db-lowercase
```

Your Docker image should be build and pushed out to DockerHub

### DB Reverse

From the directory `db-reverse`, run the following:

```bash
$ mvn clean install jib:build -Dimage=<your-docker-hub-id>/db-reverse
```

Your Docker image should be build and pushed out to DockerHub

### DB Uppercase

From the directory `db-uppercase`, run the following:

```bash
$ mvn clean install jib:build -Dimage=<your-docker-hub-id>/db-uppercase
```

Your Docker image should be build and pushed out to DockerHub
               
### File Ingest

From the directory `file-ingest`, run the following:

```bash
$ mvn clean install jib:build -Dimage=<your-docker-hub-id>/file-ingest
```

Your Docker image should be build and pushed out to DockerHub
          
### Remote Partitioned Batch Job

From the directory `partitioned-batch-job`, run the following:

```bash
$ mvn clean install jib:build -Dimage=<your-docker-hub-id>/partitioned-batch-job
```

Your Docker image should be build and pushed out to DockerHub

## Import Task Apps into SCDF

In the SCDF shell you launched previously, run the following commands:

```bash
app register --name FileIngestApp --type task --uri docker:<your-docker-hub-id>/ingest
app register --name UppercaseApp --type task --uri docker:<your-docker-hub-id>/db-uppercase
app register --name ReverseApp --type task --uri docker:<your-docker-hub-id>/db-reverse
app register --name LowercaseApp --type task --uri docker:<your-docker-hub-id>/db-lowercase
app register --name DeleteApp --type task --uri docker:<your-docker-hub-id>/db-delete
app register --name PartitionedBatchApp --type task --uri docker:<your-docker-hub-id>/partitioned-batch-job
``` 

## Run the TASK Demos

See the `task create` and `task launch` commands on the [task demo doc](TASK_DEMO.md). 

## Run the Remote Partitioned Batch Sample

In the SCDF shell you launched previously, run the following commands:

```bash
task create PartitionedBatchTask --definition "PartitionedBatchApp"
task launch PartitionedBatchTask --arguments "--increment-instance-enabled=true --spring.profiles.active=master" --properties deployer.PartitionedBatchApp.kubernetes.imagePullPolicy=Always,deployer.PartitionedBatchApp.kubernetes.deploymentServiceAccountName=my-release-data-flow
```

You should be able to use the SCDF dashboard to get the id of the pod started for this job, which you can use to view the logs.

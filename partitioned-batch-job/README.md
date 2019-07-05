# Partitioned Job using K8s

An example on how to remote partitioning a Spring Batch job using SCDF on K8s

## Requirements:

- Java 8 or Above


## Build:

```bash
mvn compile jib:dockerBuild

```
This will register the task application as a docker container in your local repo

## Execute:

- Import the App in SCDF with name `docker:abhinavrau/partitioned-batch-job:latest`
- Create a Task using this application
- Run the task with arguments "--spring.profiles.active=master" and "--increment-instance-enabled=true"

## Composed Task Runner

To use the Composed Tasks, you will need to register the dockerized version of the composed task application by 
bulk importing them by using the URL: https://dataflow.spring.io/task-docker-latest
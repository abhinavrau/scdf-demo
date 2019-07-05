#!/usr/bin/env bash
mvn compile jib:dockerBuild \
    -Djib.container.environment=spring.profiles.active="worker",spring.cloud.task.initialize.enable="false",spring.batch.initializer.enabled="false",spring.cloud.deployer.kubernetes.entryPointStyle="boot" \
    -Dimage=abhinavrau/partitioned-batch-job-worker

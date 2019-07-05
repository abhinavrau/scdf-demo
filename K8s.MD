# Install SCDF on K8


```bash
helm init
```

```bash
helm repo update
```


```bash
helm install --name my-release2 stable/spring-cloud-data-flow --set kafka.enabled=true,rabbitmq.enabled=false,server.version=2.2.0.BUILD-SNAPSHOT
```
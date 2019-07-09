## Overview

This section describes how to setup and run the demo using Spring Cloud Data Flow Local -- this is only for use
on development machines and won't work for production usage.  All streams and tasks orchestrated by SCDF will be 
run as java processes shelled out from the running SCDF process.

## Requirements
- Docker for Desktop - Set Memory >= 8GB and CPU >=4 in Preferences
- Java 1.8+
- Maven 3.6+
- Database tool to query MySQL to verify results.

## Setup 

Install Docker for your desktop (engine version 18.09.2 or higher)


### Download the code
#### Mac & Linux
```bash
git clone https://github.com/Pivotal-Field-Engineering/spring-cloud-dataflow-samples.git
```

#### Windows
On Windows you might see an error like "filename too long". To fix this, run
```bash
git clone -c core.longpaths=true https://github.com/Pivotal-Field-Engineering/spring-cloud-dataflow-samples.git
```

If you don't have git or git cloning is blocked, try downloading the zip file of the code.

### Start SCDF
From the directory where the docker-compose.yml is saved, run:

#### Mac & Linux
```bash
cd spring-cloud-dataflow-samples/batch
export DATAFLOW_VERSION=2.1.0.RELEASE
export SKIPPER_VERSION=2.0.2.RELEASE
docker-compose up
```
#### Windows
```bash
cd spring-cloud-dataflow-samples/batch
set DATAFLOW_VERSION=2.1.0.RELEASE
set SKIPPER_VERSION=2.0.2.RELEASE
docker-compose up
```

Open http://localhost:9393/dashboard to see the SCDF UI.

### Help - I don't see any Apps in my SCDF UI! 
If your Apps list is empty, then your company firewall is blocking the repo where the starter apps are being downloaded from.

- Download the `scdf-tool` from https://github.com/spring-cloud-stream-app-starters/scdf-app-tool/releases and unzip the file.

- Most likely your company requires a proxy server to reach the internet. The `scdf-tool` requires internet access to download apps. 
Open the file `scdf-app-tool` on Liunx/Mac or `scdf-app-tool.bat` on Windows and put in 
the proxy server details to the java command as 

```bash
java -Dhttps.proxyHost=<proxyAddress> -Dhttps.proxyPort=<port> -jar ...
```

Then run the `scdf-tool' according the directions here: https://github.com/spring-cloud-stream-app-starters/scdf-app-tool

This will build a Spring Boot app that you can use to register the starter apps to your local SCDF.

#### Register the Starter Apps in SCDF

Run the Spring Boot app that `scdf-tool` created. 

```bash
java -jar target/scdf-app-repo-0.0.1-SNAPSHOT.jar
```

This will server up the apps locally that can be used to bulk import starter apps from the SCDF UI.
Navigate to Apps and click on "Add Applications" and select the "Bulk Import Application coordinates from an HTTP URI location"
option. For URI field put in http://host.docker.internal:8080/import

## Connect to MySQL

Connect your favorite MySQL viewer to port 33061 on localhost. **username:**root **password:**rootpw


# Spring Cloud Stream Demo
see [STREAM_DEMO.md](STREAM_DEMO.md)

# Spring Cloud Task Demo

see [TASK_DEMO.md](TASK_DEMO.md)

# Use Spring Cloud Stream to kick off a Spring Cloud Task Demo
see [STREAM_TASK_DEMO.md](STREAM_TASK_DEMO.md)

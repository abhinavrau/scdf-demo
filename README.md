# Spring Batch Demo for Spring Cloud Data Flow

The projects here contain Spring Batch based applications used to demo [Composed Task](https://dataflow.spring.io/docs/batch-developer-guides/batch/data-flow-composed-task/) and 
Streaming use cases as well as a combination of them

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

## Custom Spring Batch Demo Applications

The repo contains the following modules.

- **core:** Contains common code used by the rest of the applications
- **file-ingest:** Spring batch application that reads first name and last name from a given csv file as `localFilePath` parameter and write to the database table called `Demo_FileImport`. 
- **db-uppercase:** Spring batch application that reads first name and last name from table `Demo_FileImport` table and convert it to uppercase and stores it in table `Demo_Case`
- **db-lowercase:** Spring batch application that reads first name and last name from table `Demo_Case` table and convert it to lowercase and stores it in table `Demo_Case`
- **db-reverse:** Spring batch application that reads first name and last name from table `Demo_Case` table and reverses the names and stores it in table `Demo_Reverse`
- **db-delete:** Spring batch application that reads first name and last name from table `Demo_Reverse` table deletes the row

## Build

```bash
mvn clean package

```

## Register the Spring Batch Jobs

Copy the jars to a common directory where SCDF can load them from.

#### Mac & Linux
```bash
./copyTasks.sh 
```

#### Windows
```bash
copyTasks.bat 
```

Register the apps in SCDF

### SCDF Shell

We will use the SCDF shell to install our applications. To run:

#### Mac & Linux
```bash
./registerApps.sh 
```

#### Windows
```bash
registerApps.bat 
```

## Simple File Ingest

### Register the file-ingest app with SCDF

Run the SCDF shell
```bash
docker exec -it dataflow-server java -jar shell.jar
```

### Create task
```bash
task create ImportTask --definition "Import: FileIngestApp"
```

### Run the task
```bash
task launch ImportTask --arguments "--increment-instance-enabled=true"
```

## Uppercase Task after ImportTask

### Create task
```bash
task create UppercaseTask --definition "ImportFile: FileIngestApp && Uppercase: UppercaseApp"
```

### Run the task
```bash
task launch UppercaseTask --arguments "--increment-instance-enabled=true"
```

## Composed Task that demos Distributed Saga Pattern
We will now create a task flow that implements a simple batch Distributed Saga pattern using the above 2 batch applications.

The flow imports a file, converts to UPPERCASE and if that succeeds, it will REVERSE the names. If there is a failure, it will 
convert it back to LOWERCASE undoing the UPPERCASE operation.
   
In this example we only want to undo the UPPERCASE so we are only doing a Compensting Request for that operation.

### Happy Path  
#### Create the Composed Task 
```bash
task create SagaHappyPath --definition "Import: FileIngestApp && Uppercase: UppercaseApp 'COMPLETED'->Reverse: ReverseApp '*'->Lowercase: LowercaseApp"
```

This will create a composed task that looks like the following when created using the SCDF UI:
![alt text](ComposedFlow.png)

#### Run the task
```bash
task launch SagaHappyPath --arguments "--increment-instance-enabled=true"
```

### Business Failure Path  
#### Create the Composed Task 
Demo failure of Uppercase which will cause Lowercase to run and skip reverse
```bash
task create SagaBusFail1 --definition "Import: FileIngestApp --localFilePath=classpath:bf-names.csv && Uppercase: UppercaseApp 'COMPLETED'->Reverse: ReverseApp '*'->Lowercase: LowercaseApp"
```

#### Run the task
```bash
task launch SagaBusFail1 --arguments "--increment-instance-enabled=true"
```


# Resources

Helpful resources to get some background on this work

##Distributed Sagas

[Distributed Sagas: A Protocol for Coordinating Microservices](https://youtu.be/0UTOLRTwOX0) - Caitie McCaffrey 


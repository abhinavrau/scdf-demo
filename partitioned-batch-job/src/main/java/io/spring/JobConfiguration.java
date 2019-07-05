/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.deployer.resource.docker.DockerResource;
import org.springframework.cloud.deployer.resource.docker.DockerResourceLoader;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.task.batch.partition.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

/**
 * @author Michael Minella
 */
@Configuration
public class JobConfiguration {

	Logger logger = LoggerFactory.getLogger(JobConfiguration.class);

	private static final int GRID_SIZE = 4;
	// @checkstyle:off
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	@Autowired
	public DataSource dataSource;
	@Autowired
	public JobRepository jobRepository;
	// @checkstyle:on
	@Autowired
	private ConfigurableApplicationContext context;
	@Autowired
	private DelegatingResourceLoader resourceLoader;
	@Autowired
	private Environment environment;

	@Bean
	public PartitionHandler partitionHandler(Environment env, @Value("${spring.profiles.active}") String activeProfile,
											 TaskLauncher taskLauncher,
											 JobExplorer jobExplorer) throws Exception {

		//Resource resource = this.resourceLoader.getResource("maven://com.example:partitioned-batch-job:0.0.1-SNAPSHOT");
		Resource resource = new DockerResourceLoader().getResource("docker:abhinavrau/partitioned-batch-job:latest");

		DeployerPartitionHandler partitionHandler =
				new DeployerPartitionHandler(taskLauncher, jobExplorer, resource, "workerStep");

		List<String> commandLineArgs = new ArrayList<>(3);
		commandLineArgs.add("--spring.profiles.active=worker");
		commandLineArgs.add("--spring.cloud.task.initialize.enable=false");
		commandLineArgs.add("--spring.batch.initializer.enabled=false");
		commandLineArgs.add("--spring.cloud.deployer.kubernetes.entryPointStyle=boot");

		// The below should be moved to an EnvironmentVariablesProvider once this works
		commandLineArgs.add("--spring.datasource.url=" + env.getProperty("spring.datasource.url"));
		commandLineArgs.add("--spring.datasource.username=" + env.getProperty("spring.datasource.username"));
		commandLineArgs.add("--spring.cloud.task.name=" + env.getProperty("spring.cloud.task.name"));
		commandLineArgs.add("--spring.datasource.driverClassName=" + env.getProperty("spring.datasource.driverClassName"));
		commandLineArgs.add("--spring.datasource.password=" + env.getProperty("spring.datasource.password"));
		commandLineArgs.add("--increment-instance-enabled=" + env.getProperty("increment-instance-enabled"));
		commandLineArgs.add("--spring.cloud.data.flow.platformname=" + env.getProperty("spring.cloud.data.flow.platformname"));

		partitionHandler.setCommandLineArgsProvider(new PassThroughCommandLineArgsProvider(commandLineArgs));
		logger.info("Env Vars is: " + this.environment.toString());
		//partitionHandler.setEnvironmentVariablesProvider(new SimpleEnvironmentVariablesProvider(this.environment));
		partitionHandler.setEnvironmentVariablesProvider(new NoOpEnvironmentVariablesProvider());
		//partitionHandler.setDefaultArgsAsEnvironmentVars(true);
		partitionHandler.setMaxWorkers(1);
		partitionHandler.setApplicationName("PartitionedBatchJobTask");

		return partitionHandler;
	}

	@Bean
	public Partitioner partitioner() {
		return new Partitioner() {
			@Override
			public Map<String, ExecutionContext> partition(int gridSize) {

				Map<String, ExecutionContext> partitions = new HashMap<>(gridSize);

				for (int i = 0; i < GRID_SIZE; i++) {
					ExecutionContext context1 = new ExecutionContext();
					context1.put("partitionNumber", i);

					partitions.put("partition" + i, context1);
				}

				return partitions;
			}
		};
	}

	@Bean
	@Profile("worker")
	public DeployerStepExecutionHandler stepExecutionHandler(JobExplorer jobExplorer) {
		return new DeployerStepExecutionHandler(this.context, jobExplorer, this.jobRepository);
	}

	@Bean
	@StepScope
	public Tasklet workerTasklet(
		final @Value("#{stepExecutionContext['partitionNumber']}") Integer partitionNumber) {

		return new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("This tasklet ran partition: " + partitionNumber);

				return RepeatStatus.FINISHED;
			}
		};
	}

	@Bean
	public Step step1(PartitionHandler partitionHandler) throws Exception {
		return this.stepBuilderFactory.get("step1")
			.partitioner(workerStep().getName(), partitioner())
			.step(workerStep())
			.partitionHandler(partitionHandler)
			.build();
	}

	@Bean
	public Step workerStep() {
		return this.stepBuilderFactory.get("workerStep")
			.tasklet(workerTasklet(null))
			.build();
	}

	@Bean
	@Profile("!worker")
	public Job partitionedJob(PartitionHandler partitionHandler) throws Exception {
		Random random = new Random();
		return this.jobBuilderFactory.get("partitionedJob" + random.nextInt())
			.start(step1(partitionHandler))
			.build();
	}
}

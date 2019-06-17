/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.cloud.dataflow.batch.ingest.config;


import com.google.common.base.Strings;
import io.spring.cloud.dataflow.batch.domain.Person;
import io.spring.cloud.dataflow.batch.ingest.mapper.fieldset.PersonFieldSetMapper;
import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;


/**
 * Class used to configure the batch job related beans.
 *
 * @author Chris Schaefer
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties({BatchProperty.class})
@EnableBatchProcessing
public class BatchConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchConfiguration.class);

    private final DataSource dataSource;

    private final ResourceLoader resourceLoader;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;


    @Autowired
    private BatchProperty batchProperty;

    @Autowired
    public BatchConfiguration(final DataSource dataSource, final JobBuilderFactory jobBuilderFactory,
                              final StepBuilderFactory stepBuilderFactory,
                              final ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }


    @Bean
    @StepScope
    public ItemStreamReader<Person> fileReader(@Value("#{jobParameters['localFilePath']}") String filePath) {

        if (Strings.isNullOrEmpty(filePath)) {
            filePath = batchProperty.getLocalFilePath();
        }

        if (!filePath.matches("[a-z]+:.*")) {
            filePath = "file:" + filePath;
        }


        return new FlatFileItemReaderBuilder<Person>()
                .name("fileReader")
                .resource(resourceLoader.getResource(filePath))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new PersonFieldSetMapper())
                .build();
    }


    @Bean(name="filePersonProcessor")
    @StepScope
    public ItemProcessor<Person, Person> processor(@Value("#{jobParameters['action']}") String action) {

        PersonItemProcessor processor = new PersonItemProcessor();
        processor.setStringAction(action);
        return processor;
    }


    @Bean
    public ItemWriter<Person> databaseInserterWriter() {
        return new JdbcBatchItemWriterBuilder<Person>()
                .beanMapped()
                .dataSource(this.dataSource)
                .sql("INSERT INTO Demo_FileImport (first_name, last_name) VALUES (:firstName, :lastName)")
                .build();
    }

    @Bean
    public Job fileIngestJob() {

        return jobBuilderFactory.get("fileIngestJob")
                .incrementer(new RunIdIncrementer())
                .flow(stepFile())
                .end()
                .build();
    }



    @Bean
    public Step stepFile() {
        return stepBuilderFactory.get("ingestFile")
                .<Person, Person>chunk(10)
                .reader(fileReader(null))
                .processor(processor(null))
                .writer(databaseInserterWriter())
                .build();
    }

}

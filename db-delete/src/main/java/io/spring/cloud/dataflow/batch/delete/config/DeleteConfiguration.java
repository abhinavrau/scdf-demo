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

package io.spring.cloud.dataflow.batch.delete.config;


import io.spring.cloud.dataflow.batch.domain.Person;
import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;
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
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;


/**
 * Class used to configure the batch job related beans.
 *
 * @author Chris Schaefer
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties({ReverseBatchProperty.class})
@EnableBatchProcessing
public class DeleteConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteConfiguration.class);

    private final DataSource dataSource;

    private final ResourceLoader resourceLoader;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ReverseBatchProperty batchProperty;

    @Autowired
    public DeleteConfiguration(final DataSource dataSource, final JobBuilderFactory jobBuilderFactory,
                               final StepBuilderFactory stepBuilderFactory,
                               final ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        
    }


    @Bean(name="dbPersonProcessor")
    @StepScope
    public ItemProcessor<Person, Person> processor() {

        PersonItemProcessor processor = new PersonItemProcessor();
        return processor;
    }


    @Bean
    @StepScope
    public ItemStreamReader<Person> databaseReader() {


        return new JdbcCursorItemReaderBuilder<Person>()
                .name("databaseReader")
                .beanRowMapper(Person.class)
                .dataSource(this.dataSource)
                .sql("SELECT person_id as id, first_name, last_name FROM Demo_Reverse")
                .build();
    }


    @Bean
    public ItemWriter<Person> databaseDeleterWriter() {
        return new JdbcBatchItemWriterBuilder<Person>()
                .beanMapped()
                .dataSource(this.dataSource)
                .sql("DELETE FROM Demo_Reverse where person_id = :id")
                .build();
    }


    @Bean
    public Job deleteNamesJob() {
        return jobBuilderFactory.get("deleteJob")
                .incrementer(new RunIdIncrementer())
                .flow(stepDatabase())
                .end()
                .build();

    }


    @Bean
    public Step stepDatabase() {
        return  stepBuilderFactory.get("deleteNames")
                .<Person, Person>chunk(10)
                .reader(databaseReader())
                .processor(processor())
                .writer(databaseDeleterWriter())
                .build();

    }
}

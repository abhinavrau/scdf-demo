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

package io.spring.cloud.dataflow.batch.ingest;

import java.util.List;
import java.util.Map;


import io.spring.cloud.dataflow.batch.ingest.config.*;
import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * BatchConfiguration test cases
 *
 * @author Chris Schaefer
 * @author David Turanski
 */

@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BatchConfiguration.class})
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class BatchApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void clearMetadata() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @After
    public void cleanup() {
        jdbcTemplate.execute("delete from Demo_FileImport");
    }

    @Test
    public void testBatchFileFail() throws Exception {


        BatchStatus status = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addString(
                "localFilePath", "classpath:missing-data.csv").toJobParameters()).getStatus();
        assertEquals("Incorrect batch status", BatchStatus.FAILED, status);
    }

    @Test
    public void testBatchDataFileInjest() throws Exception {

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("localFilePath", "classpath:data.csv")
                        .toJobParameters());

        CheckJobResult(jobExecution, PersonItemProcessor.Action.NONE);

    }

    @Test
    public void testBatchDataFileInjestUppercase() throws Exception {

        JobExecution jobExecution3 = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("localFilePath", "classpath:data.csv")
                        .addString("action", "UPPERCASE")
                        .toJobParameters());
        CheckJobResult(jobExecution3, PersonItemProcessor.Action.UPPERCASE);
    }

    @Test
    public void testBatchDataFileInjestBackwards() throws Exception {
        JobExecution jobExecution4 = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addString("localFilePath", "classpath:data.csv")
                        .addString("action", "REVERSE")
                        .toJobParameters());
        CheckJobResult(jobExecution4, PersonItemProcessor.Action.REVERSE);
    }

    @Test
    public void testBatchDataNoParams() throws Exception {

        // Use default parameters
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        CheckJobResult(jobExecution, PersonItemProcessor.Action.NONE);

    }


    private void CheckJobResult(JobExecution jobExecution, PersonItemProcessor.Action action) {
        assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());

        assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());

        CheckResultInDatabase(action);


    }

    private void CheckResultInDatabase(PersonItemProcessor.Action action) {
        List<Map<String, Object>> peopleList = jdbcTemplate.queryForList(
                "select first_name, last_name from Demo_FileImport");

        assertEquals("Incorrect number of results", 5, peopleList.size());

        for (Map<String, Object> person : peopleList) {
            assertNotNull("Received null person", person);

            String firstName = (String) person.get("first_name");
            String lastName = (String) person.get("last_name");
            String expectedFirsName = "John";
            String expectedLastName = "Doe";
            switch (action) {
                case NONE:
                    break;

                case UPPERCASE:
                    expectedFirsName = expectedFirsName.toUpperCase();
                    expectedLastName = expectedLastName.toUpperCase();
                    break;

                case REVERSE:
                    expectedFirsName = new StringBuilder(expectedFirsName).reverse().toString();
                    expectedLastName = new StringBuilder(expectedLastName).reverse().toString();
                    break;
            }
            assertEquals("Invalid first name: " + firstName, expectedFirsName, firstName);

            assertEquals("Invalid last name: " + lastName, expectedLastName, lastName);
        }
    }

}

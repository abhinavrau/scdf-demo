package io.spring.cloud.dataflow.batch.ingest;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.rule.OutputCapture;

import static org.springframework.test.util.AssertionErrors.assertTrue;


public class TaskBatchTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

   // @Test
    public void testTask() throws Exception {

        final String ACTION = "Action is: UPPERCASE";

        String[] args = {"action=UPPERCASE", "localFilePath=classpath:1-names.csv"};

        SpringApplication.run(Application.class, args);
        String output = this.outputCapture.toString();
        assertTrue("Unable to find: " + output,
                output.contains(ACTION));



    }

}

package io.spring.cloud.dataflow.batch.lowercase.saga;

import io.spring.cloud.dataflow.batch.lowercase.Application;
import org.junit.Rule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.annotation.DirtiesContext;

//@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SagaCompensatingTaskTest {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    //@Test
    public void testTask() throws Exception {

        final String ACTION = "Action is: UPPERCASE";

        String[] args = {"--spring.batch.job.names=sagaCompensatingRequestJob"};

        SpringApplication.run(Application.class, args);
        String output = this.outputCapture.toString();
//        assertTrue("Unable to find: " + output,
//                output.contains(ACTION));



    }

}

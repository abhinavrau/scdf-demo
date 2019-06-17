package io.spring.cloud.dataflow.batch.ingest.config;

import io.spring.cloud.dataflow.batch.processor.PersonItemProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties("file-ingest")
public class BatchProperty {


    /**
     * localFilePath to process. classpath:data.csv by default
     */
     String localFilePath = "classpath:data.csv";

    /**
     * Action to perform on the names. Values can be NONE, UPPERCASE,LOWERCASE, REVERSE.
     *  NONE is the default if no action is specified.
     */
    PersonItemProcessor.Action action = PersonItemProcessor.Action.NONE;


    public String getLocalFilePath() {
        Assert.hasText(localFilePath, "format must not be empty nor null");
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }


    public String getAction() {
        return action.name();
    }

    public void setAction(String action) {
        this.action = PersonItemProcessor.Action.valueOf(action);
    }

}

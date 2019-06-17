package io.spring.cloud.dataflow.batch.lowercase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("db-lowercase")
@Data
public class UppercaseBatchProperty {

    /**
     * source database table to read from
     */
    private String sourceTable = "Manager_1";

    /**
     * SagaAction to perform REQUEST is by default.
     *  Values can be REQUEST, - Do our work
     *  COMPENSATING_REQUEST - Semantically undoes the effect of a REQUEST
     */
    private String targetTable = "Manager_2";

}

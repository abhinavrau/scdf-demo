package io.spring.cloud.dataflow.batch.reverse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("db-reverse")
@Data
public class ReverseBatchProperty {

    /**
     * source database table to read from
     */
    private String sourceTable = "Manager_2";

    /**
     * SagaAction to perform REQUEST is by default.
     *  Values can be REQUEST, - Do our work
     *  COMPENSATING_REQUEST - Semantically undoes the effect of a REQUEST
     */
    private String targetTable = "Manager_3";

}

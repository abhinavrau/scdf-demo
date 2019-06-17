package io.spring.cloud.dataflow.batch.uppercase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("db-uppercase")
@Data
public class UppercaseBatchProperty {

    /**
     * source database table to read from
     */
    private String sourceTable = "Manager_1";

    /**
     * Target Table
     */
    private String targetTable = "Manager_2";

}

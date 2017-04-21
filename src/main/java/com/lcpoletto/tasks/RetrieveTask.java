package com.lcpoletto.tasks;

import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.PaginationLoadingStrategy;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.lcpoletto.tasks.model.Task;

/**
 * AWS Lambda function to list tasks based on input data.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class RetrieveTask {

    private static final Logger logger = Logger.getLogger(RetrieveTask.class);

    public List<Task> handleRequest(String input) {
        logger.debug("Listing tasks from persistence layer.");
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

        final DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withPaginationLoadingStrategy(PaginationLoadingStrategy.EAGER_LOADING).build();
        final DynamoDBMapper mapper = new DynamoDBMapper(client, mapperConfig);

        final List<Task> result = mapper.scan(Task.class, new DynamoDBScanExpression());
        logger.debug(String.format("Found %d tasks.", result.size()));
        return result;
    }
}
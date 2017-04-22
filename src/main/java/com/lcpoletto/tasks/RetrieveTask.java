package com.lcpoletto.tasks;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lcpoletto.Utils;
import com.lcpoletto.tasks.model.Task;

/**
 * AWS Lambda function to list tasks based on input data.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class RetrieveTask implements RequestHandler<String, String> {

    private static final Logger logger = Logger.getLogger(RetrieveTask.class);

    private DynamoDBMapper dynamoMapper;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public RetrieveTask() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    /**
     * Overloaded constructor which received the AWS dynamo db client. This
     * constructor was created mainly to make it easier to mock external
     * dependencies on unit tests.
     * 
     * @param dynamoClient
     *            dynamo db client to be used
     */
    public RetrieveTask(final AmazonDynamoDB dynamoClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * AWS Lambda entry point which will retrieve a list with all the tasks on
     * dynamo db.
     * 
     * @param input
     *            this input is ignored by the function
     * @param context
     *            aws lambda context
     * @return json representation of the sorted array of tasks
     */
    @Override
    public String handleRequest(final String input, final Context context) {
        return Utils.toJson(handleRequest());
    }

    /**
     * Actual function which will do all the fetching for the lambda and return
     * the sorted array, from here it's converted into json to be returned to
     * the user.
     * 
     * @return sorted task array
     */
    private Task[] handleRequest() {
        logger.debug("Listing tasks from persistence layer.");
        /*
         * After testing looks like if we use eager loading dynamo db takes way
         * more time to return the results, thus I'm iterating on results to
         * make sure I loaded all of them
         */
        final List<Task> paginatedTasks = dynamoMapper.scan(Task.class, new DynamoDBScanExpression());
        /*
         * using a linked list because we don't know the size of the results and
         * we won't do any type of sorting as of now, thus it will be more
         * efficient than resizing arrays on an ArrayList
         */
        final List<Task> unsortedTaskList = new LinkedList<>();
        if (paginatedTasks != null && !paginatedTasks.isEmpty()) {
            for (final Task task : paginatedTasks) {
                unsortedTaskList.add(task);
            }
        }
        logger.debug(String.format("Found %d tasks.", unsortedTaskList.size()));
        final Task[] result = unsortedTaskList.toArray(new Task[unsortedTaskList.size()]);
        Utils.mergeSort(result);
        return result;
    }
}
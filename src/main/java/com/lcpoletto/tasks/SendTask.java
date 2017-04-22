/**
 * 
 */
package com.lcpoletto.tasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.lcpoletto.Utils;
import com.lcpoletto.tasks.model.Task;

/**
 * <p>
 * AWS lambda function to send e-mails to users with uncompleted tasks.
 * </p>
 * 
 * @author Luis Carlos Poletto
 */
public class SendTask implements RequestHandler<String, String> {

    private static final Logger logger = Logger.getLogger(SendTask.class);
    private static final String SUCCESS = "SUCCESS";

    private DynamoDBMapper dynamoMapper;
    private AmazonSimpleEmailService emailClient;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public SendTask() {
        this(AmazonDynamoDBClientBuilder.defaultClient(), AmazonSimpleEmailServiceClientBuilder.defaultClient());
    }

    /**
     * Overloaded constructor which received the AWS dynamo db client and the
     * AWS SES client. This constructor was created mainly to make it easier to
     * mock external dependencies on unit tests.
     * 
     * @param dynamoClient
     *            dynamo db client to be used
     * @param emailClient
     *            SES client to be used
     */
    public SendTask(final AmazonDynamoDB dynamoClient, final AmazonSimpleEmailService emailClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
        this.emailClient = emailClient;
    }

    /**
     * AWS Lambda entry point which will search for uncompleted tasks and send
     * reminder e-mails to their owners.
     * 
     * @param input
     *            this input is ignored
     * @param context
     *            AWS lambda context
     * @return <code>SUCCESS</code> if no uncaught exception happens
     */
    @Override
    public String handleRequest(final String input, final Context context) {
        logger.debug("Sending tasks reminder to users.");
        final List<Task> allTasks = retrieveUncompletedTasks();

        if (allTasks != null && !allTasks.isEmpty()) {
            final Map<String, List<Task>> uncompleted = new HashMap<>();
            /*
             * Here we're generating a map with the user id and all the tasks
             * he/she's supposed to work on.
             */
            for (final Task task : allTasks) {
                List<Task> userTasks = uncompleted.get(task.getUser());
                if (userTasks == null) {
                    userTasks = new LinkedList<>();
                    uncompleted.put(task.getUser(), userTasks);
                }
                userTasks.add(task);
            }
            logger.debug(String.format("Sending notifications to %d users.", uncompleted.size()));
            /*
             * Now that we have a map with all tasks per user we can start
             * generating aggregated e-mails for each of them.
             */
            for (Map.Entry<String, List<Task>> userTasks : uncompleted.entrySet()) {
                // TODO: check for any error scenario and what we want to return
                sendMail(userTasks.getKey(), userTasks.getValue());
            }
        }
        return SUCCESS;
    }

    /**
     * Helper method which will send the e-mail using SES.
     * 
     * @param email
     *            destination
     * @param tasks
     *            list of uncompleted tasks
     */
    private SendEmailResult sendMail(final String email, List<Task> tasks) {
        logger.debug(String.format("Sending %d tasks to %s.", tasks.size(), email));
        final Destination destination = new Destination().withToAddresses(email);
        final Content subject = new Content().withData("Uncompleted tasks reminder");

        final StringBuilder text = new StringBuilder();
        text.append("This is a friendly reminder for your uncompleted tasks below: \n\n");
        for (final Task task : tasks) {
            text.append(String.format("Task: %s\nPriority: %d\n\n", task.getDescription(), task.getPriority()));
        }
        final Content textBody = new Content().withData(text.toString());
        final Body body = new Body().withText(textBody);
        final Message message = new Message().withSubject(subject).withBody(body);
        final SendEmailRequest request = new SendEmailRequest().withSource(Utils.getMailFrom())
                .withDestination(destination).withMessage(message);

        try {
            final SendEmailResult result = emailClient.sendEmail(request);
            logger.debug(
                    String.format("Message %s sent to %s with %d tasks.", result.getMessageId(), email, tasks.size()));
            return result;
        } catch (Exception e) {
            logger.error("Error sending e-mail.", e);
        }
        return null;
    }

    private List<Task> retrieveUncompletedTasks() {
        /*
         * After testing looks like if we use eager loading dynamo db takes way
         * more time to return the results, thus I'm iterating on results to
         * make sure I loaded all of them
         */
        final List<Task> paginatedTasks = dynamoMapper.scan(Task.class,
                new DynamoDBScanExpression().withFilterExpression("attribute_not_exists(completed)"));

        final List<Task> result = new LinkedList<>();
        if (paginatedTasks != null && !paginatedTasks.isEmpty()) {
            for (final Task task : paginatedTasks) {
                result.add(task);
            }
        }
        return result;
    }

}

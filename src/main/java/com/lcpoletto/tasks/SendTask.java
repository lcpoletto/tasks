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
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.lcpoletto.tasks.model.Task;

/**
 * <p>
 * AWS lambda function to send e-mails to users with uncompleted tasks.
 * </p>
 * 
 * @author Luis Carlos Poletto
 */
public class SendTask {

    private static final Logger logger = Logger.getLogger(SendTask.class);
    private static final String SUCCESS = "SUCCESS";
    private static final String DEFAULT_EMAIL_FROM = "noreply@tasks.com";

    public String handleRequest(String input) {
        logger.debug("Sending tasks reminder to users.");
        final List<Task> allTasks = retrieveAllTasks();

        if (allTasks != null && !allTasks.isEmpty()) {
            AmazonSimpleEmailService emailClient = AmazonSimpleEmailServiceClientBuilder.defaultClient();
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
                sendMail(userTasks.getKey(), userTasks.getValue(), emailClient);
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
    private SendEmailResult sendMail(final String email, List<Task> tasks, AmazonSimpleEmailService emailClient) {
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
        final SendEmailRequest request = new SendEmailRequest().withSource(getMailFrom()).withDestination(destination)
                .withMessage(message);

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

    /**
     * Helper method which will try to read configuration from environment
     * variables.
     * 
     * @return configured email source or default value
     */
    private String getMailFrom() {
        final String result = System.getenv("TASKS_MAIL_FROM");
        logger.trace(String.format("TASKS_MAIL_FROM: %s", result));
        if (result == null || result.isEmpty()) {
            return DEFAULT_EMAIL_FROM;
        }
        return result;
    }

    private List<Task> retrieveAllTasks() {
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        final DynamoDBMapper mapper = new DynamoDBMapper(client);

        /*
         * After testing looks like if we use eager loading dynamo db takes way
         * more time to return the results, thus I'm iterating on results to
         * make sure I loaded all of them
         */
        final List<Task> paginatedTasks = mapper.scan(Task.class,
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

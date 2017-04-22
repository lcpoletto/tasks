/**
 * 
 */
package com.lcpoletto.notes;

import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.model.OperationType;
import com.amazonaws.services.dynamodbv2.model.Record;
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

/**
 * AWS Lambda function that will react to a note update and send an e-mail to
 * the owner of the note if it was updated by another user.
 * 
 * This function will require the following policies:
 * arn:aws:iam::aws:policy/service-role/AWSLambdaDynamoDBExecutionRole
 * 
 * @author Luis Carlos Poletto
 *
 */
public class SendNoteUpdate implements RequestHandler<List<Record>, String> {

    private static final Logger logger = Logger.getLogger(SendNoteUpdate.class);

    private AmazonSimpleEmailService emailClient;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public SendNoteUpdate() {
        this(AmazonSimpleEmailServiceClientBuilder.defaultClient());
    }

    /**
     * Overloaded constructor which received the AWS SES client. This
     * constructor was created mainly to make it easier to mock external
     * dependencies on unit tests.
     * 
     * @param emailClient
     *            SES email client to be used
     */
    public SendNoteUpdate(final AmazonSimpleEmailService emailClient) {
        this.emailClient = emailClient;
    }

    /**
     * AWS Lambda entry point which will send an notification e-mail when there
     * is an update on the note by another user.
     * 
     * @param records
     *            events created from dynamo db streams
     * @param context
     *            aws lambda context
     * @return <code>SUCCESS</code> when processing goes well
     */
    @Override
    public String handleRequest(final List<Record> records, final Context context) {
        logger.debug(String.format("Received stream event: %s", records));
        if (records != null) {
            for (final Record record : records) {
                // even though the configuration we're going to do on the lambda
                // trigger is to only call this when there is an update, it
                // doesn't hurt to make sure we're responding to a modify event
                final OperationType operation = OperationType.fromValue(record.getEventName());
                if (operation == OperationType.MODIFY) {
                    final String updatedBy = record.getDynamodb().getNewImage().get("updatedBy").getS();
                    final String owner = record.getDynamodb().getNewImage().get("owner").getS();
                    final String content = record.getDynamodb().getNewImage().get("content").getS();

                    if (!updatedBy.equals(owner)) {
                        // TODO: verify email sending results?
                        sendMail(updatedBy, owner, content);
                    }
                }
            }
        }
        return "SUCCESS";
    }

    /**
     * Helper method which will create and send a notification email to the
     * owner of the note.
     * 
     * @param updatedBy
     *            user who updated the record
     * @param owner
     *            owner of the note
     * @param content
     *            newly updated content
     * @return email sending results
     */
    private SendEmailResult sendMail(final String updatedBy, final String owner, final String content) {
        logger.debug(String.format("Sending note update email to %s.", owner));

        final Destination destination = new Destination().withToAddresses(owner);
        final Content subject = new Content().withData("Updated note notification");

        final StringBuilder text = new StringBuilder();
        text.append("The note you've created has been updated by: ");
        text.append(updatedBy);
        text.append("\n\nNewly updated contents: ");
        text.append(content);
        final Content textBody = new Content().withData(text.toString());
        final Body body = new Body().withText(textBody);
        final Message message = new Message().withSubject(subject).withBody(body);
        final SendEmailRequest request = new SendEmailRequest().withSource(Utils.getMailFrom())
                .withDestination(destination).withMessage(message);

        try {
            final SendEmailResult result = emailClient.sendEmail(request);
            logger.debug(String.format("Update notification sent to %s.", owner));
            return result;
        } catch (Exception e) {
            logger.error("Error sending e-mail.", e);
        }
        return null;
    }
}
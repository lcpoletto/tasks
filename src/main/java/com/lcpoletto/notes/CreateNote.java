package com.lcpoletto.notes;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.notes.model.Note;

/**
 * AWS lambda function to insert a new Note into dynamo DB.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class CreateNote {

    private static final Logger logger = Logger.getLogger(CreateNote.class);

    private DynamoDBMapper dynamoMapper;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public CreateNote() {
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
    public CreateNote(final AmazonDynamoDB dynamoClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * AWS lambda entry point which will create a note into dynamo db.
     * 
     * @param input
     *            record to be inserted
     * @return the inserted record with generated id
     * @throws ValidationException
     *             if any input validation error happens
     */
    public Note handleRequest(final Note input) throws ValidationException {
        // TODO: Add input validation
        logger.debug(String.format("Inserting %s into persistence layer.", input));
        dynamoMapper.save(input);
        logger.debug(String.format("Inserted with success: %s", input));
        return input;
    }
}

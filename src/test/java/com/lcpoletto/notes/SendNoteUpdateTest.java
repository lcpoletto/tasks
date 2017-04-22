/**
 * 
 */
package com.lcpoletto.notes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.OperationType;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;

/**
 * Test fixture for {@link SendNoteUpdate}.
 * 
 * @author Luis Carlos Poletto
 */
@RunWith(MockitoJUnitRunner.class)
public class SendNoteUpdateTest {

    @Mock
    private AmazonSimpleEmailService emailClient;

    private SendNoteUpdate lambda;

    @Before
    public void setup() {
        lambda = new SendNoteUpdate(emailClient);
    }

    @Test
    public void testNullOrEmpty() {
        lambda.handleRequest(null, null);
        lambda.handleRequest(new DynamodbEvent(), null);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    public void testWrongEvents() {
        lambda.handleRequest(populateWrongRecord(), null);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    public void testUpdatedByOwner() {
        lambda.handleRequest(populateEvent("owner"), null);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    public void testUpdatedByUser() {
        Mockito.when(emailClient.sendEmail(any())).thenReturn(null);
        lambda.handleRequest(populateEvent("user"), null);
        verify(emailClient).sendEmail(any());
    }

    /**
     * Provides a dynamo db event with list of records withouth
     * <code>MODIFY</code> as the event name.
     * 
     * @return wrong events for the lambda
     */
    private DynamodbEvent populateWrongRecord() {
        final DynamodbEvent result = new DynamodbEvent();
        final List<DynamodbEvent.DynamodbStreamRecord> records = new ArrayList<>(3);

        DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventName(OperationType.INSERT);
        records.add(record);

        record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventName(OperationType.REMOVE);
        records.add(record);

        return result;
    }

    /**
     * Provides a dynamo db event with a singleton list with a record that was
     * updated by the received parameter.
     * 
     * @param updatedBy
     *            name of the user which updated the record
     * @return event updated by owner
     */
    private DynamodbEvent populateEvent(final String updatedBy) {
        final DynamodbEvent result = new DynamodbEvent();
        final StreamRecord streamRecord = new StreamRecord();
        streamRecord.addNewImageEntry("updatedBy", new AttributeValue(updatedBy));
        streamRecord.addNewImageEntry("owner", new AttributeValue("owner"));
        streamRecord.addNewImageEntry("content", new AttributeValue("content"));
        final DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
        record.setEventName(OperationType.MODIFY);
        record.setDynamodb(streamRecord);
        result.setRecords(Collections.singletonList(record));
        return result;
    }

}

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
import com.amazonaws.services.dynamodbv2.model.Record;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
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
        lambda.handleRequest(null);
        lambda.handleRequest(Collections.emptyList());
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    public void testWrongEvents() {
        lambda.handleRequest(populateWrongRecord());
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    public void testUpdatedByOwner() {
        lambda.handleRequest(populateRecord("owner"));
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    public void testUpdatedByUser() {
        Mockito.when(emailClient.sendEmail(any())).thenReturn(null);
        lambda.handleRequest(populateRecord("user"));
        verify(emailClient).sendEmail(any());
    }

    /**
     * Provides a list with records withouth <code>MODIFY</code> as the event
     * name.
     * 
     * @return wrong events for the lambda
     */
    private List<Record> populateWrongRecord() {
        final List<Record> result = new ArrayList<>(3);
        result.add(new Record().withEventName(OperationType.INSERT));
        result.add(new Record().withEventName(OperationType.REMOVE));
        return result;
    }

    /**
     * Provides a singleton list with a record that was updated by the received
     * parameter.
     * 
     * @param updatedBy
     *            name of the user which updated the record
     * @return event updated by owner
     */
    private List<Record> populateRecord(final String updatedBy) {
        final StreamRecord streamRecord = new StreamRecord();
        streamRecord.addNewImageEntry("updatedBy", new AttributeValue(updatedBy));
        streamRecord.addNewImageEntry("owner", new AttributeValue("owner"));
        streamRecord.addNewImageEntry("content", new AttributeValue("content"));
        final Record record = new Record().withEventName(OperationType.MODIFY);
        record.setDynamodb(streamRecord);
        return Collections.singletonList(record);
    }

}

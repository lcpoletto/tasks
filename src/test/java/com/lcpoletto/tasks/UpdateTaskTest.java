package com.lcpoletto.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.lcpoletto.exceptions.ObjectNotFoundException;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * Test fixture for {@link UpdateTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    private UpdateTask lambda;

    @Before
    public void setup() {
        lambda = new UpdateTask(mockClient);
    }

    @Test(expected = ValidationException.class)
    public void testNull() throws ValidationException {
        lambda.handleRequest(null, null);
    }

    @Test(expected = ValidationException.class)
    public void testEmpty() throws ValidationException {
        lambda.handleRequest(new Task(), null);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutPriority() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description.");
        lambda.handleRequest(input, null);
    }

    @Test(expected = ValidationException.class)
    public void testLowerBoundPriority() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description.");
        input.setPriority(-1);
        lambda.handleRequest(input, null);
    }

    @Test(expected = ValidationException.class)
    public void testUpperBoundPriority() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description.");
        input.setPriority(10);
        lambda.handleRequest(input, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testNotFound() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description");
        input.setPriority(5);
        input.setId("not-found");

        when(mockClient.getItem(any())).thenReturn(new GetItemResult());
        try {
            lambda.handleRequest(input, null);
            fail();
        } catch (Throwable t) {
            verify(mockClient).getItem(any());
            throw t;
        }
    }

    @Test
    public void testValid() throws ValidationException {
        final String taskId = "success-id";
        final Task input = new Task();
        input.setDescription("Test description");
        input.setPriority(5);
        input.setId(taskId);

        when(mockClient.getItem(any())).thenReturn(getGetItemResult(taskId));
        when(mockClient.updateItem(any())).thenReturn(getUpdateItemResult(taskId));
        final String result = lambda.handleRequest(input, null);
        assertEquals("SUCCESS", result);
        verify(mockClient).getItem(any());
        verify(mockClient).updateItem(any());
    }

    private GetItemResult getGetItemResult(final String id) {
        final GetItemResult result = new GetItemResult();
        result.addItemEntry("id", new AttributeValue(id));
        return result;
    }

    private UpdateItemResult getUpdateItemResult(final String id) {
        final UpdateItemResult result = new UpdateItemResult();
        result.addAttributesEntry("id", new AttributeValue(id));
        return result;
    }

}

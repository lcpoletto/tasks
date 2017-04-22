package com.lcpoletto.tasks;

import static org.junit.Assert.assertNotNull;
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
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
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
        lambda.handleRequest(null);
    }

    @Test(expected = ValidationException.class)
    public void testEmpty() throws ValidationException {
        lambda.handleRequest(new Task());
    }

    @Test(expected = ValidationException.class)
    public void testWithoutPriority() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description.");
        lambda.handleRequest(input);
    }

    @Test(expected = ValidationException.class)
    public void testLowerBoundPriority() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description.");
        input.setPriority(-1);
        lambda.handleRequest(input);
    }

    @Test(expected = ValidationException.class)
    public void testUpperBoundPriority() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description.");
        input.setPriority(10);
        lambda.handleRequest(input);
    }

    // TODO: anything to improve this test case?
    @Test
    public void testValid() throws ValidationException {
        final String taskId = "success-id";
        final Task input = new Task();
        input.setDescription("Test description");
        input.setPriority(5);
        input.setId(taskId);

        when(mockClient.updateItem(any())).thenReturn(getSuccessUpdateResult(taskId));
        final Task result = lambda.handleRequest(input);
        assertNotNull(result);
        verify(mockClient).updateItem(any());
    }

    private UpdateItemResult getSuccessUpdateResult(final String id) {
        final UpdateItemResult result = new UpdateItemResult();
        result.addAttributesEntry("id", new AttributeValue(id));
        return result;
    }

}

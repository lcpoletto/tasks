package com.lcpoletto.tasks;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
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
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class })
public class UpdateTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    @Test
    public void testInvalidInput() {
        final UpdateTask lambda = new UpdateTask();
        try {
            lambda.handleRequest(null);
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("null"));
        }

        final Task input = new Task();
        try {
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("description"));
            assertTrue(e.getMessage().contains("priority"));
            assertTrue(e.getMessage().contains(" id "));
        }

        try {
            input.setDescription("Test description.");
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertFalse(e.getMessage().contains("description"));
            assertTrue(e.getMessage().contains("priority"));
            assertTrue(e.getMessage().contains(" id "));
        }

        try {
            input.setPriority(-1);
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertFalse(e.getMessage().contains("description"));
            assertTrue(e.getMessage().contains("priority"));
            assertTrue(e.getMessage().contains(" id "));
        }

        try {
            input.setPriority(11);
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertFalse(e.getMessage().contains("description"));
            assertTrue(e.getMessage().contains("priority"));
            assertTrue(e.getMessage().contains(" id "));
        }

        try {
            input.setPriority(9);
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertFalse(e.getMessage().contains("description"));
            assertFalse(e.getMessage().contains("priority"));
            assertTrue(e.getMessage().contains(" id "));
        }
    }

    // TODO: anything to improve this test case?
    @Test
    public void testValid() throws ValidationException {
        final UpdateTask lambda = new UpdateTask();
        final String taskId = "success-id";
        final Task input = new Task();
        input.setDescription("Test description");
        input.setPriority(5);
        input.setId(taskId);

        mockStatic(AmazonDynamoDBClientBuilder.class);
        expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
        expect(mockClient.updateItem(anyObject())).andReturn(getSuccessUpdateResult(taskId));
        replay(mockClient);
        replayAll();

        final Task result = lambda.handleRequest(input);
        assertNotNull(result);
        verifyAll();
        verify(mockClient);
    }

    private UpdateItemResult getSuccessUpdateResult(final String id) {
        final UpdateItemResult result = new UpdateItemResult();
        result.addAttributesEntry("id", new AttributeValue(id));
        return result;
    }

}

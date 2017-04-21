package com.lcpoletto.tasks;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verify;

import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * Test fixture for {@link CreateTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class })
public class CreateTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    @Test
    public void testInvalidInput() {
        final CreateTask lambda = new CreateTask();
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
        }

        try {
            input.setDescription("Test description.");
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertFalse(e.getMessage().contains("description"));
            assertTrue(e.getMessage().contains("priority"));
        }

        try {
            input.setPriority(-1);
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertFalse(e.getMessage().contains("description"));
            assertTrue(e.getMessage().contains("priority"));
        }

        try {
            input.setPriority(11);
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            assertFalse(e.getMessage().contains("description"));
            assertTrue(e.getMessage().contains("priority"));
        }
    }

    @Test
    public void testValid() throws ValidationException {
        final CreateTask lambda = new CreateTask();
        final Task input = new Task();
        input.setDescription("Test description");
        input.setPriority(5);

        mockStatic(AmazonDynamoDBClientBuilder.class);
        expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
        replayAll();

        final Task result = lambda.handleRequest(input);
        assertNotNull(result.getId());
        verify(AmazonDynamoDBClientBuilder.class);
    }
}

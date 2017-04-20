/**
 * 
 */
package com.lcpoletto.tasks;

import org.easymock.EasyMock;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.lcpoletto.tasks.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * Test fixture for {@link AddTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class })
public class AddTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    @Test
    public void testInvalidInput() {
        final AddTask lambda = new AddTask();
        try {
            lambda.handleRequest(null);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("null"));
        }

        final Task input = new Task();
        try {
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("description"));
        }

        try {
            input.setDescription("Test description.");
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            Assert.assertFalse(e.getMessage().contains("description"));
            Assert.assertTrue(e.getMessage().contains("priority"));
        }

        try {
            input.setPriority(-1);
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            Assert.assertFalse(e.getMessage().contains("description"));
            Assert.assertTrue(e.getMessage().contains("priority"));
        }

        try {
            input.setPriority(11);
            lambda.handleRequest(input);
        } catch (ValidationException e) {
            Assert.assertFalse(e.getMessage().contains("description"));
            Assert.assertTrue(e.getMessage().contains("priority"));
        }
    }

    @Test
    public void testValid() throws ValidationException {
        final AddTask lambda = new AddTask();
        final Task input = new Task();
        input.setDescription("Test description");
        input.setPriority(5);

        PowerMock.mockStatic(AmazonDynamoDBClientBuilder.class);
        EasyMock.expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
        PowerMock.replay(AmazonDynamoDBClientBuilder.class);

        final Task result = lambda.handleRequest(input);
        Assert.assertNotNull(result.getId());
        PowerMock.verify(AmazonDynamoDBClientBuilder.class);
    }
}

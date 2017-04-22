package com.lcpoletto.tasks;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * Test fixture for {@link CreateTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    private CreateTask lambda;

    @Before
    public void setup() {
        lambda = new CreateTask(mockClient);
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
        input.setPriority(11);
        lambda.handleRequest(input);
    }

    @Test
    public void testValid() throws ValidationException {
        final Task input = new Task();
        input.setDescription("Test description");
        input.setPriority(5);

        final Task result = lambda.handleRequest(input);
        assertNotNull(result.getId());
    }
}

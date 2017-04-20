/**
 * 
 */
package com.lcpoletto.tasks;

import org.easymock.EasyMock;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.lcpoletto.tasks.model.Task;

/**
 * @author Luis.Poletto
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class, ListTasks.class })
public class ListTasksTest {

    @Mock
    private AmazonDynamoDB mockClient;

    @Mock
    private DynamoDBMapper mockMapper;

    /**
     * Every test we do here we are calling these mocked methods at least once,
     * thus make sense to have it on the setup method.
     */
    @Before
    public void setup() {
        PowerMock.mockStatic(AmazonDynamoDBClientBuilder.class);
        EasyMock.expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
    }

    /**
     * We have to verify at least one call on
     * {@link AmazonDynamoDBClientBuilder}.
     */
    @After
    public void tearDown() {
        PowerMock.verify(AmazonDynamoDBClientBuilder.class);
    }

    @Test
    public void testAvailableResults() throws Exception {
        PowerMock.expectNew(DynamoDBMapper.class, mockClient).andReturn(mockMapper);
        EasyMock.expect(mockMapper.scan(Task.class, EasyMock.anyObject())).andReturn(getUnorderedResults());
    }

    private PaginatedScanList<Task> getUnorderedResults() {
        return null;
    }

}

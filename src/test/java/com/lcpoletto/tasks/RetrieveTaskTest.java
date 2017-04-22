package com.lcpoletto.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.lcpoletto.tasks.model.Task;

/**
 * Test fixture for {@link RetrieveTask}.
 * 
 * @author Luis.Poletto
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RetrieveTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    private RetrieveTask lambda;

    @Before
    public void setup() {
        lambda = new RetrieveTask(mockClient);
    }

    @Test
    public void testEmptyResults() {
        when(mockClient.scan(any())).thenReturn(new ScanResult());
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(mockClient).scan(any());
    }

    @Test
    public void testNonEmptyResults() {
        when(mockClient.scan(any())).thenReturn(getUnorderedResults());
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(mockClient).scan(any());
    }

    @Test
    public void testSortedResults() {
        when(mockClient.scan(any())).thenReturn(getUnorderedResults());
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(5, result.size());

        for (int i = 0; i < result.size(); i++) {
            assertEquals("" + i, result.get(i).getId());
        }

        verify(mockClient).scan(any());
    }

    /**
     * Helper method which will produce test data.
     * 
     * @return testable data
     */
    private ScanResult getUnorderedResults() {
        final ScanResult result = new ScanResult();
        final Collection<Map<String, AttributeValue>> items = new ArrayList<>();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue("2"));
        item.put("priority", new AttributeValue().withN("1"));
        // 2017-04-22T04:09:38Z
        item.put("completed", new AttributeValue("2016-07-06T12:22:46Z"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("1"));
        item.put("priority", new AttributeValue().withN("1"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("0"));
        item.put("priority", new AttributeValue().withN("0"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("4"));
        item.put("priority", new AttributeValue().withN("0"));
        item.put("completed", new AttributeValue("2016-06-06T12:22:46Z"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("3"));
        item.put("priority", new AttributeValue().withN("2"));
        item.put("completed", new AttributeValue("2016-07-06T12:22:46Z"));
        items.add(item);

        result.setItems(items);
        return result;
    }

}

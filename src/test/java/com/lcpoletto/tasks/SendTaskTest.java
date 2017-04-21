package com.lcpoletto.tasks;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

/**
 * Test fixture for {@link SendTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class, AmazonSimpleEmailServiceClientBuilder.class })
public class SendTaskTest {

    @Mock
    private AmazonDynamoDB mockDynamo;

    @Mock
    private AmazonSimpleEmailService mockSimpleEmailService;

    /**
     * Every test we do here we are calling these mocked methods at least once,
     * thus make sense to have it on the setup method.
     */
    @Before
    public void setup() {
        mockStatic(AmazonDynamoDBClientBuilder.class);
        mockStatic(AmazonSimpleEmailServiceClientBuilder.class);
        expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockDynamo);
    }

    /**
     * We have to verify at least one call on
     * {@link AmazonDynamoDBClientBuilder}.
     */
    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testEmptyResults() {
        expect(mockDynamo.scan(anyObject())).andReturn(new ScanResult());
        replay(mockDynamo);
        replayAll();

        final SendTask lambda = new SendTask();
        final String result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals("SUCCESS", result);
        verify(mockDynamo);
    }

    @Test
    public void testWithResults() {
        expect(AmazonSimpleEmailServiceClientBuilder.defaultClient()).andReturn(mockSimpleEmailService);

        expect(mockDynamo.scan(anyObject())).andReturn(getUnorderedResults());
        replay(mockDynamo);

        // we expect 2 calls as we have only 2 different users on the list
        expect(mockSimpleEmailService.sendEmail(anyObject())).andReturn(new SendEmailResult()).times(2);
        replay(mockSimpleEmailService);

        replayAll();

        final SendTask lambda = new SendTask();
        final String result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals("SUCCESS", result);
        verify(mockDynamo);
    }

    private ScanResult getUnorderedResults() {
        final ScanResult result = new ScanResult();
        final Collection<Map<String, AttributeValue>> items = new ArrayList<>();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue("second"));
        item.put("user", new AttributeValue("second@second.com"));
        item.put("description", new AttributeValue("second description"));
        item.put("priority", new AttributeValue().withN("8"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("third"));
        item.put("user", new AttributeValue("first@first.com"));
        item.put("description", new AttributeValue("third description"));
        item.put("priority", new AttributeValue().withN("5"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("first"));
        item.put("user", new AttributeValue("first@first.com"));
        item.put("description", new AttributeValue("first description"));
        item.put("priority", new AttributeValue().withN("5"));
        items.add(item);

        result.setItems(items);
        return result;
    }

}

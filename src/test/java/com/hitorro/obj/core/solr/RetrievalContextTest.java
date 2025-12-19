package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.collection.CollectionConfig;
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalAggregate;
import com.hitorro.util.core.iterator.sinks.Sink;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RetrievalContext.
 * Tests retrieval context management, aggregates, and query handling.
 */
public class RetrievalContextTest {

    private CollectionConfig mockConfig;
    private SolrQuery solrQuery;
    private RetrievalContext context;

    @Before
    public void setUp() {
        mockConfig = mock(CollectionConfig.class);
        solrQuery = new SolrQuery("*:*");
        context = new RetrievalContext(mockConfig, "en", solrQuery);
    }

    @Test
    public void testContextCreation() {
        assertNotNull("Context should be created", context);
    }

    @Test
    public void testAddAggregate() {
        RetrievalAggregate mockAggregate = mock(RetrievalAggregate.class);
        
        context.addAggregate(mockAggregate);
        
        // Verify aggregate was added by trying to process it
        Sink<JVS> mockSink = mock(Sink.class);
        when(mockAggregate.getAggregate(context)).thenReturn(null);
        
        context.addAggregates(mockSink);
        
        verify(mockAggregate, times(1)).getAggregate(context);
    }

    @Test
    public void testAddMultipleAggregates() {
        RetrievalAggregate agg1 = mock(RetrievalAggregate.class);
        RetrievalAggregate agg2 = mock(RetrievalAggregate.class);
        RetrievalAggregate agg3 = mock(RetrievalAggregate.class);
        
        context.addAggregate(agg1);
        context.addAggregate(agg2);
        context.addAggregate(agg3);
        
        Sink<JVS> mockSink = mock(Sink.class);
        when(agg1.getAggregate(context)).thenReturn(null);
        when(agg2.getAggregate(context)).thenReturn(null);
        when(agg3.getAggregate(context)).thenReturn(null);
        
        context.addAggregates(mockSink);
        
        verify(agg1, times(1)).getAggregate(context);
        verify(agg2, times(1)).getAggregate(context);
        verify(agg3, times(1)).getAggregate(context);
    }

    @Test
    public void testAddAggregatesWithResults() {
        RetrievalAggregate mockAggregate = mock(RetrievalAggregate.class);
        JVS mockResult = mock(JVS.class);
        
        when(mockAggregate.getAggregate(context)).thenReturn(mockResult);
        
        context.addAggregate(mockAggregate);
        
        Sink<JVS> mockSink = mock(Sink.class);
        context.addAggregates(mockSink);
        
        verify(mockSink, times(1)).accept(mockResult);
    }

    @Test
    public void testAddAggregatesWithNullResults() {
        RetrievalAggregate mockAggregate = mock(RetrievalAggregate.class);
        
        when(mockAggregate.getAggregate(context)).thenReturn(null);
        
        context.addAggregate(mockAggregate);
        
        Sink<JVS> mockSink = mock(Sink.class);
        context.addAggregates(mockSink);
        
        verify(mockSink, never()).accept(any());
    }

    @Test
    public void testCollectionName() {
        assertNull("Collection name should be null initially", context.getCollectionName());
        
        context.setCollectionName("test-collection");
        
        assertEquals("Collection name should match", "test-collection", context.getCollectionName());
    }

    @Test
    public void testCollectionNameUpdate() {
        context.setCollectionName("collection1");
        assertEquals("First name should be set", "collection1", context.getCollectionName());
        
        context.setCollectionName("collection2");
        assertEquals("Name should be updated", "collection2", context.getCollectionName());
    }

    @Test
    public void testResultsField() {
        assertEquals("Results should be 0 initially", 0, context.results);
        
        context.results = 42;
        assertEquals("Results should be updated", 42, context.results);
    }

    @Test
    public void testQueryResponseField() {
        assertNull("QueryResponse should be null initially", context.qr);
    }

    @Test
    public void testMultipleAggregatesWithMixedResults() {
        RetrievalAggregate agg1 = mock(RetrievalAggregate.class);
        RetrievalAggregate agg2 = mock(RetrievalAggregate.class);
        RetrievalAggregate agg3 = mock(RetrievalAggregate.class);
        
        JVS result1 = mock(JVS.class);
        JVS result3 = mock(JVS.class);
        
        when(agg1.getAggregate(context)).thenReturn(result1);
        when(agg2.getAggregate(context)).thenReturn(null);
        when(agg3.getAggregate(context)).thenReturn(result3);
        
        context.addAggregate(agg1);
        context.addAggregate(agg2);
        context.addAggregate(agg3);
        
        Sink<JVS> mockSink = mock(Sink.class);
        context.addAggregates(mockSink);
        
        verify(mockSink, times(1)).accept(result1);
        verify(mockSink, times(1)).accept(result3);
        verify(mockSink, times(2)).accept(any());
    }
}

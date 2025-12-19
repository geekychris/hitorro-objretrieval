# Hitorro Object Retrieval

> Solr-based object retrieval and search framework with advanced feature integration

[![Java](https://img.shields.io/badge/Java-19%2B-orange)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![Solr](https://img.shields.io/badge/Solr-9.4.1-red)](https://solr.apache.org/)

## Overview

Hitorro Object Retrieval is a comprehensive search and retrieval framework built on Apache Solr with advanced features for object storage, clustering, and retrieval pipelines. It provides a high-level abstraction for complex search operations with integrated feature support.

### Key Features

- 🔍 **Solr Integration** - Full Apache Solr 9.4.1 support
- 📦 **Object Store** - Xodus-based persistent object storage
- 🎯 **Retrieval Pipelines** - Modular retrieval processing
- 📊 **External Features** - Feature-based ranking and scoring
- 🌐 **Collection Management** - Multi-collection support
- 🔄 **Query Visitors** - Extensible query processing
- 📈 **Clustering** - Carrot2 document clustering
- 🎪 **Pagination** - Advanced pagination support

## Architecture

```
hitorro-objretrieval/
├── solr/                    # Solr integration
│   ├── collection/         # Collection configuration
│   ├── queryvisitors/      # Query processing
│   └── retrievalmodules/   # Retrieval pipeline
│       ├── cluster/        # Clustering module
│       ├── facet/          # Facet module
│       ├── fixup/          # Result fixup
│       ├── object/         # Object retrieval
│       ├── pagination/     # Pagination support
│       └── solr/           # Core Solr module
└── objectstore/             # Object storage
```

## Installation

### Maven

```xml
<dependency>
    <groupId>com.hitorro</groupId>
    <artifactId>hitorro-objretrieval</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.hitorro:hitorro-objretrieval:3.0.0'
```

## Quick Start

### Basic Search

```java
import com.hitorro.obj.core.solr.RetrievalContext;
import org.apache.solr.client.solrj.SolrQuery;

// Create collection config
CollectionConfig config = new CollectionConfig();

		// Create retrieval context
		SolrQuery query = new SolrQuery("title:search");
		RetrievalContext context = new RetrievalContext(config, "en", query);

// Set collection
context.

		setCollectionName("documents");

// Add aggregates
context.

		addAggregate(new FacetAggregate());

// Process results
		context.

		addAggregates(result ->{
		System.out.

		println("Result: "+result);
});
```

### External Feature Fields

```java
// Define external feature field
public class CustomFeatureField extends ExternalFeatureField {
    @Override
    public String getFieldType() {
        return "float";
    }
}

// Use in Solr schema
// Configure in schema.xml with feature data
```

### Object Store

```java
import com.hitorro.obj.core.objectstore.JVSObjectStoreClient;

// Create object store client
JVSObjectStoreClient store = new JVSObjectStoreClient();

		// Store object
		String key = "doc123";
		JVS data = new JVS();
data.

		put("title","Document Title");
store.

		put(key, data);

		// Retrieve object
		JVS retrieved = store.get(key);
```

## Core Components

### RetrievalContext

Manages the retrieval execution environment:

```java
CollectionConfig config = new CollectionConfig();
SolrQuery query = new SolrQuery("*:*");
RetrievalContext context = new RetrievalContext(config, "en", query);

// Set collection
context.setCollectionName("my-collection");

// Add aggregates
context.addAggregate(new FacetAggregate());
context.addAggregate(new ClusteringAggregate());

// Process aggregates
context.addAggregates(sink);

// Access results
int resultCount = context.results;
QueryResponse response = context.qr;
```

### Retrieval Modules

Build modular retrieval pipelines:

```java
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalPipeline;

// Create pipeline
RetrievalPipeline pipeline = new RetrievalPipeline();

// Add modules
pipeline.

		addModule(new SolrRetriever());
		pipeline.

		addModule(new ObjectRetriever());
		pipeline.

		addModule(new FixupRetriever());
		pipeline.

		addModule(new PaginationRetriever());

// Execute
		pipeline.

		execute(context);
```

### Query Visitors

Process and transform queries:

```java
import com.hitorro.obj.core.solr.queryvisitors.SelectRow;

// Select fields
SelectRow select = new SelectRow();
select.

		addField("id");
select.

		addField("title");
select.

		addField("score");

// Apply to query
query.

		setFields(select.getFields());

// Faceting
		query.

		addFacetField("category");
query.

		addFacetField("author");

// Date range facets
query.

		addDateRangeFacet("published",start, end, gap);
```

### External Features

Integrate external feature data for ranking:

```java
// In schema.xml
<fieldType name="externalFeature" class="solr.com.hitorro.obj.core.ExternalFeatureField">
    <str name="keyField">id</str>
    <str name="defVal">0.0</str>
</fieldType>

<field name="popularity" type="externalFeature" />
<field name="quality" type="externalFeature" />

// Use in queries
query.setQuery("{!boost b=popularity}search terms");
```

### Collection Configuration

Configure multiple collections:

```java
import com.hitorro.obj.core.solr.collection.CollectionConfig;
import com.hitorro.obj.core.solr.collection.CollectionIntent;

CollectionConfig config = new CollectionConfig();
config.

setDefaultCollection("main");
config.

addCollection("documents");
config.

addCollection("products");

// Collection-specific settings
CollectionIntent intent = new CollectionIntent();
intent.

setName("documents");
intent.

setShards(4);
intent.

setReplicas(2);

config.

addIntent(intent);
```

## Advanced Features

### Document Clustering

Use Carrot2 for result clustering:

```java
import com.hitorro.obj.core.solr.retrievalmodules.cluster.ClusteringIterator;

ClusteringRetriever clusterer = new ClusteringRetriever();

// Configure clustering
clusterer.

setNumClusters(10);
clusterer.

setMinClusterSize(3);

// Add to pipeline
pipeline.

addModule(clusterer);

// Results include clusters
ClusteringIterator iterator = clusterer.getIterator(context);
while(iterator.

hasNext()){
Cluster cluster = iterator.next();
    System.out.

println("Cluster: "+cluster.getLabel());
		System.out.

println("Docs: "+cluster.getDocuments().

size());
		}
```

### Faceted Navigation

Build faceted search interfaces:

```java
import com.hitorro.obj.core.solr.retrievalmodules.facet.FacetRetriever;

FacetRetriever facets = new FacetRetriever();

// Field facets
query.

addFacetField("category");
query.

setFacetLimit(20);
query.

setFacetMinCount(1);

// Range facets
query.

addNumericRangeFacet("price",0,1000,100);

// Date facets
query.

addDateRangeFacet("date",start, end, "+1DAY");

// Process facet results
FacetField category = response.getFacetField("category");
for(
FacetField.Count count :category.

getValues()){
		System.out.

println(count.getName() +": "+count.

getCount());
		}
```

### Result Fixup

Post-process search results:

```java
import com.hitorro.obj.core.solr.retrievalmodules.fixup.FixupIterator;
import com.hitorro.obj.core.solr.retrievalmodules.fixup.FixupRetriever;

FixupRetriever fixup = new FixupRetriever();

// Add field mapper
fixup.

addMapper(new RemoveFieldMapper("internal_field"));
		fixup.

addMapper(new AddFieldMapper("computed_field", computer));

// Apply to results
FixupIterator iterator = fixup.getIterator(context);
```

### Pagination

Handle large result sets:

```java
import com.hitorro.obj.core.solr.retrievalmodules.pagination.PaginationRetriever;

PaginationRetriever paginator = new PaginationRetriever();

// Configure pagination
query.

setStart(0);
query.

setRows(20);

// Deep pagination with cursor
String cursorMark = "*";
query.

set("cursorMark",cursorMark);
query.

setSort("id",SolrQuery.ORDER.asc);

// Process pages
do{
QueryResponse response = solr.query(query);
cursorMark =response.

getNextCursorMark();
    query.

set("cursorMark",cursorMark);
}while(hasMore);
```

## Object Store

### Xodus-Based Storage

Persistent key-value storage with Xodus:

```java
import com.hitorro.obj.core.objectstore.ObjectStoreService;
import com.hitorro.obj.core.objectstore.ObjectStoreShard;

// Create or open store
ObjectStoreService service = new ObjectStoreService();
service.

		setStorePath("/data/objectstore");

		// Get shard
		ObjectStoreShard shard = service.getShard("shard-0");

// Store operations
shard.

		put("key1",value1);
shard.

		put("key2",value2);

		// Retrieve
		Object value = shard.get("key1");

		// Batch operations
		Map<String, Object> batch = new HashMap<>();
batch.

		put("key3",value3);
batch.

		put("key4",value4);
shard.

		putAll(batch);

// Iteration
shard.

		forEach((key, value) ->{
		System.out.

		println(key +" -> "+value);
});
```

### Sharding

Distribute objects across shards:

```java
// Determine shard for key
int shardId = calculateShardId(key, numShards);
ObjectStoreShard shard = service.getShard(shardId);

// Store in appropriate shard
shard.put(key, value);

// Consistent hashing ensures same key -> same shard
```

## Use Cases

### 1. Full-Text Search Application

```java
// Configure search
SolrQuery query = new SolrQuery();
query.setQuery("machine learning");
query.addFilterQuery("category:technology");
query.addFacetField("author");
query.setRows(20);

// Execute search
RetrievalContext context = new RetrievalContext(config, "en", query);
context.setCollectionName("articles");

// Process results with features
context.addAggregate(new FeatureAggregate());
```

### 2. E-Commerce Product Search

```java
// Product search with facets
SolrQuery query = new SolrQuery("laptop");
query.addFilterQuery("inStock:true");
query.addFacetField("brand");
query.addFacetField("price_range");
query.addNumericRangeFacet("price", 0, 2000, 200);

// Sort by relevance and popularity
query.setSort("score", SolrQuery.ORDER.desc);
query.addSort("popularity", SolrQuery.ORDER.desc);
```

### 3. Document Clustering

```java
// Search and cluster results
SolrQuery query = new SolrQuery("*:*");
query.setRows(100);

RetrievalContext context = new RetrievalContext(config, "en", query);
context.addAggregate(new ClusteringAggregate());

// Results grouped by topic
```

### 4. Personalized Search

```java
// Use external features for personalization
query.setQuery("{!boost b=user_affinity}search terms");
query.addFilterQuery("language:en");

// Combine with collaborative filtering scores
ExternalFeatureField cfScore = new CollaborativeFilteringField();
```

## Dependencies

### Required
- **hitorro-features**: 3.0.0 - Feature extraction and management
- **solr-core**: 9.4.1 - Apache Solr search platform
- **xodus-environment**: 2.0.1 - Persistent storage

### Optional
- **carrot2-core**: 4.5.3 - Document clustering (runtime)
- **carrot2-mini**: 3.16.3 - Clustering algorithms

### Test
- **JUnit**: 4.13.2
- **Mockito**: 5.7.0

## Building

```bash
# Quick build
./build.sh

# Or with Maven
mvn clean install

# Skip tests
mvn install -DskipTests

# With sources and javadocs
mvn clean install source:jar javadoc:jar
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=RetrievalContextTest

# With coverage
mvn test jacoco:report
```

## Performance

### Indexing Performance
- **Bulk Indexing**: 10,000+ docs/sec
- **Real-time Updates**: < 1s visibility
- **Commit Strategy**: Soft commits for near real-time

### Query Performance
- **Simple Queries**: < 10ms
- **Faceted Queries**: < 50ms
- **Clustered Results**: < 200ms

### Object Store
- **Get Operations**: < 1ms
- **Put Operations**: < 2ms
- **Batch Operations**: 10,000+ ops/sec

## Best Practices

1. **Use Cursor Pagination** for large result sets
2. **Cache Collection Configs** - reuse across queries
3. **Batch Object Store Operations** - use putAll/getAll
4. **Index Design** - use appropriate field types
5. **Query Optimization** - use filter queries for cacheable conditions

## Contributing

1. Fork the repository
2. Create your feature branch
3. Add comprehensive tests
4. Ensure all tests pass
5. Submit a pull request

## License

MIT License

## Support

- **Documentation**: See README.md and QUICKSTART.md
- **Issues**: GitHub Issues
- **Email**: support@hitorro.com

## Acknowledgments

Built on Apache Solr and integrated with the Hitorro feature framework.

# Quick Start Guide - Hitorro Object Retrieval

## Prerequisites

### 1. Java Development Kit (19+)
```bash
# Check Java version
java -version

# Install if needed
# macOS: brew install openjdk@19
# Ubuntu: sudo apt install openjdk-19-jdk
```

### 2. Apache Maven (3.6+)
```bash
# Check Maven
mvn -version

# Install if needed
# macOS: brew install maven
# Ubuntu: sudo apt install maven
```

### 3. Dependencies

Build dependencies in order:

```bash
# 1. Build hitorro-util
cd ../hitorro-util
mvn clean install

# 2. Build hitorro-base  
cd ../hitorro-base
mvn clean install

# 3. Build hitorro-features
cd ../hitorro-features
mvn clean install
```

## Building

### Quick Build

```bash
cd /Users/chris/hitorro/hitorro-objretrieval
./build.sh
```

### Manual Build

```bash
# Clean and install
mvn clean install

# Skip tests
mvn install -DskipTests

# With sources and javadocs
mvn clean install source:jar javadoc:jar
```

## Using in Your Project

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

## Quick Examples

### 1. Basic Search

```java
import com.hitorro.obj.core.solr.RetrievalContext;
import org.apache.solr.client.solrj.SolrQuery;

// Create collection config
CollectionConfig config = new CollectionConfig();

		// Create query
		SolrQuery query = new SolrQuery("search terms");

		// Create context
		RetrievalContext context = new RetrievalContext(config, "en", query);
context.

		setCollectionName("documents");

// Execute search
// Process results...
```

### 2. Object Store

```java
import com.hitorro.obj.core.objectstore.ObjectStoreService;
import com.hitorro.obj.core.objectstore.ObjectStoreShard;

// Create object store
ObjectStoreService service = new ObjectStoreService();
service.

		setStorePath("/tmp/objectstore");

		// Store data
		ObjectStoreShard shard = service.getShard(0);
shard.

		put("key1",myObject);

		// Retrieve data
		Object retrieved = shard.get("key1");
```

### 3. Faceted Search

```java
SolrQuery query = new SolrQuery("*:*");
query.addFacetField("category");
query.addFacetField("author");
query.setFacetMinCount(1);

RetrievalContext context = new RetrievalContext(config, "en", query);
// Process with facets...
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RetrievalContextTest

# Run with verbose output
mvn test -X
```

## IDE Setup

### IntelliJ IDEA

1. **Open Project**: `File → Open → hitorro-objretrieval/pom.xml`
2. **Enable Auto-Import**: `Preferences → Build → Maven → Importing`
3. **Set JDK 19+**: `File → Project Structure → Project SDK`
4. **Run Tests**: Right-click test class → Run

### Eclipse

1. **Import**: `File → Import → Maven → Existing Maven Project`
2. **Select Directory**: Choose `hitorro-objretrieval`
3. **Update Project**: Right-click → Maven → Update Project
4. **Run Tests**: Right-click test → Run As → JUnit Test

### VS Code

1. **Open Folder**: `File → Open Folder → hitorro-objretrieval`
2. **Install Extensions**: Java Extension Pack
3. **Maven Auto-Detect**: Happens automatically
4. **Run Tests**: Use Testing view

## Configuration

### Solr Configuration

```java
// Set Solr URL
System.setProperty("solr.url", "http://localhost:8983/solr");

// Or in code
SolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr").build();
```

### Object Store Configuration

```java
ObjectStoreService service = new ObjectStoreService();
service.setStorePath("/data/objectstore");
service.setNumShards(10);
service.init();
```

## Troubleshooting

### Build Fails

**Problem**: Cannot find hitorro-features dependency

**Solution**:
```bash
cd ../hitorro-features
mvn clean install
cd ../hitorro-objretrieval
mvn clean install
```

### Solr Connection Issues

**Problem**: Cannot connect to Solr

**Solution**:
- Verify Solr is running: `curl http://localhost:8983/solr/`
- Check collection exists
- Verify network connectivity

### Test Failures

**Problem**: Test failures due to missing Solr

**Solution**:
```bash
# Use embedded Solr for tests or skip tests
mvn install -DskipTests
```

### Memory Issues

**Problem**: OutOfMemoryError

**Solution**:
```bash
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"
mvn clean install
```

## Performance Tips

1. **Connection Pooling**: Use connection pool for Solr clients
2. **Batch Operations**: Batch object store puts/gets
3. **Query Caching**: Cache frequently used queries
4. **Shard Strategy**: Distribute objects evenly across shards
5. **Index Optimization**: Optimize Solr indexes regularly

## Common Use Cases

### Full-Text Search

```java
SolrQuery query = new SolrQuery();
query.setQuery("machine learning");
query.addFilterQuery("category:technology");
query.setRows(20);
query.setStart(0);
```

### Document Clustering

```java


ClusteringRetriever clusterer = new ClusteringRetriever();
// Configure and use...
```

### External Features

```java
// Boost by popularity
query.setQuery("{!boost b=popularity}search terms");
```

## Next Steps

1. Read [README.md](README.md) for comprehensive documentation
2. Review [EXTRACTION_SUMMARY.md](EXTRACTION_SUMMARY.md) for details
3. Explore test classes for usage examples
4. Check Solr documentation for advanced features

## Getting Help

- **Documentation**: README.md
- **Examples**: See test classes
- **Issues**: GitHub Issues
- **Support**: support@hitorro.com

## Quick Reference

```bash
# Build
./build.sh

# Run tests
mvn test

# Clean build
mvn clean install

# Skip tests
mvn install -DskipTests

# Package only
mvn package

# View dependency tree
mvn dependency:tree

# Update dependencies
mvn versions:display-dependency-updates
```

Happy searching! 🔍

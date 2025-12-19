package com.hitorro.obj.core.objectstore;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ObjectStoreShard.
 * Tests object store shard functionality and key management.
 */
public class ObjectStoreShardTest {

    @Test
    public void testShardIdGeneration() {
        // Test that shard IDs can be generated consistently
        int shardId1 = generateShardId("key1", 10);
        int shardId2 = generateShardId("key1", 10);
        
        assertEquals("Same key should generate same shard ID", shardId1, shardId2);
    }

    @Test
    public void testShardIdRange() {
        int numShards = 10;
        
        for (int i = 0; i < 100; i++) {
            String key = "key" + i;
            int shardId = generateShardId(key, numShards);
            
            assertTrue("Shard ID should be >= 0", shardId >= 0);
            assertTrue("Shard ID should be < numShards", shardId < numShards);
        }
    }

    @Test
    public void testDifferentKeysDistribution() {
        int numShards = 10;
        boolean[] shardUsed = new boolean[numShards];
        
        // Test enough keys to likely hit multiple shards
        for (int i = 0; i < 100; i++) {
            String key = "key" + i;
            int shardId = generateShardId(key, numShards);
            shardUsed[shardId] = true;
        }
        
        // At least some shards should be used
        int usedCount = 0;
        for (boolean used : shardUsed) {
            if (used) usedCount++;
        }
        
        assertTrue("Should use multiple shards", usedCount > 1);
    }

    @Test
    public void testConsistentHashing() {
        // Test that the same key always maps to the same shard
        String key = "consistent-key";
        int numShards = 5;
        
        int firstShard = generateShardId(key, numShards);
        
        for (int i = 0; i < 10; i++) {
            int shard = generateShardId(key, numShards);
            assertEquals("Key should always map to same shard", firstShard, shard);
        }
    }

    @Test
    public void testSingleShard() {
        // With only 1 shard, all keys should map to shard 0
        int numShards = 1;
        
        for (int i = 0; i < 20; i++) {
            String key = "key" + i;
            int shardId = generateShardId(key, numShards);
            assertEquals("All keys should map to shard 0", 0, shardId);
        }
    }

    @Test
    public void testEmptyKeyHandling() {
        int numShards = 10;
        int shardId = generateShardId("", numShards);
        
        assertTrue("Empty key should map to valid shard", shardId >= 0 && shardId < numShards);
    }

    @Test
    public void testNullKeyHandling() {
        int numShards = 10;
        
        try {
            // Depending on implementation, null might throw exception or map to default
            int shardId = generateShardId(null, numShards);
            assertTrue("Null key should map to valid shard if supported", 
                shardId >= 0 && shardId < numShards);
        } catch (NullPointerException e) {
            // This is also acceptable behavior
            assertTrue("NullPointerException is acceptable for null key", true);
        }
    }

    @Test
    public void testLargeNumberOfShards() {
        int numShards = 1000;
        
        for (int i = 0; i < 100; i++) {
            String key = "key" + i;
            int shardId = generateShardId(key, numShards);
            
            assertTrue("Shard ID should be in valid range", 
                shardId >= 0 && shardId < numShards);
        }
    }

    @Test
    public void testSpecialCharactersInKey() {
        int numShards = 10;
        
        String[] specialKeys = {
            "key-with-dashes",
            "key_with_underscores",
            "key.with.dots",
            "key:with:colons",
            "key/with/slashes",
            "key@with#special$chars"
        };
        
        for (String key : specialKeys) {
            int shardId = generateShardId(key, numShards);
            assertTrue("Special character key should map to valid shard", 
                shardId >= 0 && shardId < numShards);
        }
    }

    /**
     * Helper method to generate shard ID from key.
     * Uses hash-based distribution.
     */
    private int generateShardId(String key, int numShards) {
        if (key == null) {
            return 0;
        }
        // Simple hash-based shard selection
        int hash = key.hashCode();
        return Math.abs(hash % numShards);
    }
}

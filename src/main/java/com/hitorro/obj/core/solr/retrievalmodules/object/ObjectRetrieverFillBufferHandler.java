package com.hitorro.obj.core.solr.retrievalmodules.object;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.objectstore.JVSObjectStoreClient;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.core.iterator.FillBufferHandler;
import com.hitorro.util.core.string.StringUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectRetrieverFillBufferHandler implements FillBufferHandler<JVS, JVS> {

    private final String collectionName;

    public ObjectRetrieverFillBufferHandler(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * This code must deal with multiple sources for objects based upon domain
     *
     * @param arr
     * @param currentSize
     * @throws IOException
     * @throws HttpException
     */
    @Override
    public void fill(final GenericKeyValue<JVS, JVS>[] arr, final int currentSize) throws IOException, HttpException {
        JVSObjectStoreClient client = new JVSObjectStoreClient();

        Map<String, List<Integer>> map = getIndexPositionsByDomain(arr, currentSize);
        Map<String, Integer> idPositions = idPositions(arr, currentSize);
        for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
            //String url = getUrl(entry.getKey());
            String url = getUrl(collectionName);
            List<JVS> list = getJVSByIndex(arr, entry.getValue());

            AbstractIterator<JVS> iter = client.postStream(url, new CollectionIterator<>(list));

            while (iter.hasNext()) {
                JVS j = iter.next();
                String id = j.getId();
                Integer i = idPositions.get(id);
                if (i != null) {
                    JVS copy = arr[i].getKey().clone();
                    copy.setJVSChild(JVS.docKey, j);
                    arr[i].setValue(copy);
                } else {
                    int xi = 1;
                }
            }
        }
    }

    protected String getUrl(String domain) {
        return "http://localhost:8072/internal/action/object.stream?shard=" + domain;
    }

    /**
     * Translate JVS entries into a map of domain to index positions
     *
     * @param arr
     * @return
     */
    private Map<String, List<Integer>> getIndexPositionsByDomain(GenericKeyValue<JVS, JVS>[] arr,
                                                                 int currentSize) {
        Map<String, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < currentSize; i++) {
            GenericKeyValue<JVS, JVS> row = arr[i];
            String id = row.getKey().getString(JVS.idKey);
            String domain = getDomainFromId(id);
            List<Integer> l = map.get(domain);
            if (l == null) {
                l = new ArrayList();
                map.put(domain, l);
            }
            l.add(i);
        }
        return map;
    }

    String getDomainFromId(String id) {
        if (StringUtil.nullOrEmptyString(id)) {
            return null;
        }
        String[] s = id.split(":");
        return s[0];
    }

    private Map<String, Integer> idPositions(GenericKeyValue<JVS, JVS>[] arr, int currentSize) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < currentSize; i++) {
            map.put(arr[i].getKey().getId(), i);
        }
        return map;
    }

    private List<JVS> getJVSByIndex(GenericKeyValue<JVS, JVS>[] arr, List<Integer> pos) {
        List<JVS> l = new ArrayList<>();
        for (Integer i : pos) {
            l.add(arr[i].getKey());
        }
        return l;
    }
}

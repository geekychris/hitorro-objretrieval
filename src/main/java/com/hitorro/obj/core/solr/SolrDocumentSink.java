package com.hitorro.obj.core.solr;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.SolrService;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.HTException;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.sinks.BaseSink;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.IOUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.String2JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SolrDocumentSink extends BaseSink<JVS> {
    public static final String DEFAULT_POST_URL = "http://localhost:8983/solr/update";
    private final Semaphore available = new Semaphore(1, true);
    private final String2JsonMapper s2j = new String2JsonMapper();
    protected URL solrUrl;
    protected Sink<JsonNode> sink;
    private String coreName;
    private HttpURLConnection urlConnection;

    public SolrDocumentSink() throws MalformedURLException {
        String ip = Env.getHostIP();
        setUrl(new URL(Fmt.S("http://%s:%s/solr/update/json?commit=true", ip, Env.getHTTPPort())));
    }

    public SolrDocumentSink(String coreNameIn, boolean createIndexIfMissing) throws Exception {
        this.coreName = coreNameIn;
        setUrl(Env.getHostIP(), Env.getHTTPPort(), coreName);
        if (createIndexIfMissing) {
            try {
                SolrService.getService().addCore(coreName);
            } catch (IOException e) {
                Log.util.error("Unable to put core %s %e", e, e);
                throw new HTException("", e);
            }
        }
    }

    public SolrDocumentSink(URL url) {
        setUrl(url);
    }

    public void unlock() {
        available.release();
    }

    private void setUrl(String host, int port, String coreName) throws MalformedURLException {
        setUrl(new URL(Fmt.S("http://%s:%s/solr/%s/update/json?commit=true", host, port, coreName)));
    }

    private void setUrl(URL url) {
        if (url != null) {
            this.solrUrl = url;
            return;
        }
        URL u = null;
        try {
            solrUrl = new URL(System.getProperty("url", DEFAULT_POST_URL + "?commit=true"));
        } catch (MalformedURLException e) {
            Log.util.error("System Property 'url' is not a valid URL: %s", u);
        }
    }

    /**
     * Does a simple commit operation
     */
    public void commitIndex() {
        doGet(appendParam(solrUrl.toString(), "commit=true"));
    }


    public void optimizeIndex() {
        doGet(appendParam(solrUrl.toString(), "optimize=true"));
    }

    private String appendParam(String url, String param) {
        return url + (url.indexOf('?') > 0 ? "&" : "?") + param;
    }

    public void doGet(String url) {
        try {
            doGet(new URL(url));
        } catch (MalformedURLException e) {
            Log.util.error("%s", url);
        } catch (HTException e) {
            Log.util.error("%s", url);
        }
    }

    public void doGet(URL url) throws HTException {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (HttpURLConnection.HTTP_OK != connection.getResponseCode()) {
                throw new HTException("%s %s", connection.getResponseCode(), connection.getResponseMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            throw new HTException("Unable to post %s", e);
        }
    }


    @Override
    public boolean init(final JsonNode map) {
        return false;
    }

    public boolean start() {
        try {
            urlConnection = (HttpURLConnection) solrUrl.openConnection();
            try {
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setAllowUserInteraction(false);
                urlConnection.setConnectTimeout(1000000);
                urlConnection.setReadTimeout(1000000);
                urlConnection.setChunkedStreamingMode((int) Constants.MBytes);
                urlConnection.setRequestProperty("Content-type", Constants.APPLICATION_JSON);
            } catch (ProtocolException e) {
                Log.util.error("Unable to configure url connection %s %e", e, e);
            }

            OutputStream os = urlConnection.getOutputStream();
            sink = BaseFileUtil.os2JArraysonSink.apply(os);
            sink.start();
            return true;
        } catch (IOException e) {
            Log.util.error("Unable to open connection %s %e %s", e, e, solrUrl);
        }
        return false;
    }

    public int addList(List<JVS> list) {
        for (JVS jn : list) {
            add(jn);
        }
        return list.size();
    }

    public boolean add(JVS jsonNode) {
        if (jsonNode != null) {
            try {
                return sink.add(jsonNode.getJsonNode());
            } catch (IOException e) {
                Log.util.error("Unable to put element to sink %s %e", e, e);
            } catch (StoreException e) {
                Log.util.error("Unable to put element to sink %s %e", e, e);
            }
        }
        return true;
    }


    public void reportResult(JsonNode node) {
        // should eventually wire this up to a resource context by the way of an addition to sink to include resource context.
    }

    public boolean stop() {
        try {
            sink.stop();
            int code = urlConnection.getResponseCode();
            if (code == 200) {
                InputStream is = urlConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtil.copyStream(is, baos);
                String returned = baos.toString();
                JsonNode result = s2j.apply(returned);
                reportResult(result);
                Log.util.info("%s", returned);
            } else {
                Log.util.info("Solr error code %s", code);
                return false;
            }
        } catch (IOException e) {
            Log.util.error("Unable to consume input stream for SolrSink %s %e", e, e);
        }
        urlConnection.disconnect();
        commitIndex();
        return true;
    }


    public void close() {

    }
}

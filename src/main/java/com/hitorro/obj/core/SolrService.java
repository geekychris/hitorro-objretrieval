package com.hitorro.obj.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.features.FeatureService;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.servlet.ServletService;
import com.hitorro.obj.core.objectstore.ObjectStoreService;
import com.hitorro.obj.core.solr.JVS2JVSEnrichMapper;
import com.hitorro.obj.core.solr.RetrievalContext;
import com.hitorro.obj.core.solr.SolrInfo;
import com.hitorro.obj.core.solr.collection.CollectionConfig;
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalPipeline;
import com.hitorro.obj.core.solr.retrievalmodules.RetrievalPipelineContext;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.FileInfo;
import com.hitorro.util.basefile.fs.FileInfoMapper;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.cache.SingletonCache;
import com.hitorro.util.core.http.JSONHTTPIterableClient;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.CollectionProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.startupframework.Dependency;
import com.hitorro.util.startupframework.phases.ServiceDefinition;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import com.hitorro.util.basefile.tools.EnvBaseFiles;

import java.io.File;
import java.io.IOException;
import java.util.List;

@ServiceDefinition(
        shortName = "solr",
        description = "solr runtime",
        debugCommands = {},
        typeManagedClasses = {},
        uiDirectories = {})
public class SolrService {
    public static StringProperty ContextPath =
            new StringProperty("context path for solr configuration", "solr.contextpath", "/solr");
    public static final String SolrEvent = "SolrAdmin";
    public static final String TemplateKey = "template";
    public static StringProperty langProp = new StringProperty("lang", "", "en");
    public static BooleanProperty asArrayProp = new BooleanProperty("array", "", true);
    public static StringProperty analyzerProp = new StringProperty("", "LOWER,PHONETIC", "en");
    public static StringProperty textProp = new StringProperty("text", "", "This is a test");
    public static SingletonCache<SolrInfo> cache =
            new SingletonCache(true,
                    true, SolrEvent, new SolrInfoMapper(), null);
    public static StringProperty collectionKey = new StringProperty("collection", "", null);
    public static StringProperty queryKey = new StringProperty("search.query", "", null);
    /*


    -javaagent:/Users/chris/.m2/repository/org/aspectj/aspectjweaver/1.9.1/aspectjweaver-1.9.1.jar

                        1.8.13
     */
    public static CollectionProperty<String> analyzersProp = new CollectionProperty("analyzers", "", null, analyzerProp);
    private static SolrService service;
    @Dependency(clazz = ServletService.class)
    ServletService servletService;
    @Dependency(clazz = ObjectStoreService.class)
    ObjectStoreService oss;
    @Dependency(clazz = FeatureService.class)
    FeatureService featureService;
    private String ip;
    private int port;
    private boolean local;
    private SolrAspectInterceptor interceptor;

    public static SolrService getService() {
        return service;
    }

    public static File getFileSolrData() {
        return new File(Env.getHome(), "solrdata");
    }

    public static BaseFile getLocalDir(String coreName) {
        BaseFile bf = EnvBaseFiles.getBaseFile(SolrService.getFileSolrData());
        return bf.getChild(coreName);
    }

    /**
     * Adding a core will also create the appropriate files from the template
     *
     * @param core
     * @return
     * @throws IOException
     */

    @CommandDef(command = "index.addcore", description = "put a solr core")
    public boolean addCore(@DebugArgAno(keyName = "core",
            description = "",
            defaultValue = "",
            argType = ArgType.Regular) String core) throws Exception {
        JVS jvs = getCoreStatus();
        if (jvs == null) {
            // couldnt even get the status, dont continue.
            Log.indexer.info("Couldnt get core stats for core: %s", core);
            return false;
        }
        boolean flag = jvs.pathContainsKey("status", core);
        if (!flag) {
            File solrData = new File(Env.getHome(), Fmt.S("solrdata/%s", core));
            String path = solrData.getAbsolutePath();

            boolean missing = !solrData.exists();

            if (local) {

                if (!solrData.exists()) {
                    Log.indexer.info("Adding core (local) solrdata dir copying: %s", core);
                    File copyFromMe = new File(Env.getHome(), Fmt.S("solrdata/" + TemplateKey));
                    if (copyFromMe.exists()) {
                        FileUtil.copyDirectory(copyFromMe, solrData);
                        Log.indexer.info("Copied core %s from %s to %s", core, copyFromMe, solrData);
                    } else {
                        Log.indexer.error("Unable to copy index structure, template does not exist %s : %s", copyFromMe, core);
                    }
                }
                createSchemaFromTemplate(solrData);
                //http://localhost:8090/solr/admin/cores?action=CREATE&name=coreb&instanceDir=&persist=true
            }
            String url = Fmt.S("http://%s:%s/solr/admin/cores?action=CREATE&name=%s&instanceDir=%s&persist=true&wt=json&dataDir=%s",
                    ip, port, core, path, path);
            JSONHTTPIterableClient client = new JSONHTTPIterableClient();
            JsonNode jn = client.getStream(url).getFirstItemAndClose();
            if (jn == null) {
                return false;
            }

        }
        cache.flushCacheBit();
        return true;
    }

    private boolean createSchemaFromTemplate(File solrData) throws IOException {
        BaseFile sdBF = EnvBaseFiles.getBaseFile(solrData).getChild("conf");
        if (!sdBF.exists()) {
            Log.indexer.error("Directory does not exist to create schema %s", sdBF);
            return false;
        }
        BaseFile target = sdBF.getChild("schema.xml");
        BaseFile fieldTemp = sdBF.getChild("fields-master.xml");
        if (fieldTemp.exists()) {
            int i = 1;
        }
        String fieldTempS = fieldTemp.readString();
        StringBuilder sb = new StringBuilder();
        try {
            applyFields(fieldTempS, sb, "_s", "false");
            applyFields(fieldTempS, sb, "_m", "true");
            BaseFile schemaTemp = sdBF.getChild("schema-template.xml");
            String schemaTempS = schemaTemp.readString();
            JVS on = new JVS().set("dynamic_insert", sb.toString());
            String out = on.resolveJsonVariable(schemaTempS);
            target.writeString(out);
        } catch (PropaccessError e) {
            Log.util.error("Unable to create schema %s %e", e, e);
            return false;
        }


        return true;
    }

    private void applyFields(String fieldTempS, StringBuilder sb, String ext, String multiVal) throws PropaccessError {
        JVS on = new JVS().set("cardinality", ext).set("ismulti", multiVal);

        String out = on.resolveJsonVariable(fieldTempS);
        sb.append(out);
        Console.bprintln(sb);
    }

    @CommandDef(command = "index.removeallcores", description = "put a solr core")
    public boolean removeAllCores() throws Exception {
        Log.indexer.info("Removing all cores");
        List<String> list = getCoreNames();
        for (String core : list) {
            if (TemplateKey.equals(core)) {
                continue;
            }
            if (!removeCore(core)) {
                Log.indexer.error("Unable to remove core %s", core);
                return false;
            }
        }
        return true;
    }

    @CommandDef(command = "index.removecore", description = "remove a solr core")
    public boolean removeCore(@DebugArgAno(keyName = "core",
            description = "",
            defaultValue = "",
            argType = ArgType.Regular) String core) throws Exception {
        Log.indexer.info("Removing core %s", core);
        JVS jvs = getCoreStatus();
        if (jvs == null) {
            // couldnt even get the status, dont continue.
            return false;
        }
        boolean flag = jvs.pathContainsKey("status", core);
        if (flag) {
            String url = Fmt.S("http://%s:%s/solr/admin/cores?action=UNLOAD&core=%s&persist=true&deleteIndex=true&wt=json",
                    ip, port, core);
            JSONHTTPIterableClient client = new JSONHTTPIterableClient();
            JsonNode jn = client.getStream(url).getFirstItemAndClose();
            if (jn == null) {
                Log.indexer.info("Unable to remove core, no status for unload operation core %s", core);
                return false;
            }
            Log.indexer.info("Remove core %s called Unload", core);

            File solrData = new File(Env.getHome(), Fmt.S("solrdata/%s", core));
            if (local) {
                Log.indexer.info("deleting core directory for  core %s", core);
                FileUtil.deleteDirectoryContent(solrData, true);
            }
        }
        cache.flushCacheBit();
        return true;
    }

    @CommandDef(command = "index.getcores", description = "get the list of cores")
    public List<String> getCoreNames() throws Exception {
        JVS jvs = getCoreStatus();
        if (jvs != null) {
            return jvs.getKeys("status");
        }
        return null;
    }

    public JVS getCoreStatus() throws Exception {
        String url = Fmt.S("http://%s:%s/solr/admin/cores?action=STATUS&wt=json", ip, port);
        JSONHTTPIterableClient client = new JSONHTTPIterableClient();
        JsonNode jn = client.getStream(url).getFirstItemAndClose();
        if (jn == null) {
            return null;
        }
        return new JVS(jn);
    }

    @CommandDef(command = "index.doccount", description = "number of documents in a core")
    public Integer getDocCount(@DebugArgAno(keyName = "core",
            description = "",
            defaultValue = "",
            argType = ArgType.Regular) String core) {
        IntegerProperty docCountProperty = new IntegerProperty("", "status." + core + ".index.numDocs", null);
        try {
            JVS jvs = getCoreStatus();
            if (jvs != null) {
                JsonNode statusJsonNode = jvs.getJsonNode();
                return docCountProperty.apply(statusJsonNode);
            }
        } catch (Exception e) {

        }
        return 0;
    }

    public void init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        service = this;
        ip = Env.getHostIP();
        port = Env.getHTTPPort();
        this.local = true;

        File warPath = Env.getSolrConfigRootFile();
        File solrData = getFileSolrData();
        if (!solrData.exists()) {
            solrData.mkdir();
            BaseFile a = EnvBaseFiles.getDataBaseFile().getChild("solrmaster");
            if (!a.exists()) {
                Log.util.fatal("Unable to copy solrmaster, cant find solrmaster");
                return;
            }
            File aF = a.getLocalFileIfPossible();
            try {
                FileUtil.copyDirectory(aF, solrData);
            } catch (IOException e) {
                Log.util.fatal("Unable to copy solrmaster %s %e", e, e);
                return;
            }
        }
        System.setProperty("solr.solr.home", solrData.getAbsolutePath());
        System.setProperty("solr.data.dir", solrData.getAbsolutePath());

        Log.util.info("WAR path: %s", warPath);

        String ct = ContextPath.apply();
        Log.util.info("Context path is: %s", ct);
        // XXX TODO readd
        //String e = servletService.addWebApplication(warPath.getAbsolutePath(), ct);
        String e = null;
        if (e == null) {


            TestAspect ta = new TestAspect();
            Test t = new Test();
            String v = t.hello();
            String v2 = t.hello1(12);


            interceptor = new SolrAspectInterceptor();

            Log.util.info("Solr initialized.");
            return;
        }
        throw new RuntimeException(e);

    }

    @CommandDef(command = "enrich", description = "enrich", restOperations = {RestOperations.Get, RestOperations.Post})
    public boolean enrich(
            @DebugArgAno(keyName = "in",
                    argType = ArgType.JVSRequestIterator) AbstractIterator<JVS> iter,
            @DebugArgAno(keyName = "out",
                    argType = ArgType.JVSResponseSink) Sink<JVS> sink) throws Exception {
        JVS2JVSEnrichMapper mapper = new JVS2JVSEnrichMapper();
        iter.map(mapper).sink(sink);
        return true;
    }

    @CommandDef(command = "fi", description = "enrich", restOperations = {RestOperations.Get, RestOperations.Post})
    public boolean fi(
            @DebugArgAno(keyName = "in",
                    argType = ArgType.JVSRequestIterator) AbstractIterator<JVS> iter,
            @DebugArgAno(keyName = "out",
                    argType = ArgType.JVSResponseSink) Sink<JVS> sink) {

        BaseFile f = EnvBaseFiles.getBaseFile(new File("/Users/chris/pics"));
        try {
            AbstractIterator<FileInfo> t = com.hitorro.util.core.iterator.helpers.BaseFileIterators.list(f).map(FileInfoMapper.me);
            t.map(fi -> new JVS(fi.getAsJsonNode())).sink(sink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @CommandDef(command = "search", description = "put a solr core", restOperations = {RestOperations.Get, RestOperations.Post})
    public boolean search(
            @DebugArgAno(keyName = "in",
                    argType = ArgType.JVSRequestIterator) AbstractIterator<JVS> iter,
            @DebugArgAno(keyName = "out",
                    argType = ArgType.JVSResponseSink) Sink<JVS> sink) throws Exception {
        JVS query = iter.getFirstItem();
        SolrQuery sq = new SolrQuery();
        String collection = collectionKey.apply(query.getJsonNode());
        CollectionConfig cc = CollectionConfig.configCache.get(collection);
        String lang = "en";

        RetrievalContext rc = new RetrievalContext(cc, lang, sq);
        rc.setCollectionName(collection);
        RetrievalPipeline rp = RetrievalPipelineContext.me().get(query, rc);
        AbstractIterator<JVS> siter = rp.constructChain(query, null, rc);

        siter.sink(sink, true, false);
        rp.applyAggregates(sink, rc);
        sink.stop();
        return true;
    }


    private HttpSolrClient getClient() {
        final String solrUrl = "http://localhost:8072/solr";
        return new HttpSolrClient.Builder(solrUrl)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }
}

class SolrInfoMapper extends BaseMapper<Object, SolrInfo> {
    @Override
    public SolrInfo apply(Object o) {
        try {
            return new SolrInfo(SolrService.getService());
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

package com.hitorro.obj.core.objectstore;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.Json2JVSMapper;
import com.hitorro.network.servlet.ServletService;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.commandandcontrol.RedirectHttp;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.JSONIterator;
import com.hitorro.util.io.largedata.compressedstreams.OutputOutputStream;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.startupframework.Dependency;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import javax.servlet.http.HttpServletRequest;
import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@ServiceDefinition(
        shortName = "objectstore",
        description = "object store runtime",
        debugCommands = {},
        typeManagedClasses = {},
        uiDirectories = {})
public class ObjectStoreService {
    public static StringProperty shardKey = new StringProperty("shard", "", null);
    public static ObjectStoreService oss;
    private final Map<String, ObjectStoreShard> shards = new HashMap();
    @Dependency(clazz = ServletService.class)
    ServletService servletService;

    @CommandDef(command = "object.t", description = "x")
    public static RedirectHttp t(@DebugArgAno(argType = ArgType.Raw) String raw,
                                 @DebugArgAno(argType = ArgType.Args) JVS args,
                                 @DebugArgAno(argType = ArgType.Uri) String uri,
                                 @DebugArgAno(argType = ArgType.Request) HttpServletRequest req
    ) throws Exception {
        BaseFile bf = Env.getBaseFile(new File("/Users/chris/wikienrich.json.bz2"));
        String shardName = "test";
        testWriteRead(bf, shardName);
        return null;
    }

    private static void testWriteRead(final BaseFile bf, final String shardName) throws java.io.IOException {
        AtomicLong counter = new AtomicLong();
        ObjectStoreShard oss = ObjectStoreService.oss.getShard(shardName);
        new JSONIterator(bf.getReader()).map(Json2JVSMapper.me).
                skipNTakeM(0, 10, false).
                count(counter, 100000).
                //mapParallel(oss.getCompressionMapper(),100, 1000).
                        map(oss.getCompressionMapper()).
                sink(oss.getCompressedSink().maxPerTransaction(10000));


        AbstractIterator<JVS> it = new JSONIterator(bf.getReader()).map(Json2JVSMapper.me).
                skipNTakeM(0, 10, false);

        for (JVS jvs : it.iterator()) {
            String id = jvs.getId();
            try {
                JVS j2 = oss.get(id);
                Console.println("Got it %s vs %s", j2.getId(), id);
            } catch (Exception e) {
                Console.println("Got it %s", id);
            }

        }
    }

    public void init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        oss = this;
    }

    private File getObjectStoreDir() {
        return new File(Env.getData(), "objectstore");
    }

    public ObjectStoreShard getShard(String name) {
        ObjectStoreShard shard = shards.get(name);
        if (shard == null) {
            File dir = new File(getObjectStoreDir(), name);
            shard = new ObjectStoreShard(dir);
            shards.put(name, shard);
        }
        return shard;
    }

    @CommandDef(command = "object.stream", description = "get a stream of objects from the Object store", restOperations = {RestOperations.Get, RestOperations.Post})
    public boolean getCollectionAsStreamOfBytes(
            @DebugArgAno(keyName = "in",
                    description = "",
                    defaultValue = "",
                    argType = ArgType.JVSRequestIterator) AbstractIterator<JVS> iter,
            @DebugArgAno(keyName = "out",
                    description = "",
                    defaultValue = "",
                    argType = ArgType.OutputStream) OutputStream os,
            @DebugArgAno(keyName = "out",
                    description = "",
                    defaultValue = "",
                    argType = ArgType.Args) JVS args) throws Exception {
        String shard = shardKey.apply(args);
        ObjectStoreShard s = getShard(shard);
        OutputOutputStream oos = new OutputOutputStream(new DataOutputStream(os));
        s.getValuesAsByteStream(iter, oos);
        oos.flush();
        os.flush();
        return true;
    }
}

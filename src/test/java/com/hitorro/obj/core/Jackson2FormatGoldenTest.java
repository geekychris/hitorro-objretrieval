package com.hitorro.obj.core;
import tools.jackson.databind.*; import tools.jackson.databind.node.*;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.SolrService; import com.hitorro.obj.core.TypeFieldMapper;
import com.hitorro.obj.core.objectstore.*;
import com.hitorro.obj.core.solr.*; import com.hitorro.obj.core.solr.collection.*; import com.hitorro.obj.core.solr.queryvisitors.*;
import com.hitorro.obj.core.solr.retrievalmodules.facet.FacetRetriever; import com.hitorro.obj.core.solr.retrievalmodules.fixup.FixupRetriever;
import com.hitorro.obj.core.solr.retrievalmodules.cluster.ClusteringIterator;
import com.hitorro.util.core.iterator.AbstractIterator; import com.hitorro.util.core.iterator.CollectionIterator; import com.hitorro.util.core.iterator.sinks.BaseSink; import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import org.apache.solr.client.solrj.SolrQuery; import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument; import org.apache.solr.common.SolrDocumentList; import org.apache.solr.common.util.NamedList; import org.apache.solr.common.util.SimpleOrderedMap;
import com.sun.net.httpserver.HttpServer;
import java.io.*; import java.math.*; import java.net.*; import java.nio.charset.StandardCharsets; import java.nio.file.*; import java.util.*;
/**
 * Pins what hitorro-objretrieval stores and sends to what 3.0.3 did on Jackson 2.18: the compressed JSON
 * bytes the Xodus object store keeps (and reading them back), the object-store wire stream and sink, the
 * JSON array SolrDocumentSink posts to Solr, SolrService's core admin calls, Solr responses turned into JSON
 * (SolrJSONUtils, SolrIterator aggregate, facets), the Solr parameters every query visitor produces from a
 * JVS query, fixups, clustering and collection configs. {@code jackson2-objretrieval.txt} is the output of
 * the same code run on the 3.0.3 sources (generator: ~/hitorro-j3/tools/Jackson2ObjretrievalGolden.java.txt
 * in the migration workspace). StringNode is mapped back to TextNode before comparing.
 */
public class Jackson2FormatGoldenTest {
  static final Map<String, String> out = new LinkedHashMap<>();
  static void p(String k, Object v){ out.put(k, String.valueOf(v).replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r")); }
  static boolean jackson(Throwable t){ return t.getClass().getName().startsWith("com.fasterxml.jackson") || t.getClass().getName().startsWith("tools.jackson"); }
  static String desc(Throwable t){ String s = (t instanceof IOException ? "IOE:" : "RTE:") + (jackson(t) ? "jackson" : t.getClass().getSimpleName());
    if (!jackson(t) && t.getCause() != null) s += "<" + (jackson(t.getCause()) ? "jackson" : t.getCause().getClass().getSimpleName()) + ">"; return s; }
  static String tryIt(java.util.concurrent.Callable<Object> c){ try { return String.valueOf(c.call()); } catch(Throwable t){ if (System.getenv("OG_TRACE") != null) t.printStackTrace(); return "!"+desc(t); } }
  static String hex(byte[] b, int len){ StringBuilder s = new StringBuilder(); for (int i = 0; i < len; i++) s.append(String.format("%02x", b[i])); return s.toString(); }
  static byte[] unhex(String h){ byte[] b = new byte[h.length() / 2]; for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(h.substring(2 * i, 2 * i + 2), 16); return b; }
  // node classes of a tree (TextNode/StringNode are mapped by the test)
  static String sig(JsonNode n){ if (n == null) return "null"; if (n.isObject()) { StringBuilder sb = new StringBuilder("{"); Iterator<String> it = n.propertyNames().iterator(); while (it.hasNext()) { String k = it.next(); sb.append(k).append(':').append(sig(n.get(k))).append(it.hasNext() ? "," : ""); } return sb.append('}').toString(); }
    if (n.isArray()) { StringBuilder sb = new StringBuilder("["); for (int i = 0; i < n.size(); i++) sb.append(i > 0 ? "," : "").append(sig(n.get(i))); return sb.append(']').toString(); }
    return n.getClass().getSimpleName(); }
  // JsonNode.toString() fails past 500 levels on Jackson 3 (2.x: 1000), so print trees with the mapper
  static final ObjectMapper W = com.hitorro.util.json.JsonMappers.json();
  static String str(JsonNode n){ if (n == null) return "null"; try { return W.writeValueAsString(n); } catch (Exception e) { throw new RuntimeException(e); } }
  static String jvs(JVS j){ if (j == null) return "null"; try { return str(j.getJsonNode()) + " | " + sig(j.getJsonNode()); } catch (Exception e) { return "!print:" + desc(e); } }
  static String deep(int d){ StringBuilder sb = new StringBuilder(); for (int i = 0; i < d; i++) sb.append("{\"a\":"); sb.append(1); for (int i = 0; i < d; i++) sb.append('}'); return sb.toString(); }
  static void reset(Class<?> c, String f) throws Exception { java.lang.reflect.Field x = c.getDeclaredField(f); x.setAccessible(true); Object o = x.get(null);
    if (o instanceof java.util.concurrent.atomic.AtomicLong al) al.set(0); else ((java.util.concurrent.atomic.AtomicInteger) o).set(0); }

  static final String[] DOCS = {
    "{\"id\":{\"id\":\"news:d1\",\"did\":\"d1\",\"domain\":\"news\"},\"title\":\"First Doc\",\"count\":12,\"score\":1.10,\"flag\":true,\"tags\":[\"a\",\"b\"],\"meta\":{\"k\":\"v\",\"z\":null},\"big\":12345678901234567890,\"dec\":123456789012345678901234567890.5,\"e\":1e30,\"neg\":-0.0,\"s\":\"\u00e9/\u2028\\\"<tag>&\",\"emoji\":\"\uD83D\uDE00\"}",
    "{\"id\":{\"id\":\"x:2\"},\"empty\":{},\"arr\":[],\"nested\":[[1,2],[{\"q\":[null,false]}]],\"ctl\":\"\\u0000\\t\\n\",\"long\":9223372036854775807,\"int\":-2147483648}",
    "{\"id\":{\"id\":\"x:3\"},\"z\":1,\"a\":2,\"m\":{\"b\":1,\"a\":2}}",
    "{\"id\":{\"id\":\"x:deep\"},\"d\":" + deep(600) + "}",
    "{\"id\":{\"id\":\"x:toodeep\"},\"d\":" + deep(1100) + "}",
  };

  // A RetrievalContext whose field translation is deterministic and needs no type system
  static class RC extends RetrievalContext {
    RC(SolrQuery q) { super(new CollectionConfig(), "en", q); setCollectionName("coll"); }
    @Override public String getTranslatedField(String s) { return s == null ? null : "T(" + s + ")"; }
    @Override public String getMappedField(String s) { return "M(" + s + ")"; }
    @Override public String getPathForFeature(String s) { return "F(" + s + ")"; }
    @Override public TypeFieldMapper.FieldType getFieldType(Propaccess a) { return String.valueOf(a).startsWith("feat") ? TypeFieldMapper.FieldType.FeatureField : TypeFieldMapper.FieldType.TypeField; }
  }
  static String rc(RetrievalContext rc){ List<String> al = new ArrayList<>(); for (SelectRow r : rc.getAliases()) al.add(r.getField() + "/" + r.getAlias());
    StringBuilder ps = new StringBuilder(); Iterator<String> pn = rc.getSolrQuery().getParameterNamesIterator(); while (pn.hasNext()) { String k = pn.next(); ps.append(k).append("=").append(Arrays.toString(rc.getSolrQuery().getParams(k))).append("&"); }
    return "params=" + ps + " aliases=" + al + " results=" + rc.results + " errors=" + rc.getErrors().hasErrors(); }
  static final String[] QUERIES = {
    "{}",
    "{\"search\":{\"query\":[{\"query\":\"title:a\"},{\"query\":\"b\",\"must\":true},{\"query\":\"c\",\"must\":\"true\"},{\"query\":7},{\"must\":false}],\"filter\":[\"type:x\",5,null,{\"a\":1},true],\"rows\":15}}",
    "{\"search\":{\"query\":{\"query\":\"single\"},\"filter\":\"one:filter\",\"rows\":\"25\"}}",
    "{\"search\":{\"rows\":15.7,\"sort\":[{\"field\":\"date\",\"dir\":\"asc\"},{\"field\":\"feat_rank\"},{\"field\":\"x\",\"dir\":3},{\"dir\":\"asc\"}],\"debug\":true,\"timing\":false}}",
    "{\"search\":{\"rows\":true,\"sort\":{\"field\":\"one\"},\"debug\":\"true\",\"timing\":\"false\"}}",
    "{\"search\":{\"rows\":null,\"select\":[\"title\",\"max(a) as m\",\"has as\",{\"field\":\"body\",\"alias\":\"b\"},{\"field\":\"nofield\"},{\"alias\":\"x\"},3,null],\"debug\":1}}",
    "{\"search\":{\"select\":\"title as t\",\"rows\":-3}}",
    "{\"search\":{\"facets\":[{\"field\":\"cat\",\"limit\":10},{\"field\":\"price\",\"isrange\":true,\"limit\":\"5\",\"sort\":\"index\"},{\"field\":\"when\",\"isdate\":\"true\",\"sort\":\"\"},{\"field\":3,\"limit\":2.5}]},\"gap\":\"+1DAY\",\"start\":0,\"end\":\"NOW\"}",
    "{\"search\":{\"facets\":{\"field\":\"only\",\"isdate\":true}},\"gap\":10,\"start\":\"2020\",\"end\":null}",
    "{\"search\":{\"facets\":\"notanobject\",\"query\":\"str\",\"filter\":{\"x\":1},\"sort\":\"str\",\"select\":{\"field\":\"f\",\"alias\":\"a\"}}}",
    "{\"search\":[1,2]}",
  };

  static NamedList<Object> nl(Object... kv){ NamedList<Object> n = new NamedList<>(); for (int i = 0; i < kv.length; i += 2) n.add((String) kv[i], kv[i + 1]); return n; }
  static SimpleOrderedMap<Object> som(Object... kv){ SimpleOrderedMap<Object> n = new SimpleOrderedMap<>(); for (int i = 0; i < kv.length; i += 2) n.add((String) kv[i], kv[i + 1]); return n; }

  public static Map<String, String> compute(Path dir) throws Exception {
    out.clear();
    TimeZone tz = TimeZone.getDefault(); TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    try { computeAux(dir); } finally { TimeZone.setDefault(tz); }
    return out;
  }
  static void computeAux(Path dir) throws Exception {
    ObjectMapper om = com.hitorro.util.json.JsonMappers.json();
    // compression dictionaries (hitorro-all data/compression_dictionaries/1.txt; 0 is the wire version)
    Path dicts = dir.resolve("data").resolve("compression_dictionaries"); Files.createDirectories(dicts);
    for (String v : new String[]{"0", "1"}) Files.writeString(dicts.resolve(v + ".txt"), "the quick brown fox jumped over the lazy dogs\n");
    System.setProperty("HT_DATA", dir.resolve("data").toString());
    reset(Sort.class, "sortFieldCounter"); reset(SelectRow.class, "intCounter");
    // ---- object store: the bytes stored in Xodus and reading them back
    ObjectStoreShard shard = new ObjectStoreShard(dir.resolve("store").toFile());
    for (int i = 0; i < DOCS.length; i++) { final int fi = i;
      JVS doc = tryParse(om, DOCS[i]);
      p("store.parse." + i, tryIt(() -> sig(doc.getJsonNode()).replace("StringNode", "TextNode").length()));
      p("store.bytes." + i, tryIt(() -> { jetbrains.exodus.ByteIterable bi = shard.getBytes(doc).getValue(); return hex(bi.getBytesUnsafe(), bi.getLength()); }));
      p("store.write." + i, tryIt(() -> { shard.write(doc); return "ok"; }));
      p("store.get." + i, tryIt(() -> jvs(shard.get(doc.getId()))));
      p("store.util0." + i, tryIt(() -> { jetbrains.exodus.ArrayByteIterable b = ObjectStoreUtil.getArrayByteIterableFromJVS(doc, 0); return hex(b.getBytesUnsafe(), b.getLength()); }));
    }
    p("store.get.missing", tryIt(() -> jvs(shard.get("nope"))));
    p("store.frombytes.garbage", tryIt(() -> jvs(ObjectStoreShard.getJvsFromBytes(new byte[]{1, 2, 3}, 3))));
    // ---- wire: the stream the object-store client reads (InputStream2CompressedJVS)
    p("wire.is2jvs", tryIt(() -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(bo);
      for (int i = 0; i < 3; i++) { jetbrains.exodus.ArrayByteIterable b = ObjectStoreUtil.getArrayByteIterableFromJVS(tryParse(om, DOCS[i]), 0); dos.writeLong(b.getLength()); dos.write(b.getBytesUnsafe(), 0, b.getLength()); }
      dos.writeLong(0); dos.flush(); String h = hex(bo.toByteArray(), bo.size());
      AbstractIterator<JVS> it = JVSObjectStoreClient.is2jvs.apply(new ByteArrayInputStream(bo.toByteArray())); List<String> l = new ArrayList<>(); while (it.hasNext()) l.add(jvs(it.next())); return h + " -> " + l; }));
    // ---- wire: what the object-store client sends (os2JVSSink)
    p("wire.os2sink", tryIt(() -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); BaseSink<JVS> s = JVSObjectStoreClient.os2JVSSink.apply(bo);
      s.init(null); s.start(); for (int i = 0; i < 4; i++) s.add(tryParse(om, DOCS[i])); s.stop(); return bo.toString(StandardCharsets.UTF_8); }));
    // ---- Solr: documents posted by SolrDocumentSink, and the Solr admin calls of SolrService
    List<String> reqs = new ArrayList<>();
    HttpServer srv = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    srv.createContext("/", ex -> { String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); String q = String.valueOf(ex.getRequestURI().getQuery());
      synchronized (reqs) { reqs.add(ex.getRequestMethod() + " " + ex.getRequestURI().getPath() + "?" + q.replaceAll("(instanceDir|dataDir)=[^&]*", "$1=P") + " " + body); }
      String resp = "{\"responseHeader\":{\"status\":0,\"QTime\":1}}";
      if (q.contains("action=STATUS")) resp = "{\"responseHeader\":{\"status\":0},\"status\":{\"c1\":{\"name\":\"c1\",\"index\":{\"numDocs\":42}},\"c2\":{\"name\":\"c2\",\"index\":{\"numDocs\":\"7\"}},\"template\":{}}}";
      byte[] rb = resp.getBytes(StandardCharsets.UTF_8); ex.sendResponseHeaders(200, rb.length); ex.getResponseBody().write(rb); ex.close(); });
    srv.start();
    try {
      int port = srv.getAddress().getPort();
      p("solr.sink", tryIt(() -> { reqs.clear(); SolrDocumentSink s = new SolrDocumentSink(new URL("http://127.0.0.1:" + port + "/solr/c1/update/json?commit=true"));
        s.init(null); boolean st = s.start(); for (int i = 0; i < 4; i++) s.add(tryParse(om, DOCS[i])); s.add(null); boolean sp = s.stop(); return st + "," + sp + " " + reqs; }));
      SolrService svc = new SolrService();
      for (String[] f : new String[][]{{"ip", "127.0.0.1"}, {"port", String.valueOf(port)}}) { java.lang.reflect.Field x = SolrService.class.getDeclaredField(f[0]); x.setAccessible(true); if (f[0].equals("port")) x.setInt(svc, port); else x.set(svc, f[1]); }
      p("solr.status", tryIt(() -> { reqs.clear(); return jvs(svc.getCoreStatus()) + " " + reqs; }));
      p("solr.cores", tryIt(() -> svc.getCoreNames()));
      for (String c : new String[]{"c1", "c2", "c3"}) p("solr.doccount." + c, tryIt(() -> svc.getDocCount(c)));
      p("solr.addcore.c3", tryIt(() -> { reqs.clear(); return svc.addCore("c3") + " " + reqs; }));
      p("solr.addcore.c1", tryIt(() -> { reqs.clear(); return svc.addCore("c1") + " " + reqs; }));
      p("solr.removecore.c2", tryIt(() -> { reqs.clear(); return svc.removeCore("c2") + " " + reqs; }));
    } finally { srv.stop(0); }
    // ---- Solr responses -> JSON (SolrJSONUtils)
    Object[] vals = {"s", 1, 2L, 1.5f, 2.25d, 0.1f, true, null, new Date(0), new BigDecimal("1.10"), new BigInteger("12345678901234567890"), new byte[]{1, 2}, UUID.fromString("00000000-0000-0000-0000-000000000001"),
      Arrays.asList(1, "a", null, som("x", 1)), som("n", som("m", Arrays.asList(1.0, 2.5f))), nl("a", 1, "a", 2), new HashMap<>(Map.of("k", 1)), new Object[]{1, "a"}, (short) 3, (byte) 4, 'c', Double.NaN, Float.POSITIVE_INFINITY, "\u00e9\uD83D\uDE00", new StringBuilder("sb")};
    for (int i = 0; i < vals.length; i++) { Object v = vals[i]; p("solrmap." + i, tryIt(() -> { JsonNode n = SolrJSONUtils.getSolrMap2Json(v); return str(n) + " | " + sig(n); })); }
    p("solrmap.simple", tryIt(() -> { ObjectNode n = JsonNodeFactory.instance.objectNode(); SolrJSONUtils.simpleMapToJsonNode(som("b", 1, "a", Arrays.asList("x"), "b", 2), n); return str(n) + " | " + sig(n); }));
    // ---- Solr QueryResponse -> result rows and aggregates (SolrIterator), facets (FacetRetriever)
    SolrDocumentList sdl = new SolrDocumentList(); sdl.setNumFound(2); sdl.setStart(0); sdl.setMaxScore(1.5f);
    SolrDocument d1 = new SolrDocument(); d1.setField("id.id", "news:d1"); d1.setField("title_s", "Hello"); d1.setField("score", 1.5f); d1.setField("n", 3L); d1.setField("when", new Date(0)); d1.addField("multi", "a"); d1.addField("multi", "b"); sdl.add(d1);
    SolrDocument d2 = new SolrDocument(); d2.setField("id.id", "x:2"); d2.setField("score", 0.5f); d2.setField("b", true); d2.setField("dec", 2.5d); sdl.add(d2);
    NamedList<Object> resp = nl("responseHeader", nl("status", 0, "QTime", 3), "response", sdl,
      "debug", nl("timing", som("time", 2.0d, "prepare", som("time", 1.0d)), "explain", som("news:d1", "1.5 = weight(title)", "x:2", som("value", 0.5))),
      "facet_counts", nl("facet_queries", nl(), "facet_fields", nl("cat", nl("a", 3L, "ANY_VALUE", 9L, "b", 1L), "empty", nl()),
        "facet_ranges", nl("price", nl("counts", nl("0", 2, "10", 5), "gap", 10, "start", 0, "end", 20))));
    QueryResponse qr = new QueryResponse(); qr.setResponse(resp); qr.setElapsedTime(5);
    p("solriter.rows", tryIt(() -> { SolrIterator it = new SolrIterator(qr); List<String> l = new ArrayList<>(); while (it.hasNext()) l.add(jvs(it.next())); return l; }));
    p("solriter.aggregate", tryIt(() -> { RC c = new RC(new SolrQuery()); return jvs(new SolrIterator(qr).getAggregate(c)); }));
    p("facets.aggregate", tryIt(() -> { RC c = new RC(new SolrQuery()); c.qr = qr; JVS q = tryParse(om, "{\"search\":{\"facets\":[{\"field\":\"cat\"}]}}");
      FacetRetriever fr = new FacetRetriever(); boolean part = fr.getParticipate(q, c); fr.appendRetriever(null, q, c); List<String> l = new ArrayList<>(); c.addAggregates(new BaseSink<JVS>() { public boolean init(JsonNode n) { return true; } public boolean start() { return true; } public boolean add(JVS j) { l.add(jvs(j)); return true; } public boolean stop() { return true; } }); return part + " " + l; }));
    // ---- JVS query -> Solr query parameters (query visitors)
    QueryVisitor[] vs = {new Query(), new Filter(), new Rows(), new Sort(), new Select(), new Debug(), new Group(), new Facets()};
    for (int i = 0; i < QUERIES.length; i++) { JVS q = tryParse(om, QUERIES[i]);
      for (QueryVisitor v : vs) p("visit." + i + "." + v.getClass().getSimpleName(), tryIt(() -> { RC c = new RC(new SolrQuery()); v.visit(q, c); v.finalize(q, c); return rc(c); }));
      p("visit." + i + ".all", tryIt(() -> { RC c = new RC(new SolrQuery()); for (QueryVisitor v : vs) v.visit(q, c); for (QueryVisitor v : vs) v.finalize(q, c); return rc(c); }));
      p("visit." + i + ".participate", tryIt(() -> new FacetRetriever().getParticipate(q, new RC(new SolrQuery())) + "," + new FixupRetriever().getParticipate(q, new RC(new SolrQuery()))));
    }
    // ---- fixup (remove/add fields) on result rows
    String[] fixups = {"{\"fixup\":{\"remove\":[\"doc.title\",\"doc.meta.k\"],\"add\":[\"doc.count\"]}}", "{\"fixup\":{\"remove\":\"doc.flag\"}}", "{\"fixup\":{\"remove\":[1,null]}}", "{\"fixup\":{}}"};
    for (int i = 0; i < fixups.length; i++) { String f = fixups[i]; p("fixup." + i, tryIt(() -> { JVS q = tryParse(om, f); List<JVS> rows = new ArrayList<>();
      for (int k = 0; k < 2; k++) { JVS r = new JVS(); r.setJVSChild(JVS.docKey, tryParse(om, DOCS[k])); rows.add(r); }
      AbstractIterator<JVS> it = new FixupRetriever().appendRetriever(new CollectionIterator<>(rows), q, new RC(new SolrQuery())); List<String> l = new ArrayList<>(); while (it.hasNext()) l.add(jvs(it.next())); return l; })); }
    // ---- clustering aggregate (Carrot2 over result rows)
    for (String alg : new String[]{"lingo.LingoClusteringAlgorithm", "kmeans.BisectingKMeansClusteringAlgorithm"}) p("cluster." + alg, tryIt(() -> { List<JVS> rows = new ArrayList<>(); String[] t = {"apple pie recipe", "apple tart recipe", "car engine repair", "car engine oil", "apple cake recipe", "engine repair manual"};
      for (int k = 0; k < t.length; k++) { JVS r = new JVS(); r.setJVSChild(JVS.docKey, tryParse(om, "{\"id\":{\"id\":\"c:" + k + "\"},\"title\":\"" + t[k] + "\",\"body\":\"" + t[k] + " " + t[k] + "\",\"url\":\"http://x/" + k + "\"}")); rows.add(r); }
      ClusteringIterator ci = new ClusteringIterator(new CollectionIterator<>(rows), "title", "body", "url", "recipe", true, Class.forName("org.carrot2.clustering." + alg), "lingo");
      while (ci.hasNext()) ci.next(); return jvs(ci.getAggregate(new RC(new SolrQuery()))); }));
    // ---- collection config (intents)
    String[] ccs = {"{\"name\":\"news\",\"lang\":\"de\",\"intents\":{\"sniff\":{\"class\":\"com.hitorro.obj.core.solr.collection.modulators.SniffModulator\"},\"both\":[{\"class\":\"com.hitorro.obj.core.solr.collection.modulators.SniffModulator\"},{\"class\":\"com.hitorro.obj.core.solr.collection.modulators.AnswerTypeModulator\"},{\"class\":\"no.such.Class\"},{}],\"bad\":5}}",
      "{\"intents\":[1]}", "{}"};
    for (int i = 0; i < ccs.length; i++) { String c = ccs[i]; p("collection." + i, tryIt(() -> { CollectionConfig cc = new JsonNode2CollectionConfigMapper().apply(om.readTree(c)); List<String> l = new ArrayList<>();
      for (String in : new String[]{"sniff", "both", "bad", "none"}) { CollectionIntent ci = cc.getIntent(in); List<String> m = new ArrayList<>(); if (ci != null) for (Object o : ci.get()) m.add(o.getClass().getSimpleName()); l.add(in + "=" + (ci == null ? "null" : m)); }
      return cc.getName() + " " + l; })); }
  }
  static JVS tryParse(ObjectMapper om, String s) { try { return new JVS(om.readTree(s)); } catch (Exception e) { return new JVS(JsonNodeFactory.instance.objectNode().put("parseError", jackson(e) ? "jackson" : e.getClass().getSimpleName())); } }
  static Map<String, String> load() throws IOException {
    Map<String, String> m = new LinkedHashMap<>();
    try (InputStream in = Jackson2FormatGoldenTest.class.getResourceAsStream("jackson2-objretrieval.txt")) {
      for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
        if (line.isEmpty()) continue;
        int i = line.indexOf('=');
        m.put(line.substring(0, i), line.substring(i + 1));
      }
    }
    return m;
  }
  static String esc(String v){ return v.replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r"); }

  @org.junit.Rule public org.junit.rules.TemporaryFolder tmp = new org.junit.rules.TemporaryFolder();

  // run in a fresh thread: CompressorCache keeps one compressor (with its dictionary) per thread
  static <T> T inThread(java.util.concurrent.Callable<T> c) throws Exception {
    Object[] res = new Object[2];
    String old = System.getProperty("HT_DATA");
    Thread t = new Thread(() -> { try { res[0] = c.call(); } catch (Throwable e) { res[1] = e; } });
    t.start(); t.join();
    if (old == null) System.clearProperty("HT_DATA"); else System.setProperty("HT_DATA", old);
    if (res[1] instanceof Exception e) throw e;
    if (res[1] instanceof Error e) throw e;
    @SuppressWarnings("unchecked") T r = (T) res[0];
    return r;
  }

  @org.junit.Test
  public void matchesJackson2Output() throws Exception {
    Map<String, String> expected = load();
    Path dir = tmp.newFolder("golden").toPath();
    Map<String, String> actual = new LinkedHashMap<>();
    inThread(() -> compute(dir)).forEach((k, v) -> actual.put(k, v.replace("StringNode", "TextNode")));
    org.junit.Assert.assertEquals(186, expected.size());
    org.junit.Assert.assertEquals(new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()));
    for (String k : expected.keySet()) org.junit.Assert.assertEquals(k, expected.get(k), actual.get(k));
  }

  /** Values the 3.0.3 object store wrote (the golden hex) are read back by 4.0.0 exactly as 3.0.3 read them. */
  @org.junit.Test
  public void readsObjectStoreBytesWrittenBy3x() throws Exception {
    Map<String, String> expected = load();
    Path dir = tmp.newFolder("readback").toPath();
    inThread(() -> {
      Path dicts = dir.resolve("data").resolve("compression_dictionaries"); Files.createDirectories(dicts);
      for (String v : new String[]{"0", "1"}) Files.writeString(dicts.resolve(v + ".txt"), "the quick brown fox jumped over the lazy dogs\n");
      System.setProperty("HT_DATA", dir.resolve("data").toString());
      int n = 0;
      for (int i = 0; i < DOCS.length; i++) {
        String hx = expected.get("store.bytes." + i);
        if (hx.startsWith("!")) continue;
        byte[] b = unhex(hx);
        org.junit.Assert.assertEquals("store.readback." + i, expected.get("store.get." + i), esc(jvs(ObjectStoreShard.getJvsFromBytes(b, b.length)).replace("StringNode", "TextNode")));
        n++;
      }
      org.junit.Assert.assertEquals(DOCS.length, n);
      String wire = expected.get("wire.is2jvs");
      int sep = wire.indexOf(" -> ");
      AbstractIterator<JVS> it = JVSObjectStoreClient.is2jvs.apply(new ByteArrayInputStream(unhex(wire.substring(0, sep))));
      List<String> l = new ArrayList<>(); while (it.hasNext()) l.add(jvs(it.next()).replace("StringNode", "TextNode"));
      org.junit.Assert.assertEquals(wire.substring(sep + 4), esc(l.toString()));
      return null;
    });
  }
}

package com.hitorro.obj.core.solr;

import com.hitorro.jsontypesystem.Type;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.SortField;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.uninverting.UninvertingReader;

import java.io.IOException;
import java.util.Map;

public abstract class ExternalFeatureField extends FieldType {
    private FieldType ftype;
    private String keyFieldName;
    private IndexSchema schema;
    private float defVal;
    private Type type;

    public ExternalFeatureField() {

    }

    public abstract String getFieldType();

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        restrictProps(SORT_MISSING_FIRST | SORT_MISSING_LAST);
        // valType has never been used for anything except to throw an error, so make it optional since the
        // code (see getValueSource) gives you a FileFloatSource.
        String ftypeS = getFieldType();
        if (ftypeS != null) {
            ftype = schema.getFieldTypes().get(ftypeS);
        }
        keyFieldName = args.remove("keyField");
        String defValS = args.remove("defVal");
        defVal = defValS == null ? 0 : Float.parseFloat(defValS);
        this.schema = schema;
    }

    @Override
    public UninvertingReader.Type getUninversionType(final SchemaField sf) {
        return null;
    }

    @Override
    public void write(TextResponseWriter writer, String name, IndexableField f) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortField getSortField(SchemaField field, boolean reverse) {
        ValueSource source = getFileSource(field);
        return source.getSortField(reverse);
    }

    @Override
    public ValueSource getValueSource(SchemaField field, QParser parser) {
        return getFileSource(field, parser.getReq().getCore().getLatestSchema());
    }

    /**
     * Get a FileFloatSource for the given field, using the datadir from the
     * IndexSchema
     *
     * @param field the field to get a source for
     * @return a FileFloatSource
     */
    public ValueSource getFileSource(SchemaField field) {
        return getFileSource(field, schema);
    }

    public ValueSource getFileSource(SchemaField field, IndexSchema indexSchema) {
        //XXX  implement Feature ValueSource
        return null;
    }

    // If no key field is defined, we use the unique key field
    private SchemaField getKeyField() {
        return keyFieldName == null ? schema.getUniqueKeyField() : schema.getField(keyFieldName);
    }

}

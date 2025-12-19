package com.hitorro.obj.core;

import com.hitorro.obj.core.solr.QueryModulationBase;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.parser.Token;
import org.apache.solr.response.transform.DocTransformers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


@Aspect
public class SolrAspectInterceptor {
    /*

    help on adding agent
    https://www.eclipse.org/aspectj/doc/released/README-187.html

     */
    @Around("execution(* org.apache.solr.schema.IndexSchema.getField(java.lang.String )) && args(field)")
    public Object indexSchema_fieldQuery(ProceedingJoinPoint thisJoinPoint, java.lang.String field) throws Throwable {
        return thisJoinPoint.proceed(new Object[]{reWriteFieldName(field)});
    }

    @Around("execution(* org.apache.solr.schema.IndexSchema.getFieldOrNull(java.lang.String )) && args(field)")
    public Object indexSchema_getFieldOrNullQuery(ProceedingJoinPoint thisJoinPoint, java.lang.String field) throws Throwable {
        return thisJoinPoint.proceed(new Object[]{reWriteFieldName(field)});
    }

    @Around("execution(protected * org.apache.solr.parser.SolrQueryParserBase.getFieldQuery(java.lang.String, java.lang.String, boolean )) && args(field, queryText, slop)")
    public Object solrParser_GetFieldQuery(ProceedingJoinPoint thisJoinPoint, java.lang.String field, java.lang.String queryText, boolean slop) throws Throwable {
        return thisJoinPoint.proceed(new Object[]{reWriteFieldName(field), queryText, slop});
    }

    @Around("execution(* org.apache.solr.handler.component.QueryComponent.prepare(org.apache.solr.handler.component.ResponseBuilder)) && args(builder)")
    public Object queryComponent_prepare(ProceedingJoinPoint thisJoinPoint, ResponseBuilder builder) throws Throwable {
        SolrQueryContext sqc = SolrQueryContext.get();
        sqc.setResponse(builder);
        Object o = thisJoinPoint.proceed(new Object[]{builder});
        QueryModulationBase.me.modulate(builder, sqc);
        return o;
    }


    @Around("execution(* org.apache.solr.parser.SolrQueryParserBase.handleBareTokenQuery(java.lang.String, org.apache.solr.parser.Token, org.apache.solr.parser.Token, boolean, boolean, boolean, boolean)) && args(qfield, term, fuzzySlop, prefix, wildcard, fuzzy, regexp)")
    public Object solrParser_handleBareTokenQuery(ProceedingJoinPoint thisJoinPoint, String qfield, Token term, Token fuzzySlop, boolean prefix, boolean wildcard, boolean fuzzy, boolean regexp) throws Throwable {
        return thisJoinPoint.proceed(new Object[]{reWriteFieldName(qfield), term, fuzzySlop, prefix, wildcard, fuzzy, regexp});
    }


    @Around("execution(private * org.apache.solr.search.SolrReturnFields.addField(java.lang.String, java.lang.String, org.apache.solr.response.transform.DocTransformers, boolean )) && args(field, key, augmenters, isPseudoField)")
    public Object solrReturnFields_addField(ProceedingJoinPoint thisJoinPoint, String field, String key, DocTransformers augmenters, boolean isPseudoField) throws Throwable {
        return thisJoinPoint.proceed(new Object[]{reWriteFieldName(field), key, augmenters, isPseudoField});
    }

    @Around("execution(org.apache.solr.response.transform.RenameFieldTransformer.new(java.lang.String, java.lang.String, boolean )) && args(from, to, copy)")
    public Object renameFieldTransformer_constructor(ProceedingJoinPoint thisJoinPoint, String from, String to, boolean copy) throws Throwable {
        return thisJoinPoint.proceed(new Object[]{reWriteFieldName(from), to, copy});
    }

    @Around("execution(public * org.apache.lucene.queryparser.classic.QueryParser.Term(java.lang.String)) && args(field)")
    public Object ADSQueryParser_Term(ProceedingJoinPoint thisJoinPoint, String field) throws Throwable {
        return thisJoinPoint.proceed(new Object[]{reWriteFieldName(field)});
    }

    private String reWriteFieldName(String field) {
        SolrQueryContext sqc = SolrQueryContext.get();
        if (sqc.isValid()) {
            field = sqc.getRetrievalContext().getMappedField(field);
        }
        return field;
    }
}

/*

Aspectj class that does the weaving?

ClassPreProcessorAgentAdapter

ClassLoaderWeavingAdapter



-javaagent:/Users/chris/.m2/repository/org/aspectj/aspectjweaver/1.9.1/aspectjweaver-1.9.1.jar


 */

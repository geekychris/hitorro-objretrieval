package com.hitorro.obj.core.solr.retrievalmodules.fixup;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.obj.core.solr.JVS2JVSEnrichMapper;
import com.hitorro.obj.core.solr.JVS2JVSRemoveMapper;
import com.hitorro.jsontypesystem.JVS2JVSTranslationMapper;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RemoveAddFieldMapper implements Function<JVS, JVS> {
    private final List<Propaccess> fields;
    private final boolean remove;

    RemoveAddFieldMapper(List<String> fieldsIn, boolean remove) {
        fields = new ArrayList();
        for (String f : fieldsIn) {
            fields.add(new Propaccess(f));
        }
        this.remove = remove;
    }

    public static Function<JVS, JVS> get(List<String> remove, List<String> add, List<String> enrichTags, List<String> removeTags) {
        Function<JVS, JVS> func = null;
        if (!ListUtil.nullOrEmpty(add)) {
            func = new RemoveAddFieldMapper(add, false);
        }
        if (!ListUtil.nullOrEmpty(remove)) {
            func = combine(func, new RemoveAddFieldMapper(remove, true));
        }
        if (!ListUtil.nullOrEmpty(enrichTags)) {
            func = combine(func, new JVS2JVSEnrichMapper(enrichTags.toArray(new String[enrichTags.size()])));
        }
        if (!ListUtil.nullOrEmpty(removeTags)) {
            func = combine(func, new JVS2JVSRemoveMapper(removeTags.toArray(new String[removeTags.size()])));
        }
        return func;
    }

    /**
     * Get a function that includes translation of mls fields.
     * 
     * @param remove Fields to remove
     * @param add Fields to add
     * @param enrichTags Tags for enrichment
     * @param removeTags Tags for removal
     * @param translator The translator to use for translations
     * @param targetLanguages Target languages for translation (e.g., "de", "fr", "es")
     * @param mlsFields MLS fields to translate (e.g., "title.mls", "body.mls")
     * @param sourceLanguage Source language code (e.g., "en")
     * @return Combined function
     */
    public static Function<JVS, JVS> getWithTranslation(List<String> remove, List<String> add, 
            List<String> enrichTags, List<String> removeTags,
            JVS2JVSTranslationMapper.Translator translator,
            List<String> targetLanguages, List<String> mlsFields, String sourceLanguage) {
        Function<JVS, JVS> func = get(remove, add, enrichTags, removeTags);
        
        if (translator != null && !ListUtil.nullOrEmpty(targetLanguages) && !ListUtil.nullOrEmpty(mlsFields)) {
            JVS2JVSTranslationMapper translationMapper = new JVS2JVSTranslationMapper(
                translator, targetLanguages, mlsFields, sourceLanguage);
            func = combine(func, translationMapper);
        }
        
        return func;
    }

    private static Function<JVS, JVS> combine(Function<JVS, JVS> func1, Function<JVS, JVS> func2) {
        if (func1 != null) {
            if (func2 != null) {
                return func1.compose(func2);
            }
            return func1;
        }
        return func2;
    }

    @Override
    public JVS apply(final JVS jvs) {
        for (Propaccess p : fields) {
            if (remove) {
                jvs.set(p, null);
            } else {
                jvs.get(p);
            }
        }
        return jvs;
    }
}

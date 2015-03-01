/*
 * Copyright 2009-2015 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.arrayexpress.utils.search;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import uk.ac.ebi.arrayexpress.utils.RegexHelper;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironment;
import uk.ac.ebi.arrayexpress.utils.saxon.search.QueryConstructor;

import java.util.Map;

public class BackwardsCompatibleQueryConstructor extends QueryConstructor {
    private final static RegexHelper ACCESSION_REGEX = new RegexHelper("^[ae]-\\w{4}-\\d+$", "i");

    @Override
    public Query construct(IndexEnvironment env, Map<String, String[]> querySource) throws ParseException {
        if ("1".equals(StringTools.arrayToString(querySource.get("queryversion"), ""))) {
            // preserving old stuff:
            // 1. all lucene special chars to be quoted
            // 2. if "wholewords" is "on" or "true" -> don't add *_*, otherwise add *_*
            BooleanQuery result = new BooleanQuery();
            String wholeWords = StringTools.arrayToString(querySource.get("wholewords"), "");
            boolean useWildcards = !(null != wholeWords && StringTools.stringToBoolean(wholeWords));
            for (Map.Entry<String, String[]> queryItem : querySource.entrySet()) {
                String field = queryItem.getKey();
                if (env.fields.containsKey(field) && queryItem.getValue().length > 0) {
                    for (String value : queryItem.getValue()) {
                        if (null != value) {
                            value = value.trim().toLowerCase();
                            if (0 != value.length()) {
                                if ("keywords".equals(field) && ACCESSION_REGEX.test(value)) {
                                    result.add(new TermQuery(new Term("accession", value)), BooleanClause.Occur.MUST);
                                } else if ("keywords".equals(field) && '"' == value.charAt(0) && '"' == value.charAt(value.length() - 1)) {
                                    value = value.substring(1, value.length() - 1);
                                    PhraseQuery q = new PhraseQuery();
                                    String[] tokens = value.split("\\s+");
                                    for (String token : tokens) {
                                        q.add(new Term(field, token));
                                    }
                                    result.add(q, BooleanClause.Occur.MUST);
                                } else {
                                    String[] tokens = value.split("\\s+");
                                    for (String token : tokens) {
                                        // we use wildcards for keywords depending on "wholewords" switch,
                                        // *ALWAYS* for other fields, *NEVER* for user id and accession or boolean fields
                                        Query q = !"boolean".equals(env.fields.get(field).type) && !" userid  accession ".contains(" " + field + " ") && (useWildcards || (!" keywords ".contains(" " + field + " ")))
                                                ? new WildcardQuery(new Term(field, "*" + token + "*"))
                                                : new TermQuery(new Term(field, token));
                                        result.add(q, BooleanClause.Occur.MUST);
                                    }
                                }
                            }
                        }

                    }
                }
            }
            return result;
        } else {
            return super.construct(env, querySource);
        }
    }

    @Override
    public Query construct(IndexEnvironment env, String queryString) throws ParseException {
        return super.construct(env, queryString);
    }
}

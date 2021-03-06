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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;

public final class LowercaseAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new LowercaseTokenizer();
        //TokenStream filter = new ASCIIFoldingFilter(source);
        return new TokenStreamComponents(source);
    }

    private static class LowercaseTokenizer extends LetterTokenizer {
        @Override
        protected boolean isTokenChar(int c) {
            return true;
        }

        @Override
        protected int normalize(int c) {
            return Character.toLowerCase(c);
        }
    }
}

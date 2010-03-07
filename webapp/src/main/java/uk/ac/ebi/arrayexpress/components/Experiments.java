package uk.ac.ebi.arrayexpress.components;

/*
 * Copyright 2009-2010 Microarray Informatics Group, European Bioinformatics Institute
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

import net.sf.saxon.om.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.autocompletion.AutocompleteData;
import uk.ac.ebi.arrayexpress.utils.autocompletion.SetTrie;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
import uk.ac.ebi.arrayexpress.utils.persistence.PersistableStringList;
import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.DocumentSource;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Experiments extends ApplicationComponent implements DocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String dataSource;
    private TextFilePersistence<PersistableDocumentContainer> experiments;
    private TextFilePersistence<PersistableStringList> experimentsInAtlas;
    private TextFilePersistence<PersistableString> species;
    private TextFilePersistence<PersistableString> arrays;
    private Map<String, String> assaysByMolecule;
    private Map<String, String> assaysByInstrument;
    private SetTrie<AutocompleteData> autocompleteStore;

    private SaxonEngine saxon;
    private SearchEngine search;

    public Experiments()
    {
        super("Experiments");
    }

    public void initialize()
    {
        saxon = (SaxonEngine)getComponent("SaxonEngine");
        search = (SearchEngine)getComponent("SearchEngine");

        this.experiments = new TextFilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer()
                , new File(getPreferences().getString("ae.experiments.file.location"))
        );

        this.experimentsInAtlas = new TextFilePersistence<PersistableStringList>(
                new PersistableStringList()
                , new File(getPreferences().getString("ae.atlasexperiments.file.location"))
        );

        this.species = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(getPreferences().getString("ae.species.file.location"))

        );

        this.arrays = new TextFilePersistence<PersistableString>(
                new PersistableString()
                , new File(getPreferences().getString("ae.arrays.file.location"))
        );

        this.assaysByMolecule = new HashMap<String, String>();
        assaysByMolecule.put("", "<option value=\"\">All assays by molecule</option><option value=\"DNA assay\">DNA assay</option><option value=\"metabolomic profiling\">Metabolite assay</option><option value=\"protein assay\">Protein assay</option><option value=\"RNA assay\">RNA assay</option>");
        assaysByMolecule.put("array assay", "<option value=\"\">All assays by molecule</option><option value=\"DNA assay\">DNA assay</option><option value=\"RNA assay\">RNA assay</option>");
        assaysByMolecule.put("high throughput sequencing assay", "<option value=\"\">All assays by molecule</option><option value=\"DNA assay\">DNA assay</option><option value=\"RNA assay\">RNA assay</option>");
        assaysByMolecule.put("proteomic profiling by mass spectrometer", "<option value=\"protein assay\">Protein assay</option>");

        this.assaysByInstrument = new HashMap<String, String>();
        assaysByInstrument.put("", "<option value=\"\">All technologies</option><option value=\"array assay\">Array</option><option value=\"high throughput sequencing assay\">High-throughput sequencing</option><option value=\"proteomic profiling by mass spectrometer\">Mass spectrometer</option>");
        assaysByInstrument.put("DNA assay", "<option value=\"\">All technologies</option><option value=\"array assay\">Array</option><option value=\"high throughput sequencing assay\">High-throughput sequencing</option>");
        assaysByInstrument.put("metabolomic profiling", "<option value=\"\">All technologies</option>");
        assaysByInstrument.put("protein assay", "<option value=\"\">All technologies</option><option value=\"proteomic profiling by mass spectrometer\">Mass spectrometer</option>");
        assaysByInstrument.put("RNA assay", "<option value=\"\">All technologies</option><option value=\"array assay\">Array</option><option value=\"high throughput sequencing assay\">High-throughput sequencing</option>");

        this.autocompleteStore = new SetTrie<AutocompleteData>();
        indexExperiments();
        saxon.registerDocumentSource(this);
    }

    public void terminate()
    {
        saxon = null;
    }

    // implementation of DocumentSource.getDocument()
    public String getDocumentURI()
    {
        return "experiments.xml";
    }

    // implementation of DocumentSource.getDocument()
    public synchronized DocumentInfo getDocument()
    {
        return this.experiments.getObject().getDocument();
    }

    public boolean isAccessible( String accession, String userId )
    {
        if ("0".equals(userId)) {
            return true;
        } else {
            return Boolean.parseBoolean(
                saxon.evaluateXPathSingle(
                        getDocument()
                        , "exists(//experiment[accession = '" + accession + "' and user = '" + userId + "'])"
                )
            );
        }
    }

    public boolean isInAtlas( String accession )
    {
        return this.experimentsInAtlas.getObject().contains(accession);
    }

    public String getSpecies()
    {
        return this.species.getObject().get();
    }

    public String getArrays()
    {
        return this.arrays.getObject().get();
    }

    public String getAssaysByMolecule( String key )
    {
        return this.assaysByMolecule.get(key);
    }

    public String getAssaysByInstrument( String key )
    {
        return this.assaysByInstrument.get(key);
    }

    public String getKeywords( String prefix )
    {
        StringBuilder sb = new StringBuilder("");
        List<AutocompleteData> matches = this.autocompleteStore.findCompletions(prefix);
        for (AutocompleteData match : matches) {
            sb.append(match.getText()).append('|').append(match.getDataType()).append('|').append(match.getData()).append('\n');
        }
        return sb.toString();
    }

    public String getDataSource()
    {
        if (null == this.dataSource) {
            this.dataSource = getPreferences().getString("ae.experiments.db.datasources");
        }

        return this.dataSource;
    }

    public void setDataSource( String dataSource )
    {
        this.dataSource = dataSource;
    }

    public void reload( String xmlString )
    {
        DocumentInfo doc = loadExperimentsFromString(xmlString);
        if (null != doc) {
            setExperiments(doc);
            buildSpeciesArraysExpTypes(doc);
            indexExperiments();
        }
    }

    public void setExperimentsInAtlas( List<String> expList )
    {
        this.experimentsInAtlas.setObject(new PersistableStringList(expList));
    }

    private synchronized void setExperiments( DocumentInfo doc )
    {
        if (null != doc) {
            this.experiments.setObject(new PersistableDocumentContainer(doc));
        } else {
            this.logger.error("Experiments NOT updated, NULL document passed");
        }
    }

    private DocumentInfo loadExperimentsFromString( String xmlString )
    {
        DocumentInfo doc = saxon.transform(xmlString, "preprocess-experiments-xml.xsl", null);
        if (null == doc) {
            this.logger.error("Transformation [preprocess-experiments-xml.xsl] returned an error, returning null");
            return null;
        }
        return doc;
    }

    private void indexExperiments()
    {
        try {
            search.getController().index("experiments", experiments.getObject().getDocument());

            List<String> keywords = search.getController().getTerms("experiments", "keywords", 10);
            autocompleteStore.clear();
            for (String keyword : keywords) {
                autocompleteStore.load(new AutocompleteData(keyword, AutocompleteData.DATA_TEXT, ""));
            }
            logger.debug("Retrieved experiment keywords list, size [{}]", keywords.size());
        } catch (Exception x) {
            this.logger.error("Caught an exception:", x);
        }
    }

    private void buildSpeciesArraysExpTypes( DocumentInfo doc )
    {
        String speciesString = saxon.transformToString(doc, "build-species-list-html.xsl", null);
        this.species.setObject(new PersistableString(speciesString));

        String arraysString = saxon.transformToString(doc, "build-arrays-list-html.xsl", null);
        this.arrays.setObject(new PersistableString(arraysString));

        //String expTypesString = saxon.transformToString(doc, "build-exptypes-list-html.xsl", null);
        //this.experimentTypes.setObject(new PersistableString(expTypesString));
    }
}

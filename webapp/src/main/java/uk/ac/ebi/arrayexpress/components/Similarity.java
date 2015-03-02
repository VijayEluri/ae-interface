package uk.ac.ebi.arrayexpress.components;

/*
 * Copyright 2009-2014 European Molecular Biology Laboratory
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
import uk.ac.ebi.arrayexpress.jobListeners.AE2ExperimentReloadJobListener;
import uk.ac.ebi.arrayexpress.jobListeners.SimilarityJobListener;
import uk.ac.ebi.arrayexpress.jobs.ReloadExperimentsAfterSimilarityJob;
import uk.ac.ebi.arrayexpress.jobs.SimilarityJob;
import uk.ac.ebi.arrayexpress.utils.persistence.FilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.Document;
import uk.ac.ebi.arrayexpress.utils.saxon.IDocumentSource;
import uk.ac.ebi.arrayexpress.utils.saxon.PersistableDocumentContainer;
import uk.ac.ebi.fg.utils.ISimilarityComponent;

import java.io.File;
import java.io.IOException;

public class Similarity extends ApplicationComponent implements IDocumentSource, ISimilarityComponent
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FilePersistence<PersistableDocumentContainer> document;
    private SaxonEngine saxon;
    private SearchEngine search;

    public final String INDEX_ID = "similarity";

    public static interface IEventInformation
    {
        public abstract DocumentInfo getEventXML() throws Exception;
    }

    public Similarity()
    {
    }

    public void initialize() throws Exception
    {
        this.saxon = (SaxonEngine) getComponent("SaxonEngine");
        this.search = (SearchEngine) getComponent("SearchEngine");

        this.document = new FilePersistence<PersistableDocumentContainer>(
                new PersistableDocumentContainer("similarity")
                , new File(getPreferences().getString("ae.similarity.persistence-location"))
        );

        updateAccelerators();
        this.saxon.registerDocumentSource(this);

        JobsController jobsController = (JobsController) getComponent("JobsController");

        jobsController.addJob("recalculate-similarity", SimilarityJob.class);
        jobsController.addJobListener(new AE2ExperimentReloadJobListener("ae2-data-reload-listener"));
        jobsController.addJob("similarity-update-ae2-xml", ReloadExperimentsAfterSimilarityJob.class);
        jobsController.addJobListener(new SimilarityJobListener("similarity-recalculation-listener"));
        jobsController.executeJob("recalculate-similarity");
    }

    public void terminate() throws Exception
    {
    }

    // implementation of IDocumentSource.getDocumentURI()
    public String getDocumentURI()
    {
        return "similarity.xml";
    }

    // implementation of IDocumentSource.getDocument()
    public synchronized Document getDocument() throws IOException
    {
        return this.document.getObject().getDocument();
    }

    // implementation of IDocumentSource.setDocument(Document)
    public synchronized void setDocument( Document doc ) throws IOException
    {
        if (null != doc) {
            this.document.setObject(new PersistableDocumentContainer("similarity", doc));
            updateAccelerators();
        } else {
            this.logger.error("Similarity NOT updated, NULL document passed");
        }
    }

    private void updateIndex()
    {
        try {
            this.search.getController().index(INDEX_ID, this.getDocument());
        } catch (Exception x) {
            this.logger.error("Caught an exception:", x);
        }
    }

    private void updateAccelerators()
    {
        /* todo:
        this.logger.debug("Updating accelerators for similarity");

        ExtFunctions.clearAccelerator("similar-experiments");
        ExtFunctions.clearAccelerator("similar-experiments-reversed");
        try {

            XPath xp = new XPathEvaluator(getDocument().getConfiguration());
            XPathExpression xpe = xp.compile("/similarity/experiment");
            XPathExpression similarExperiment = xp.compile("similarOntologyExperiments/similarExperiment | similarPubMedExperiments/similarExperiment");
            List documentNodes = (List) xpe.evaluate(getDocument(), XPathConstants.NODESET);

            XPathExpression accessionXpe = xp.compile("accession");
            for (Object node : documentNodes) {
                try {
                    List simNodes = (List) similarExperiment.evaluate(node, XPathConstants.NODESET);
                    for ( Object simNode : simNodes ) {
                        String similarAccession = accessionXpe.evaluate(simNode);

                        Set<Object> similartExperimentsReversed = (Set)ExtFunctions.getAcceleratorValue("similar-experiments-reversed", similarAccession);
                        if ( similartExperimentsReversed == null ) {
                            similartExperimentsReversed = new HashSet<Object> ();
                            ExtFunctions.addAcceleratorValue("similar-experiments-reversed", similarAccession, similartExperimentsReversed);
                        }
                        similartExperimentsReversed.add(node);
                    }
                    // get all the expressions taken care of
                    String accession = accessionXpe.evaluate(node);
                    ExtFunctions.addAcceleratorValue("similar-experiments", accession, node);
                } catch (XPathExpressionException x) {
                    this.logger.error("Caught an exception:", x);
                }
            }

            this.logger.debug("Accelerators updated");
        } catch (Exception x) {
            this.logger.error("Caught an exception:", x);
        }
        */
    }

    public void sendExceptionReport( String message, Throwable x )
    {
        super.getApplication().handleException(message, x);
    }
}
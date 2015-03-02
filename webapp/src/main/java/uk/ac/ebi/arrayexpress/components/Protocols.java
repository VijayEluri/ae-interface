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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.persistence.FilePersistence;
import uk.ac.ebi.arrayexpress.utils.saxon.*;
import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexerException;

import java.io.File;
import java.io.IOException;

public class Protocols extends ApplicationComponent implements IDocumentSource
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FilePersistence<PersistableDocumentContainer> document;
    private SaxonEngine saxon;
    private SearchEngine search;

    public final String INDEX_ID = "protocols";

    public enum ProtocolsSource
    {
        AE1, AE2;

        public String getStylesheetName()
        {
            switch (this) {
                case AE1:   return "preprocess-protocols-ae1-xml.xsl";
                case AE2:   return "preprocess-protocols-ae2-xml.xsl";
            }
            return null;
        }
    }

    public Protocols()
    {
    }

    @Override
    public void initialize() throws Exception
    {
        this.saxon = (SaxonEngine) getComponent("SaxonEngine");
        this.search = (SearchEngine) getComponent("SearchEngine");

        this.document = new FilePersistence<>(
                new PersistableDocumentContainer("protocols")
                , new File(getPreferences().getString("ae.protocols.persistence-location"))
        );

        updateIndex();
        this.saxon.registerDocumentSource(this);
    }

    @Override
    public void terminate() throws Exception
    {
    }

    // implementation of IDocumentSource.getDocumentURI()
    public String getDocumentURI()
    {
        return "protocols.xml";
    }

    // implementation of IDocumentSource.getDocument()
    public synchronized Document getDocument() throws IOException
    {
        return this.document.getObject().getDocument();
    }

    // implementation of IDocumentSource.setDocument(Document)
    public synchronized void setDocument( Document doc ) throws IOException, InterruptedException
    {
        if (null != doc) {
            this.document.setObject(new PersistableDocumentContainer("protocols", doc));
            updateIndex();
        } else {
            this.logger.error("Protocols NOT updated, NULL document passed");
        }
    }

    public void update( String xmlString, ProtocolsSource source ) throws IOException, InterruptedException
    {
        try {
            Document updateDoc = this.saxon.transform(xmlString, source.getStylesheetName(), null);
            if (null != updateDoc) {
                new DocumentUpdater(this, updateDoc).update();
            }
        } catch (SaxonException x) {
            throw new RuntimeException(x);
        }
    }

    private void updateIndex() throws IOException, InterruptedException
    {
        Thread.sleep(0);
        try {
            this.search.getController().index(INDEX_ID, this.getDocument());
        } catch (IndexerException x) {
            throw new RuntimeException(x);
        }
    }
}
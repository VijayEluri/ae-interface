package uk.ac.ebi.arrayexpress.components;

/*
 * Copyright 2009-2012 European Molecular Biology Laboratory
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

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.SequenceWriter;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.xpath.XPathEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.saxon.IDocumentSource;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.FormatFileSizeFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.GetAcceleratorValueFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.TabularDocumentFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.TrimTrailingDotFunction;
import uk.ac.ebi.arrayexpress.utils.saxon.functions.saxon.ParseHTMLFunction;
import uk.ac.ebi.fg.utils.saxon.IXPathEngine;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class SaxonEngine extends ApplicationComponent implements URIResolver, ErrorListener, IXPathEngine
{
    // logging machinery
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TransformerFactoryImpl trFactory;
    private Map<String, Templates> templatesCache = new Hashtable<String, Templates>();
    private Map<String, IDocumentSource> documentSources = new Hashtable<String, IDocumentSource>();

    private DocumentInfo appDocument;

    private static final String XML_STRING_ENCODING = "UTF-8";

    public SaxonEngine()
    {
    }

    public void initialize() throws Exception
    {
        // This is so we make sure we use Saxon and not anything else
        trFactory = (TransformerFactoryImpl) TransformerFactoryImpl.newInstance();
        trFactory.setErrorListener(this);
        trFactory.setURIResolver(this);
        trFactory.getConfiguration().setTreeModel(Builder.TINY_TREE_CONDENSED);

        // create application document
        appDocument = buildDocument(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><application name=\""
                        + getApplication().getName()
                        + "\"/>"
        );

        registerExtensionFunction(new ParseHTMLFunction());
        registerExtensionFunction(new TabularDocumentFunction());
        registerExtensionFunction(new GetAcceleratorValueFunction());
        registerExtensionFunction(new FormatFileSizeFunction());
        registerExtensionFunction(new TrimTrailingDotFunction());
    }

    public void terminate() throws Exception
    {
    }

    public void registerDocumentSource( IDocumentSource documentSource )
    {
        logger.debug("Registering source [{}]", documentSource.getDocumentURI());
        this.documentSources.put(documentSource.getDocumentURI(), documentSource);
    }

    public void unregisterDocumentSource( IDocumentSource documentSource )
    {
        logger.debug("Removing source [{}]", documentSource.getDocumentURI());
        this.documentSources.remove(documentSource.getDocumentURI());
    }

    public DocumentInfo getRegisteredDocument( String documentURI ) throws Exception
    {
        if (this.documentSources.containsKey(documentURI)) {
            return this.documentSources.get(documentURI).getDocument();
        } else {
            return null;
        }
    }

    // implements URIResolver.resolve
    public Source resolve( String href, String base ) throws TransformerException
    {
        Source src;
        try {
            // try document sources first
            if (documentSources.containsKey(href)) {
                return documentSources.get(href).getDocument();
            } else {
                URL resource = Application.getInstance().getResource("/WEB-INF/server-assets/stylesheets/" + href);
                if (null == resource) {
                    throw new TransformerException("Unable to locate stylesheet resource [" + href + "]");
                }
                InputStream input = resource.openStream();
                if (null == input) {
                    throw new TransformerException("Unable to open stream for resource [" + resource + "]");
                }
                src = new StreamSource(input);
            }
        } catch (TransformerException x) {
            throw x;
        } catch (Exception x) {
            logger.error("Caught an exception:", x);
            throw new TransformerException(x.getMessage());
        }

        return src;
    }

    // implements ErrorListener.error
    public void error( TransformerException x ) throws TransformerException
    {
        throw x;
    }

    // implements ErrorListener.fatalError
    public void fatalError( TransformerException x ) throws TransformerException
    {
        throw x;
    }

    // implements ErrorListener.warning
    public void warning( TransformerException x )
    {
        //if (logger.isDebugEnabled()) {
        //    logger.debug("There was a warning while transforming:", x);
        //} else {
        logger.warn(x.getLocalizedMessage());
        //}
    }

    public DocumentInfo getAppDocument()
    {
        return appDocument;
    }

    public void registerExtensionFunction( ExtensionFunctionDefinition f )
    {
        trFactory.getConfiguration().registerExtensionFunction(f);
    }

    public String serializeDocument( DocumentInfo document ) throws Exception
    {
        Transformer transformer = trFactory.newTransformer();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "US-ASCII");

        transformer.transform(document, new StreamResult(outStream));
        return outStream.toString(XML_STRING_ENCODING);
    }

    public DocumentInfo buildDocument( String xml ) throws XPathException
    {
        StringReader reader = new StringReader(xml);
        Configuration config = trFactory.getConfiguration();
        return config.buildDocument(new StreamSource(reader));
    }

    public DocumentInfo buildDocument( InputStream stream ) throws XPathException
    {
        Configuration config = trFactory.getConfiguration();
        return config.buildDocument(new StreamSource(stream));
    }

    public List evaluateXPath( DocumentInfo doc, String xpath ) throws XPathExpressionException
    {
        XPath xp = new XPathEvaluator(trFactory.getConfiguration());
        XPathExpression xpe = xp.compile(xpath);
        Object o = xpe.evaluate(doc, XPathConstants.NODESET);
        return (o instanceof List) ? (List)o : null;
    }

    public String evaluateXPathSingle( DocumentInfo doc, String xpath ) throws XPathExpressionException
    {
        XPath xp = new XPathEvaluator(trFactory.getConfiguration());
        XPathExpression xpe = xp.compile(xpath);
        return xpe.evaluate(doc);
    }

    public boolean transformToWriter( Source srcDocument, String stylesheet, Map<String, String[]> params, Writer dstWriter ) throws Exception
    {
        return transform(srcDocument, stylesheet, params, new StreamResult(dstWriter));
    }

    @SuppressWarnings("unused")
    public boolean transformToFile( DocumentInfo srcDocument, String stylesheet, Map<String, String[]> params, File dstFile ) throws Exception
    {
        return transform(srcDocument, stylesheet, params, new StreamResult(dstFile));
    }

    public String transformToString( URL src, String stylesheet, Map<String, String[]> params ) throws Exception
    {
        String str;
        InputStream inStream = null;
        ByteArrayOutputStream outStream = null;
        try {
            inStream = src.openStream();
            outStream = new ByteArrayOutputStream();
            if (transform(new StreamSource(inStream), stylesheet, params, new StreamResult(outStream))) {
                str = outStream.toString(XML_STRING_ENCODING);
                outStream.close();
                return str;
            } else {
                return null;
            }
        } finally {
            if (null != inStream)
                inStream.close();
            if (null != outStream)
                outStream.close();
        }
    }

    public String transformToString( Source source, String stylesheet, Map<String, String[]> params ) throws Exception
    {
        String str;
        ByteArrayOutputStream outStream = null;
        try {
            outStream = new ByteArrayOutputStream();

            if (transform(source, stylesheet, params, new StreamResult(outStream))) {
                str = outStream.toString(XML_STRING_ENCODING);
                return str;
            } else {
                return null;
            }
        } finally {
            if (null != outStream)
                outStream.close();
        }
    }

    public DocumentInfo transform( String srcXmlString, String stylesheet, Map<String, String[]> params ) throws Exception
    {
        Source src = new StreamSource(new StringReader(srcXmlString));
        TinyBuilder dstDocument = new TinyBuilder(trFactory.getConfiguration().makePipelineConfiguration());
        if (transform(src, stylesheet, params, dstDocument)) {
            return (DocumentInfo) dstDocument.getCurrentRoot();
        }
        return null;
    }

    public DocumentInfo transform( Source source, String stylesheet, Map<String, String[]> params ) throws Exception
    {
        TinyBuilder dstDocument = new TinyBuilder(trFactory.getConfiguration().makePipelineConfiguration());
        if (transform(source, stylesheet, params, dstDocument)) {
            return (DocumentInfo) dstDocument.getCurrentRoot();
        }
        return null;
    }

    private boolean transform( Source src, String stylesheet, Map<String, String[]> params, Result dst ) throws Exception
    {
        boolean result = false;
        try {
            Templates templates;
            if (!templatesCache.containsKey(stylesheet)) {
                logger.debug("Caching prepared stylesheet [{}]", stylesheet);
                // Open the stylesheet
                Source xslSource = resolve(stylesheet, null);

                templates = trFactory.newTemplates(xslSource);
                templatesCache.put(stylesheet, templates);
            } else {
                logger.debug("Getting prepared stylesheet [{}] from cache", stylesheet);
                templates = templatesCache.get(stylesheet);
            }
            Transformer xslt = templates.newTransformer();

            // redirect all messages to logger
            ((Controller) xslt).setMessageEmitter(new LoggerWriter(logger));

            // assign the parameters (if not null)
            if (null != params) {
                for (Map.Entry<String, String[]> param : params.entrySet()) {
                    xslt.setParameter(param.getKey(), StringTools.arrayToString(param.getValue(), " "));
                }
            }

            // Perform the transformation, sending the output to the response.
            logger.debug("Performing transformation, stylesheet [{}]", stylesheet);
            xslt.transform(src, dst);
            logger.debug("Transformation completed");

            result = true;
        } catch (TerminationException x) {
            logger.error("Transformation has been terminated by xsl instruction, please inspect log for details");
        }
        return result;
    }

    class LoggerWriter extends SequenceWriter
    {
        private Logger logger;

        protected LoggerWriter( Logger logger )
        {
            super(null);
            this.logger = logger;
        }

        public void write( Item item )
        {
            logger.debug("[xsl:message] {}", item.getStringValue());
        }
    }
}

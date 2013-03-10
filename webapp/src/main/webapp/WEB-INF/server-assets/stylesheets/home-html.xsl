<?xml version="1.0" encoding="UTF-8"?>
<!--
 * Copyright 2009-2013 European Molecular Biology Laboratory
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
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress/XSLT/Extension"
                xmlns:search="http://www.ebi.ac.uk/arrayexpress/XSLT/SearchExtension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="fn ae search html xs"
                exclude-result-prefixes="fn ae search html xs"
                version="2.0">

    <xsl:include href="ae-html-page.xsl"/>
    <xsl:include href="ae-date-functions.xsl"/>
    <xsl:include href="ae-sort-experiments.xsl"/>

    <xsl:template match="/">
        <xsl:call-template name="ae-page">
            <xsl:with-param name="pIsFixedWidth" select="fn:true()"/>
            <xsl:with-param name="pIsSearchVisible" select="fn:true()"/>
            <xsl:with-param name="pSearchInputValue"/>
            <xsl:with-param name="pTitleTrail"/>
            <xsl:with-param name="pExtraCSS"/>
            <xsl:with-param name="pBreadcrumbTrail"/>
            <xsl:with-param name="pExtraJS"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="ae-content-section">
        <div class="grid_18 alpha">
            <section>
                <h2>ArrayExpress - functional genomics data</h2>
                <p class="intro justify">ArrayExpress is a database of functional genomics experiments that can be queried and the data downloaded. It includes gene expression data from microarray and high throughput sequencing studies.  Data is collected to <a href="http://www.mged.org/Workgroups/MIAME/miame.html">MIAME</a> and <a href="http://www.fged.org/projects/minseqe/">MINSEQE</a> standards. Experiments are submitted directly to ArrayExpress or are imported from the NCBI GEO database.</p>
            </section>
        </div>

        <xsl:variable name="vExperiments" select="search:queryIndex('experiments', 'visible:true public:true')"/>
        <xsl:variable name="vTotal" select="fn:count($vExperiments)"/>
        <xsl:variable name="vRetrieved" select="$vExperiments[1]/../@retrieved"/>
        <xsl:variable name="vFiles" select="search:queryIndex('files', 'userid:1 (kind:raw OR kind:processed)')"/>

        <xsl:if test="$vTotal > 0">
            <div class="grid_6 omega">
                <section>
                    <h3 class="icon icon-generic" data-icon="g">Data Content</h3>
                    <xsl:if test="fn:exists($vRetrieved)">
                        <h5>Updated <xsl:value-of select="ae:formatDateTime2($vRetrieved)"/></h5>
                    </xsl:if>
                    <!-- <p>ArrayExpress statistics:</p> -->
                    <ul>
                        <li><xsl:value-of select="$vTotal"/> experiment<xsl:if test="fn:count($vExperiments) > 1">s</xsl:if></li>
                        <li><xsl:value-of select="fn:sum($vExperiments/assays) cast as xs:integer"/> assays</li>
                        <li><xsl:value-of select="ae:formatFileSize(fn:sum($vFiles/@size) cast as xs:integer)"/> of archived data</li>
                    </ul>
                </section>
            </div>
        </xsl:if>
        <div class="grid_24 alpha">
            <section id="ae-news">
                <h3 class="icon icon-generic" data-icon="N">Latest News</h3>
                <p class="news">5 Mar 2013 - <strong>New ArrayExpress interface</strong><br/>
                    ArrayExpress, along with the EBI website, has a new look and feel. Click the <a href="http://www.ebi.ac.uk/arrayexpress/experiments/browse.html">experiment tab</a> to see our new layout or <a href="http://www.ebi.ac.uk/about/news/press-releases/website-relaunch">read about the new design</a>. We have preserved the previous functionality to maintain a consistent query experience and we would love to hear what you think.
                    Please use the <a href="#" onclick="$.aeFeedback(event)">Feedback</a> functionality to provide comments.</p>
            </section>
            <section>
                <div class="grid_8 alpha">
                    <h3 class="icon icon-generic" data-icon="L">Links</h3>
                    <p>Information about how to search ArrayExpress, understand search results, how to submit data and FAQ can be found in our <a href="{$context-path}/help/index.html">Help section</a>.</p>
                    <p>Find out more about the <a href="/about/people/alvis-brazma">Functional Genomics group</a>.</p>
                </div>
                <div class="grid_8">
                    <h3 class="icon icon-functional" data-icon="t">Tools and Access</h3>
                    <p><a href="http://www.bioconductor.org/packages/release/bioc/html/ArrayExpress.html">ArrayExpress Bioconductor package</a>: an R package to access ArrayExpress and build data structures.</p>
                    <p><a href="{$context-path}/help/programmatic_access.html">Programmatic access</a>: query and download data using web services or JSON.</p>
                    <p><a href="ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/">FTP access</a>: data can be downloaded directly from our FTP site.</p>
                </div>
                <div class="grid_8 omega">
                    <h3 class="icon icon-generic" data-icon="L">Related Projects</h3>
                    <p>Discover up and down regulated genes in numerous experimental conditions in the <a href="/gxa">Expression Atlas</a>.</p>
                    <p>Explore the <a href="/efo">Experimental Factor Ontology</a> used to support queries and annotation of ArrayExpress data.</p>
                </div>
            </section>
        </div>
    </xsl:template>

</xsl:stylesheet>
<?xml version="1.0" encoding="UTF-8"?>
<!--
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
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:ae="http://www.ebi.ac.uk/arrayexpress/XSLT/Extension"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="ae fn html xs"
                exclude-result-prefixes="ae fn html xs"
                version="2.0">
    <xsl:output method="xml" encoding="UTF-8" indent="no"/>

    <xsl:include href="ae-parse-html-function.xsl"/>

    <xsl:template match="/experiments">
        <experiments
            version="{@version}" total="{fn:count(experiment)}">

            <xsl:apply-templates select="experiment">
                <xsl:sort select="id" order="descending" data-type="number"/>
            </xsl:apply-templates>
        </experiments>
    </xsl:template>

    <xsl:template match="experiment">
        <experiment>
            <xsl:variable name="vAccession" select="accession"/>

            <xsl:variable name="vGenDescription">
                <xsl:variable name="vGenDescriptionRaw" select="description[contains(text(), '(Generated description)')]"/>
                <xsl:choose>
                    <!-- nobody is going to fix these so SUPPRESS
                    <xsl:when test="fn:count($vGenDescriptionRaw) > 1">
                        <xsl:message>[WARN] Multiple generated descriptions found for [<xsl:value-of select="$vAccession"/>]</xsl:message>
                    </xsl:when>
                    -->
                    <xsl:when test="fn:count($vGenDescriptionRaw) = 0">
                        <xsl:message>[ERROR] No generated descriptions found for [<xsl:value-of select="$vAccession"/>]</xsl:message>
                        <hybs>0</hybs>
                        <samples>0</samples>
                        <rawdatafiles>0</rawdatafiles>
                        <processeddatafiles>0</processeddatafiles>
                    </xsl:when>
                </xsl:choose>
                <xsl:analyze-string select="fn:string($vGenDescriptionRaw[1])" regex="with\s(\d+)\shybridizations.+using\s(\d*)\s*samples.+producing\s(\d+)\sraw.+and\s(\d+)\stransformed" flags="i">
                    <xsl:matching-substring>
                        <hybs><xsl:value-of select="regex-group(1)"/></hybs>
                        <samples><xsl:value-of select="regex-group(2)"/></samples>
                        <rawdatafiles><xsl:value-of select="regex-group(3)"/></rawdatafiles>
                        <processeddatafiles><xsl:value-of select="regex-group(4)"/></processeddatafiles>
                    </xsl:matching-substring>
                </xsl:analyze-string>
            </xsl:variable>

            <xsl:if test="ae:getMappedValue('experiments-in-atlas', accession)">
                <xsl:attribute name="loadedinatlas">true</xsl:attribute>
            </xsl:if>

            <source id="ae1"/>
            <xsl:copy-of select="user"/>

            <releasedate>
                <xsl:choose>
                    <xsl:when test="(releasedate castable as xs:date)">
                        <xsl:value-of select="releasedate"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>[WARN] Release date was missing for [<xsl:value-of select="$vAccession"/>]</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </releasedate>
            <xsl:for-each select="fn:distinct-values(sampleattribute[@category = 'Organism']/@value, 'http://saxon.sf.net/collation?ignore-case=yes')">
                <species><xsl:value-of select="."/></species>
            </xsl:for-each>

            <miamescores>
                <xsl:for-each select="miamescore">
                    <xsl:element name="{fn:lower-case(@name)}">
                        <xsl:value-of select="@value"/>
                    </xsl:element>
                </xsl:for-each>
                <overallscore>
                    <xsl:value-of select="fn:sum(miamescore/@value)"/>
                </overallscore>
            </miamescores>
            <assays>
                <xsl:choose>
                    <xsl:when test="$vGenDescription/hybs > 0">
                        <xsl:value-of select="$vGenDescription/hybs"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="bioassaydatagroup[@isderived = '1']">
                                <xsl:value-of select="fn:sum(bioassaydatagroup[@isderived = '1']/@bioassays)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="bioassaydatagroup[@isderived = '0']">
                                        <xsl:value-of select="fn:sum(bioassaydatagroup[@isderived = '0']/@bioassays)"/>
                                    </xsl:when>
                                    <xsl:otherwise>0</xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </assays>
            <samples>
                <xsl:choose>
                    <xsl:when test="fn:number($vGenDescription/samples) > 0">
                        <xsl:value-of select="$vGenDescription/samples"/>
                    </xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
            </samples>
            <rawdatafiles>
                <xsl:attribute name="available" select="if ('0' != fn:sum(ae:getMappedValue('raw-files', $vAccession))) then 'true' else 'false'"/>
                <xsl:value-of select="$vGenDescription/rawdatafiles"/>
            </rawdatafiles>
            <processeddatafiles>
                <xsl:attribute name="available" select="if ('0' != fn:sum(ae:getMappedValue('processed-files', $vAccession))) then 'true' else 'false'"/>
                <xsl:value-of select="$vGenDescription/processeddatafiles"/>
            </processeddatafiles>
            <xsl:for-each-group select="sampleattribute[@value != '']" group-by="@category">
                <xsl:sort select="fn:lower-case(@category)" order="ascending"/>
                <sampleattribute>
                    <category><xsl:value-of select="@category"/></category>
                    <xsl:for-each select="current-group()">
                        <xsl:sort select="fn:lower-case(@value)" order="ascending"/>
                        <value><xsl:value-of select="@value"/></value>
					</xsl:for-each>
                </sampleattribute>
            </xsl:for-each-group>
            <xsl:for-each-group select="experimentalfactor[@value != '']" group-by="@name">
                <xsl:sort select="fn:lower-case(@name)" order="ascending"/>
                <experimentalfactor>
                    <name><xsl:value-of select="@name"/></name>
                    <xsl:for-each select="current-group()">
                        <xsl:sort select="fn:lower-case(@value)" order="ascending"/>
                        <value><xsl:value-of select="@value"/></value>
					</xsl:for-each>
                </experimentalfactor>
            </xsl:for-each-group>

            <xsl:apply-templates select="*" mode="copy" />
        </experiment>
    </xsl:template>

    <!-- this template prohibits default copying of these elements -->
    <xsl:template match="sampleattribute | experimentalfactor | miamescore | releasedate | user" mode="copy"/>

    <xsl:template match="name" mode="copy">
        <name>
            <xsl:apply-templates mode="html" select="ae:htmlDocument(fn:concat('&lt;body&gt;', ., '&lt;/body&gt;'))" />
        </name>
    </xsl:template>

    <xsl:template match="secondaryaccession" mode="copy">
        <xsl:choose>
            <xsl:when test="fn:string-length(.) = 0"/>
            <xsl:when test="fn:contains(., ';G')">
            <xsl:variable name="vValues" select="fn:tokenize(., '\s*;\s*')"/>
                <xsl:for-each select="$vValues">
                    <xsl:element name="secondaryaccession">
                        <xsl:value-of select="."/>
                    </xsl:element>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise><xsl:copy-of select="."/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="experimentdesign | experimenttype" mode="copy">
        <xsl:variable name="vName" select="fn:name()"/>
        <xsl:variable name="vValues" select="fn:tokenize(., '\s*,\s*')"/>
        <xsl:for-each select="$vValues">
            <xsl:element name="{$vName}">
                <xsl:value-of select="."/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="bibliography" mode="copy">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:variable name="vAttrName" select="fn:lower-case(fn:name())"/>
                <xsl:variable name="vAttrValue" select="."/>
                <xsl:choose>
                    <xsl:when test="$vAttrName = 'pages' and ($vAttrValue = '' or $vAttrValue = '-')"/>
                    <xsl:otherwise>
                        <xsl:element name="{$vAttrName}">
                            <xsl:value-of select="$vAttrValue" />
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="description" mode="copy">
        <description>
            <id><xsl:value-of select="@id"/></id>
            <text>
                <xsl:copy-of select="ae:parseHtml(.)"/>
            </text>
        </description>
    </xsl:template>

    <xsl:template match="*" mode="copy">
        <xsl:copy>
            <xsl:if test="@*">
                <xsl:for-each select="@*">
                    <xsl:element name="{fn:lower-case(fn:name())}">
                        <xsl:value-of select="." />
                    </xsl:element>
                </xsl:for-each>
            </xsl:if>
            <xsl:apply-templates mode="copy" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
package uk.ac.ebi.arrayexpress.utils.saxon.functions.saxon;

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

public class ParseHTMLFunction //extends ExtensionFunctionDefinition
{
    /*
    private static final StructuredQName qName =
            new StructuredQName("", NamespaceConstant.SAXON, "parse-html");

    public StructuredQName getFunctionQName()
    {
        return qName;
    }

    public int getMinimumNumberOfArguments()
    {
        return 1;
    }

    public int getMaximumNumberOfArguments()
    {
        return 1;
    }

    public SequenceType[] getArgumentTypes()
    {
        return new SequenceType[]{SequenceType.SINGLE_STRING};
    }

    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes)
    {
        return SequenceType.SINGLE_NODE;
    }

    public ExtensionFunctionCall makeCallExpression()
    {
        return new ParseHTMLCall();
    }

    private static class ParseHTMLCall extends ExtensionFunctionCall
    {
        private static final long serialVersionUID = 1907927904336872728L;

        private String baseURI;
        private transient Parser parser;

        @SuppressWarnings("unchecked")
        public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException
        {
            Controller controller = context.getController();
            baseURI = (null != context.getContextItem()) ? ((NodeInfo)context.getContextItem()).getBaseURI() : "";

            StringReader sr = new StringReader(SequenceTool.getStringValue(arguments[0]));

            InputSource is = new InputSource(sr);
            is.setSystemId(baseURI);
            Source source = new SAXSource(getParser(), is);
            source.setSystemId(baseURI);
            Builder b = controller.makeBuilder();
            Receiver s = b;
            source = AugmentedSource.makeAugmentedSource(source);
            ((AugmentedSource) source).setStripSpace(Whitespace.XSLT);
            if (controller.getExecutable().stripsInputTypeAnnotations()) {
                s = controller.getConfiguration().getAnnotationStripper(s);
            }
            try {
                Sender.send(source, s, null);
                NodeInfo node = b.getCurrentRoot();
                b.reset();
                return node;
            } catch (XPathException err) {
                throw new XPathException(err);
            }
        }

        private Parser getParser()
        {
            if (null == parser) {
                parser = new Parser();
                // configure it the way we want
                try {
                    parser.setFeature(Parser.defaultAttributesFeature, false);
                    parser.setFeature(Parser.ignoreBogonsFeature, true);
                } catch (Exception x) {
                    // do nothing
                }
            }

            return parser;
        }
    }
    */
}

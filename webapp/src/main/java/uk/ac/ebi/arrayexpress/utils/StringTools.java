package uk.ac.ebi.arrayexpress.utils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

public class StringTools
{
    private StringTools()
    {
    }

    public final static String EOL = System.getProperty("line.separator");
    public final static String EMPTY_STRING = "";

    public static boolean isNotEmpty( String s )
    {
        return null != s && !s.isEmpty();
    }

    public static String listToString( List<String> l, String separator )
    {
        if (null == l) {
            return null;
        }

        return arrayToString(l.toArray(new String[l.size()]), separator);
    }

    public static String arrayToString( String[] a, String separator )
    {
        if (null == a || null == separator) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i = 1; i < a.length; i++) {
                result.append(separator);
                result.append(a[i]);
            }
        }
        return result.toString();
    }

    public static String streamToString( InputStream is, String encoding ) throws IOException
    {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(EOL);
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public static Set<String> streamToStringSet( InputStream is, String encoding ) throws IOException
    {
        String[] lines = streamToString(is, encoding).split(EOL);
        return new HashSet<>(Arrays.asList(lines));
    }

    public static String fileToString( File f, String encoding ) throws IOException
    {
        if (f.exists()) {
            InputStream is = new FileInputStream(f);
            return streamToString(is, encoding);
        } else {
            throw new FileNotFoundException("File [" + f.getName() + "] not found");
        }
    }

    public static void stringToFile( String string, File file, String encoding ) throws IOException
    {
        BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file)
                        , encoding
                )
        );

        w.write(string);
        w.close();
    }

    public static String longDateTimeToXSDDateTime( long dateTime )
    {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(dateTime));
    }

    public static Date rfc822StringToDate( String rfc822 )
    {
        Date date = null;
        try {
            if (null != rfc822) {
                // hack for crazy mail.ru bot
                rfc822 = rfc822.replaceFirst("^(\\w{3}, \\d{2} \\w{3}) \\d{5} (\\d{2}:\\d{2}:\\d{2} \\w{3})$", "$1 2999 $2");
                date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").parse(rfc822);
            }
        } catch (ParseException x) {
            // ignore
        }
        return date;
    }

    public static String safeToString( Object obj, String nullObjString )
    {
        return (null == obj) ? nullObjString : obj.toString();
    }

    public static String nullToEmpty( String str ) {
        return (null == str) ? EMPTY_STRING : str;
    }

    public static Boolean stringToBoolean( String boolString )
    {
        if (null == boolString) {
            throw new IllegalArgumentException("Cannot accept null argument");
        } else {
            return "true".equalsIgnoreCase(boolString) || "1".equals(boolString) || "on".equalsIgnoreCase(boolString);
        }
    }

    private static HashMap<String,String> htmlEntities;
    static {
        htmlEntities = new HashMap<>();
        htmlEntities.put("lt","<")    ; htmlEntities.put("gt",">");
        htmlEntities.put("amp","&");
        htmlEntities.put("apos", "'") ; htmlEntities.put("quot","\"");
        htmlEntities.put("agrave","à"); htmlEntities.put("Agrave","À");
        htmlEntities.put("acirc","â") ; htmlEntities.put("auml","ä");
        htmlEntities.put("Auml","Ä")  ; htmlEntities.put("Acirc","Â");
        htmlEntities.put("aring","å") ; htmlEntities.put("Aring","Å");
        htmlEntities.put("aelig","æ") ; htmlEntities.put("AElig","Æ" );
        htmlEntities.put("ccedil","ç"); htmlEntities.put("Ccedil","Ç");
        htmlEntities.put("eacute","é"); htmlEntities.put("Eacute","É" );
        htmlEntities.put("egrave","è"); htmlEntities.put("Egrave","È");
        htmlEntities.put("ecirc","ê") ; htmlEntities.put("Ecirc","Ê");
        htmlEntities.put("euml","ë")  ; htmlEntities.put("Euml","Ë");
        htmlEntities.put("iuml","ï")  ; htmlEntities.put("Iuml","Ï");
        htmlEntities.put("ocirc","ô") ; htmlEntities.put("Ocirc","Ô");
        htmlEntities.put("ouml","ö")  ; htmlEntities.put("Ouml","Ö");
        htmlEntities.put("oslash","ø"); htmlEntities.put("Oslash","Ø");
        htmlEntities.put("szlig","ß") ; htmlEntities.put("ugrave","ù");
        htmlEntities.put("Ugrave","Ù"); htmlEntities.put("ucirc","û");
        htmlEntities.put("Ucirc","Û") ; htmlEntities.put("uuml","ü");
        htmlEntities.put("Uuml","Ü")  ; htmlEntities.put("nbsp"," ");
        htmlEntities.put("copy","\u00a9");
        htmlEntities.put("reg","\u00ae");
        htmlEntities.put("euro","\u20a0");
    }

    public static String unescapeHTMLEntities( String in )
    {
        if (null == in)
            return null;

        StringBuilder out = new StringBuilder(in.length());

        int entityStart;
        int entityEnd;
        int currentIndex = 0;

        while (currentIndex < in.length()) {
            entityStart = in.indexOf("&", currentIndex);
            if (-1 == entityStart) {
                out.append(in.substring(currentIndex));
                break;
            }
            out.append(in.substring(currentIndex, entityStart));
            entityEnd = in.indexOf(";", entityStart);
            if (-1 != entityEnd && entityEnd - entityStart < 8 && entityEnd - entityStart > 1) {
                String entity = htmlEntities.get(in.substring(entityStart + 1, entityEnd));
                if (null != entity) {
                    out.append(entity);
                    currentIndex = entityEnd + 1;
                    continue;
                }
            }
            out.append("&");
            currentIndex = entityStart + 1;
        }
        return out.toString();
    }

    public static String unescapeXMLDecimalEntities( String in )
    {
        if (null == in)
            return null;

        StringBuilder out = new StringBuilder(in.length());

        int entityStart;
        int entityEnd;
        int currentIndex = 0;

        while (currentIndex < in.length()) {
            entityStart = in.indexOf("&#", currentIndex);
            if (-1 == entityStart) {
                out.append(in.substring(currentIndex));
                break;
            }
            out.append(in.substring(currentIndex, entityStart));
            entityEnd = in.indexOf(";", entityStart);
            if (-1 != entityEnd && in.substring(entityStart + 2, entityEnd).matches("^\\d+$")) {
                // good stuff, we found decimal entity
                out.append((char) Integer.parseInt(in.substring(entityStart + 2, entityEnd)));
                currentIndex = entityEnd + 1;
            } else {
                out.append("&#");
                currentIndex = entityStart + 2;
            }
        }
        return out.toString();
    }

    public final static char ILLEGAL_CHAR_REPRESENATION = 0xfffd;
    private final static char C1_CONTROL_TRANSCODE_MAP[] = {
            8364, ILLEGAL_CHAR_REPRESENATION, 8218, 402, 8222, 8230, 8224, 8225,
            710, 8240, 352, 8249, 338, ILLEGAL_CHAR_REPRESENATION, 381, ILLEGAL_CHAR_REPRESENATION,
            ILLEGAL_CHAR_REPRESENATION, 8216, 8217, 8220, 8221, 8226, 8211, 8212,
            732, 8482, 353, 8250, 339, ILLEGAL_CHAR_REPRESENATION, 382, 376
    };

    public static String escapeChar( char in )
    {
        return "&#" + String.valueOf((int) in) + ";";
    }

    public static Character transcodeUnsafeHTMLChar(char in)
    {
        if (0x09 == in || 0x0a == in || 0x0d == in || (in >= 0x20 && in <= 0x7e)) {
            return in;
        } else if (in >= 0x80 && in <= 0x9f) {
            return C1_CONTROL_TRANSCODE_MAP[in - 0x80];
        } else if ((in >= 0xa0 && in <= 0xd7ff) || (in >= 0xe000 && in <= 0xfffd) || (in >= 0x10000)) {
            return in;
        } else if (in >= 0x80) {
            return ILLEGAL_CHAR_REPRESENATION;
        } else {
            return null;
        }
    }

    public static String replaceIllegalHTMLCharacters( String in, boolean shouldEscape )
    {
        if (null == in)
            return null;

        StringBuilder out = new StringBuilder(in.length() * 2);
        Character decoded;

        for (int i = 0; i < in.length(); i++) {
            decoded = transcodeUnsafeHTMLChar(in.charAt(i));
            if (null != decoded) {
                out.append(decoded >= 0x80 && shouldEscape ? escapeChar(decoded) : decoded);
            }
        }
        return out.toString();
    }

    public static String detectDecodeUTF8Sequences( String in )
    {
        if (null == in)
            return null;

        StringBuilder sb = new StringBuilder(in.length() * 2);

        char ch, decoded;
        for (int ix = 0; ix < in.length(); ++ix) {
            ch = in.charAt(ix);

            if (ch <= 0x7f) {
                // there are no problems with ascii

                // wow, that's a hack
                if (0x43 == ch && (ix + 3) < in.length() && 0x2 == in.charAt(ix + 1) && 0x42 == in.charAt(ix + 2) && (in.charAt(ix + 3) >= 0x12 && in.charAt(ix + 3) <= 0x14)) {
                    switch (in.charAt(ix + 3)) {
                        case 0x12:
                            sb.append('\'');
                            break;

                        case 0x13:
                        case 0x14:
                            sb.append('"');
                            break;
                    }
                    ix += 3;
                } else {
                    sb.append(ch);
                }
            } else if (ch >= 0xc2 && ch <= 0xdf && (ix + 1) < in.length()) {
                // this is possibly a start of two-byte utf-8 sequence
                // let's try to decode it and if it's any good, use it
                // or just copy the original sequence
                decoded = StringTools.decodeUTF8((byte) ch, (byte) in.charAt(ix + 1));
                if (ILLEGAL_CHAR_REPRESENATION == decoded) {
                    sb.append(ch);
                } else {
                    sb.append(decoded);
                    ix++;
                }
            } else if (ch >= 0xe0 && ch <= 0xef && (ix + 2) < in.length()) {
                // this is possibly a start of three-byte utf-8 sequence
                // let's try to decode it and if it's any good, use it
                // or just copy the original sequence
                decoded = StringTools.decodeUTF8((byte) ch, (byte) in.charAt(ix + 1), (byte) in.charAt(ix + 2));
                if (ILLEGAL_CHAR_REPRESENATION == decoded) {
                    sb.append(ch);
                } else {
                    sb.append(decoded);
                    ix += 2;
                }
            } else {
                // the rest is interpreted as pure unicode (we don't decode four-byte utf-8's)
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static char decodeUTF8( int[] b )
    {
        if (2 == b.length) {
            return decodeUTF8((byte)b[0], (byte)b[1]);
        } else if (3 == b.length)
            return decodeUTF8((byte)b[0], (byte)b[1], (byte)b[2]);
        else
            return ILLEGAL_CHAR_REPRESENATION;
    }

    public static char decodeUTF8( byte b0, byte b1 )
    {
        if ((0xc0 == (b0 & 0xe0)) && (0x80 == (b1 & 0xc0))) {
            return (char) (((b0 & 0x1f) << 6) + (b1 & 0x3f));
        } else {
            return ILLEGAL_CHAR_REPRESENATION;
        }
    }

    public static char decodeUTF8( byte b0, byte b1, byte b2 )
    {
        if ((0xe0 == (b0 & 0xf0)) && (0x80 == (b1 & 0xc0)) && (0x80 == (b2 & 0xc0))) {
            return (char) (((b0 & 0x0f) << 12) + ((b1 & 0x3f) << 6) + (b2 & 0x3f));
        } else {
            return ILLEGAL_CHAR_REPRESENATION;
        }
    }

}

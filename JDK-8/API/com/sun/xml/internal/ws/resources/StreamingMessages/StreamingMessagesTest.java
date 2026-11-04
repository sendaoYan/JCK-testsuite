/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.StreamingMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main StreamingMessagesTest
 */

import java.lang.reflect.Modifier;


public class StreamingMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.StreamingMessages", false, StreamingMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.StreamingMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("FASTINFOSET_DECODING_NOT_ACCEPTED"), "method FASTINFOSET_DECODING_NOT_ACCEPTED");
        check(mn.contains("FASTINFOSET_NO_IMPLEMENTATION"), "method FASTINFOSET_NO_IMPLEMENTATION");
        check(mn.contains("INVALID_PROPERTY_VALUE_INTEGER"), "method INVALID_PROPERTY_VALUE_INTEGER");
        check(mn.contains("INVALID_PROPERTY_VALUE_LONG"), "method INVALID_PROPERTY_VALUE_LONG");
        check(mn.contains("SOURCEREADER_INVALID_SOURCE"), "method SOURCEREADER_INVALID_SOURCE");
        check(mn.contains("STAXREADER_XMLSTREAMEXCEPTION"), "method STAXREADER_XMLSTREAMEXCEPTION");
        check(mn.contains("STAX_CANT_CREATE"), "method STAX_CANT_CREATE");
        check(mn.contains("STREAMING_IO_EXCEPTION"), "method STREAMING_IO_EXCEPTION");
        check(mn.contains("STREAMING_PARSE_EXCEPTION"), "method STREAMING_PARSE_EXCEPTION");
        check(mn.contains("WOODSTOX_CANT_LOAD"), "method WOODSTOX_CANT_LOAD");
        check(mn.contains("XMLREADER_ILLEGAL_STATE_ENCOUNTERED"), "method XMLREADER_ILLEGAL_STATE_ENCOUNTERED");
        check(mn.contains("XMLREADER_IO_EXCEPTION"), "method XMLREADER_IO_EXCEPTION");
        check(mn.contains("XMLREADER_NESTED_ERROR"), "method XMLREADER_NESTED_ERROR");
        check(mn.contains("XMLREADER_PARSE_EXCEPTION"), "method XMLREADER_PARSE_EXCEPTION");
        check(mn.contains("XMLREADER_UNEXPECTED_CHARACTER_CONTENT"), "method XMLREADER_UNEXPECTED_CHARACTER_CONTENT");
        check(mn.contains("XMLREADER_UNEXPECTED_STATE"), "method XMLREADER_UNEXPECTED_STATE");
        check(mn.contains("XMLREADER_UNEXPECTED_STATE_MESSAGE"), "method XMLREADER_UNEXPECTED_STATE_MESSAGE");
        check(mn.contains("XMLREADER_UNEXPECTED_STATE_TAG"), "method XMLREADER_UNEXPECTED_STATE_TAG");
        check(mn.contains("XMLRECORDER_RECORDING_ENDED"), "method XMLRECORDER_RECORDING_ENDED");
        check(mn.contains("XMLWRITER_IO_EXCEPTION"), "method XMLWRITER_IO_EXCEPTION");
        check(mn.contains("XMLWRITER_NESTED_ERROR"), "method XMLWRITER_NESTED_ERROR");
        check(mn.contains("XMLWRITER_NO_PREFIX_FOR_URI"), "method XMLWRITER_NO_PREFIX_FOR_URI");
        check(mn.contains("localizableFASTINFOSET_DECODING_NOT_ACCEPTED"), "method localizableFASTINFOSET_DECODING_NOT_ACCEPTED");
        check(mn.contains("localizableFASTINFOSET_NO_IMPLEMENTATION"), "method localizableFASTINFOSET_NO_IMPLEMENTATION");
        check(mn.contains("localizableINVALID_PROPERTY_VALUE_INTEGER"), "method localizableINVALID_PROPERTY_VALUE_INTEGER");
        check(mn.contains("localizableINVALID_PROPERTY_VALUE_LONG"), "method localizableINVALID_PROPERTY_VALUE_LONG");
        check(mn.contains("localizableSOURCEREADER_INVALID_SOURCE"), "method localizableSOURCEREADER_INVALID_SOURCE");
        check(mn.contains("localizableSTAXREADER_XMLSTREAMEXCEPTION"), "method localizableSTAXREADER_XMLSTREAMEXCEPTION");
        check(mn.contains("localizableSTAX_CANT_CREATE"), "method localizableSTAX_CANT_CREATE");
        check(mn.contains("localizableSTREAMING_IO_EXCEPTION"), "method localizableSTREAMING_IO_EXCEPTION");
        check(mn.contains("localizableSTREAMING_PARSE_EXCEPTION"), "method localizableSTREAMING_PARSE_EXCEPTION");
        check(mn.contains("localizableWOODSTOX_CANT_LOAD"), "method localizableWOODSTOX_CANT_LOAD");
        check(mn.contains("localizableXMLREADER_ILLEGAL_STATE_ENCOUNTERED"), "method localizableXMLREADER_ILLEGAL_STATE_ENCOUNTERED");
        check(mn.contains("localizableXMLREADER_IO_EXCEPTION"), "method localizableXMLREADER_IO_EXCEPTION");
        check(mn.contains("localizableXMLREADER_NESTED_ERROR"), "method localizableXMLREADER_NESTED_ERROR");
        check(mn.contains("localizableXMLREADER_PARSE_EXCEPTION"), "method localizableXMLREADER_PARSE_EXCEPTION");
        check(mn.contains("localizableXMLREADER_UNEXPECTED_CHARACTER_CONTENT"), "method localizableXMLREADER_UNEXPECTED_CHARACTER_CONTENT");
        check(mn.contains("localizableXMLREADER_UNEXPECTED_STATE"), "method localizableXMLREADER_UNEXPECTED_STATE");
        check(mn.contains("localizableXMLREADER_UNEXPECTED_STATE_MESSAGE"), "method localizableXMLREADER_UNEXPECTED_STATE_MESSAGE");
        check(mn.contains("localizableXMLREADER_UNEXPECTED_STATE_TAG"), "method localizableXMLREADER_UNEXPECTED_STATE_TAG");
        check(mn.contains("localizableXMLRECORDER_RECORDING_ENDED"), "method localizableXMLRECORDER_RECORDING_ENDED");
        check(mn.contains("localizableXMLWRITER_IO_EXCEPTION"), "method localizableXMLWRITER_IO_EXCEPTION");
        check(mn.contains("localizableXMLWRITER_NESTED_ERROR"), "method localizableXMLWRITER_NESTED_ERROR");
        check(mn.contains("localizableXMLWRITER_NO_PREFIX_FOR_URI"), "method localizableXMLWRITER_NO_PREFIX_FOR_URI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

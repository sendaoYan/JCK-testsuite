/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.MtomCodec conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MtomCodecTest
 */

import java.lang.reflect.Modifier;


public class MtomCodecTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.MtomCodec", false, MtomCodecTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.MtomCodec"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.encoding.MimeCodec"), "superclass com.sun.xml.internal.ws.encoding.MimeCodec");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copy"), "method copy");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("determinePacketEncoding"), "method determinePacketEncoding");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("getActionParameter"), "method getActionParameter");
        check(mn.contains("getMimeType"), "method getMimeType");
        check(mn.contains("getSOAPXopContentType"), "method getSOAPXopContentType");
        check(mn.contains("getStaticContentType"), "method getStaticContentType");
        check(mn.contains("getStaticContentTypeStatic"), "method getStaticContentTypeStatic");
        check(mn.contains("writeMimeHeaders"), "method writeMimeHeaders");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("XOP_LOCALNAME"), "field XOP_LOCALNAME");
        check(fn.contains("XOP_NAMESPACEURI"), "field XOP_NAMESPACEURI");
        check(fn.contains("XOP_XML_MIME_TYPE"), "field XOP_XML_MIME_TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

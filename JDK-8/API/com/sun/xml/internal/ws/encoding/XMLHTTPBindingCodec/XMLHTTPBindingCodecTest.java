/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XMLHTTPBindingCodecTest
 */

import java.lang.reflect.Modifier;


public class XMLHTTPBindingCodecTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec", false, XMLHTTPBindingCodecTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.encoding.MimeCodec"), "superclass com.sun.xml.internal.ws.encoding.MimeCodec");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copy"), "method copy");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("getMimeRootCodec"), "method getMimeRootCodec");
        check(mn.contains("getMimeType"), "method getMimeType");
        check(mn.contains("getStaticContentType"), "method getStaticContentType");
        check(mn.contains("requiresTransformationOfDataSource"), "method requiresTransformationOfDataSource");
        check(mn.contains("transformDataSource"), "method transformDataSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

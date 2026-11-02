/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.SOAPBindingCodec conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAPBindingCodecTest
 */

import java.lang.reflect.Modifier;


public class SOAPBindingCodecTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.SOAPBindingCodec", false, SOAPBindingCodecTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.SOAPBindingCodec"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.encoding.MimeCodec"), "superclass com.sun.xml.internal.ws.encoding.MimeCodec");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.pipe.SOAPBindingCodec"), "implements com.sun.xml.internal.ws.api.pipe.SOAPBindingCodec");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copy"), "method copy");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("getMimeType"), "method getMimeType");
        check(mn.contains("getStaticContentType"), "method getStaticContentType");
        check(mn.contains("getXMLCodec"), "method getXMLCodec");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("DEFAULT_ENCODING"), "field DEFAULT_ENCODING");
        check(fn.contains("UTF8_ENCODING"), "field UTF8_ENCODING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

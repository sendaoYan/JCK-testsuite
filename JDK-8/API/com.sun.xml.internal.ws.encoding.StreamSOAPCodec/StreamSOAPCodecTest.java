/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.StreamSOAPCodec conformance: reflection (kind, superclass, interfaces, methods).
 * @run main StreamSOAPCodecTest
 */

import java.lang.reflect.Modifier;


public class StreamSOAPCodecTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.StreamSOAPCodec", false, StreamSOAPCodecTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.StreamSOAPCodec"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.pipe.StreamSOAPCodec"), "implements com.sun.xml.internal.ws.api.pipe.StreamSOAPCodec");
        check(has(ifs, "com.sun.xml.internal.ws.encoding.RootOnlyCodec"), "implements com.sun.xml.internal.ws.encoding.RootOnlyCodec");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copy"), "method copy");
        check(mn.contains("create"), "method create");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("getContenTypeBuilder"), "method getContenTypeBuilder");
        check(mn.contains("getContentType"), "method getContentType");
        check(mn.contains("getDefaultContentType"), "method getDefaultContentType");
        check(mn.contains("getExpectedContentTypes"), "method getExpectedContentTypes");
        check(mn.contains("getStaticContentType"), "method getStaticContentType");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("serializationFeature"), "field serializationFeature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

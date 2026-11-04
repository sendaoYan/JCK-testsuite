/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.MtomCodec.MtomStreamWriterImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MtomStreamWriterImplTest
 */

import java.lang.reflect.Modifier;


public class MtomStreamWriterImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.MtomCodec$MtomStreamWriterImpl", false, MtomStreamWriterImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.MtomCodec$MtomStreamWriterImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.util.xml.XMLStreamWriterFilter"), "superclass com.sun.xml.internal.ws.util.xml.XMLStreamWriterFilter");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.org.jvnet.staxex.XMLStreamWriterEx"), "implements com.sun.xml.internal.org.jvnet.staxex.XMLStreamWriterEx");
        check(has(ifs, "com.sun.xml.internal.ws.encoding.HasEncoding"), "implements com.sun.xml.internal.ws.encoding.HasEncoding");
        check(has(ifs, "com.sun.xml.internal.ws.streaming.MtomStreamWriter"), "implements com.sun.xml.internal.ws.streaming.MtomStreamWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttachmentMarshaller"), "method getAttachmentMarshaller");
        check(mn.contains("getEncoding"), "method getEncoding");
        check(mn.contains("getMtomAttachments"), "method getMtomAttachments");
        check(mn.contains("getNamespaceContext"), "method getNamespaceContext");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("writeBinary"), "method writeBinary");
        check(mn.contains("writePCDATA"), "method writePCDATA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.sun.xml.internal.ws.encoding.fastinfoset.FastInfosetStreamReaderRecyclable conformance: reflection (kind, superclass, interfaces, methods).
 * @run main FastInfosetStreamReaderRecyclableTest
 */

import java.lang.reflect.Modifier;


public class FastInfosetStreamReaderRecyclableTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.encoding.fastinfoset.FastInfosetStreamReaderRecyclable", false, FastInfosetStreamReaderRecyclableTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.encoding.fastinfoset.FastInfosetStreamReaderRecyclable"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser"), "superclass com.sun.xml.internal.fastinfoset.stax.StAXDocumentParser");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory$RecycleAware"), "implements com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory$RecycleAware");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("onRecycled"), "method onRecycled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

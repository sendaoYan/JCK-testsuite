/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.stream.XMLStreamReaderMessage conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XMLStreamReaderMessageTest
 */

import java.lang.reflect.Modifier;


public class XMLStreamReaderMessageTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.stream.XMLStreamReaderMessage", false, XMLStreamReaderMessageTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.stream.XMLStreamReaderMessage"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.message.stream.StreamBasedMessage"), "superclass com.sun.xml.internal.ws.api.message.stream.StreamBasedMessage");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("msg"), "field msg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

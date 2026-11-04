/*
 * @test
 * @summary com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil conformance: reflection (kind, superclass, interfaces, methods).
 * @run main XMLStreamReaderUtilTest
 */

import java.lang.reflect.Modifier;


public class XMLStreamReaderUtilTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil", false, XMLStreamReaderUtilTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "method close");
        check(mn.contains("currentWhiteSpaceContent"), "method currentWhiteSpaceContent");
        check(mn.contains("getAttributes"), "method getAttributes");
        check(mn.contains("getElementQName"), "method getElementQName");
        check(mn.contains("getElementText"), "method getElementText");
        check(mn.contains("getStateName"), "method getStateName");
        check(mn.contains("next"), "method next");
        check(mn.contains("nextContent"), "method nextContent");
        check(mn.contains("nextElementContent"), "method nextElementContent");
        check(mn.contains("nextWhiteSpaceContent"), "method nextWhiteSpaceContent");
        check(mn.contains("readRest"), "method readRest");
        check(mn.contains("skipElement"), "method skipElement");
        check(mn.contains("skipSiblings"), "method skipSiblings");
        check(mn.contains("toNextTag"), "method toNextTag");
        check(mn.contains("verifyReaderState"), "method verifyReaderState");
        check(mn.contains("verifyTag"), "method verifyTag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

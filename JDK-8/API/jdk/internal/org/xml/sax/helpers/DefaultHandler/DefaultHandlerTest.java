/*
 * @test
 * @summary jdk.internal.org.xml.sax.helpers.DefaultHandler conformance: type shape via reflection (headless-safe).
 * @run main DefaultHandlerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DefaultHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.helpers.DefaultHandler", false, DefaultHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "jdk.internal.org.xml.sax.EntityResolver"), "implements jdk.internal.org.xml.sax.EntityResolver");
        check(hasInterface(c, "jdk.internal.org.xml.sax.DTDHandler"), "implements jdk.internal.org.xml.sax.DTDHandler");
        check(hasInterface(c, "jdk.internal.org.xml.sax.ContentHandler"), "implements jdk.internal.org.xml.sax.ContentHandler");
        check(hasInterface(c, "jdk.internal.org.xml.sax.ErrorHandler"), "implements jdk.internal.org.xml.sax.ErrorHandler");
        check(hasMethod(c, "characters"), "declares characters");
        check(hasMethod(c, "endDocument"), "declares endDocument");
        check(hasMethod(c, "endElement"), "declares endElement");
        check(hasMethod(c, "endPrefixMapping"), "declares endPrefixMapping");
        check(hasMethod(c, "error"), "declares error");
        check(hasMethod(c, "fatalError"), "declares fatalError");
        check(hasMethod(c, "ignorableWhitespace"), "declares ignorableWhitespace");
        check(hasMethod(c, "notationDecl"), "declares notationDecl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

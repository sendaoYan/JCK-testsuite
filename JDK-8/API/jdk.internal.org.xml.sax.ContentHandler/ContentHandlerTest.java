/*
 * @test
 * @summary jdk.internal.org.xml.sax.ContentHandler conformance: type shape via reflection (headless-safe).
 * @run main ContentHandlerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ContentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.xml.sax.ContentHandler", false, ContentHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasMethod(c, "characters"), "declares characters");
        check(hasMethod(c, "endDocument"), "declares endDocument");
        check(hasMethod(c, "endElement"), "declares endElement");
        check(hasMethod(c, "endPrefixMapping"), "declares endPrefixMapping");
        check(hasMethod(c, "ignorableWhitespace"), "declares ignorableWhitespace");
        check(hasMethod(c, "processingInstruction"), "declares processingInstruction");
        check(hasMethod(c, "setDocumentLocator"), "declares setDocumentLocator");
        check(hasMethod(c, "skippedEntity"), "declares skippedEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.internal.util.xml.PropertiesDefaultHandler conformance: type shape via reflection (headless-safe).
 * @run main PropertiesDefaultHandlerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PropertiesDefaultHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.PropertiesDefaultHandler", false, PropertiesDefaultHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.xml.sax.helpers.DefaultHandler"), "superclass jdk.internal.org.xml.sax.helpers.DefaultHandler");
        check(hasMethod(c, "characters"), "declares characters");
        check(hasMethod(c, "endElement"), "declares endElement");
        check(hasMethod(c, "error"), "declares error");
        check(hasMethod(c, "fatalError"), "declares fatalError");
        check(hasMethod(c, "load"), "declares load");
        check(hasMethod(c, "notationDecl"), "declares notationDecl");
        check(hasMethod(c, "resolveEntity"), "declares resolveEntity");
        check(hasMethod(c, "startElement"), "declares startElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

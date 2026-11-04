/*
 * @test
 * @summary com.sun.beans.decoder.DocumentHandler conformance: type shape via reflection (headless-safe).
 * @run main DocumentHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DocumentHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.beans.decoder.DocumentHandler", false, DocumentHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.beans.decoder.DocumentHandler"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getClassLoader"), "method getClassLoader");
        check(names.contains("setClassLoader"), "method setClassLoader");
        check(names.contains("getExceptionListener"), "method getExceptionListener");
        check(names.contains("setExceptionListener"), "method setExceptionListener");
        check(names.contains("getOwner"), "method getOwner");
        check(names.contains("setOwner"), "method setOwner");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

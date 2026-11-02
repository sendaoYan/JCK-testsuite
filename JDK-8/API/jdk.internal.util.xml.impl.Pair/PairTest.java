/*
 * @test
 * @summary jdk.internal.util.xml.impl.Pair conformance: type shape via reflection (headless-safe).
 * @run main PairTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PairTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.xml.impl.Pair", false, PairTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "eqname"), "declares eqname");
        check(hasMethod(c, "eqpref"), "declares eqpref");
        check(hasMethod(c, "local"), "declares local");
        check(hasMethod(c, "pref"), "declares pref");
        check(hasMethod(c, "qname"), "declares qname");
        check(hasField(c, "chars"), "declares field chars");
        check(hasField(c, "id"), "declares field id");
        check(hasField(c, "list"), "declares field list");
        check(hasField(c, "name"), "declares field name");
        check(hasField(c, "next"), "declares field next");
        check(hasField(c, "num"), "declares field num");
        check(hasField(c, "value"), "declares field value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.jfr.internal.EventWriterMethod conformance: type shape via reflection (headless-safe).
 * @run main EventWriterMethodTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EventWriterMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.EventWriterMethod", false, EventWriterMethodTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        check(hasMethod(c, "asASM"), "declares asASM");
        check(hasMethod(c, "lookupMethod"), "declares lookupMethod");
        check(hasMethod(c, "valueOf"), "declares valueOf");
        check(hasMethod(c, "values"), "declares values");
        check(hasField(c, "BEGIN_EVENT"), "declares field BEGIN_EVENT");
        check(hasField(c, "END_EVENT"), "declares field END_EVENT");
        check(hasField(c, "PUT_BOOLEAN"), "declares field PUT_BOOLEAN");
        check(hasField(c, "PUT_BYTE"), "declares field PUT_BYTE");
        check(hasField(c, "PUT_CHAR"), "declares field PUT_CHAR");
        check(hasField(c, "PUT_CLASS"), "declares field PUT_CLASS");
        check(hasField(c, "PUT_DOUBLE"), "declares field PUT_DOUBLE");
        check(hasField(c, "PUT_EVENT_THREAD"), "declares field PUT_EVENT_THREAD");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

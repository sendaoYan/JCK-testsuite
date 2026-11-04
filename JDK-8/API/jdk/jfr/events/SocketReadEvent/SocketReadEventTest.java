/*
 * @test
 * @summary jdk.jfr.events.SocketReadEvent conformance: type shape via reflection (headless-safe).
 * @run main SocketReadEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SocketReadEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.events.SocketReadEvent", false, SocketReadEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.events.AbstractJDKEvent"), "superclass jdk.jfr.events.AbstractJDKEvent");
        check(hasMethod(c, "reset"), "declares reset");
        check(hasField(c, "EVENT"), "declares field EVENT");
        check(hasField(c, "address"), "declares field address");
        check(hasField(c, "bytesRead"), "declares field bytesRead");
        check(hasField(c, "endOfStream"), "declares field endOfStream");
        check(hasField(c, "host"), "declares field host");
        check(hasField(c, "port"), "declares field port");
        check(hasField(c, "timeout"), "declares field timeout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

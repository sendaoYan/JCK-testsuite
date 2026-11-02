/*
 * @test
 * @summary jdk.jfr.internal.MetadataRepository conformance: type shape via reflection (headless-safe).
 * @run main MetadataRepositoryTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MetadataRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.MetadataRepository", false, MetadataRepositoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getEventControls"), "declares getEventControls");
        check(hasMethod(c, "getEventType"), "declares getEventType");
        check(hasMethod(c, "getInstance"), "declares getInstance");
        check(hasMethod(c, "getRegisteredEventTypes"), "declares getRegisteredEventTypes");
        check(hasMethod(c, "register"), "declares register");
        check(hasMethod(c, "setSettings"), "declares setSettings");
        check(hasMethod(c, "setUnregistered"), "declares setUnregistered");
        check(hasMethod(c, "unregister"), "declares unregister");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

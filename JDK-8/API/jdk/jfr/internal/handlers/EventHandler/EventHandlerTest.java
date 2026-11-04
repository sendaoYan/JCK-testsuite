/*
 * @test
 * @summary jdk.jfr.internal.handlers.EventHandler conformance: type shape via reflection (headless-safe).
 * @run main EventHandlerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EventHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.handlers.EventHandler", false, EventHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "duration"), "declares duration");
        check(hasMethod(c, "getEventControl"), "declares getEventControl");
        check(hasMethod(c, "getEventType"), "declares getEventType");
        check(hasMethod(c, "getPlatformEventType"), "declares getPlatformEventType");
        check(hasMethod(c, "isEnabled"), "declares isEnabled");
        check(hasMethod(c, "isRegistered"), "declares isRegistered");
        check(hasMethod(c, "setRegistered"), "declares setRegistered");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

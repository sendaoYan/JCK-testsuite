/*
 * @test
 * @summary jdk.jfr.internal.Utils conformance: type shape via reflection (headless-safe).
 * @run main UtilsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.Utils", false, UtilsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "checkAccessFlightRecorder"), "declares checkAccessFlightRecorder");
        check(hasMethod(c, "checkRegisterPermission"), "declares checkRegisterPermission");
        check(hasMethod(c, "ensureInitialized"), "declares ensureInitialized");
        check(hasMethod(c, "ensureValidEventSubclass"), "declares ensureValidEventSubclass");
        check(hasMethod(c, "formatBits"), "declares formatBits");
        check(hasMethod(c, "formatBitsPerSecond"), "declares formatBitsPerSecond");
        check(hasMethod(c, "formatBytes"), "declares formatBytes");
        check(hasMethod(c, "formatBytesCompact"), "declares formatBytesCompact");
        check(hasField(c, "ACCESS_FLIGHT_RECORDER"), "declares field ACCESS_FLIGHT_RECORDER");
        check(hasField(c, "EVENTS_PACKAGE_NAME"), "declares field EVENTS_PACKAGE_NAME");
        check(hasField(c, "HANDLERS_PACKAGE_NAME"), "declares field HANDLERS_PACKAGE_NAME");
        check(hasField(c, "INSTRUMENT_PACKAGE_NAME"), "declares field INSTRUMENT_PACKAGE_NAME");
        check(hasField(c, "REGISTER_EVENT"), "declares field REGISTER_EVENT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

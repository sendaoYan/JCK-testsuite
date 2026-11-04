/*
 * @test
 * @summary jdk.jfr.internal.EventInstrumentation conformance: type shape via reflection (headless-safe).
 * @run main EventInstrumentationTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EventInstrumentationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.EventInstrumentation", false, EventInstrumentationTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "builUninstrumented"), "declares builUninstrumented");
        check(hasMethod(c, "buildInstrumented"), "declares buildInstrumented");
        check(hasMethod(c, "getClassName"), "declares getClassName");
        check(hasMethod(c, "getEventName"), "declares getEventName");
        check(hasMethod(c, "getFieldInfos"), "declares getFieldInfos");
        check(hasMethod(c, "getSettingInfos"), "declares getSettingInfos");
        check(hasMethod(c, "isValidField"), "declares isValidField");
        check(hasMethod(c, "makeWriteMethod"), "declares makeWriteMethod");
        check(hasField(c, "FIELD_DURATION"), "declares field FIELD_DURATION");
        check(hasField(c, "FIELD_EVENT_THREAD"), "declares field FIELD_EVENT_THREAD");
        check(hasField(c, "FIELD_STACK_TRACE"), "declares field FIELD_STACK_TRACE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

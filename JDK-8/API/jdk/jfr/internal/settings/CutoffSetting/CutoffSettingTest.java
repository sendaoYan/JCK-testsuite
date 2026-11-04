/*
 * @test
 * @summary jdk.jfr.internal.settings.CutoffSetting conformance: type shape via reflection (headless-safe).
 * @run main CutoffSettingTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CutoffSettingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.settings.CutoffSetting", false, CutoffSettingTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.internal.Control"), "superclass jdk.jfr.internal.Control");
        check(hasMethod(c, "combine"), "declares combine");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "isType"), "declares isType");
        check(hasMethod(c, "parseValueSafe"), "declares parseValueSafe");
        check(hasMethod(c, "setValue"), "declares setValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

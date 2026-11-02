/*
 * @test
 * @summary jdk.management.jfr.EventTypeInfo conformance: class.
 * @run main EventTypeInfoTest
 */

import java.lang.reflect.*;


public class EventTypeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.EventTypeInfo");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(hasMethod(c, "createCategoryNames"), "declares createCategoryNames");
        check(hasMethod(c, "creatingSettingDescriptorInfos"), "declares creatingSettingDescriptorInfos");
        check(hasMethod(c, "createSettingDescriptors"), "declares createSettingDescriptors");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        check(hasMethod(c, "getCategoryNames"), "declares getCategoryNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

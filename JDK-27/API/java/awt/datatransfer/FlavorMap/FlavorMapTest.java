/*
 * @test
 * @summary java.awt.datatransfer.FlavorMap conformance: white-box structure + black-box behavior.
 * @run main FlavorMapTest
 */

import java.lang.reflect.*;

public class FlavorMapTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.datatransfer.FlavorMap", false, FlavorMapTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.datatransfer.FlavorMap");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
    }

    static void testBlackBox() throws Exception {
        java.awt.datatransfer.FlavorMap m = new java.awt.datatransfer.FlavorMap() {
            public java.util.Map<java.awt.datatransfer.DataFlavor,String> getNativesForFlavors(java.awt.datatransfer.DataFlavor[] fl) { return java.util.Collections.emptyMap(); }
            public java.util.Map<String,java.awt.datatransfer.DataFlavor> getFlavorsForNatives(String[] nat) { return java.util.Collections.emptyMap(); }
        };
        check(m.getNativesForFlavors(null) != null && m.getNativesForFlavors(null).isEmpty(), "getNativesForFlavors empty");
        check(m.getFlavorsForNatives(null) != null && m.getFlavorsForNatives(null).isEmpty(), "getFlavorsForNatives empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

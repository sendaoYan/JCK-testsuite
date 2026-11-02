/*
 * @test
 * @summary com.sun.beans.editors.EnumEditor conformance: type shape via reflection (headless-safe).
 * @run main EnumEditorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EnumEditorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.beans.editors.EnumEditor", false, EnumEditorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.beans.editors.EnumEditor"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.beans.PropertyEditor"), "implements java.beans.PropertyEditor");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getValue"), "method getValue");
        check(names.contains("setValue"), "method setValue");
        check(names.contains("getAsText"), "method getAsText");
        check(names.contains("setAsText"), "method setAsText");
        check(names.contains("getTags"), "method getTags");
        check(names.contains("getJavaInitializationString"), "method getJavaInitializationString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

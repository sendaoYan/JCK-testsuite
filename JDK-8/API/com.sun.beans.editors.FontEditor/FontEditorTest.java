/*
 * @test
 * @summary com.sun.beans.editors.FontEditor conformance: type shape via reflection (headless-safe).
 * @run main FontEditorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FontEditorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.beans.editors.FontEditor", false, FontEditorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.beans.editors.FontEditor"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.Panel"), "superclass");
        check(hasInterface(c, "java.beans.PropertyEditor"), "implements java.beans.PropertyEditor");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("preferredSize"), "method preferredSize");
        check(names.contains("setValue"), "method setValue");
        check(names.contains("getValue"), "method getValue");
        check(names.contains("getJavaInitializationString"), "method getJavaInitializationString");
        check(names.contains("action"), "method action");
        check(names.contains("isPaintable"), "method isPaintable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

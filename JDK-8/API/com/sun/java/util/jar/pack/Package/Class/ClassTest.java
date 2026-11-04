/*
 * @test
 * @summary com.sun.java.util.jar.pack.Package.Class conformance: type shape via reflection (headless-safe).
 * @run main ClassTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.util.jar.pack.Package$Class", false, ClassTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.util.jar.pack.Package$Class"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.java.util.jar.pack.Attribute$Holder"), "superclass");
        check(hasInterface(c, "java.lang.Comparable"), "implements java.lang.Comparable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getPackage"), "method getPackage");
        check(names.contains("getName"), "method getName");
        check(names.contains("compareTo"), "method compareTo");
        check(names.contains("getCPMap"), "method getCPMap");
        check(names.contains("setCPMap"), "method setCPMap");
        check(names.contains("setInnerClasses"), "method setInnerClasses");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

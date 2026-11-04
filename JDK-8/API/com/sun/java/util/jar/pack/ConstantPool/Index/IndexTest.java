/*
 * @test
 * @summary com.sun.java.util.jar.pack.ConstantPool.Index conformance: type shape via reflection (headless-safe).
 * @run main IndexTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IndexTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.util.jar.pack.ConstantPool$Index", false, IndexTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.util.jar.pack.ConstantPool$Index"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractList"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getMap"), "method getMap");
        check(names.contains("setMap"), "method setMap");
        check(names.contains("size"), "method size");
        check(names.contains("get"), "method get");
        check(names.contains("getEntry"), "method getEntry");
        check(names.contains("contains"), "method contains");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

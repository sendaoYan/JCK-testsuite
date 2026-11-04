/*
 * @test
 * @summary com.sun.java.util.jar.pack.PackerImpl conformance: type shape via reflection (headless-safe).
 * @run main PackerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PackerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.util.jar.pack.PackerImpl", false, PackerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.util.jar.pack.PackerImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.java.util.jar.pack.TLGlobals"), "superclass");
        check(hasInterface(c, "java.util.jar.Pack200$Packer"), "implements java.util.jar.Pack200$Packer");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("properties"), "method properties");
        check(names.contains("pack"), "method pack");
        check(names.contains("addPropertyChangeListener"), "method addPropertyChangeListener");
        check(names.contains("removePropertyChangeListener"), "method removePropertyChangeListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

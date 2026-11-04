/*
 * @test
 * @summary javax.swing.TransferHandler.TransferSupport conformance: type shape via reflection (headless-safe).
 * @run main TransferSupportTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TransferSupportTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.TransferHandler$TransferSupport", false, TransferSupportTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isDrop"), "method isDrop");
        check(names.contains("getComponent"), "method getComponent");
        check(names.contains("getDropLocation"), "method getDropLocation");
        check(names.contains("setShowDropLocation"), "method setShowDropLocation");
        check(names.contains("setDropAction"), "method setDropAction");
        check(names.contains("getDropAction"), "method getDropAction");
        check(names.contains("getUserDropAction"), "method getUserDropAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

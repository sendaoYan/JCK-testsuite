/*
 * @test
 * @summary javax.xml.bind.annotation.DomHandler conformance: interface hierarchy and methods.
 * @run main DomHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DomHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.DomHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createUnmarshaller"), "has method createUnmarshaller");
        check(names.contains("getElement"), "has method getElement");
        check(names.contains("marshal"), "has method marshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

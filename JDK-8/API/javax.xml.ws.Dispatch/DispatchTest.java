/*
 * @test
 * @summary javax.xml.ws.Dispatch conformance: interface hierarchy and methods.
 * @run main DispatchTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DispatchTest {
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
        Class<?> c = Class.forName("javax.xml.ws.Dispatch");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.BindingProvider"), "implements javax.xml.ws.BindingProvider");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("invoke"), "has method invoke");
        check(names.contains("invokeAsync"), "has method invokeAsync");
        check(names.contains("invokeOneWay"), "has method invokeOneWay");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

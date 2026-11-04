/*
 * @test
 * @summary javax.activation.DataContentHandler conformance: interface hierarchy and methods.
 * @run main DataContentHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DataContentHandlerTest {
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
        Class<?> c = Class.forName("javax.activation.DataContentHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getTransferDataFlavors"), "has method getTransferDataFlavors");
        check(names.contains("getTransferData"), "has method getTransferData");
        check(names.contains("getContent"), "has method getContent");
        check(names.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

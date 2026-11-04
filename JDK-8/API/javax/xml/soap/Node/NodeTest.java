/*
 * @test
 * @summary javax.xml.soap.Node conformance: interface hierarchy and methods.
 * @run main NodeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NodeTest {
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
        Class<?> c = Class.forName("javax.xml.soap.Node");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.w3c.dom.Node"), "implements org.w3c.dom.Node");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "has method getValue");
        check(names.contains("setValue"), "has method setValue");
        check(names.contains("setParentElement"), "has method setParentElement");
        check(names.contains("getParentElement"), "has method getParentElement");
        check(names.contains("detachNode"), "has method detachNode");
        check(names.contains("recycleNode"), "has method recycleNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

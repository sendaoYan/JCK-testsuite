/*
 * @test
 * @summary javax.xml.soap.SOAPHeader conformance: interface hierarchy and methods.
 * @run main SOAPHeaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPHeaderTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPHeader");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.SOAPElement"), "implements javax.xml.soap.SOAPElement");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addHeaderElement"), "has method addHeaderElement");
        check(names.contains("examineMustUnderstandHeaderElements"), "has method examineMustUnderstandHeaderElements");
        check(names.contains("examineHeaderElements"), "has method examineHeaderElements");
        check(names.contains("extractHeaderElements"), "has method extractHeaderElements");
        check(names.contains("addNotUnderstoodHeaderElement"), "has method addNotUnderstoodHeaderElement");
        check(names.contains("addUpgradeHeaderElement"), "has method addUpgradeHeaderElement");
        check(names.contains("examineAllHeaderElements"), "has method examineAllHeaderElements");
        check(names.contains("extractAllHeaderElements"), "has method extractAllHeaderElements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.xml.crypto.dsig.SignedInfo conformance: interface hierarchy and methods.
 * @run main SignedInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SignedInfoTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignedInfo");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.XMLStructure"), "implements javax.xml.crypto.XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getCanonicalizationMethod"), "has method getCanonicalizationMethod");
        check(names.contains("getSignatureMethod"), "has method getSignatureMethod");
        check(names.contains("getReferences"), "has method getReferences");
        check(names.contains("getId"), "has method getId");
        check(names.contains("getCanonicalizedData"), "has method getCanonicalizedData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

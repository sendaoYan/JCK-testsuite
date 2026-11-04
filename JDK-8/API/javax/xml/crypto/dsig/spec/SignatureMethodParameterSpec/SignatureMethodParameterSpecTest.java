/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec conformance: interface hierarchy and methods.
 * @run main SignatureMethodParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SignatureMethodParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.security.spec.AlgorithmParameterSpec"), "implements java.security.spec.AlgorithmParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

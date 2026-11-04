/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLValidateContext conformance: interface hierarchy and methods.
 * @run main XMLValidateContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLValidateContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLValidateContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.XMLCryptoContext"), "implements javax.xml.crypto.XMLCryptoContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.xml.crypto.XMLCryptoContext conformance: interface hierarchy and methods.
 * @run main XMLCryptoContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLCryptoContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.XMLCryptoContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getBaseURI"), "has method getBaseURI");
        check(names.contains("setBaseURI"), "has method setBaseURI");
        check(names.contains("getKeySelector"), "has method getKeySelector");
        check(names.contains("setKeySelector"), "has method setKeySelector");
        check(names.contains("getURIDereferencer"), "has method getURIDereferencer");
        check(names.contains("setURIDereferencer"), "has method setURIDereferencer");
        check(names.contains("getNamespacePrefix"), "has method getNamespacePrefix");
        check(names.contains("putNamespacePrefix"), "has method putNamespacePrefix");
        check(names.contains("getDefaultNamespacePrefix"), "has method getDefaultNamespacePrefix");
        check(names.contains("setDefaultNamespacePrefix"), "has method setDefaultNamespacePrefix");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("get"), "has method get");
        check(names.contains("put"), "has method put");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

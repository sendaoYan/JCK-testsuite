/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.DigestMethodParameterSpec conformance: interface hierarchy and methods.
 * @run main DigestMethodParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;


public class DigestMethodParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.DigestMethodParameterSpec");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(ifs.length == 1 && ifs[0].equals("java.security.spec.AlgorithmParameterSpec"),
            "extends java.security.spec.AlgorithmParameterSpec only");
        // pure marker
        check(c.getDeclaredMethods().length == 0, "marker interface, no declared methods");
        check(java.lang.reflect.Modifier.isInterface(c.getModifiers()), "interface modifier");
        // no JDK implementation shipped in this release; anonymous impls are constructible
        DigestMethodParameterSpec anon = new DigestMethodParameterSpec() { };
        check(anon != null, "anonymous implementation constructible");
        // the DOM provider rejects any digest parameters for standard digest algorithms
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        for (String uri : new String[] {DigestMethod.SHA1, DigestMethod.SHA256, DigestMethod.SHA512}) {
            try {
                sf.newDigestMethod(uri, anon);
                check(false, "digest params should be rejected for " + uri);
            } catch (java.security.InvalidAlgorithmParameterException e) {
                check(e.getMessage().contains("no parameters should be specified"), "rejection message for " + uri);
            }
        }
        // null parameters remain the legal form and yield getParameterSpec() == null
        check(sf.newDigestMethod(DigestMethod.SHA256, null).getParameterSpec() == null,
            "null params legal, getParameterSpec null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

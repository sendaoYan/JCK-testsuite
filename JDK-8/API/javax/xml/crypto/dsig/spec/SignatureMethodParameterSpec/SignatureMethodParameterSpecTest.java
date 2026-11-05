/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec conformance: interface hierarchy and methods.
 * @run main SignatureMethodParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.HMACParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;


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
        check(ifs.length == 1 && ifs[0].equals("java.security.spec.AlgorithmParameterSpec"),
            "extends java.security.spec.AlgorithmParameterSpec only");
        // pure marker
        check(c.getDeclaredMethods().length == 0, "marker interface, no declared methods");
        check(java.lang.reflect.Modifier.isInterface(c.getModifiers()), "interface modifier");
        // sole JDK implementation
        HMACParameterSpec h = new HMACParameterSpec(256);
        check(h instanceof SignatureMethodParameterSpec, "HMACParameterSpec implements SignatureMethodParameterSpec");
        // accepted by the factory with identity round-trip
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        check(sf.newSignatureMethod(SignatureMethod.HMAC_SHA1, h).getParameterSpec() == h,
            "HMAC_SHA1 accepts SignatureMethodParameterSpec with identity round-trip");
        // anonymous implementation rejected for algorithms that take no parameters
        try {
            sf.newSignatureMethod(SignatureMethod.RSA_SHA1, new SignatureMethodParameterSpec() { });
            check(false, "anonymous spec should be rejected for RSA_SHA1");
        } catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage().contains("no parameters should be specified"), "rejection message names algorithm");
        }
        // null params legal for RSA
        check(sf.newSignatureMethod(SignatureMethod.RSA_SHA1, null).getParameterSpec() == null,
            "null params legal for RSA_SHA1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

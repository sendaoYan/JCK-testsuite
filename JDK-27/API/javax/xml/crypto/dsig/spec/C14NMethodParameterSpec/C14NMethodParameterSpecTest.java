/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.C14NMethodParameterSpec conformance: interface hierarchy and methods.
 * @run main C14NMethodParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import java.util.Collections;


public class C14NMethodParameterSpecTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dsig.spec.C14NMethodParameterSpec");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.crypto.dsig.spec.TransformParameterSpec"), "implements javax.xml.crypto.dsig.spec.TransformParameterSpec");
        // interface is a pure marker: declares no abstract methods of its own
        check(c.getDeclaredMethods().length == 0, "marker interface, no declared methods");
        check(java.lang.reflect.Modifier.isInterface(c.getModifiers()), "interface modifier");
        // sole JDK implementation
        check(new ExcC14NParameterSpec(Collections.singletonList("p")) instanceof C14NMethodParameterSpec,
            "ExcC14NParameterSpec implements C14NMethodParameterSpec");
        check(new ExcC14NParameterSpec(Collections.singletonList("p")) instanceof javax.xml.crypto.dsig.spec.TransformParameterSpec,
            "C14NMethodParameterSpec extends TransformParameterSpec transitively");
        // accepted by the factory for EXCLUSIVE with identity round-trip
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        C14NMethodParameterSpec spec = new ExcC14NParameterSpec(Collections.singletonList("ns1"));
        check(sf.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, spec).getParameterSpec() == spec,
            "EXCLUSIVE accepts C14NMethodParameterSpec with identity round-trip");
        // anonymous implementation compiles but is rejected by the DOM provider with a precise message
        try {
            sf.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, new C14NMethodParameterSpec() { });
            check(false, "anonymous spec should be rejected");
        } catch (java.security.InvalidAlgorithmParameterException e) {
            check(e.getMessage().contains("ExcC14NParameterSpec"), "anonymous spec rejection names required type");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.xml.crypto.AlgorithmMethod conformance.
 * @run main AlgorithmMethodTest
 */

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.HMACParameterSpec;

public class AlgorithmMethodTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.AlgorithmMethod");
        check(c.isInterface(), "is interface");
        check(c.getInterfaces().length == 0, "extends nothing");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAlgorithm"), "has getAlgorithm");
        check(names.contains("getParameterSpec"), "has getParameterSpec");
        // the dsig algorithm types implement it
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        DigestMethod dm = sf.newDigestMethod(DigestMethod.SHA256, null);
        check(dm instanceof AlgorithmMethod, "DigestMethod is AlgorithmMethod");
        check(dm.getAlgorithm().equals(DigestMethod.SHA256), "digest algorithm URI round-trip");
        check(dm.getParameterSpec() == null, "digest parameter spec null without params");
        SignatureMethod sm = sf.newSignatureMethod(SignatureMethod.HMAC_SHA1, new HMACParameterSpec(160));
        check(sm instanceof AlgorithmMethod, "SignatureMethod is AlgorithmMethod");
        check(sm.getAlgorithm().equals(SignatureMethod.HMAC_SHA1), "signature algorithm URI round-trip");
        check(sm.getParameterSpec() instanceof HMACParameterSpec, "signature parameter spec round-trip");
        check(((HMACParameterSpec) sm.getParameterSpec()).getOutputLength() == 160, "spec content preserved");
        Transform tr = sf.newTransform(Transform.BASE64, (javax.xml.crypto.dsig.spec.TransformParameterSpec) null);
        check(tr instanceof AlgorithmMethod, "Transform is AlgorithmMethod");
        check(tr.getAlgorithm().equals(Transform.BASE64), "transform algorithm URI round-trip");
        // algorithm equality is by URI string
        DigestMethod dm2 = sf.newDigestMethod(DigestMethod.SHA256, null);
        check(dm.getAlgorithm().equals(dm2.getAlgorithm()), "same URI across instances");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.xml.crypto.URIReference conformance.
 * @run main URIReferenceTest
 */

import javax.xml.crypto.URIReference;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.crypto.dsig.XMLSignatureFactory;

public class URIReferenceTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.URIReference");
        check(c.isInterface(), "is interface");
        check(c.getInterfaces().length == 0, "extends nothing");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getURI"), "has getURI");
        check(names.contains("getType"), "has getType");
        // Reference and RetrievalMethod are the shipped implementations
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Reference r = sf.newReference("#doc",
            sf.newDigestMethod(DigestMethod.SHA256, null), null,
            "http://www.w3.org/2000/09/xmldsig#Object", "ref-id");
        check(r instanceof URIReference, "Reference implements URIReference");
        check("#doc".equals(r.getURI()), "Reference URI round-trip");
        check("http://www.w3.org/2000/09/xmldsig#Object".equals(r.getType()), "Reference type round-trip");
        // bare reference: type null
        Reference r2 = sf.newReference("", sf.newDigestMethod(DigestMethod.SHA256, null));
        check(r2 instanceof URIReference && "".equals(r2.getURI()) && r2.getType() == null,
            "bare reference: empty URI, null type");
        RetrievalMethod rm = sf.getKeyInfoFactory().newRetrievalMethod("#keys",
            "http://www.w3.org/2000/09/xmldsig#KeyValue", null);
        check(rm instanceof URIReference, "RetrievalMethod implements URIReference");
        check("#keys".equals(rm.getURI()), "RetrievalMethod URI round-trip");
        check("http://www.w3.org/2000/09/xmldsig#KeyValue".equals(rm.getType()), "RetrievalMethod type round-trip");
        // custom implementation for the raw contract
        URIReference custom = new URIReference() {
            public String getURI() { return "http://remote/doc.xml"; }
            public String getType() { return null; }
        };
        check("http://remote/doc.xml".equals(custom.getURI()), "custom URI");
        check(custom.getType() == null, "custom null type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

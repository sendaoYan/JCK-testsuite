/*
 * @test
 * @summary javax.xml.crypto.NodeSetData conformance.
 * @run main NodeSetDataTest
 */

import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodeSetDataTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.NodeSetData");
        check(c.isInterface(), "is interface");
        check(javax.xml.crypto.Data.class.isAssignableFrom(c), "extends Data");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("iterator"), "adds iterator");
        // custom implementation carrying an ordered node list
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final List<Node> nodes = new ArrayList<Node>();
        nodes.add(d.createElement("a"));
        nodes.add(d.createElement("b"));
        NodeSetData data = new NodeSetData() {
            public Iterator iterator() { return nodes.iterator(); }
        };
        check(data instanceof Data, "custom implementation is Data");
        int n = 0;
        boolean sawA = false, sawB = false;
        for (Iterator it = data.iterator(); it.hasNext(); ) {
            Node x = (Node) it.next();
            n++;
            if ("a".equals(x.getNodeName())) sawA = true;
            if ("b".equals(x.getNodeName())) sawB = true;
        }
        check(n == 2 && sawA && sawB, "iterator preserves order and content");
        // empty node set iterates zero times
        final List<Node> empty = new ArrayList<Node>();
        NodeSetData emptyData = new NodeSetData() {
            public Iterator iterator() { return empty.iterator(); }
        };
        check(!emptyData.iterator().hasNext(), "empty node set");
        // cached reference content from a real signature arrives as NodeSetData
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM");
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document sd = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
            "<r><t Id=\"t1\">NODESET</t></r>".getBytes("UTF-8")));
        Element t1 = (Element) sd.getElementsByTagName("t").item(0);
        t1.setIdAttribute("Id", true);
        javax.xml.crypto.dsig.Reference ref = sf.newReference("#t1",
            sf.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA256, null));
        javax.xml.crypto.dsig.SignedInfo si = sf.newSignedInfo(
            sf.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null),
            sf.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
            java.util.Collections.singletonList(ref));
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        java.security.KeyPair kp = kpg.generateKeyPair();
        sf.newXMLSignature(si, null).sign(new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), sd.getDocumentElement()));
        Element sigEl = (Element) sd.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature").item(0);
        javax.xml.crypto.dsig.dom.DOMValidateContext vc =
            new javax.xml.crypto.dsig.dom.DOMValidateContext(
                javax.xml.crypto.KeySelector.singletonKeySelector(kp.getPublic()), sigEl);
        vc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
        javax.xml.crypto.dsig.XMLSignature vsig = sf.unmarshalXMLSignature(vc);
        vsig.validate(vc);
        javax.xml.crypto.dsig.Reference vr = (javax.xml.crypto.dsig.Reference)
            vsig.getSignedInfo().getReferences().get(0);
        check(vr.getDereferencedData() instanceof NodeSetData, "dereferenced same-document data is NodeSetData");
        int cnt = 0;
        for (Iterator it = ((NodeSetData) vr.getDereferencedData()).iterator(); it.hasNext(); it.next()) cnt++;
        check(cnt >= 1, "cached node set non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

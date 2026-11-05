/*
 * @test
 * @summary javax.xml.crypto.dsig.SignatureProperty conformance.
 * @run main SignaturePropertyTest
 */

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SignaturePropertyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Document newDoc(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dsig.SignatureProperty");
        check(c.isInterface(), "is interface");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        boolean xmls = false;
        for (String i : ifs) if (i.equals("javax.xml.crypto.XMLStructure")) xmls = true;
        check(xmls, "implements XMLStructure");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getTarget"), "has getTarget");
        check(names.contains("getId"), "has getId");
        check(names.contains("getContent"), "has getContent");
    }

    static void testGetterMatrix() throws Exception {
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        Document d = newDoc("<r/>");
        DOMStructure c1 = new DOMStructure(d.createTextNode("one"));
        DOMStructure c2 = new DOMStructure(d.createElementNS("urn:p", "el"));
        SignatureProperty p = sf.newSignatureProperty(Arrays.asList(c1, c2), "#sig-target", "pid");
        check("#sig-target".equals(p.getTarget()), "target round-trip");
        check("pid".equals(p.getId()), "id round-trip");
        check(p.getContent().size() == 2, "two content items");
        check(p.getContent().get(0) == c1, "content identity/order");
        // null id allowed
        SignatureProperty p2 = sf.newSignatureProperty(Collections.singletonList(c1), "#t", null);
        check(p2.getId() == null, "null id round-trip");
        // content list immutable
        try { p.getContent().add(c1); check(false, "content immutable"); }
        catch (UnsupportedOperationException e) { check(true, "content immutable UOE"); }
        // negatives: null target / null content / empty content
        try { sf.newSignatureProperty(Collections.singletonList(c1), null, "id"); check(false, "null target"); }
        catch (NullPointerException e) { check(true, "null target NPE"); }
        try { sf.newSignatureProperty(null, "#t", "id"); check(false, "null content"); }
        catch (NullPointerException e) { check(true, "null content NPE"); }
        try { sf.newSignatureProperty(new ArrayList<XMLStructure>(), "#t", "id"); check(false, "empty content"); }
        catch (IllegalArgumentException e) { check(true, "empty content IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetterMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

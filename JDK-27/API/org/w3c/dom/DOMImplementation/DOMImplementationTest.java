/*
 * @test
 * @summary org.w3c.dom.DOMImplementation conformance: interface and methods.
 * @run main DOMImplementationTest
 */

import org.w3c.dom.DOMImplementation;

public class DOMImplementationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMImplementation.class.isInterface(), "DOMImplementation is an interface");
        check(DOMImplementation.class.getMethod("hasFeature", String.class, String.class)
                .getReturnType() == boolean.class, "hasFeature(String,String) returns boolean");
        check(DOMImplementation.class.getMethod("createDocumentType", String.class,
                String.class, String.class) != null, "createDocumentType(String,String,String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMImplementation a = new org.w3c.dom.DOMImplementation(){
            public boolean hasFeature(java.lang.String p0, java.lang.String p1) { return true; }
            public org.w3c.dom.DocumentType createDocumentType(java.lang.String p0, java.lang.String p1, java.lang.String p2) { return null; }
            public org.w3c.dom.Document createDocument(java.lang.String p0, java.lang.String p1, org.w3c.dom.DocumentType p2) { return null; }
            public java.lang.Object getFeature(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of DOMImplementation");
        check(a.hasFeature("s", "s") == true, "hasFeature() returns fixed default");
        check(a.createDocumentType("s", "s", "s") == null, "createDocumentType() returns fixed default");
        check(a.createDocument("s", "s", (org.w3c.dom.DocumentType) null) == null, "createDocument() returns fixed default");
        check(a.getFeature("s", "s") == null, "getFeature() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMImplementation contract (hasFeature/getFeature/create*) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DOMImplementation impl = d.getImplementation();
        // hasFeature matrix
        check(impl.hasFeature("Core", "1.0") && impl.hasFeature("Core", "2.0") && impl.hasFeature("Core", "3.0"), "hasFeature Core 1.0/2.0/3.0");
        check(impl.hasFeature("XML", "1.0") && impl.hasFeature("XML", "2.0") && impl.hasFeature("XML", "3.0"), "hasFeature XML all levels");
        check(impl.hasFeature("Traversal", "2.0") && impl.hasFeature("Range", "2.0") && impl.hasFeature("Events", "2.0"), "hasFeature Traversal/Range/Events 2.0");
        check(impl.hasFeature("MutationEvents", "2.0") && impl.hasFeature("LS", "3.0"), "hasFeature MutationEvents/LS");
        check(impl.hasFeature("Core", null) && impl.hasFeature("LS", null), "hasFeature null version means highest supported");
        check(impl.hasFeature("core", "2.0") && impl.hasFeature("xMl", "3.0"), "hasFeature is case-insensitive");
        check(!impl.hasFeature("Core", "4.0") && !impl.hasFeature("Core", "9.9"), "hasFeature unknown version false");
        check(!impl.hasFeature("bogus", "1.0") && !impl.hasFeature("XPath", "3.0"), "hasFeature unknown feature false");
        // per-side null-feature behavior difference
        boolean nullFeatureThrows = false;
        try { impl.hasFeature(null, null); } catch (NullPointerException expected) { nullFeatureThrows = true; }
        if (System.getProperty("java.version").startsWith("1.8")) {
            check(nullFeatureThrows, "JDK 8: hasFeature(null,null) -> NullPointerException");
        } else {
            check(!nullFeatureThrows && !impl.hasFeature(null, null), "JDK 27: hasFeature(null,null) -> false");
        }
        // getFeature: only LS is exposed beside Core
        check(impl.getFeature("LS", "3.0") != null, "getFeature(LS,3.0) returns DOMImplementationLS");
        check(impl.getFeature("Core", "3.0") != null, "getFeature(Core,3.0) returns implementation");
        check(impl.getFeature("Traversal", "2.0") == null && impl.getFeature("Range", "2.0") == null, "getFeature Traversal/Range null (use casts)");
        check(impl.getFeature("Events", "2.0") == null, "getFeature Events null (use casts)");
        check(impl.getFeature("CSS", "2.0") == null && impl.getFeature("HTML", "2.0") == null, "getFeature CSS/HTML null (not implemented)");
        // createDocument
        org.w3c.dom.DocumentType cdt = impl.createDocumentType("q", "p", "s");
        org.w3c.dom.Document cd = impl.createDocument("urn:n", "n:doc", cdt);
        check(cd.getChildNodes().getLength() == 2, "createDocument installs doctype + root");
        check(cd.getDoctype() == cdt && "q".equals(cd.getDoctype().getName()), "createDocument doctype child");
        check("doc".equals(cd.getDocumentElement().getLocalName()) && "urn:n".equals(cd.getDocumentElement().getNamespaceURI()), "createDocument namespaced root");
        check("1.0".equals(cd.getXmlVersion()), "createDocument default XML version 1.0");
        org.w3c.dom.Document plain = impl.createDocument(null, "plain", null);
        check(plain.getChildNodes().getLength() == 1 && "plain".equals(plain.getDocumentElement().getNodeName()), "createDocument(null ns, no doctype)");
        check(impl.createDocument("", "e", null) != null, "createDocument empty uri allowed");
        try { impl.createDocument("urn:x", "bad name", null); check(false, "createDocument(illegal qname) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createDocument('bad name') -> INVALID_CHARACTER_ERR"); }
        try { impl.createDocument(null, "p:x", null); check(false, "createDocument(prefixed, null ns) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NAMESPACE_ERR, "createDocument(null, 'p:x') -> NAMESPACE_ERR"); }
        // createDocumentType
        check("q".equals(cdt.getName()) && "p".equals(cdt.getPublicId()) && "s".equals(cdt.getSystemId()), "createDocumentType id round-trip");
        // implementation objects are shared per document factory
        check(d.getImplementation() == impl, "getImplementation returns the cached instance");
        org.w3c.dom.Document d2 = f.newDocumentBuilder().newDocument();
        check(d2.getImplementation() != null && d2.getImplementation().hasFeature("Core", "3.0"), "second document also hands out a working implementation");
    }
}

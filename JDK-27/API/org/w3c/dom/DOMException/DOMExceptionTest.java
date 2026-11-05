/*
 * @test
 * @summary org.w3c.dom.DOMException conformance: exception hierarchy, constructor, code field, full error-code constant matrix.
 * @run main DOMExceptionTest
 */

import org.w3c.dom.DOMException;

public class DOMExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        // ---- exception hierarchy ----
        check(RuntimeException.class.isAssignableFrom(DOMException.class), "extends RuntimeException");
        check(Exception.class.isAssignableFrom(DOMException.class), "is an Exception");

        // ---- full error-code constant matrix (DOM Level 3) ----
        check(DOMException.INDEX_SIZE_ERR == 1, "INDEX_SIZE_ERR == 1");
        check(DOMException.DOMSTRING_SIZE_ERR == 2, "DOMSTRING_SIZE_ERR == 2");
        check(DOMException.HIERARCHY_REQUEST_ERR == 3, "HIERARCHY_REQUEST_ERR == 3");
        check(DOMException.WRONG_DOCUMENT_ERR == 4, "WRONG_DOCUMENT_ERR == 4");
        check(DOMException.INVALID_CHARACTER_ERR == 5, "INVALID_CHARACTER_ERR == 5");
        check(DOMException.NO_DATA_ALLOWED_ERR == 6, "NO_DATA_ALLOWED_ERR == 6");
        check(DOMException.NO_MODIFICATION_ALLOWED_ERR == 7, "NO_MODIFICATION_ALLOWED_ERR == 7");
        check(DOMException.NOT_FOUND_ERR == 8, "NOT_FOUND_ERR == 8");
        check(DOMException.NOT_SUPPORTED_ERR == 9, "NOT_SUPPORTED_ERR == 9");
        check(DOMException.INUSE_ATTRIBUTE_ERR == 10, "INUSE_ATTRIBUTE_ERR == 10");
        check(DOMException.INVALID_STATE_ERR == 11, "INVALID_STATE_ERR == 11");
        check(DOMException.SYNTAX_ERR == 12, "SYNTAX_ERR == 12");
        check(DOMException.INVALID_MODIFICATION_ERR == 13, "INVALID_MODIFICATION_ERR == 13");
        check(DOMException.NAMESPACE_ERR == 14, "NAMESPACE_ERR == 14");
        check(DOMException.INVALID_ACCESS_ERR == 15, "INVALID_ACCESS_ERR == 15");
        check(DOMException.VALIDATION_ERR == 16, "VALIDATION_ERR == 16");
        check(DOMException.TYPE_MISMATCH_ERR == 17, "TYPE_MISMATCH_ERR == 17");

        // ---- constructor + public code field ----
        DOMException e = new DOMException(DOMException.NAMESPACE_ERR, "ns message");
        check(e.code == DOMException.NAMESPACE_ERR, "code field populated from constructor");
        check("ns message".equals(e.getMessage()), "message preserved");
        check(e.getCause() == null, "no cause by default");

        DOMException e2 = new DOMException(DOMException.NOT_FOUND_ERR, null);
        check(e2.code == DOMException.NOT_FOUND_ERR, "code with null message");
        check(e2.getMessage() == null, "null message preserved");

        // ---- code is a mutable public field ----
        e2.code = DOMException.WRONG_DOCUMENT_ERR;
        check(e2.code == DOMException.WRONG_DOCUMENT_ERR, "code field is public and mutable");

        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMException contract (real triggers per code) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        // constructor and cause behavior
        org.w3c.dom.DOMException de = new org.w3c.dom.DOMException(org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "boom");
        check(de.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR && "boom".equals(de.getMessage()), "constructor stores code and message");
        de.initCause(new RuntimeException("r"));
        check("r".equals(de.getCause().getMessage()), "initCause works on DOMException (unlike during construction)");
        check(new org.w3c.dom.DOMException((short) 3, null).getMessage() == null, "null message preserved");
        // real trigger table: one operation per distinct code
        org.w3c.dom.Text t = d.createTextNode("hello");
        try { t.substringData(9, 1); check(false, "trigger INDEX_SIZE_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(9,1) triggers INDEX_SIZE_ERR(1)"); }
        try { d.appendChild(d.createElement("r1")); d.appendChild(d.createElement("r2")); check(false, "trigger HIERARCHY_REQUEST_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR, "second document element triggers HIERARCHY_REQUEST_ERR(3)"); }
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element mounted = d.createElement("e");
        try { mounted.appendChild(other.createElement("x")); check(false, "trigger WRONG_DOCUMENT_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR, "cross-document append triggers WRONG_DOCUMENT_ERR(4)"); }
        try { d.createElement("a b"); check(false, "trigger INVALID_CHARACTER_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createElement('a b') triggers INVALID_CHARACTER_ERR(5)"); }
        String dtdXml = "<!DOCTYPE r [<!ENTITY e1 'v'>]><r/>";
        d.getDocumentElement().appendChild(d.createComment("reset"));
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(dtdXml.getBytes("UTF-8")));
        org.w3c.dom.Entity ent = (org.w3c.dom.Entity) pd.getDoctype().getEntities().getNamedItem("e1");
        try { ent.appendChild(pd.createElement("x")); check(false, "trigger NO_MODIFICATION_ALLOWED_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entity appendChild triggers NO_MODIFICATION_ALLOWED_ERR(7)"); }
        org.w3c.dom.Element attrHost = d.createElement("ah");
        d.getDocumentElement().appendChild(attrHost);
        attrHost.setAttribute("k", "v");
        try { attrHost.getAttributes().removeNamedItem("missing"); check(false, "trigger NOT_FOUND_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "removeNamedItem(missing) triggers NOT_FOUND_ERR(8)"); }
        try { d.setXmlVersion("9.9"); check(false, "trigger NOT_SUPPORTED_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "setXmlVersion(9.9) triggers NOT_SUPPORTED_ERR(9)"); }
        org.w3c.dom.Document wd = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element wr = wd.createElement("r");
        wd.appendChild(wr);
        org.w3c.dom.traversal.NodeIterator it = ((org.w3c.dom.traversal.DocumentTraversal) wd).createNodeIterator(wr, -1, null, false);
        it.detach();
        try { it.nextNode(); check(false, "trigger INVALID_STATE_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_STATE_ERR, "detached iterator nextNode triggers INVALID_STATE_ERR(11)"); }
        try { d.createElementNS(null, "p:x"); check(false, "trigger NAMESPACE_ERR"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NAMESPACE_ERR, "createElementNS(null,'p:x') triggers NAMESPACE_ERR(14)"); }
        org.w3c.dom.DOMException ranged = new org.w3c.dom.DOMException(org.w3c.dom.DOMException.TYPE_MISMATCH_ERR, "tm");
        check(ranged.code == org.w3c.dom.DOMException.TYPE_MISMATCH_ERR && org.w3c.dom.DOMException.TYPE_MISMATCH_ERR == 17, "TYPE_MISMATCH_ERR constant 17 (LS-layer code)");
        // message text carries the operation context
        try { d.createElement("a<b"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.getMessage() != null && ex.getMessage().length() > 0, "exception message non-empty"); }
    }
}

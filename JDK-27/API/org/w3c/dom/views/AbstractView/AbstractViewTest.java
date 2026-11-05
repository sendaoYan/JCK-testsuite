/*
 * @test
 * @summary org.w3c.dom.views.AbstractView conformance: interface and getDocument.
 * @run main AbstractViewTest
 */

import org.w3c.dom.views.AbstractView;

public class AbstractViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(AbstractView.class.isInterface(), "AbstractView is an interface");
        check(AbstractView.class.getMethod("getDocument") != null, "getDocument()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.views.AbstractView a = new org.w3c.dom.views.AbstractView(){
            public org.w3c.dom.views.DocumentView getDocument() { return null; }
        };
        check(a != null, "anon impl of AbstractView");
        check(a.getDocument() == null, "getDocument() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: AbstractView contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("Views", "2.0") && impl.getFeature("Views", "2.0") == null, "Views module unimplemented");
        final org.w3c.dom.views.DocumentView[] holder = new org.w3c.dom.views.DocumentView[1];
        org.w3c.dom.views.AbstractView v = new org.w3c.dom.views.AbstractView() {
            public org.w3c.dom.views.DocumentView getDocument() { return holder[0]; }
        };
        holder[0] = new org.w3c.dom.views.DocumentView() {
            public org.w3c.dom.views.AbstractView getDefaultView() { return v; }
        };
        check(v.getDocument() == holder[0], "getDocument returns the wired DocumentView");
        check(v.getDocument().getDefaultView() == v, "view/document pair is mutually linked");
    }
}

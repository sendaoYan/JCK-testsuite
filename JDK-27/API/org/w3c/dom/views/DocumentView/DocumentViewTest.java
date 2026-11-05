/*
 * @test
 * @summary org.w3c.dom.views.DocumentView conformance: interface and getDefaultView.
 * @run main DocumentViewTest
 */

import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

public class DocumentViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentView.class.isInterface(), "DocumentView is an interface");
        check(DocumentView.class.getMethod("getDefaultView")
                .getReturnType() == AbstractView.class,
              "getDefaultView() returns AbstractView");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.views.DocumentView a = new org.w3c.dom.views.DocumentView(){
            public org.w3c.dom.views.AbstractView getDefaultView() { return null; }
        };
        check(a != null, "anon impl of DocumentView");
        check(a.getDefaultView() == null, "getDefaultView() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: DocumentView contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("Views", "2.0") && impl.getFeature("Views", "2.0") == null, "Views module unimplemented");
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(!org.w3c.dom.views.DocumentView.class.isInstance(d), "Document is not a DocumentView (module absent)");
        final org.w3c.dom.views.DocumentView[] holder = new org.w3c.dom.views.DocumentView[1];
        org.w3c.dom.views.AbstractView view = new org.w3c.dom.views.AbstractView() {
            public org.w3c.dom.views.DocumentView getDocument() { return holder[0]; }
        };
        holder[0] = new org.w3c.dom.views.DocumentView() {
            public org.w3c.dom.views.AbstractView getDefaultView() { return view; }
        };
        check(holder[0].getDefaultView() == view && view.getDocument() == holder[0], "defaultView/getDocument mutual routing");
    }
}

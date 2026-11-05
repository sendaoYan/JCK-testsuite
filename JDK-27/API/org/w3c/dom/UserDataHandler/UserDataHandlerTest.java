/*
 * @test
 * @summary org.w3c.dom.UserDataHandler conformance: interface and handle method.
 * @run main UserDataHandlerTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

public class UserDataHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(UserDataHandler.class.isInterface(), "UserDataHandler is an interface");
        check(UserDataHandler.class.getMethod("handle", short.class, String.class,
                Object.class, Node.class, Node.class) != null,
              "handle(short,String,Object,Node,Node)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.UserDataHandler.NODE_CLONED == 1, "NODE_CLONED == 1");
        check(org.w3c.dom.UserDataHandler.NODE_IMPORTED == 2, "NODE_IMPORTED == 2");
        check(org.w3c.dom.UserDataHandler.NODE_DELETED == 3, "NODE_DELETED == 3");
        check(org.w3c.dom.UserDataHandler.NODE_RENAMED == 4, "NODE_RENAMED == 4");
        check(org.w3c.dom.UserDataHandler.NODE_ADOPTED == 5, "NODE_ADOPTED == 5");
        org.w3c.dom.UserDataHandler a = new org.w3c.dom.UserDataHandler(){
            public void handle(short p0, java.lang.String p1, java.lang.Object p2, org.w3c.dom.Node p3, org.w3c.dom.Node p4) { }
        };
        check(a != null, "anon impl of UserDataHandler");
        a.handle((short)2, "s", (java.lang.Object) null, (org.w3c.dom.Node) null, (org.w3c.dom.Node) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral UserDataHandler contract (operation routing + live params) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        final java.util.List<Short> ops = new java.util.ArrayList<Short>();
        final java.util.List<String> keys = new java.util.ArrayList<String>();
        final java.util.List<org.w3c.dom.Node> srcs = new java.util.ArrayList<org.w3c.dom.Node>();
        final java.util.List<org.w3c.dom.Node> dsts = new java.util.ArrayList<org.w3c.dom.Node>();
        org.w3c.dom.UserDataHandler h = new org.w3c.dom.UserDataHandler() {
            public void handle(short operation, String key, Object data, org.w3c.dom.Node src, org.w3c.dom.Node dst) {
                ops.add(operation); keys.add(key + "=" + data);
                srcs.add(src); dsts.add(dst);
            }
        };
        check(org.w3c.dom.UserDataHandler.NODE_CLONED == 1, "NODE_CLONED = 1");
        check(org.w3c.dom.UserDataHandler.NODE_IMPORTED == 2, "NODE_IMPORTED = 2");
        check(org.w3c.dom.UserDataHandler.NODE_DELETED == 3, "NODE_DELETED = 3");
        check(org.w3c.dom.UserDataHandler.NODE_RENAMED == 4, "NODE_RENAMED = 4");
        check(org.w3c.dom.UserDataHandler.NODE_ADOPTED == 5, "NODE_ADOPTED = 5");
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e = d.createElement("n");
        d.appendChild(e);
        e.setUserData("k", "v", h);
        // clone -> NODE_CLONED with src=original dst=clone
        org.w3c.dom.Node cl = e.cloneNode(true);
        check(ops.get(0) == org.w3c.dom.UserDataHandler.NODE_CLONED, "cloneNode triggers NODE_CLONED");
        check("k=v".equals(keys.get(0)), "handler receives key and data");
        check(srcs.get(0) == e && dsts.get(0) == cl, "clone: src is the original, dst is the clone");
        // import -> NODE_IMPORTED with dst=imported copy
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Node imp = other.importNode(e, true);
        check(ops.get(1) == org.w3c.dom.UserDataHandler.NODE_IMPORTED, "importNode triggers NODE_IMPORTED");
        check(srcs.get(1) == e && dsts.get(1) == imp, "import: dst is the imported copy");
        // rename -> NODE_RENAMED with dst null (Xerces)
        org.w3c.dom.Node renamed = d.renameNode(e, null, "n2");
        check(ops.get(2) == org.w3c.dom.UserDataHandler.NODE_RENAMED, "renameNode triggers NODE_RENAMED");
        check(srcs.get(2) == renamed, "rename: src is the renamed node");
        check(dsts.get(2) == null, "rename: dst null (Xerces)");
        // adopt -> NODE_ADOPTED with dst null (Xerces)
        org.w3c.dom.Document third = f.newDocumentBuilder().newDocument();
        third.adoptNode(renamed);
        check(ops.get(3) == org.w3c.dom.UserDataHandler.NODE_ADOPTED, "adoptNode triggers NODE_ADOPTED");
        check(dsts.get(3) == null, "adopt: dst null (Xerces)");
        // NODE_DELETED never fires for live trees; dropping the reference is not observable
        // userData survives adoption and stays readable
        check("v".equals(renamed.getUserData("k")), "userData value readable after adoption");
        check(renamed.getUserData("missing") == null, "getUserData(unknown key) null");
        // userData is never auto-copied to clones: the handler is the application's only hook
        int before = ops.size();
        org.w3c.dom.Element quiet = d.createElement("quiet");
        org.w3c.dom.Element newRoot = d.createElement("root2");
        d.appendChild(newRoot);
        newRoot.appendChild(quiet);
        quiet.setUserData("k2", "v2", null);
        org.w3c.dom.Node cl2 = quiet.cloneNode(true);
        check(ops.size() == before, "null handler produces no callbacks");
        check(cl2.getUserData("k2") == null, "clone does not inherit userData (handler must copy it)");
        check("v2".equals(quiet.getUserData("k2")), "original keeps its userData after cloning");
        // multiple keys are handled independently
        quiet.setUserData("a", "1", h);
        quiet.setUserData("b", "2", h);
        d.renameNode(quiet, null, "quiet2");
        check(keys.contains("a=1") && keys.contains("b=2"), "each key routed separately");
    }
}

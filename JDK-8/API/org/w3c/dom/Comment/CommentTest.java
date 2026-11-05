/*
 * @test
 * @summary org.w3c.dom.Comment conformance: reflection shape checks.
 * @run main CommentTest
 */

import java.lang.reflect.*;

public class CommentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.Comment");
        check(c != null, "org.w3c.dom.Comment loads");
        check(c.getName().equals("org.w3c.dom.Comment"), "binary name is org.w3c.dom.Comment");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.Comment is public");
        check(c.isInterface(), "org.w3c.dom.Comment is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.CharacterData")), "direct superinterfaces");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Comment contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Comment c = d.createComment(" hi ");
        check("#comment".equals(c.getNodeName()), "comment nodeName #comment");
        check(org.w3c.dom.Node.COMMENT_NODE == c.getNodeType(), "comment nodeType 8");
        check(" hi ".equals(c.getData()), "comment preserves content verbatim incl. surrounding spaces");
        check(c.getLength() == 4, "comment getLength");
        check(c instanceof org.w3c.dom.CharacterData, "Comment is CharacterData");
        // Comment has no splitText (Text-only API)
        boolean absent = false;
        try { org.w3c.dom.Comment.class.getMethod("splitText", int.class); }
        catch (NoSuchMethodException expected) { absent = true; }
        check(absent, "Comment interface has no splitText");
        // CharacterData channels
        c.setData("n1");
        check("n1".equals(c.getNodeValue()), "comment setData syncs getNodeValue");
        c.setNodeValue("n2");
        check("n2".equals(c.getData()), "comment setNodeValue syncs getData");
        c.setTextContent("n3");
        check("n3".equals(c.getData()) && "n3".equals(c.getTextContent()), "comment setTextContent channel");
        c.appendData("!");
        check("n3!".equals(c.getData()), "comment appendData");
        c.insertData(0, ">");
        check(">n3!".equals(c.getData()), "comment insertData");
        c.deleteData(0, 1);
        check("n3!".equals(c.getData()), "comment deleteData");
        c.replaceData(0, 3, "z");
        check("z".equals(c.getData()), "comment replaceData");
        try { c.substringData(99, 1); check(false, "comment substringData OOB must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "comment substringData(99,1) -> INDEX_SIZE_ERR"); }
        // null handling
        org.w3c.dom.Comment n = d.createComment(null);
        check(n.getData() == null, "createComment(null) -> null data");
        boolean npe = false;
        try { n.getLength(); } catch (NullPointerException expected) { npe = true; }
        check(npe, "comment getLength after null -> NullPointerException");
        n.setData("z");
        check(n.getLength() == 1, "comment setData recovers");
        // structure: comment is a leaf
        check(c.getChildNodes().getLength() == 0 && c.getFirstChild() == null, "comment has no children");
        check(c.getAttributes() == null, "comment getAttributes null");
        check(c.getParentNode() == null, "factory comment has no parent");
        // placement: comments allowed anywhere, incl. document level before root
        org.w3c.dom.Element root = d.createElement("r");
        d.appendChild(root);
        root.appendChild(c);
        check(c.getParentNode() == root, "comment attaches inside element");
        org.w3c.dom.Comment docLevel = d.createComment("doc");
        d.insertBefore(docLevel, d.getDocumentElement());
        check(docLevel.getParentNode() == d && d.getChildNodes().getLength() == 2, "comment allowed at document level before root");
        d.removeChild(docLevel);
        check(d.getChildNodes().getLength() == 1, "comment removable from document");
        // equality and clone
        check(d.createComment("same").isEqualNode(d.createComment("same")), "comment isEqualNode same data");
        check(!d.createComment("a").isEqualNode(d.createComment("b")), "comment differs on data");
        check("same".equals(((org.w3c.dom.Comment) d.createComment("same").cloneNode(true)).getData()), "comment clone keeps data");
        // parse: comments survive parsing
        String xml = "<r><a/><!--cmt--></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Comment pc = (org.w3c.dom.Comment) pd.getDocumentElement().getChildNodes().item(1);
        check("cmt".equals(pc.getData()) && org.w3c.dom.Node.COMMENT_NODE == pc.getNodeType(), "parsed comment preserved");
        // textContent aggregation skips comments
        check("".equals(pd.getDocumentElement().getTextContent()), "comment invisible to textContent aggregation");
    }
}

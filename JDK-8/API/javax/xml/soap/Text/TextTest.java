/*
 * @test
 * @summary javax.xml.soap.Text conformance: addTextNode 返回元素自身陷阱/相邻文本节点分离与 getValue 归并/DOM 通道 Comment 即 SAAJ Text 且 isComment 分叉/无 addComment(SAAJ 1.3)/CharacterData 往返.
 * @run main TextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class TextTest {
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
        Class<?> c = Class.forName("javax.xml.soap.Text");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.Node"), "implements javax.xml.soap.Node");
        check(has(ifs, "org.w3c.dom.Text"), "implements org.w3c.dom.Text");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isComment"), "has method isComment");
        check(c.getDeclaredMethods().length == 1, "exactly 1 own method (isComment)");
        testAddTextNodeReturnsElement();
        testAdjacentTextNodes();
        testIsCommentFalse();
        testCommentViaDom();
        testNoAddCommentMethod();
        testCharacterDataRoundTrip();
        testEmptyText();
        testTextNodeInChildIteration();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPBodyElement host() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPBody()
                .addBodyElement(new javax.xml.namespace.QName("urn:t", "host", "t"));
    }

    static int count(java.util.Iterator<?> it) {
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        return n;
    }

    // API 陷阱：SOAPElement.addTextNode 返回元素自身（this），不是 Text 节点
    static void testAddTextNodeReturnsElement() throws Exception {
        javax.xml.soap.SOAPBodyElement el = host();
        javax.xml.soap.SOAPElement ret = el.addTextNode("a");
        check(ret == el, "addTextNode returns the element itself (not a Text node)");
        check(ret instanceof javax.xml.soap.SOAPElement, "addTextNode return type is SOAPElement");
    }

    // 相邻文本：两次 addTextNode 生成两个分离 Text 节点；元素 getValue 连接；随后 RI 归并（normalize-on-read）
    static void testAdjacentTextNodes() throws Exception {
        javax.xml.soap.SOAPBodyElement el = host();
        el.addTextNode("a");
        el.addTextNode("b");
        java.util.Iterator<?> it = el.getChildElements();
        int textNodes = 0, all = 0;
        StringBuilder concat = new StringBuilder();
        while (it.hasNext()) {
            Object o = it.next();
            all++;
            if (o instanceof javax.xml.soap.Text) {
                textNodes++;
                concat.append(((javax.xml.soap.Text) o).getValue());
            }
        }
        check(all == 2, "two child nodes after two addTextNode");
        check(textNodes == 2, "both children are SAAJ Text nodes (not merged yet)");
        check("ab".equals(concat.toString()), "per-node values a and b");
        check("ab".equals(el.getTextContent()), "element getTextContent concatenates");
        check("ab".equals(el.getValue()), "element getValue concatenates");
        check(count(el.getChildElements()) == 1,
                "RI getValue() normalizes adjacent text nodes into one (locked)");
        javax.xml.soap.Text merged = (javax.xml.soap.Text) el.getChildElements().next();
        check("ab".equals(merged.getValue()), "merged text node carries concatenated data");
    }

    // 文本节点 isComment()==false（多节点逐一验证）
    static void testIsCommentFalse() throws Exception {
        javax.xml.soap.SOAPBodyElement el = host();
        el.addTextNode("t1");
        el.addTextNode("t2");
        java.util.Iterator<?> it = el.getChildElements();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof javax.xml.soap.Text) {
                check(!((javax.xml.soap.Text) o).isComment(), "text node isComment false");
            }
        }
    }

    // DOM 通道建注释：CommentImpl 实现 SAAJ Text 且 isComment()==true；注释不计入元素 getValue
    static void testCommentViaDom() throws Exception {
        javax.xml.soap.SOAPBodyElement el = host();
        el.addTextNode("real");
        org.w3c.dom.Comment cmt = el.getOwnerDocument().createComment("note");
        el.appendChild(cmt);
        java.util.Iterator<?> it = el.getChildElements();
        boolean sawComment = false, sawText = false;
        String commentValue = null;
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof javax.xml.soap.Text) {
                javax.xml.soap.Text t = (javax.xml.soap.Text) o;
                if (t.isComment()) { sawComment = true; commentValue = t.getValue(); }
                else sawText = true;
            }
        }
        check(sawText, "text child still present after DOM comment append");
        check(sawComment, "DOM comment visible as SAAJ Text in getChildElements");
        check("note".equals(commentValue), "comment node getValue is comment data");
        check("real".equals(el.getValue()), "comment excluded from element getValue");
        check("real".equals(el.getTextContent()), "comment excluded from DOM textContent");
    }

    // JDK-8 SAAJ 1.3 实测：SOAPElement 无 addComment（SAAJ 1.4 才引入）
    static void testNoAddCommentMethod() throws Exception {
        try {
            javax.xml.soap.SOAPElement.class.getMethod("addComment", String.class);
            check(false, "addComment should be absent in SAAJ 1.3");
        } catch (NoSuchMethodException e) {
            check(true, "SOAPElement has no addComment(String) in JDK-8 (SAAJ 1.3)");
        }
    }

    // org.w3c.dom.Text 面：getData/setData/getLength/appendData + SAAJ setValue 等价 setData
    static void testCharacterDataRoundTrip() throws Exception {
        javax.xml.soap.SOAPBodyElement el = host();
        el.addTextNode("hello");
        javax.xml.soap.Text t = null;
        java.util.Iterator<?> it = el.getChildElements();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof javax.xml.soap.Text) { t = (javax.xml.soap.Text) o; break; }
        }
        check(t != null, "text node found");
        check("hello".equals(t.getData()), "DOM getData");
        check(t.getLength() == 5, "DOM getLength");
        t.setData("world");
        check("world".equals(t.getData()), "setData round-trip");
        check("world".equals(t.getValue()), "SAAJ getValue follows setData");
        t.setValue("setval");
        check("setval".equals(t.getData()), "SAAJ setValue equivalent to setData (RI)");
        check("setval".equals(t.getValue()), "setValue/getValue round-trip on Text");
        t.appendData("!");
        check("setval!".equals(t.getData()), "appendData extends data");
        check(t instanceof org.w3c.dom.CharacterData, "Text is DOM CharacterData");
        check(t instanceof org.w3c.dom.Node, "Text is DOM Node");
        check(t.getNodeType() == org.w3c.dom.Node.TEXT_NODE, "text nodeType TEXT_NODE");
        check(t.getParentElement() == el, "text node parent is host element");
    }

    // 空串文本：单一子节点、getValue null、textContent 空
    static void testEmptyText() throws Exception {
        javax.xml.soap.SOAPBodyElement el = host();
        javax.xml.soap.SOAPElement ret = el.addTextNode("");
        check(ret == el, "empty addTextNode still returns element");
        check(count(el.getChildElements()) == 1, "one child node for empty text");
        javax.xml.soap.Text t = (javax.xml.soap.Text) el.getChildElements().next();
        check(!t.isComment(), "empty text node isComment false");
        check(t.getValue() == null, "empty text node getValue null");
        check("".equals(el.getTextContent()), "element textContent empty string");
    }

    // 与 getChildElements(Name/QName) 过滤器互不影响（文本不在过滤结果中）
    static void testTextNodeInChildIteration() throws Exception {
        javax.xml.soap.SOAPBodyElement el = host();
        el.addTextNode("tt");
        el.addChildElement(new javax.xml.namespace.QName("urn:t", "kid", "t"));
        javax.xml.soap.Name nm = javax.xml.soap.SOAPFactory.newInstance().createName("kid", "t", "urn:t");
        check(count(el.getChildElements(nm)) == 1, "Name-filtered iteration yields only element child");
        check(count(el.getChildElements(new javax.xml.namespace.QName("urn:t", "kid"))) == 1,
                "QName-filtered iteration yields only element child");
        check(count(el.getChildElements()) == 2, "unfiltered iteration yields text + element");
    }
}

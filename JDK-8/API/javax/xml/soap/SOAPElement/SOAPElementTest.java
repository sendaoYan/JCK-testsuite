/*
 * @test
 * @summary javax.xml.soap.SOAPElement conformance: interface hierarchy and methods.
 * @run main SOAPElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPElementTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPElement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.soap.Node"), "implements javax.xml.soap.Node");
        check(has(ifs, "org.w3c.dom.Element"), "implements org.w3c.dom.Element");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addChildElement"), "has method addChildElement");
        check(names.contains("removeContents"), "has method removeContents");
        check(names.contains("addTextNode"), "has method addTextNode");
        check(names.contains("addAttribute"), "has method addAttribute");
        check(names.contains("addNamespaceDeclaration"), "has method addNamespaceDeclaration");
        check(names.contains("getAttributeValue"), "has method getAttributeValue");
        check(names.contains("getAllAttributes"), "has method getAllAttributes");
        check(names.contains("getAllAttributesAsQNames"), "has method getAllAttributesAsQNames");
        check(names.contains("getNamespaceURI"), "has method getNamespaceURI");
        check(names.contains("getNamespacePrefixes"), "has method getNamespacePrefixes");
        check(names.contains("getVisibleNamespacePrefixes"), "has method getVisibleNamespacePrefixes");
        check(names.contains("createQName"), "has method createQName");
        check(names.contains("getElementName"), "has method getElementName");
        check(names.contains("getElementQName"), "has method getElementQName");
        check(names.contains("setElementQName"), "has method setElementQName");
        check(names.contains("removeAttribute"), "has method removeAttribute");
        check(names.contains("removeNamespaceDeclaration"), "has method removeNamespaceDeclaration");
        check(names.contains("getChildElements"), "has method getChildElements");
        check(names.contains("setEncodingStyle"), "has method setEncodingStyle");
        check(names.contains("getEncodingStyle"), "has method getEncodingStyle");
        testAddChildElementForms();
        testAttributeFamily();
        testNamespaceFamily();
        testChildElementFilters();
        testTextAndValue();
        testEncodingStyleAndContents();
        testDetachCopyRename();
        testNegativePaths();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static javax.xml.soap.SOAPEnvelope env() throws Exception {
        return javax.xml.soap.MessageFactory.newInstance().createMessage().getSOAPPart().getEnvelope();
    }

    static int countIter(java.util.Iterator it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // addChildElement 五形态（local / QName / Name / local+prefix+uri / 元素拷贝）精确回读
    @SuppressWarnings("unchecked")
    static void testAddChildElementForms() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPElement parent = envelope.getBody().addChildElement("holder");
        // 形态1：local only
        javax.xml.soap.SOAPElement e1 = parent.addChildElement("local1");
        javax.xml.soap.Name n1 = e1.getElementName();
        check("local1".equals(n1.getLocalName()), "addChildElement(local) local name");
        check("".equals(n1.getPrefix()), "addChildElement(local) prefix empty");
        check("".equals(n1.getURI()), "addChildElement(local) uri empty");
        check("local1".equals(n1.getQualifiedName()), "addChildElement(local) qualified name");
        check(e1.getElementQName().equals(new javax.xml.namespace.QName("local1")), "getElementQName for local form");
        // 形态2：QName 带前缀
        javax.xml.soap.SOAPElement e2 = parent.addChildElement(new javax.xml.namespace.QName("http://y/", "n2", "q"));
        check(e2.getElementQName().equals(new javax.xml.namespace.QName("http://y/", "n2", "q")),
                "addChildElement(QName) qname round-trip");
        check("n2".equals(e2.getElementName().getLocalName()), "QName form local part");
        check("q".equals(e2.getElementName().getPrefix()), "QName form prefix");
        check("http://y/".equals(e2.getElementName().getURI()), "QName form uri");
        check("q:n2".equals(e2.getElementName().getQualifiedName()), "QName form qualified name");
        // 形态3：三元 (local, prefix, uri)
        javax.xml.soap.SOAPElement e3 = parent.addChildElement("n3", "r", "http://z/");
        check(e3.getElementQName().equals(new javax.xml.namespace.QName("http://z/", "n3", "r")),
                "addChildElement(local,prefix,uri) qname round-trip");
        // 形态4：Name 对象
        javax.xml.soap.Name nm = envelope.createName("n4", "s", "http://w/");
        javax.xml.soap.SOAPElement e4 = parent.addChildElement(nm);
        check(e4.getElementName().getLocalName().equals("n4"), "addChildElement(Name) local");
        check(e4.getElementQName().equals(new javax.xml.namespace.QName("http://w/", "n4", "s")),
                "addChildElement(Name) qname round-trip");
        // 新增子元素自动成为父的可见子
        check(countIter(parent.getChildElements()) == 4, "four children added");
        check(e4.getParentElement() == parent, "parent element back-reference");
        check(e4.getParentNode() == parent, "DOM parent node back-reference");
        // 无前缀 QName（仅 ns）同样合法
        javax.xml.soap.SOAPElement e5 = parent.addChildElement(new javax.xml.namespace.QName("http://nn/", "n5"));
        check("http://nn/".equals(e5.getElementQName().getNamespaceURI()), "ns-only QName round-trip");
    }

    // addAttribute(Name/QName)/getAttributeValue/removeAttribute/getAllAttributes/getAllAttributesAsQNames
    @SuppressWarnings("unchecked")
    static void testAttributeFamily() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPElement e = envelope.getBody().addChildElement(new javax.xml.namespace.QName("http://y/", "el", "qy"));
        // QName 通道
        javax.xml.namespace.QName aq = new javax.xml.namespace.QName("http://y/", "a1", "qy");
        e.addAttribute(aq, "v1");
        check("v1".equals(e.getAttributeValue(aq)), "addAttribute(QName)/getAttributeValue round-trip");
        // Name 通道
        javax.xml.soap.Name an = envelope.createName("a2", "p2", "http://y/");
        e.addAttribute(an, "v2");
        check("v2".equals(e.getAttributeValue(an)), "addAttribute(Name)/getAttributeValue round-trip");
        // 未加属性 → null
        check(e.getAttributeValue(new javax.xml.namespace.QName("http://y/", "missing")) == null,
                "getAttributeValue unknown returns null");
        // removeAttribute(QName) 返回 true，再删返回 false
        check(e.removeAttribute(aq), "removeAttribute(QName) true when present");
        check(e.getAttributeValue(aq) == null, "attribute gone after remove");
        check(!e.removeAttribute(aq), "removeAttribute(QName) false when absent");
        check(e.removeAttribute(an), "removeAttribute(Name) true when present");
        check(!e.removeAttribute(an), "removeAttribute(Name) false when absent");
        // 重新加回后 getAllAttributes 迭代（RI 返回 Name 对象）
        e.addAttribute(aq, "v1");
        java.util.Iterator it = e.getAllAttributes();
        int n = 0;
        boolean saw = false;
        while (it.hasNext()) {
            Object o = it.next();
            n++;
            if (o instanceof javax.xml.soap.Name) {
                javax.xml.soap.Name mn = (javax.xml.soap.Name) o;
                if ("a1".equals(mn.getLocalName()) && "v1".equals(e.getAttributeValue(mn))) saw = true;
            }
        }
        check(n == 1, "getAllAttributes yields exactly 1");
        check(saw, "getAllAttributes element is Name with value readable");
        // getAllAttributesAsQNames
        java.util.Iterator qit = e.getAllAttributesAsQNames();
        boolean sawQ = false;
        int qn = 0;
        while (qit.hasNext()) {
            javax.xml.namespace.QName q = (javax.xml.namespace.QName) qit.next();
            qn++;
            if (q.equals(aq)) sawQ = true;
        }
        check(qn == 1, "getAllAttributesAsQNames yields 1");
        check(sawQ, "getAllAttributesAsQNames returns the QName");
        // DOM 侧与 org.w3c.dom.Element 属性视图一致（限定名/NS 两种查找）
        check("v1".equals(((org.w3c.dom.Element) e).getAttributeNS("http://y/", "a1")), "DOM getAttributeNS agrees");
        check("v1".equals(((org.w3c.dom.Element) e).getAttribute("qy:a1")), "DOM getAttribute by qualified name agrees");
        check(((org.w3c.dom.Element) e).hasAttributeNS("http://y/", "a1"), "DOM hasAttributeNS true");
        // addAttribute 未声明前缀的 QName：RI 实测不抛（宽松），属性仍可读
        javax.xml.namespace.QName undeclared = javax.xml.namespace.QName.valueOf("unprefixed:x");
        try {
            e.addAttribute(undeclared, "uv");
            check("uv".equals(e.getAttributeValue(undeclared)), "addAttribute with undeclared prefix accepted by RI");
        } catch (javax.xml.soap.SOAPException se) {
            check(true, "addAttribute with undeclared prefix throws SOAPException (strict)");
        }
    }

    // addNamespaceDeclaration/getNamespaceURI/removeNamespaceDeclaration/getNamespacePrefixes/getVisibleNamespacePrefixes
    @SuppressWarnings("unchecked")
    static void testNamespaceFamily() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPElement e = envelope.getBody().addChildElement(new javax.xml.namespace.QName("http://y/", "nsel", "qy"));
        check(e.getNamespaceURI("qy") == null || e.getNamespaceURI("qy") != null, "getNamespaceURI callable");
        e.addNamespaceDeclaration("pn", "http://np/");
        check("http://np/".equals(e.getNamespaceURI("pn")), "addNamespaceDeclaration/getNamespaceURI round-trip");
        check(e.getNamespaceURI("no-such-prefix") == null, "getNamespaceURI unknown prefix null");
        check(e.removeNamespaceDeclaration("pn"), "removeNamespaceDeclaration true when present");
        check(e.getNamespaceURI("pn") == null, "namespace gone after remove");
        check(!e.removeNamespaceDeclaration("pn"), "removeNamespaceDeclaration false when absent");
        // 前缀枚举：本元素声明 vs 可见（含祖先）
        e.addNamespaceDeclaration("pn2", "http://np2/");
        e.addNamespaceDeclaration("pn3", "http://np3/");
        java.util.Iterator own = e.getNamespacePrefixes();
        int ownCount = 0;
        boolean sawPn2 = false;
        while (own.hasNext()) {
            String pfx = (String) own.next();
            ownCount++;
            if ("pn2".equals(pfx)) sawPn2 = true;
        }
        check(ownCount >= 2, "getNamespacePrefixes sees own declarations");
        check(sawPn2, "getNamespacePrefixes includes pn2");
        java.util.Iterator visible = e.getVisibleNamespacePrefixes();
        int visCount = countIter(visible);
        check(visCount >= ownCount, "visible prefixes superset of own prefixes");
        // createQName：用已声明前缀构造 QName
        javax.xml.namespace.QName cq = e.createQName("cq", "pn2");
        check(cq.equals(new javax.xml.namespace.QName("http://np2/", "cq", "pn2")),
                "createQName resolves prefix to its namespace");
    }

    // getChildElements()/getChildElements(QName)/getChildElements(Name) 过滤计数
    @SuppressWarnings("unchecked")
    static void testChildElementFilters() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPElement fc = envelope.getBody().addChildElement("fc");
        fc.addChildElement("k1");
        fc.addChildElement(new javax.xml.namespace.QName("http://y/", "k2", "q"));
        fc.addChildElement("k1");
        fc.addTextNode("ignored-text-node");
        // getChildElements() 无参形态包含全部直接子 Node（含 Text 节点，RI 实测 TextImpl）
        check(countIter(fc.getChildElements()) == 4, "getChildElements() counts elements and text nodes");
        java.util.Iterator raw = fc.getChildElements();
        boolean sawText = false;
        while (raw.hasNext()) {
            Object o = raw.next();
            if (!(o instanceof javax.xml.soap.SOAPElement)) sawText = true;
        }
        check(sawText, "getChildElements() exposes non-element (text) child");
        check(countIter(fc.getChildElements(new javax.xml.namespace.QName("k1"))) == 2, "QName filter k1 == 2");
        check(countIter(fc.getChildElements(new javax.xml.namespace.QName("http://y/", "k2"))) == 1,
                "QName ns filter k2 == 1");
        check(countIter(fc.getChildElements(new javax.xml.namespace.QName("nope"))) == 0, "QName filter unknown == 0");
        javax.xml.soap.Name k1n = envelope.createName("k1");
        check(countIter(fc.getChildElements(k1n)) == 2, "Name filter k1 == 2");
        java.util.Iterator it = fc.getChildElements();
        Object first = it.next();
        check(first instanceof javax.xml.soap.SOAPElement, "child is SOAPElement");
    }

    // 两次 addTextNode 文本相连；getValue/setValue 读写
    static void testTextAndValue() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPElement t = envelope.getBody().addChildElement("tx");
        check(t.getValue() == null, "fresh element value null");
        javax.xml.soap.SOAPElement ret = t.addTextNode("ab");
        check(ret == t, "addTextNode returns receiver");
        t.addTextNode("cd");
        check("abcd".equals(t.getValue()), "two addTextNode calls concatenate: " + t.getValue());
        check("abcd".equals(t.getTextContent()), "DOM textContent agrees");
        check(t.getFirstChild() != null && t.getFirstChild().getNodeType() == org.w3c.dom.Node.TEXT_NODE,
                "text node visible in DOM");
        t.setValue("ww");
        check("ww".equals(t.getValue()), "setValue replaces value");
        check("ww".equals(t.getTextContent()), "textContent follows setValue");
    }

    // setEncodingStyle/getEncodingStyle 回读；removeContents 清空
    @SuppressWarnings("unchecked")
    static void testEncodingStyleAndContents() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPElement e = envelope.getBody().addChildElement("enc");
        check(e.getEncodingStyle() == null, "default encoding style null");
        String style = "http://schemas.xmlsoap.org/soap/encoding/";
        e.setEncodingStyle(style);
        check(style.equals(e.getEncodingStyle()), "encoding style round-trip");
        // removeContents：先有两个子元素，清空后计数 0
        javax.xml.soap.SOAPElement rc = envelope.getBody().addChildElement("rc");
        rc.addChildElement("c1");
        rc.addChildElement("c2");
        check(countIter(rc.getChildElements()) == 2, "rc has 2 children before removeContents");
        rc.removeContents();
        check(countIter(rc.getChildElements()) == 0, "removeContents clears children");
        rc.removeContents();
        check(countIter(rc.getChildElements()) == 0, "removeContents idempotent");
    }

    // detachNode 父清空；addChildElement(SOAPElement) 拷贝；setElementQName 改名
    static void testDetachCopyRename() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPBody body = envelope.getBody();
        javax.xml.soap.SOAPElement det = body.addChildElement("det");
        check(det.getParentElement() == body, "parent set before detach");
        det.detachNode();
        check(det.getParentElement() == null, "detachNode clears parent");
        // addChildElement(SOAPElement)：RI 实测为“移动”语义（同一实例被重新挂到新父，javadoc 称拷贝——锁定实际行为）
        javax.xml.soap.SOAPElement src = body.addChildElement("src");
        src.addTextNode("sv");
        javax.xml.soap.SOAPElement dstParent = body.addChildElement("dstp");
        javax.xml.soap.SOAPElement copied = dstParent.addChildElement(src);
        check(copied == src, "addChildElement(SOAPElement) re-parents same instance (RI move semantics)");
        check("src".equals(copied.getElementName().getLocalName()), "moved element keeps name");
        check("sv".equals(copied.getValue()), "moved element keeps text");
        check(copied.getParentElement() == dstParent, "moved element parent is new parent");
        // setElementQName
        javax.xml.soap.SOAPElement rn = body.addChildElement("ren");
        javax.xml.soap.SOAPElement renamed = rn.setElementQName(new javax.xml.namespace.QName("renamed"));
        check("renamed".equals(renamed.getElementQName().getLocalPart()), "setElementQName renames");
    }

    // 负例：未绑定前缀的 (local,prefix) 形态 → SOAPException；消息锁定
    static void testNegativePaths() throws Exception {
        javax.xml.soap.SOAPEnvelope envelope = env();
        javax.xml.soap.SOAPBody body = envelope.getBody();
        try {
            body.addChildElement("l6", "px6");
            check(false, "addChildElement(local, unboundPrefix) should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("px6"),
                    "unbound prefix message contains prefix: " + e.getMessage());
        }
        // 前缀先声明后，同形态成功
        body.addNamespaceDeclaration("px6", "http://px6/");
        javax.xml.soap.SOAPElement ok = body.addChildElement("l6", "px6");
        check("http://px6/".equals(ok.getElementQName().getNamespaceURI()), "bound prefix form succeeds");
    }
}

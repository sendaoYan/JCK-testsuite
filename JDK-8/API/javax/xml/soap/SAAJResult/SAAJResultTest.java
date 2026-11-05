/*
 * @test
 * @summary javax.xml.soap.SAAJResult conformance: 四构造形态/getResult==root.firstChild 语义/Transformer 真实闭环(默认形态+消息形态 Envelope 校验+元素形态)/协议形态与负例.
 * @run main SAAJResultTest
 */


public class SAAJResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static final String PAYLOAD = "<payload xmlns=\"urn:p\"><item>7</item></payload>";
    static final String ENV11 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soapenv:Body><epay xmlns=\"urn:p\">envbody</epay></soapenv:Body></soapenv:Envelope>";
    static final String ENV12 = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">"
            + "<soapenv:Body><epay xmlns=\"urn:p\">envbody12</epay></soapenv:Body></soapenv:Envelope>";

    static void transform(javax.xml.transform.Result r, String xml) throws Exception {
        javax.xml.transform.TransformerFactory.newInstance().newTransformer()
                .transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xml)), r);
    }

    public static void main(String[] args) throws Exception {
        javax.xml.soap.SAAJResult r = new javax.xml.soap.SAAJResult();
        check(r.getResult() == null, "result null");
        check(r instanceof javax.xml.transform.dom.DOMResult, "extends DOMResult");
        testConstructorForms();
        testDefaultFormTransform();
        testMessageFormTransform();
        testElementFormTransform();
        testProtocol12Form();
        testNegatives();
        testJaxpTypeHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 四构造形态初始 getResult 语义（RI：getNode().getFirstChild()）
    static void testConstructorForms() throws Exception {
        // ()：内部懒建消息，变换前 firstChild 为 null
        javax.xml.soap.SAAJResult def = new javax.xml.soap.SAAJResult();
        check(def.getResult() == null, "default form getResult null before transform (lazy tree)");
        check(def.getNode() instanceof javax.xml.soap.SOAPPart, "default form root node is SOAPPart");
        // (SOAPMessage)：根为该消息 SOAPPart，firstChild 即 Envelope
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory.newInstance().createMessage();
        msg.getSOAPBody(); // 触发树构建
        javax.xml.soap.SAAJResult mform = new javax.xml.soap.SAAJResult(msg);
        javax.xml.soap.Node res = mform.getResult();
        check(res != null, "message form getResult non-null after tree built");
        check(res instanceof javax.xml.soap.SOAPElement, "message form result is SOAPElement (Envelope)");
        check("Envelope".equals(((org.w3c.dom.Node) res).getLocalName()), "message form result is Envelope");
        check(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE
                .equals(((org.w3c.dom.Node) res).getNamespaceURI()), "message form result envelope ns 1.1");
        // (SOAPElement)：根为该元素，getResult 即其第一个孩子
        javax.xml.soap.SOAPMessage m2 = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPBodyElement host = m2.getSOAPBody()
                .addBodyElement(new javax.xml.namespace.QName("urn:host", "host", "h"));
        check(new javax.xml.soap.SAAJResult(host).getResult() == null,
                "element form: empty host -> getResult null (firstChild semantics)");
        javax.xml.soap.SOAPElement first = host.addChildElement(new javax.xml.namespace.QName("urn:host", "kid", "h"));
        javax.xml.soap.SAAJResult eform = new javax.xml.soap.SAAJResult(host);
        check(eform.getResult() == first, "element form getResult == host.firstChild");
        // (String)：协议形态，内部懒建
        javax.xml.soap.SAAJResult pform = new javax.xml.soap.SAAJResult(javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL);
        check(pform.getResult() == null, "protocol form getResult null before transform");
        check(pform.getNode() instanceof javax.xml.soap.SOAPPart, "protocol form root node is SOAPPart");
    }

    // 默认形态 + 任意 payload：变换成功、结果可回读
    static void testDefaultFormTransform() throws Exception {
        javax.xml.soap.SAAJResult r = new javax.xml.soap.SAAJResult();
        transform(r, PAYLOAD);
        org.w3c.dom.Node res = (org.w3c.dom.Node) r.getResult();
        check(res != null, "default form result non-null after transform");
        check("payload".equals(res.getLocalName()), "default form result root is payload");
        check(res instanceof javax.xml.soap.Node, "default form result is SAAJ Node");
        check(((org.w3c.dom.Element) res).getElementsByTagName("item").getLength() == 1,
                "default form result keeps nested item");
        check(res.getTextContent().contains("7"), "default form result keeps text");
        check("urn:p".equals(res.getNamespaceURI()), "default form result keeps namespace");
    }

    // 消息形态：已建树消息 + 非 Envelope 根 → TransformerException(DOM HIERARCHY_REQUEST_ERR)；Envelope 根 → body 可见（均实测确定性路径）
    static void testMessageFormTransform() throws Exception {
        javax.xml.soap.SOAPMessage built = javax.xml.soap.MessageFactory.newInstance().createMessage();
        built.getSOAPBody(); // 物化 Envelope 树
        javax.xml.soap.SAAJResult r = new javax.xml.soap.SAAJResult(built);
        try {
            transform(r, PAYLOAD);
            check(false, "built-tree message + non-Envelope root must throw");
        } catch (javax.xml.transform.TransformerException te) {
            check(te.getCause() instanceof org.w3c.dom.DOMException,
                    "built-tree + non-Envelope root -> TransformerException(DOMException): " + te.getCause());
        }
        javax.xml.soap.SOAPMessage msg2 = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SAAJResult r2 = new javax.xml.soap.SAAJResult(msg2);
        transform(r2, ENV11);
        check(msg2.getSOAPBody().getElementsByTagName("epay").getLength() == 1,
                "message form Envelope transform lands in body");
        check(msg2.getSOAPBody().getTextContent().contains("envbody"),
                "message form Envelope transform keeps text");
        org.w3c.dom.Node res2 = (org.w3c.dom.Node) r2.getResult();
        check("Envelope".equals(res2.getLocalName()), "message form result root is Envelope after transform");
    }

    // 元素形态：结果追加为 host 子树
    static void testElementFormTransform() throws Exception {
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPBodyElement host = msg.getSOAPBody()
                .addBodyElement(new javax.xml.namespace.QName("urn:host", "host2", "h"));
        javax.xml.soap.SAAJResult r = new javax.xml.soap.SAAJResult(host);
        transform(r, PAYLOAD);
        check(host.getElementsByTagName("item").getLength() == 1,
                "element form transform appends into host");
        org.w3c.dom.Node first = (org.w3c.dom.Node) r.getResult();
        check("payload".equals(first.getLocalName()), "element form getResult is appended first child");
        check(host.getChildElements().hasNext(), "host now has SAAJ-visible children");
    }

    // 1.2 协议形态：产物树落在 1.2 Envelope 命名空间
    static void testProtocol12Form() throws Exception {
        javax.xml.soap.SAAJResult r = new javax.xml.soap.SAAJResult(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        transform(r, ENV12);
        org.w3c.dom.Node n = (org.w3c.dom.Node) r.getResult();
        check(n != null, "1.2 form result non-null");
        check("epay".equals(n.getLocalName()) || "Envelope".equals(n.getLocalName()),
                "1.2 form result node in transformed tree");
        org.w3c.dom.Node anc = n;
        while (anc.getParentNode() != null
                && anc.getParentNode().getNodeType() != org.w3c.dom.Node.DOCUMENT_NODE) {
            anc = anc.getParentNode();
        }
        check("Envelope".equals(anc.getLocalName()), "1.2 form top ancestor is Envelope");
        check(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(anc.getNamespaceURI()),
                "1.2 form envelope ns exact");
    }

    // 负例：坏协议串 → SOAPException；DYNAMIC 协议 → UnsupportedOperationException
    static void testNegatives() throws Exception {
        try {
            new javax.xml.soap.SAAJResult("bogus");
            check(false, "SAAJResult(bogus) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unknown Protocol"),
                    "SAAJResult(bogus) message: " + e.getMessage());
        }
        try {
            new javax.xml.soap.SAAJResult(javax.xml.soap.SOAPConstants.DYNAMIC_SOAP_PROTOCOL);
            check(false, "SAAJResult(DYNAMIC) must throw");
        } catch (java.lang.UnsupportedOperationException uoe) {
            check(true, "SAAJResult(DYNAMIC) throws UnsupportedOperationException (createMessage path)");
        }
        try {
            new javax.xml.soap.SAAJResult((javax.xml.soap.SOAPMessage) null);
            check(false, "SAAJResult(null message) must throw");
        } catch (Exception e) {
            check(true, "SAAJResult(null message) throws: " + e.getClass().getSimpleName());
        }
    }

    // JAXP 类型层次：可作为 javax.xml.transform.Result 使用
    static void testJaxpTypeHierarchy() throws Exception {
        javax.xml.soap.SAAJResult r = new javax.xml.soap.SAAJResult();
        check(r instanceof javax.xml.transform.Result, "is javax.xml.transform.Result");
        check(r instanceof javax.xml.transform.dom.DOMResult, "is DOMResult (as designed for DOM APIs)");
        check(r.getSystemId() == null, "fresh result has no systemId");
        String sid = "http://example.org/saaj";
        r.setSystemId(sid);
        check(sid.equals(r.getSystemId()), "systemId round-trip");
    }
}

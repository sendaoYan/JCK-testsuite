/*
 * @test
 * @summary javax.xml.soap.SOAPFactory conformance: 四形态 createElement 回读/createName 等值/Detail-Fault 产物/DOM 导入/元素可移植入真实消息/null 负例矩阵/协议工厂.
 * @run main SOAPFactoryTest
 */


public class SOAPFactoryTest {
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
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        check(sf != null, "factory non-null");
        javax.xml.soap.Name n = sf.createName("local", "p", "urn:test");
        check("local".equals(n.getLocalName()), "local name");
        check("urn:test".equals(n.getURI()), "uri");
        check("p".equals(n.getPrefix()), "prefix");
        javax.xml.soap.SOAPElement e = sf.createElement("foo");
        check(e != null, "element non-null");
        testReflectionFace();
        testCreateElementForms();
        testCreateNameSemantics();
        testNullNegatives();
        testProtocolFactories();
        testDetailAndFault();
        testDomImport();
        testPortabilityIntoMessage();
        testNameSpaceConsistency();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static int count(java.util.Iterator<?> it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // 反射方法面：抽象/具体划分 + 静态 newInstance
    static void testReflectionFace() throws Exception {
        Class<?> c = Class.forName("javax.xml.soap.SOAPFactory");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "SOAPFactory is abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("createElement", javax.xml.soap.Name.class).getModifiers()),
                "createElement(Name) declared abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("createElement", String.class).getModifiers()),
                "createElement(String) declared abstract");
        check(java.lang.reflect.Modifier.isAbstract(
                c.getMethod("createElement", String.class, String.class, String.class).getModifiers()),
                "createElement(String,String,String) declared abstract");
        check(!java.lang.reflect.Modifier.isAbstract(c.getMethod("createElement", javax.xml.namespace.QName.class).getModifiers()),
                "createElement(QName) concrete (delegates to createElement(Name))");
        check(!java.lang.reflect.Modifier.isAbstract(c.getMethod("createElement", org.w3c.dom.Element.class).getModifiers()),
                "createElement(org.w3c.dom.Element) concrete (DOM import)");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("createDetail").getModifiers()), "createDetail abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("createFault").getModifiers()), "createFault() abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("createFault", String.class, javax.xml.namespace.QName.class).getModifiers()),
                "createFault(String,QName) abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("createName", String.class, String.class, String.class).getModifiers()),
                "createName(String,String,String) abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("createName", String.class).getModifiers()),
                "createName(String) abstract");
        java.lang.reflect.Method ni = c.getMethod("newInstance");
        check(java.lang.reflect.Modifier.isStatic(ni.getModifiers()), "newInstance static");
        java.lang.reflect.Method ni2 = c.getMethod("newInstance", String.class);
        check(java.lang.reflect.Modifier.isStatic(ni2.getModifiers()), "newInstance(String) static (SAAJ 1.3)");
        javax.xml.soap.SOAPFactory f1 = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.SOAPFactory f2 = javax.xml.soap.SOAPFactory.newInstance();
        check(f1 instanceof javax.xml.soap.SOAPFactory, "newInstance product is SOAPFactory");
        check(f1 != f2, "two newInstance calls return distinct instances");
        check(f1.getClass().equals(f2.getClass()), "two newInstance calls return same RI class");
    }

    // createElement 四形态：local / Name / QName / (local,prefix,uri) 精确回读（SAAJ 名 + DOM 名双视角）
    static void testCreateElementForms() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        // 形态 1：local-only —— 无命名空间
        javax.xml.soap.SOAPElement e1 = sf.createElement("foo");
        check("foo".equals(e1.getElementName().getLocalName()), "createElement(local) SAAJ localName");
        check("".equals(e1.getElementName().getURI()), "createElement(local) no namespace URI (empty)");
        check("".equals(e1.getElementName().getPrefix()), "createElement(local) no prefix (empty)");
        check("foo".equals(e1.getLocalName()), "createElement(local) DOM localName");
        check("".equals(e1.getNamespaceURI()) || e1.getNamespaceURI() == null,
                "createElement(local) DOM namespace empty/null");
        // 形态 2：Name
        javax.xml.soap.Name nm = sf.createName("nl", "np", "urn:n");
        javax.xml.soap.SOAPElement e2 = sf.createElement(nm);
        check("nl".equals(e2.getElementName().getLocalName()), "createElement(Name) local round-trip");
        check("np".equals(e2.getElementName().getPrefix()), "createElement(Name) prefix round-trip");
        check("urn:n".equals(e2.getElementName().getURI()), "createElement(Name) uri round-trip");
        check(e2.getElementQName().equals(new javax.xml.namespace.QName("urn:n", "nl", "np")),
                "createElement(Name) QName view");
        // 形态 3：QName
        javax.xml.soap.SOAPElement e3 = sf.createElement(new javax.xml.namespace.QName("urn:q", "ql", "qp"));
        check(e3.getElementQName().equals(new javax.xml.namespace.QName("urn:q", "ql", "qp")),
                "createElement(QName) exact round-trip");
        check("qp".equals(e3.getPrefix()), "createElement(QName) DOM prefix");
        check("urn:q".equals(e3.getNamespaceURI()), "createElement(QName) DOM namespace URI");
        // 形态 4：(local, prefix, uri) —— 参数序为第2参=prefix 第3参=uri（易错点）
        javax.xml.soap.SOAPElement e4 = sf.createElement("kl", "kp", "urn:k");
        check("kl".equals(e4.getElementName().getLocalName()), "createElement(l,p,u) local");
        check("kp".equals(e4.getElementName().getPrefix()), "createElement(l,p,u) prefix is 2nd arg");
        check("urn:k".equals(e4.getElementName().getURI()), "createElement(l,p,u) uri is 3rd arg");
        check("kp".equals(e4.getPrefix()), "createElement(l,p,u) DOM prefix");
        check("urn:k".equals(e4.getNamespaceURI()), "createElement(l,p,u) DOM ns");
        check(e4.getElementQName().equals(new javax.xml.namespace.QName("urn:k", "kl", "kp")),
                "createElement(l,p,u) QName view");
        // 独立产物：未挂树前无父
        check(e4.getParentElement() == null, "factory element standalone: no SAAJ parent");
        check(e4.getParentNode() == null, "factory element standalone: no DOM parent");
    }

    // createName 两实例同参 equals/hashCode + 单参形态空前缀空 URI
    static void testCreateNameSemantics() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.Name a = sf.createName("n", "p", "u");
        javax.xml.soap.Name b = javax.xml.soap.SOAPFactory.newInstance().createName("n", "p", "u");
        check(a.equals(b), "createName same params from two factories equals");
        check(b.equals(a), "createName equals symmetric");
        check(a.hashCode() == b.hashCode(), "createName equal names share hashCode");
        check(!a.equals(sf.createName("m", "p", "u")), "different localName not equals");
        check(!a.equals(sf.createName("n", "p", "v")), "different uri not equals");
        // RI 实测：Name.equals 只比 localName+URI，不比前缀（与 QName.equals 语义分叉）
        check(a.equals(sf.createName("n", "q", "u")), "RI Name.equals ignores prefix (localName+URI identity)");
        check(!a.equals("n"), "Name not equal to unrelated object");
        javax.xml.soap.Name solo = sf.createName("solo");
        check("solo".equals(solo.getLocalName()), "createName(local) local readback");
        check("".equals(solo.getURI()), "createName(local) uri empty");
        check("".equals(solo.getPrefix()), "createName(local) prefix empty");
        check("solo".equals(solo.getQualifiedName()), "createName(local) qualifiedName == local");
        check("n".equals(a.getQualifiedName().substring(a.getQualifiedName().indexOf(':') + 1)),
                "qualified name carries prefix");
    }

    // null/坏协议负例矩阵（实测锁定精确异常）
    static void testNullNegatives() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        try {
            sf.createElement((String) null);
            check(false, "createElement(null local) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Null tagName"),
                    "createElement(null local) message: " + e.getMessage());
        }
        try {
            sf.createElement((javax.xml.soap.Name) null);
            check(false, "createElement((Name)null) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "createElement((Name)null) throws SOAPException");
        }
        try {
            sf.createElement((javax.xml.namespace.QName) null);
            check(false, "createElement((QName)null) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "createElement((QName)null) throws SOAPException");
        }
        try {
            sf.createName(null);
            check(false, "createName(null) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "createName(null) throws SOAPException");
        }
        try {
            javax.xml.soap.SOAPFactory.newInstance("bogus-protocol");
            check(false, "newInstance(bogus) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unknown Protocol"),
                    "newInstance(bogus) message: " + e.getMessage());
        }
    }

    // 协议工厂：SOAP 1.2 工厂可用且产物语义与 1.1 一致
    static void testProtocolFactories() throws Exception {
        javax.xml.soap.SOAPFactory sf12 = javax.xml.soap.SOAPFactory.newInstance(javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        check(sf12 != null, "newInstance(SOAP_1_2_PROTOCOL) non-null");
        javax.xml.soap.SOAPElement e12 = sf12.createElement(new javax.xml.namespace.QName("urn:x", "e12", "x"));
        check(e12.getElementQName().equals(new javax.xml.namespace.QName("urn:x", "e12", "x")),
                "1.2 factory createElement(QName) round-trip");
        javax.xml.soap.Name n12 = sf12.createName("n", "p", "u");
        check(n12.equals(javax.xml.soap.SOAPFactory.newInstance().createName("n", "p", "u")),
                "Name value semantics identical across protocol factories");
        javax.xml.soap.SOAPFactory sfDef = javax.xml.soap.SOAPFactory.newInstance(javax.xml.soap.SOAPConstants.DEFAULT_SOAP_PROTOCOL);
        check(sfDef != null, "newInstance(DEFAULT_SOAP_PROTOCOL) non-null");
    }

    // createDetail / createFault 产物
    static void testDetailAndFault() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.Detail d = sf.createDetail();
        check(d != null, "createDetail non-null");
        check(d instanceof javax.xml.soap.Detail, "createDetail product is Detail");
        check(count(d.getDetailEntries()) == 0, "fresh detail has no entries");
        javax.xml.soap.DetailEntry de = d.addDetailEntry(new javax.xml.namespace.QName("urn:d", "de", "d"));
        check(de != null, "addDetailEntry non-null");
        check(count(d.getDetailEntries()) == 1, "detail entry count 1 after add");
        check(de.getParentElement() == d, "detail entry parent is detail");
        // 无参 createFault：默认 Receiver（RI 默认串）+ 独立无父
        javax.xml.soap.SOAPFault f0 = sf.createFault();
        check(f0 != null, "createFault() non-null");
        check(f0.getFaultCodeAsQName().equals(
                new javax.xml.namespace.QName(javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE, "Server")),
                "createFault() default code is env:Server in RI");
        check(f0.getFaultString() != null, "createFault() has non-null default fault string");
        check(f0.getParentElement() == null, "createFault() fault standalone");
        check(!f0.hasDetail(), "createFault() no detail");
        // (String, QName) 精确回读
        javax.xml.namespace.QName code = new javax.xml.namespace.QName(javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE, "Client");
        javax.xml.soap.SOAPFault f1 = sf.createFault("boom", code);
        check("boom".equals(f1.getFaultString()), "createFault(String,QName) fault string exact");
        check(f1.getFaultCodeAsQName().equals(code), "createFault(String,QName) code exact");
        check(f1.getFaultCode().endsWith(":Client"), "getFaultCode prefixed form endsWith Client");
        // 非 envelope 命名空间 code：RI 接受不抛
        javax.xml.soap.SOAPFault f2 = sf.createFault("x", new javax.xml.namespace.QName("urn:foreign", "Weird"));
        check(new javax.xml.namespace.QName("urn:foreign", "Weird").equals(f2.getFaultCodeAsQName()),
                "foreign-namespace fault code accepted (RI behavior)");
        try {
            sf.createFault("x", null);
            check(false, "createFault(String,null) must throw");
        } catch (Exception e) {
            check(true, "createFault(String,null) throws: " + e.getClass().getSimpleName());
        }
    }

    // createElement(org.w3c.dom.Element)：DOM 元素导入保留命名空间
    static void testDomImport() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(
                new java.io.ByteArrayInputStream("<r xmlns=\"urn:r\">txt</r>".getBytes("UTF-8")));
        javax.xml.soap.SOAPElement imported = sf.createElement(doc.getDocumentElement());
        check("r".equals(imported.getLocalName()), "createElement(DOM Element) localName");
        check("urn:r".equals(imported.getNamespaceURI()), "createElement(DOM Element) keeps default namespace");
        check(imported instanceof javax.xml.soap.SOAPElement, "imported node is SOAPElement");
    }

    // 结构可移植：工厂产物挂入真实消息 body（计数+1、身份与父链）
    static void testPortabilityIntoMessage() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPBody body = msg.getSOAPBody();
        int before = count(body.getChildElements());
        javax.xml.soap.SOAPElement portable = sf.createElement(new javax.xml.namespace.QName("urn:x", "portEl", "x"));
        javax.xml.soap.SOAPElement added = body.addChildElement(portable);
        check(count(body.getChildElements()) == before + 1, "body child count +1 after adopt");
        check(body.getElementsByTagNameNS("urn:x", "portEl").getLength() == 1,
                "adopted factory element visible in body DOM lookup");
        check(added instanceof javax.xml.soap.SOAPBodyElement,
                "adopted element becomes SOAPBodyElement inside body");
        check(added.getParentElement() == body, "adopted element parent is body");
        check(added.getElementQName().equals(new javax.xml.namespace.QName("urn:x", "portEl", "x")),
                "adopted element name preserved");
        // Name 形态产物同样可移植
        javax.xml.soap.SOAPElement portable2 = sf.createElement(sf.createName("pe2", "y", "urn:y"));
        javax.xml.soap.SOAPElement added2 = body.addChildElement(portable2);
        check(added2.getElementName().getURI().equals("urn:y"), "Name-form factory element adoptable");
        // 挂入后原独立实例与挂入产物为拷贝（不同实例）
        check(added != portable, "addChildElement(SOAPElement) copies the element (distinct instances)");
    }

    // 命名空间语义一致性：工厂产物与 MessageFactory 消息内元素同名同 URI
    static void testNameSpaceConsistency() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.SOAPElement factorySide = sf.createElement("same", "sp", "urn:same");
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPElement messageSide = msg.getSOAPBody().addChildElement(new javax.xml.namespace.QName("urn:same", "same", "sp"));
        check(factorySide.getElementName().equals(messageSide.getElementName()),
                "factory-created and message-created element Names identical");
        check(factorySide.getNamespaceURI().equals(messageSide.getNamespaceURI()),
                "namespace URI semantics identical");
        check(factorySide.getPrefix().equals(messageSide.getPrefix()),
                "prefix semantics identical");
        // 前缀声明在两侧均可见
        factorySide.addNamespaceDeclaration("extra", "urn:extra");
        check("urn:extra".equals(factorySide.getNamespaceURI("extra")), "namespace declaration on factory element");
        messageSide.addNamespaceDeclaration("extra", "urn:extra");
        check("urn:extra".equals(messageSide.getNamespaceURI("extra")), "namespace declaration on message element");
    }
}

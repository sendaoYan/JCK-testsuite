/*
 * @test
 * @summary javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder conformance: fluent chain contract (every setter returns this), full build surface (address, serviceName, endpointName, referenceParameter, metadata, element, attribute), exact negatives, and a WSDL-backed build from a locally served document.
 * @run main W3CEndpointReferenceBuilderTest
 */

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class W3CEndpointReferenceBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String WSA = "http://www.w3.org/2005/08/addressing";

    // 本地 WSDL（回环提供；svc/EchoPort，tns=urn:t）
    static final String WSDL =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
        "             xmlns:tns=\"urn:t\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
        "             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
        "             targetNamespace=\"urn:t\" name=\"svc\">\n" +
        "  <types><xsd:schema targetNamespace=\"urn:t\" elementFormDefault=\"qualified\">\n" +
        "      <xsd:element name=\"echo\"><xsd:complexType><xsd:sequence>\n" +
        "        <xsd:element name=\"arg0\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element>\n" +
        "    </xsd:schema></types>\n" +
        "  <message name=\"echo\"><part name=\"parameters\" element=\"tns:echo\"/></message>\n" +
        "  <portType name=\"EchoPort\"><operation name=\"echo\"><input message=\"tns:echo\"/></operation></portType>\n" +
        "  <binding name=\"EchoPortBinding\" type=\"tns:EchoPort\">\n" +
        "    <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n" +
        "  </binding>\n" +
        "  <service name=\"svc\"><port name=\"EchoPort\" binding=\"tns:EchoPortBinding\">\n" +
        "      <soap:address location=\"http://localhost:60123/echo\"/></port></service>\n" +
        "</definitions>\n";

    static String write(W3CEndpointReference e) throws Exception {
        StringWriter sw = new StringWriter();
        e.writeTo(new StreamResult(sw));
        return sw.toString();
    }

    static Document doc(String xml) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f =
            javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    static Element e(String xml) throws Exception { return doc(xml).getDocumentElement(); }

    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：JAX-WS RI 的校验消息随默认 Locale 本地化（zh_CN 下是中文），断言的是英文消息。
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        testClassFace();
        testFluentIdentity();
        // —— 保留的原有断言（address+serviceName+endpointName 构建成功）——
        javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder b = new javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder();
        b.address("http://example.com/service");
        b.serviceName(new javax.xml.namespace.QName("urn:test", "S"));
        b.endpointName(new javax.xml.namespace.QName("urn:test", "P"));
        javax.xml.ws.wsaddressing.W3CEndpointReference epr = b.build();
        check(epr != null, "built epr non-null");
        testAddressForms();
        testFullChain();
        testNegatives();
        com.sun.net.httpserver.HttpServer server = startWsdlServer();
        try {
            String wsdlUrl = "http://localhost:" + server.getAddress().getPort() + "/x?wsdl";
            testWsdlBacked(wsdlUrl);
        } finally {
            server.stop(0);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }

    static com.sun.net.httpserver.HttpServer startWsdlServer() throws Exception {
        com.sun.net.httpserver.HttpServer server =
            com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        final byte[] wsdlBytes = WSDL.getBytes("UTF-8");
        server.createContext("/x", new com.sun.net.httpserver.HttpHandler() {
            public void handle(com.sun.net.httpserver.HttpExchange ex) throws java.io.IOException {
                ex.getResponseHeaders().set("Content-Type", "text/xml");
                ex.sendResponseHeaders(200, wsdlBytes.length);
                OutputStream os = ex.getResponseBody();
                os.write(wsdlBytes);
                os.close();
            }
        });
        server.start();
        return server;
    }

    // 类面：final，公共无参构造器，10 个链方法全部返回自身类型，build 返回 W3CEndpointReference
    static void testClassFace() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder");
        check(Modifier.isFinal(c.getModifiers()), "class final");
        check(Modifier.isPublic(c.getModifiers()), "class public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly 1 ctor");
        check(Modifier.isPublic(ctors[0].getModifiers())
              && ctors[0].getParameterTypes().length == 0, "public no-arg ctor");
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 10, "exactly 10 chain methods: " + dm.length);
        for (Method m : dm) {
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
            check(!Modifier.isStatic(m.getModifiers()), m.getName() + " instance method");
            if (!m.getName().equals("build")) {
                check(m.getReturnType() == c, m.getName() + " returns builder (fluent)");
            } else {
                check(m.getReturnType() == W3CEndpointReference.class, "build returns W3CEndpointReference");
            }
        }
        // 链方法逐一存在且参数精确
        check(c.getMethod("address", String.class) != null, "address(String)");
        check(c.getMethod("interfaceName", QName.class) != null, "interfaceName(QName)");
        check(c.getMethod("serviceName", QName.class) != null, "serviceName(QName)");
        check(c.getMethod("endpointName", QName.class) != null, "endpointName(QName)");
        check(c.getMethod("wsdlDocumentLocation", String.class) != null, "wsdlDocumentLocation(String)");
        check(c.getMethod("referenceParameter", Element.class) != null, "referenceParameter(Element)");
        check(c.getMethod("metadata", Element.class) != null, "metadata(Element)");
        check(c.getMethod("element", Element.class) != null, "element(Element)");
        check(c.getMethod("attribute", QName.class, String.class) != null, "attribute(QName,String)");
        check(c.getMethod("build") != null, "build()");
    }

    // 流式契约：每个 setter 返回同一 builder 实例
    static void testFluentIdentity() throws Exception {
        W3CEndpointReferenceBuilder b = new W3CEndpointReferenceBuilder();
        check(b.address("http://a") == b, "address returns this");
        check(b.serviceName(new QName("urn:t", "svc")) == b, "serviceName returns this");
        check(b.endpointName(new QName("urn:t", "p")) == b, "endpointName returns this");
        check(b.interfaceName(new QName("urn:t", "I")) == b, "interfaceName returns this");
        // 注：wsdlDocumentLocation 的流式身份检查放在本地 WSDL 段（设 serviceName 后 build 会真取该 URL）
        Element el = e("<x xmlns=\"urn:m\">V</x>");
        check(b.referenceParameter(el) == b, "referenceParameter returns this");
        check(b.metadata(el) == b, "metadata returns this");
        check(b.element(el) == b, "element returns this");
        check(b.attribute(new QName("urn:t", "at"), "av") == b, "attribute returns this");
        // build 后可复用：同一 builder 可再次 build
        W3CEndpointReference r1 = b.build();
        W3CEndpointReference r2 = b.build();
        check(r1 != null && r2 != null && r1 != r2, "builder reusable, distinct EPR instances");
    }

    // 地址形态：普通地址、空地址、地址覆盖
    static void testAddressForms() throws Exception {
        W3CEndpointReference plain =
            new W3CEndpointReferenceBuilder().address("http://x/y").build();
        check(write(plain).contains("http://x/y"), "plain address round-trip");
        W3CEndpointReference empty =
            new W3CEndpointReferenceBuilder().address("").build();
        String ex = write(empty);
        check(ex.contains("<Address></Address>"), "empty address yields empty Address element: " + ex);
        // 覆盖：后设地址生效
        W3CEndpointReferenceBuilder over = new W3CEndpointReferenceBuilder().address("http://one/");
        over.address("http://two/");
        check(write(over.build()).contains("http://two/"), "later address wins");
        // serviceName+endpointName 与地址共存（无 WSDL 也成立）
        W3CEndpointReference named = new W3CEndpointReferenceBuilder()
            .address("http://n").serviceName(new QName("urn:t", "svc"))
            .endpointName(new QName("urn:t", "p")).build();
        String nx = write(named);
        check(nx.contains("EndpointName=\"p\""), "EndpointName recorded in ServiceName metadata");
        check(nx.contains("wsns:svc"), "ServiceName recorded: " + nx);
    }

    // 全链：引用参数/元数据/扩展元素/扩展属性全部落位
    static void testFullChain() throws Exception {
        Element rp = e("<rk xmlns=\"urn:m\">RV</rk>");
        Element md = e("<mk xmlns=\"urn:m\">MV</mk>");
        Element ext = e("<xk xmlns=\"urn:m\">XV</xk>");
        W3CEndpointReference full = new W3CEndpointReferenceBuilder()
            .address("http://full")
            .serviceName(new QName("urn:t", "svc"))
            .endpointName(new QName("urn:t", "EchoPort"))
            .interfaceName(new QName("urn:t", "EchoPort"))
            .referenceParameter(rp)
            .metadata(md)
            .element(ext)
            .attribute(new QName("urn:t", "at"), "av")
            .build();
        String fx = write(full);
        check(fx.contains("http://full"), "full: address");
        check(fx.contains("ReferenceParameters"), "full: ReferenceParameters section");
        check(fx.contains("RV"), "full: reference parameter content");
        check(fx.contains("Metadata"), "full: Metadata section");
        check(fx.contains("MV"), "full: metadata content");
        check(fx.contains("XV"), "full: extension element present");
        check(fx.contains("at=\"av\""), "full: extension attribute written: " + fx);
        check(fx.contains(WSA), "full: WSA namespace");
        // 多个引用参数依序保留
        Element rp2 = e("<r2 xmlns=\"urn:m\">R2</r2>");
        W3CEndpointReference two = new W3CEndpointReferenceBuilder()
            .address("http://rp2").referenceParameter(rp).referenceParameter(rp2).build();
        String tx = write(two);
        check(tx.contains("RV") && tx.contains("R2"), "multiple reference parameters preserved");
    }

    // 负例：精确异常与消息（全部实测）
    static void testNegatives() {
        try {
            new W3CEndpointReferenceBuilder().build();
            check(false, "empty build must throw");
        } catch (IllegalStateException ise) {
            check("Address in an EPR cannot be null, when serviceName or portName is null"
                  .equals(ise.getMessage()), "empty build message: " + ise.getMessage());
        }
        try {
            new W3CEndpointReferenceBuilder().address(null).build();
            check(false, "address(null) must throw");
        } catch (IllegalStateException ise) {
            check(ise.getMessage().contains("Address in an EPR cannot be null"),
                  "address(null) message: " + ise.getMessage());
        }
        try {
            new W3CEndpointReferenceBuilder().serviceName(new QName("urn:t", "svc")).build();
            check(false, "serviceName without address must throw");
        } catch (IllegalStateException ise) {
            check(ise.getMessage().contains("Address in an EPR cannot be null"),
                  "svc-no-address message: " + ise.getMessage());
        }
        try {
            new W3CEndpointReferenceBuilder().address("http://a")
                .endpointName(new QName("urn:t", "p"));
            check(false, "endpointName before serviceName must throw");
        } catch (IllegalStateException ise) {
            check(ise.getMessage().equals(
                "The W3CEndpointReferenceBuilder's serviceName must be set before setting the endpointName: {urn:t}p"),
                "endpoint-before-service message: " + ise.getMessage());
        }
        try {
            new W3CEndpointReferenceBuilder().address("http://a").referenceParameter(null).build();
            check(false, "referenceParameter(null) must throw");
        } catch (IllegalArgumentException iae) {
            check("The referenceParameter cannot be null.".equals(iae.getMessage()),
                  "referenceParameter(null) message: " + iae.getMessage());
        }
        try {
            new W3CEndpointReferenceBuilder().address("http://a").metadata(null).build();
            check(false, "metadata(null) must throw");
        } catch (IllegalArgumentException iae) {
            check("The metadataElement cannot be null.".equals(iae.getMessage()),
                  "metadata(null) message: " + iae.getMessage());
        }
        try {
            new W3CEndpointReferenceBuilder().address("http://a").element(null).build();
            check(false, "element(null) must throw");
        } catch (IllegalArgumentException iae) {
            check("The extension element cannot be null.".equals(iae.getMessage()),
                  "element(null) message: " + iae.getMessage());
        }
        try {
            new W3CEndpointReferenceBuilder().address("http://a")
                .attribute(new QName("urn:t", "x"), null).build();
            check(false, "attribute(q,null) must throw");
        } catch (IllegalArgumentException iae) {
            check("The extension attribute name or value cannot be null.".equals(iae.getMessage()),
                  "attribute value-null message: " + iae.getMessage());
        }
    }

    // WSDL 支撑链：wsdlLocation 落 Metadata；服务名不符 -> ISE
    static void testWsdlBacked(String wsdlUrl) throws Exception {
        W3CEndpointReferenceBuilder wb = new W3CEndpointReferenceBuilder()
            .address("http://wsdl")
            .serviceName(new QName("urn:t", "svc"))
            .endpointName(new QName("urn:t", "EchoPort"));
        check(wb.wsdlDocumentLocation(wsdlUrl) == wb, "wsdlDocumentLocation returns this");
        W3CEndpointReference w = wb.build();
        String wx = write(w);
        check(wx.contains("http://wsdl"), "wsdl-backed address");
        check(wx.contains("?wsdl"), "wsdlLocation recorded");
        check(wx.contains("EchoPort"), "EndpointName from WSDL chain");
        // WSDL 校验负例：serviceName 与文档不符
        try {
            new W3CEndpointReferenceBuilder()
                .address("http://wsdl2")
                .serviceName(new QName("urn:t", "nosuch"))
                .wsdlDocumentLocation(wsdlUrl)
                .build();
            check(false, "wrong serviceName must throw");
        } catch (IllegalStateException ise) {
            check(ise.getMessage() != null && ise.getMessage().contains("Error in parsing WSDL"),
                  "wrong serviceName message: " + ise.getMessage());
        }
        // interfaceName 与服务链共存
        W3CEndpointReference i = new W3CEndpointReferenceBuilder()
            .address("http://iface")
            .serviceName(new QName("urn:t", "svc"))
            .endpointName(new QName("urn:t", "EchoPort"))
            .interfaceName(new QName("urn:t", "EchoPort"))
            .wsdlDocumentLocation(wsdlUrl)
            .build();
        check(write(i).contains("EchoPort"), "interfaceName chain builds fine");
    }
}

/*
 * @test
 * @summary javax.xml.ws.spi.Provider conformance: static provider lookup, full factory method face with overload counts, service delegate creation matrix, endpoint creation and W3C EPR factory over a locally served WSDL.
 * @run main ProviderTest
 */

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class ProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 本地 WSDL（回环提供，绝不由测试访问外网）
    static final String WSDL =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
        "             xmlns:tns=\"urn:t\"\n" +
        "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
        "             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
        "             targetNamespace=\"urn:t\" name=\"svc\">\n" +
        "  <types><xsd:schema targetNamespace=\"urn:t\" elementFormDefault=\"qualified\">\n" +
        "      <xsd:element name=\"echo\"><xsd:complexType><xsd:sequence>\n" +
        "        <xsd:element name=\"arg0\" type=\"xsd:string\"/></xsd:sequence></xsd:complexType></xsd:element>\n" +
        "    </xsd:schema></types>\n" +
        "  <message name=\"echo\"><part name=\"parameters\" element=\"tns:echo\"/></message>\n" +
        "  <portType name=\"EchoPort\">\n" +
        "    <operation name=\"echo\"><input message=\"tns:echo\"/></operation>\n" +
        "  </portType>\n" +
        "  <binding name=\"EchoPortBinding\" type=\"tns:EchoPort\">\n" +
        "    <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n" +
        "    <operation name=\"echo\"><soap:operation soapAction=\"\"/>\n" +
        "      <input><soap:body use=\"literal\"/></input></operation>\n" +
        "  </binding>\n" +
        "  <service name=\"svc\">\n" +
        "    <port name=\"EchoPort\" binding=\"tns:EchoPortBinding\">\n" +
        "      <soap:address location=\"http://localhost:60123/echo\"/>\n" +
        "    </port>\n" +
        "  </service>\n" +
        "</definitions>\n";

    static final String GOOD_EPR =
        "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\">"
        + "<Address>http://a</Address></EndpointReference>";

    @javax.jws.WebService(serviceName = "s2", targetNamespace = "urn:t")
    public static class Impl {
        public String m() { return "x"; }
    }

    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：JAX-WS RI 的校验消息随默认 Locale 本地化（zh_CN 下是中文），断言的是英文消息。
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        Class<?> c = Class.forName("javax.xml.ws.spi.Provider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("provider"), "has method provider");
        check(names.contains("createServiceDelegate"), "has method createServiceDelegate");
        check(names.contains("createEndpoint"), "has method createEndpoint");
        check(names.contains("createAndPublishEndpoint"), "has method createAndPublishEndpoint");
        check(names.contains("readEndpointReference"), "has method readEndpointReference");
        check(names.contains("getPort"), "has method getPort");
        check(names.contains("createW3CEndpointReference"), "has method createW3CEndpointReference");
        testClassFace(c);
        testProviderLookup();
        com.sun.net.httpserver.HttpServer server = startWsdlServer();
        try {
            String wsdlUrl = "http://localhost:" + server.getAddress().getPort() + "/x?wsdl";
            testDelegateMatrix(wsdlUrl);
            testEndpointFactories();
            testReadEndpointReference();
            testCreateW3CEndpointReference(wsdlUrl);
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

    // 类面：protected 构造器、方法重载数表、静态 provider()
    static void testClassFace(Class<?> c) throws Exception {
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly 1 declared ctor");
        check(Modifier.isProtected(ctors[0].getModifiers()), "ctor protected (subclass-only)");
        check(ctors[0].getParameterTypes().length == 0, "ctor no-arg");
        // 常量
        check(Provider.JAXWSPROVIDER_PROPERTY.equals("javax.xml.ws.spi.Provider"),
              "JAXWSPROVIDER_PROPERTY: " + Provider.JAXWSPROVIDER_PROPERTY);
        // 各方法重载数（公有方法，仅本类声明）
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (Method m : c.getMethods()) {
            if (m.getDeclaringClass() == Provider.class) {
                Integer old = counts.get(m.getName());
                counts.put(m.getName(), old == null ? 1 : old + 1);
            }
        }
        check(counts.get("createServiceDelegate") == 2, "createServiceDelegate x2: " + counts.get("createServiceDelegate"));
        check(counts.get("createEndpoint") == 3, "createEndpoint x3: " + counts.get("createEndpoint"));
        check(counts.get("createAndPublishEndpoint") == 2, "createAndPublishEndpoint x2");
        check(counts.get("createW3CEndpointReference") == 2, "createW3CEndpointReference x2");
        check(counts.get("readEndpointReference") == 1, "readEndpointReference x1");
        check(counts.get("getPort") == 1, "getPort x1");
        check(counts.get("provider") == 1, "provider x1");
        check(Modifier.isStatic(c.getMethod("provider").getModifiers()), "provider() static");
        // 抽象方法（子类必须实现）：6 个核心
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(abstractCount == 6, "6 abstract core methods: " + abstractCount);
    }

    // 静态查找：内置 RI 提供 ProviderImpl
    static void testProviderLookup() {
        Provider p1 = Provider.provider();
        check(p1 != null, "provider() non-null");
        check(p1 instanceof Provider, "lookup result is a Provider");
        Provider p2 = Provider.provider();
        check(p2 != null && p2 instanceof Provider, "repeated lookups keep returning a Provider");
        // Service.create 内部经 Provider（委托能力详见 delegate 矩阵）
        Service svc = Service.create(new QName("urn:t", "svc"));
        check(svc != null, "Service.create routes through Provider SPI");
    }

    // 委托矩阵：无 WSDL delegate 行为
    static void testDelegateMatrix(String wsdlUrl) throws Exception {
        Provider p = Provider.provider();
        QName svcName = new QName("urn:t", "svc");
        ServiceDelegate d = p.createServiceDelegate(null, svcName, Service.class);
        check(d != null, "createServiceDelegate non-null");
        check(d instanceof ServiceDelegate, "delegate is ServiceDelegate");
        check(svcName.equals(d.getServiceName()), "delegate service name round-trip");
        check(d.getWSDLDocumentLocation() == null, "no WSDL location for null URL");
        check(!d.getPorts().hasNext(), "no ports for no-WSDL service");
        check(d.getHandlerResolver() == null, "default handler resolver null");
        javax.xml.ws.handler.HandlerResolver r = new javax.xml.ws.handler.HandlerResolver() {
            public java.util.List<javax.xml.ws.handler.Handler> getHandlerChain(
                    javax.xml.ws.handler.PortInfo pi) {
                return new java.util.ArrayList<javax.xml.ws.handler.Handler>();
            }
        };
        d.setHandlerResolver(r);
        check(d.getHandlerResolver() == r, "delegate resolver identity round-trip");
        // 实测：无 WSDL delegate 的默认 executor 可为 null（lazy）；仅验证可设置/可读回
        java.util.concurrent.Executor ex = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            d.setExecutor(ex);
            check(d.getExecutor() == ex, "delegate executor identity round-trip");
            d.setExecutor(null);
            check(d.getExecutor() == null, "setExecutor(null) tolerated (null after)");
        } finally {
            ((java.util.concurrent.ExecutorService) ex).shutdown();
        }
        // 负例：null 实现类 -> NPE（实测）
        try {
            p.createServiceDelegate(null, svcName, null);
            check(false, "null implClass must throw");
        } catch (NullPointerException npe) {
            check(true, "null implClass -> NullPointerException");
        }
        // 带 WebServiceFeature 的重载存在；本 RI 不支持 Service 级特性（实测记录为异常面）
        try {
            ServiceDelegate d2 = p.createServiceDelegate(null, svcName, Service.class,
                new javax.xml.ws.RespectBindingFeature());
            check(d2 != null, "4-arg (features) overload returns delegate when accepted");
        } catch (WebServiceException wse) {
            check(wse.getMessage() != null && wse.getMessage().contains("features"),
                  "features overload surfaces unsupported-feature exception (recorded): " + wse.getMessage());
        }
        // Service.create 等价路径
        Service svc = Service.create(svcName);
        check(svcName.equals(svc.getServiceName()), "Service.create mirrors delegate service name");
    }

    // 端点工厂：createEndpoint / createAndPublishEndpoint
    static void testEndpointFactories() {
        Provider p = Provider.provider();
        Object impl = new Impl();
        Endpoint e = p.createEndpoint(SOAPBinding.SOAP11HTTP_BINDING, impl);
        check(e != null, "createEndpoint non-null");
        check(!e.isPublished(), "createEndpoint unpublished");
        check(e.getImplementor() == impl, "implementor identity");
        check(e.getBinding() != null, "binding available pre-publish");
        e.stop();
        // 非法绑定 ID -> WebServiceException（消息含 Wrong binding ID）
        try {
            p.createEndpoint("http://bogus/binding", impl);
            check(false, "bogus binding must throw");
        } catch (WebServiceException wse) {
            check(wse.getMessage() != null && wse.getMessage().contains("Wrong binding ID"),
                  "bogus binding message: " + wse.getMessage());
        }
        // 未标注实现对象：创建被容忍（发布时才校验——实测记录）
        Endpoint plain = p.createEndpoint(SOAPBinding.SOAP11HTTP_BINDING, new Object());
        check(plain != null, "plain object tolerated at create (recorded)");
        plain.stop();
        // createAndPublishEndpoint：localhost:0 发布后立即停止
        Endpoint cp = p.createAndPublishEndpoint("http://localhost:0/z", impl);
        check(cp != null, "createAndPublishEndpoint non-null");
        check(cp.isPublished(), "createAndPublishEndpoint published");
        cp.stop();
        check(!cp.isPublished(), "stopped after createAndPublish");
        // 带特性重载
        Endpoint cf = p.createEndpoint(SOAPBinding.SOAP11HTTP_BINDING, impl,
            new javax.xml.ws.soap.MTOMFeature());
        check(cf != null, "createEndpoint(features) non-null");
        cf.stop();
    }

    // readEndpointReference：合法/坏 XML/null
    static void testReadEndpointReference() {
        Provider p = Provider.provider();
        EndpointReference er = p.readEndpointReference(
            new StreamSource(new java.io.StringReader(GOOD_EPR)));
        check(er != null, "readEndpointReference non-null");
        check(er instanceof W3CEndpointReference, "yields W3CEndpointReference: " + er.getClass().getName());
        java.io.StringWriter sw = new java.io.StringWriter();
        er.writeTo(new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().contains("http://a"), "read EPR carries address");
        try {
            p.readEndpointReference(null);
            check(false, "null source must throw");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() != null && iae.getMessage().contains("must not be null"),
                  "null source message: " + iae.getMessage());
        }
        try {
            p.readEndpointReference(new StreamSource(new java.io.StringReader("<bad")));
            check(false, "malformed source must throw");
        } catch (WebServiceException wse) {
            check(true, "malformed source -> WebServiceException");
        }
    }

    // createW3CEndpointReference：最小/完整/负例
    static void testCreateW3CEndpointReference(String wsdlUrl) throws Exception {
        Provider p = Provider.provider();
        W3CEndpointReference minimal = p.createW3CEndpointReference("http://c", null, null, null, null, null);
        check(minimal != null, "6-arg minimal non-null");
        java.io.StringWriter sw = new java.io.StringWriter();
        minimal.writeTo(new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().contains("http://c"), "minimal EPR carries address");
        // 完整：svc+port+本地 WSDL
        W3CEndpointReference full = p.createW3CEndpointReference("http://c2",
            new QName("urn:t", "svc"), new QName("urn:t", "EchoPort"), null, wsdlUrl, null);
        java.io.StringWriter sw2 = new java.io.StringWriter();
        full.writeTo(new javax.xml.transform.stream.StreamResult(sw2));
        String xml = sw2.toString();
        check(xml.contains("EndpointName=\"EchoPort\""), "full EPR carries EndpointName: "
              + xml.substring(0, Math.min(220, xml.length())));
        check(xml.contains("?wsdl"), "full EPR carries wsdl location");
        // 负例：null address（svc/port 也 null）-> ISE
        try {
            p.createW3CEndpointReference(null, null, null, null, null, null);
            check(false, "null address must throw");
        } catch (IllegalStateException ise) {
            check(ise.getMessage() != null && ise.getMessage().contains("Address in an EPR cannot be null"),
                  "null address message: " + ise.getMessage());
        }
        // 9 参重载：(address, interfaceName, serviceName, portName, refParams, wsdl, metadata, extensions, attributes)
        W3CEndpointReference nine = p.createW3CEndpointReference("http://c9",
            new QName("urn:t", "EchoPort"), new QName("urn:t", "svc"),
            new QName("urn:t", "EchoPort"), null, wsdlUrl,
            new java.util.ArrayList<org.w3c.dom.Element>(),
            new java.util.ArrayList<org.w3c.dom.Element>(),
            new HashMap<QName, String>());
        check(nine != null, "9-arg overload non-null");
    }
}

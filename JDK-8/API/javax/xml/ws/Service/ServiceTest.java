/*
 * @test
 * @summary javax.xml.ws.Service conformance: WSDL service discovery closure, port proxies, dispatch, handler resolver, creation matrix.
 * @run main ServiceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.HandlerResolver;

public class ServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // SEI 必须带 @WebService(name="EchoPort", targetNamespace="urn:t")，
    // 裸标注（无 name/ns）会在 RI 内部 NPE（实测锁定）。
    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface Echo {
        @javax.jws.WebMethod
        String echo(@javax.jws.WebParam(name = "arg0") String s);
    }

    // name 与 WSDL portType 不匹配的 SEI（负例）
    @javax.jws.WebService(name = "Other", targetNamespace = "urn:t")
    public interface OtherName {
        String echo(String s);
    }

    // 完全未标注的 SEI（负例）
    public interface BareSEI {
        String echo(String s);
    }

    // 最小合法 WSDL：document/literal wrapped，targetNamespace "urn:t"，
    // service "svc" / port "EchoPort" / operation "echo"；
    // soap:address 指向本机不监听端口，仅作元数据解析，绝不发起连接。
    static final String WSDL =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
        "             xmlns:tns=\"urn:t\"\n" +
        "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
        "             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
        "             targetNamespace=\"urn:t\" name=\"svc\">\n" +
        "  <types>\n" +
        "    <xsd:schema targetNamespace=\"urn:t\" elementFormDefault=\"qualified\">\n" +
        "      <xsd:element name=\"echo\"><xsd:complexType><xsd:sequence>\n" +
        "        <xsd:element name=\"arg0\" type=\"xsd:string\"/>\n" +
        "      </xsd:sequence></xsd:complexType></xsd:element>\n" +
        "      <xsd:element name=\"echoResponse\"><xsd:complexType><xsd:sequence>\n" +
        "        <xsd:element name=\"return\" type=\"xsd:string\"/>\n" +
        "      </xsd:sequence></xsd:complexType></xsd:element>\n" +
        "    </xsd:schema>\n" +
        "  </types>\n" +
        "  <message name=\"echo\"><part name=\"parameters\" element=\"tns:echo\"/></message>\n" +
        "  <message name=\"echoResponse\"><part name=\"parameters\" element=\"tns:echoResponse\"/></message>\n" +
        "  <portType name=\"EchoPort\">\n" +
        "    <operation name=\"echo\"><input message=\"tns:echo\"/><output message=\"tns:echoResponse\"/></operation>\n" +
        "  </portType>\n" +
        "  <binding name=\"EchoPortBinding\" type=\"tns:EchoPort\">\n" +
        "    <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n" +
        "    <operation name=\"echo\"><soap:operation soapAction=\"\"/>\n" +
        "      <input><soap:body use=\"literal\"/></input>\n" +
        "      <output><soap:body use=\"literal\"/></output>\n" +
        "    </operation>\n" +
        "  </binding>\n" +
        "  <service name=\"svc\">\n" +
        "    <port name=\"EchoPort\" binding=\"tns:EchoPortBinding\">\n" +
        "      <soap:address location=\"http://localhost:60123/echo\"/>\n" +
        "    </port>\n" +
        "  </service>\n" +
        "</definitions>\n";

    static Path writeTempWsdl(String content) throws Exception {
        Path f = Files.createTempFile("jcksvc", ".wsdl");
        Files.write(f, content.getBytes(StandardCharsets.UTF_8));
        return f;
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        QName qn = new javax.xml.namespace.QName("urn:test", "HelloService");
        javax.xml.ws.Service svc = javax.xml.ws.Service.create(qn);
        check(svc.getServiceName().equals(qn), "service name");
        check(svc.getWSDLDocumentLocation() == null, "no wsdl location");
        testWsdlDiscovery();
        testBadWsdl();
        testNullCreation();
        testHandlerResolver(svc);
        testExecutor(svc);
        testAddPort();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 结构与签名面 ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Service");
        check(!Modifier.isAbstract(c.getModifiers()), "Service is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(Service.class.getConstructors().length == 0, "no public constructors (factory only)");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        String[] expect = {"getPort", "addPort", "createDispatch", "getServiceName",
                           "getPorts", "getWSDLDocumentLocation", "getHandlerResolver",
                           "setHandlerResolver", "getExecutor", "setExecutor", "create"};
        for (String e : expect) check(names.contains(e), "has method " + e);
        // 静态工厂签名面
        check(Service.class.getMethod("create", URL.class, QName.class).getReturnType() == Service.class,
              "create(URL,QName) static returns Service");
        check(Modifier.isStatic(Service.class.getMethod("create", URL.class, QName.class).getModifiers()),
              "create(URL,QName) is static");
        check(Service.class.getMethod("getPorts").getReturnType() == Iterator.class,
              "getPorts returns Iterator<QName>");
        check(Service.class.getMethod("getWSDLDocumentLocation").getReturnType() == URL.class,
              "getWSDLDocumentLocation returns URL");
        // 嵌套枚举 Service.Mode
        Class<?> mode = Class.forName("javax.xml.ws.Service$Mode");
        check(mode.isEnum(), "Service.Mode is enum");
        check(mode.getEnumConstants().length == 2, "Mode has exactly 2 constants");
        Object[] consts = mode.getEnumConstants();
        check(java.util.Arrays.asList(consts).toString().equals("[MESSAGE, PAYLOAD]"),
              "Mode constants order MESSAGE,PAYLOAD: " + java.util.Arrays.asList(consts));
        // Mode 枚举语义（valueOf/name/ordinal/compareTo）
        Service.Mode message = Service.Mode.valueOf("MESSAGE");
        Service.Mode payload = Service.Mode.valueOf("PAYLOAD");
        check(message == Service.Mode.MESSAGE, "valueOf MESSAGE identity");
        check(payload == Service.Mode.PAYLOAD, "valueOf PAYLOAD identity");
        check("MESSAGE".equals(Service.Mode.MESSAGE.name()), "MESSAGE name");
        check("PAYLOAD".equals(Service.Mode.PAYLOAD.name()), "PAYLOAD name");
        check(Service.Mode.MESSAGE.ordinal() == 0, "MESSAGE ordinal 0");
        check(Service.Mode.PAYLOAD.ordinal() == 1, "PAYLOAD ordinal 1");
        check(Service.Mode.MESSAGE.compareTo(Service.Mode.PAYLOAD) < 0, "MESSAGE < PAYLOAD");
        check(Service.Mode.PAYLOAD.compareTo(Service.Mode.MESSAGE) > 0, "PAYLOAD > MESSAGE");
        check(Service.Mode.MESSAGE.compareTo(Service.Mode.MESSAGE) == 0, "compareTo self zero");
        check(Service.Mode.values().length == 2, "values() length 2");
        check(Service.Mode.values()[0] == Service.Mode.MESSAGE && Service.Mode.values()[1] == Service.Mode.PAYLOAD,
              "values() order");
        try {
            Service.Mode.valueOf("BOGUS");
            check(false, "Mode.valueOf(unknown) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "Mode.valueOf(unknown) -> IllegalArgumentException");
        }
    }

    // ---- 核心：WSDL 服务发现闭环 ----
    static void testWsdlDiscovery() throws Exception {
        Path f = writeTempWsdl(WSDL);
        try {
            URL url = f.toUri().toURL();
            QName svcQn = new QName("urn:t", "svc");
            Service svc = Service.create(url, svcQn);
            check(svc != null, "Service.create(url,qn) non-null");
            check(svc.getServiceName().equals(svcQn), "getServiceName()=={urn:t}svc");
            check(svc.getServiceName().getNamespaceURI().equals("urn:t"), "serviceName ns");
            check(svc.getServiceName().getLocalPart().equals("svc"), "serviceName local");
            // WSDL 位置回读
            URL loc = svc.getWSDLDocumentLocation();
            check(loc != null, "getWSDLDocumentLocation non-null for WSDL-backed service");
            check(loc.toString().equals(url.toString()), "wsdl location round-trip: " + loc);
            // 端口发现
            Iterator<QName> ports = svc.getPorts();
            check(ports != null, "getPorts non-null");
            List<QName> found = new ArrayList<QName>();
            while (ports.hasNext()) found.add(ports.next());
            check(found.size() == 1, "exactly 1 port discovered: " + found);
            check(found.get(0).equals(new QName("urn:t", "EchoPort")), "port qname {urn:t}EchoPort");
            // 代理生成（不发起任何网络调用）
            QName portQn = new QName("urn:t", "EchoPort");
            Echo proxy = svc.getPort(portQn, Echo.class);
            check(proxy != null, "getPort(qn,SEI) non-null");
            check(proxy instanceof Echo, "proxy instanceof SEI");
            Echo proxy2 = svc.getPort(Echo.class);
            check(proxy2 != null, "getPort(SEI) single-arg non-null");
            check(proxy2 instanceof Echo, "single-arg proxy instanceof SEI");
            check(proxy != proxy2, "two getPort calls yield distinct proxies");
            // 代理是 BindingProvider（请求上下文已由 WSDL 填充）
            check(proxy instanceof javax.xml.ws.BindingProvider, "proxy instanceof BindingProvider");
            javax.xml.ws.BindingProvider bp = (javax.xml.ws.BindingProvider) proxy;
            Object addr = bp.getRequestContext().get(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            check("http://localhost:60123/echo".equals(addr), "request ctx addr from soap:address: " + addr);
            // 请求上下文：soapaction/session 键存在且值为 null（RI 预置形态，实测锁定）
            java.util.Map<String, Object> rc = bp.getRequestContext();
            check(rc.containsKey(javax.xml.ws.BindingProvider.SOAPACTION_USE_PROPERTY),
                  "soapaction.use key pre-seeded");
            check(rc.get(javax.xml.ws.BindingProvider.SOAPACTION_USE_PROPERTY) == null,
                  "soapaction.use default value null");
            check(rc.containsKey(javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY),
                  "soapaction.uri key pre-seeded");
            check(rc.get(javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY) == null,
                  "session.maintain default null");
            // put/get 往返（标准属性 + 自定义键 + 非 String 值）
            rc.put(javax.xml.ws.BindingProvider.USERNAME_PROPERTY, "u1");
            check("u1".equals(rc.get(javax.xml.ws.BindingProvider.USERNAME_PROPERTY)), "username put/get round-trip");
            rc.put("custom.key", Integer.valueOf(42));
            check(Integer.valueOf(42).equals(rc.get("custom.key")), "custom key round-trip non-String value");
            // Dispatch 生成（PAYLOAD 模式，不 invoke）
            Dispatch<Source> disp = svc.createDispatch(portQn, Source.class, Service.Mode.PAYLOAD);
            check(disp != null, "createDispatch(qn,Source,PAYLOAD) non-null");
            check(disp instanceof javax.xml.ws.BindingProvider, "dispatch instanceof BindingProvider");
            Dispatch<Source> disp2 = svc.createDispatch(portQn, Source.class, Service.Mode.MESSAGE);
            check(disp2 != null, "createDispatch(qn,Source,MESSAGE) non-null");
            // JAXBContext 形态 Dispatch<Object>
            javax.xml.bind.JAXBContext jctx = javax.xml.bind.JAXBContext.newInstance(String.class);
            Dispatch<Object> jdisp = svc.createDispatch(portQn, jctx, Service.Mode.PAYLOAD);
            check(jdisp != null, "createDispatch(qn,JAXBContext,PAYLOAD) non-null");
            // 同一 WSDL 重复 create 得到独立实例、同名解析
            Service again = Service.create(url, svcQn);
            check(again != svc, "two creates yield distinct instances");
            check(again.getServiceName().equals(svc.getServiceName()), "same service name resolved");
            // 未知端口名 createDispatch → 负例
            try {
                svc.createDispatch(new QName("urn:t", "NoSuchPort"), Source.class, Service.Mode.PAYLOAD);
                check(false, "createDispatch unknown port must throw");
            } catch (WebServiceException e) {
                check(true, "createDispatch unknown port -> WebServiceException");
            }
            // 端口名/服务名不匹配的精确负例（RI 消息含有效值清单，勿锁标点）
            try {
                svc.getPort(new QName("urn:t", "NoPort"), Echo.class);
                check(false, "getPort unknown port must throw");
            } catch (WebServiceException e) {
                check(e.getMessage().contains("NoPort") && e.getMessage().contains("not a valid port"),
                      "unknown port message: " + e.getMessage());
                check(e.getMessage().contains("EchoPort"), "unknown port message lists valid ports");
            }
            // 两次 getPorts 迭代器相互独立
            Iterator<QName> i1 = svc.getPorts();
            Iterator<QName> i2 = svc.getPorts();
            check(i1.hasNext() && i2.hasNext(), "both port iterators have elements");
            check(i1.next().equals(i2.next()), "independent iterators yield same port");
            try {
                java.util.Iterator<QName> fresh = svc.getPorts();
                fresh.remove();
                check(false, "port iterator remove-before-next must throw");
            } catch (IllegalStateException | UnsupportedOperationException e) {
                check(true, "port iterator remove rejected: " + e.getClass().getSimpleName());
            }
            // 带 WebServiceFeature 变参的形态
            Echo pf = svc.getPort(portQn, Echo.class, new javax.xml.ws.WebServiceFeature[0]);
            check(pf instanceof Echo, "getPort(qn,SEI,features) proxy");
            Dispatch<Source> df = svc.createDispatch(portQn, Source.class, Service.Mode.PAYLOAD,
                                                     new javax.xml.ws.WebServiceFeature[0]);
            check(df != null, "createDispatch with empty feature vararg");
            Service s3 = Service.create(url, svcQn, new javax.xml.ws.WebServiceFeature[0]);
            check(s3.getServiceName().equals(svcQn), "create(url,qn,features) name kept");
            // 代理为 JDK 动态代理（java.lang.reflect.Proxy 家族）
            check(java.lang.reflect.Proxy.isProxyClass(proxy.getClass()),
                  "port proxy is a dynamic proxy");
            // WSDL 服务名错配 → 精确负例消息
            try {
                Service.create(url, new QName("urn:t", "nosuch"));
                check(false, "wrong service name must throw");
            } catch (WebServiceException e) {
                check(e.getMessage().contains("nosuch") && e.getMessage().contains("not a valid service"),
                      "wrong service message: " + e.getMessage());
                check(e.getMessage().contains("svc"), "wrong service message lists valid services");
            }
            // SEI name 与 WSDL portType 不匹配 → 负例
            try {
                svc.getPort(OtherName.class);
                check(false, "mismatched SEI name must throw");
            } catch (WebServiceException e) {
                check(e.getMessage().contains("Undefined port type"),
                      "mismatched SEI message: " + e.getMessage());
            }
            // 裸 SEI（无 @WebService）→ RuntimeException（RI 为 RuntimeModelerException）
            try {
                svc.getPort(BareSEI.class);
                check(false, "bare SEI must throw");
            } catch (RuntimeException e) {
                check(e.getMessage() != null && e.getMessage().contains("WebService annotation"),
                      "bare SEI message mentions annotation: " + e.getMessage());
            }
        } finally {
            Files.deleteIfExists(f);
        }
    }

    // ---- 负例：垃圾 WSDL ----
    static void testBadWsdl() throws Exception {
        Path f = writeTempWsdl("this is not xml at all {{{");
        try {
            try {
                Service.create(f.toUri().toURL(), new QName("urn:t", "svc"));
                check(false, "garbage wsdl must throw");
            } catch (RuntimeException e) {
                // RI 为 XMLStreamReaderException extends JAXWSExceptionBase，只锁 RuntimeException + 消息非空
                check(e.getMessage() != null && !e.getMessage().isEmpty(),
                      "garbage wsdl exception message non-empty");
            }
        } finally {
            Files.deleteIfExists(f);
        }
    }

    // ---- 创建矩阵（null 参数/单参形态/无 WSDL 服务） ----
    static void testNullCreation() throws Exception {
        // create(url, null) -> WebServiceException（消息含 "not a valid service"，实测）
        URL anyUrl = new java.io.File("/tmp/whatever.wsdl").toURI().toURL();
        try {
            Service.create(anyUrl, null);
            check(false, "create(url,null) must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("not a valid service"),
                  "create(url,null) message contains 'not a valid service': " + e.getMessage());
        }
        // create(null, qn) -> 不抛（空服务：无 WSDL、无端口）
        Service empty = Service.create((URL) null, new QName("urn:t", "svc"));
        check(empty != null, "create(null,qn) non-null (no-throw contract)");
        check(empty.getWSDLDocumentLocation() == null, "empty service wsdl location null");
        Iterator<QName> it = empty.getPorts();
        check(it != null, "empty service getPorts non-null iterator");
        check(!it.hasNext(), "empty service has no ports");
        check(empty.getServiceName().equals(new QName("urn:t", "svc")), "empty service name kept");
        // create(QName) 单参：同一无 WSDL 形态
        Service s2 = Service.create(new QName("urn:t", "svc"));
        check(s2.getServiceName().equals(new QName("urn:t", "svc")), "create(QName) name");
        check(s2.getWSDLDocumentLocation() == null, "create(QName) wsdl null");
        check(!s2.getPorts().hasNext(), "create(QName) no ports");
        // create(QName) null -> WebServiceException
        try {
            Service.create((QName) null);
            check(false, "create(null QName) must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("not a valid service"),
                  "create(null QName) message: " + e.getMessage());
        }
        // 不存在的 WSDL URL -> RuntimeException（RI：IOException 包装）
        Path ghost = java.nio.file.Paths.get("/tmp/jck-ghost-does-not-exist.wsdl");
        try {
            Service.create(ghost.toUri().toURL(), new QName("urn:t", "svc"));
            check(false, "nonexistent wsdl url must throw");
        } catch (RuntimeException e) {
            check(e.getMessage() == null || !e.getMessage().isEmpty(), "nonexistent wsdl message shape");
        }
        // getPort 对无 WSDL 服务（SEI 也无 WSDL 信息）-> WebServiceException（实测）
        try {
            empty.getPort(Echo.class);
            check(false, "getPort on no-wsdl service must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("WSDL"),
                  "getPort no-wsdl message mentions WSDL: " + e.getMessage());
        }
    }

    // ---- HandlerResolver：默认 null + 同一实例回读 ----
    static void testHandlerResolver(Service svc) {
        check(svc.getHandlerResolver() == null, "default handler resolver null (RI contract)");
        HandlerResolver recording = new HandlerResolver() {
            @Override
            public java.util.List<javax.xml.ws.handler.Handler> getHandlerChain(
                    javax.xml.ws.handler.PortInfo info) {
                return new ArrayList<javax.xml.ws.handler.Handler>();
            }
        };
        svc.setHandlerResolver(recording);
        check(svc.getHandlerResolver() == recording, "setHandlerResolver same instance read back");
        // setHandlerResolver(null) 不抛，回读仍 null
        svc.setHandlerResolver(null);
        check(svc.getHandlerResolver() == null, "setHandlerResolver(null) accepted");
        // HandlerResolver 接口结构
        check(HandlerResolver.class.isInterface(), "HandlerResolver is interface");
        try {
            Method m = HandlerResolver.class.getMethod("getHandlerChain", javax.xml.ws.handler.PortInfo.class);
            check(m.getReturnType() == java.util.List.class, "getHandlerChain returns List<Handler>");
            // 自造实现回调闭环
            javax.xml.ws.handler.PortInfo pi = new javax.xml.ws.handler.PortInfo() {
                @Override public QName getPortName() { return new QName("urn:t", "EchoPort"); }
                @Override public QName getServiceName() { return new QName("urn:t", "svc"); }
                @Override public String getBindingID() { return "http://schemas.xmlsoap.org/wsdl/soap/http"; }
            };
            check(pi.getPortName().equals(new QName("urn:t", "EchoPort")), "PortInfo portName read back");
            check(pi.getServiceName().equals(new QName("urn:t", "svc")), "PortInfo serviceName read back");
            check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(pi.getBindingID()), "PortInfo bindingID read back");
            check(recording.getHandlerChain(pi).isEmpty(), "custom resolver returns empty chain");
        } catch (NoSuchMethodException e) {
            check(false, "HandlerResolver.getHandlerChain signature missing");
        }
    }

    // ---- Executor ----
    static void testExecutor(Service svc) {
        check(svc.getExecutor() == null, "default executor null (uses default provider executor)");
        java.util.concurrent.Executor ex = java.util.concurrent.Executors.newSingleThreadExecutor(
            new java.util.concurrent.ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "jck-svc-ex");
                    t.setDaemon(true);
                    return t;
                }
            });
        svc.setExecutor(ex);
        check(svc.getExecutor() == ex, "setExecutor same instance read back");
        svc.setExecutor(null);
        check(svc.getExecutor() == null, "setExecutor(null) accepted");
    }

    // ---- addPort：动态端口注册后可发现、可 Dispatch ----
    static void testAddPort() {
        Service svc = Service.create(new QName("urn:t", "svc"));
        QName dynamic = new QName("urn:t", "DynPort");
        svc.addPort(dynamic, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:60123/dyn");
        List<QName> found = new ArrayList<QName>();
        Iterator<QName> it = svc.getPorts();
        while (it.hasNext()) found.add(it.next());
        check(found.contains(dynamic), "added port discoverable via getPorts");
        Dispatch<Source> d = svc.createDispatch(dynamic, Source.class, Service.Mode.PAYLOAD);
        check(d != null, "createDispatch on added port (no WSDL needed)");
        check("http://localhost:60123/dyn".equals(
                  d.getRequestContext().get(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
              "dispatch addr from addPort");
    }
}

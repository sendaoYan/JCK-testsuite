/*
 * @test
 * @summary javax.xml.ws.Dispatch conformance: generic interface shape, four invoke signatures, real dispatch creation without network calls.
 * @run main DispatchTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

public class DispatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.jws.WebService(name = "EchoPort", targetNamespace = "urn:t")
    public interface Echo {
        String echo(String s);
    }

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

    public static void main(String[] args) throws Exception {
        testStructure();
        testSignatures();
        testRealDispatch();
        testCustomImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.Dispatch");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.BindingProvider"), "implements javax.xml.ws.BindingProvider");
        check(c.getInterfaces().length == 1, "single super-interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("invoke"), "has method invoke");
        check(names.contains("invokeAsync"), "has method invokeAsync");
        check(names.contains("invokeOneWay"), "has method invokeOneWay");
        // 泛型接口：Dispatch<T> 有一个类型参数
        check(c.getTypeParameters().length == 1, "generic with one type parameter T");
        check("T".equals(c.getTypeParameters()[0].getName()), "type parameter named T");
    }

    // ---- 四个调用形态的精确签名（不发起任何调用） ----
    static void testSignatures() throws Exception {
        Class<?> c = javax.xml.ws.Dispatch.class;
        Method sync = c.getMethod("invoke", Object.class);
        check(sync.getReturnType() == Object.class, "invoke(T) returns T (erasure Object)");
        check(!Modifier.isStatic(sync.getModifiers()), "invoke is instance method");
        Method async1 = c.getMethod("invokeAsync", Object.class);
        check(async1.getReturnType() == Response.class, "invokeAsync(T) returns Response<T>");
        Method async2 = c.getMethod("invokeAsync", Object.class, javax.xml.ws.AsyncHandler.class);
        check(async2.getReturnType() == java.util.concurrent.Future.class,
              "invokeAsync(T,AsyncHandler) returns Future<?>");
        Method oneway = c.getMethod("invokeOneWay", Object.class);
        check(oneway.getReturnType() == void.class, "invokeOneWay returns void");
        // 精确 4 个自身声明方法
        Method[] own = c.getDeclaredMethods();
        check(own.length == 4, "exactly 4 declared methods: " + own.length);
        // BindingProvider 契约面继承可用
        check(BindingProvider.class.isAssignableFrom(c), "Dispatch assignable to BindingProvider");
        // 抽象性：接口方法默认 public abstract
        for (Method m : own) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
            check(Modifier.isPublic(m.getModifiers()), "public: " + m.getName());
        }
    }

    // ---- 真实 Dispatch 创建（WSDL 支撑 + addPort 支撑，绝不 invoke） ----
    static void testRealDispatch() throws Exception {
        Path f = Files.createTempFile("jckdisp", ".wsdl");
        try {
            Files.write(f, WSDL.getBytes(StandardCharsets.UTF_8));
            URL url = f.toUri().toURL();
            Service svc = Service.create(url, new QName("urn:t", "svc"));
            QName portQn = new QName("urn:t", "EchoPort");
            // PAYLOAD / MESSAGE 两种消息模式均可创建（源 <T>：Source/DataSource/JAXBContext 支撑类型）
            Dispatch<javax.xml.transform.Source> pay =
                svc.createDispatch(portQn, javax.xml.transform.Source.class, Service.Mode.PAYLOAD);
            check(pay != null, "createDispatch(Source,PAYLOAD) non-null");
            check(pay instanceof Dispatch, "PAYLOAD dispatch is Dispatch");
            check(pay instanceof BindingProvider, "PAYLOAD dispatch is BindingProvider");
            Dispatch<javax.xml.transform.Source> msg =
                svc.createDispatch(portQn, javax.xml.transform.Source.class, Service.Mode.MESSAGE);
            check(msg != null, "createDispatch(Source,MESSAGE) non-null");
            check(pay != msg, "two dispatches are distinct objects");
            // 请求上下文（继承自 BindingProvider）可写读
            pay.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:60123/d1");
            check("http://localhost:60123/d1".equals(
                      pay.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
                  "dispatch request context round-trip");
            check("http://schemas.xmlsoap.org/wsdl/soap/http".equals(pay.getBinding().getBindingID()),
                  "dispatch binding id SOAP 1.1");
            // SAAJ 形态（SOAPMessage）创建
            Dispatch<javax.xml.soap.SOAPMessage> saaj =
                svc.createDispatch(portQn, javax.xml.soap.SOAPMessage.class, Service.Mode.MESSAGE);
            check(saaj != null, "createDispatch(SOAPMessage,MESSAGE) non-null");
            // JAXBContext 形态（Dispatch<Object>）
            javax.xml.bind.JAXBContext jctx = javax.xml.bind.JAXBContext.newInstance(String.class);
            Dispatch<Object> jaxb = svc.createDispatch(portQn, jctx, Service.Mode.PAYLOAD);
            check(jaxb != null, "createDispatch(JAXBContext,PAYLOAD) non-null");
            // feature 变参形态
            Dispatch<javax.xml.transform.Source> feat =
                svc.createDispatch(portQn, javax.xml.transform.Source.class, Service.Mode.PAYLOAD,
                                   new javax.xml.ws.WebServiceFeature[0]);
            check(feat != null, "createDispatch with empty feature vararg");
            // EndpointReference 形态存在（签名层；行为在 spi/wsaddressing 批次覆盖）
            try {
                Method m = Service.class.getMethod("createDispatch",
                        javax.xml.ws.EndpointReference.class, Class.class, Service.Mode.class,
                        javax.xml.ws.WebServiceFeature[].class);
                check(m != null, "createDispatch(EPR,Class,Mode,features) signature present");
            } catch (NoSuchMethodException e) {
                check(false, "EPR createDispatch signature missing");
            }
            // 未知端口负例
            try {
                svc.createDispatch(new QName("urn:t", "Ghost"), javax.xml.transform.Source.class,
                                   Service.Mode.PAYLOAD);
                check(false, "createDispatch unknown port must throw");
            } catch (javax.xml.ws.WebServiceException e) {
                check(e.getMessage() != null && e.getMessage().contains("Ghost"),
                      "unknown port message names ghost port");
            }
        } finally {
            Files.deleteIfExists(f);
        }
        // 无 WSDL 服务 + addPort 也能创建 Dispatch（binding+address 已由 addPort 给出）
        Service bare = Service.create(new QName("urn:t", "svc"));
        bare.addPort(new QName("urn:t", "P1"), "http://schemas.xmlsoap.org/wsdl/soap/http",
                     "http://localhost:60123/p1");
        Dispatch<javax.xml.transform.Source> d =
            bare.createDispatch(new QName("urn:t", "P1"), javax.xml.transform.Source.class,
                                Service.Mode.PAYLOAD);
        check(d != null, "createDispatch on addPort'd port (no WSDL)");
        check("http://localhost:60123/p1".equals(
                  d.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
              "addPort dispatch addr seeded");
    }

    // ---- 自造 Dispatch 实现记录调用（回调契约，无网络） ----
    static final java.util.List<String> CALLS = new java.util.ArrayList<String>();

    static final class RecordingDispatch implements Dispatch<String> {
        @Override public String invoke(String msg) {
            CALLS.add("invoke:" + msg);
            return "ack:" + msg;
        }
        @Override public Response<String> invokeAsync(String msg) {
            CALLS.add("async:" + msg);
            final java.util.concurrent.FutureTask<String> ft =
                new java.util.concurrent.FutureTask<String>(new java.util.concurrent.Callable<String>() {
                    @Override public String call() { return "async-done"; }
                });
            ft.run(); // 立即完成，保证 get() 非阻塞（测试不真异步）
            return new Response<String>() {
                @Override public boolean cancel(boolean may) { return ft.cancel(may); }
                @Override public boolean isCancelled() { return ft.isCancelled(); }
                @Override public boolean isDone() { return ft.isDone(); }
                @Override public String get() { try { return ft.get(); } catch (Exception e) { throw new RuntimeException(e); } }
                @Override public String get(long t, java.util.concurrent.TimeUnit u) {
                    try { return ft.get(t, u); } catch (Exception e) { throw new RuntimeException(e); }
                }
                @Override public java.util.Map<String, Object> getContext() {
                    return new java.util.HashMap<String, Object>();
                }
            };
        }
        @Override public java.util.concurrent.Future<?> invokeAsync(String msg, javax.xml.ws.AsyncHandler<String> h) {
            CALLS.add("asyncH:" + msg);
            return java.util.concurrent.CompletableFuture.completedFuture("ignored");
        }
        @Override public void invokeOneWay(String msg) { CALLS.add("oneway:" + msg); }
        @Override public java.util.Map<String, Object> getRequestContext() {
            return new java.util.HashMap<String, Object>();
        }
        @Override public java.util.Map<String, Object> getResponseContext() {
            return new java.util.HashMap<String, Object>();
        }
        @Override public javax.xml.ws.Binding getBinding() { return null; }
        @Override public javax.xml.ws.EndpointReference getEndpointReference() { return null; }
        @Override public <T extends javax.xml.ws.EndpointReference> T getEndpointReference(Class<T> t) { return null; }
    }

    static void testCustomImpl() throws Exception {
        RecordingDispatch rd = new RecordingDispatch();
        check("ack:m1".equals(rd.invoke("m1")), "custom dispatch invoke round-trip");
        Response<String> r = rd.invokeAsync("m2");
        check(r != null, "custom invokeAsync returns Response");
        check(r.getContext() != null, "custom Response context non-null");
        rd.invokeAsync("m3", new javax.xml.ws.AsyncHandler<String>() {
            @Override public void handleResponse(Response<String> res) { }
        });
        rd.invokeOneWay("m4");
        check(CALLS.size() == 4, "all four invoke forms recorded");
        check(CALLS.get(0).equals("invoke:m1"), "invoke recorded first");
        check(CALLS.get(1).equals("async:m2"), "async recorded second");
        check(CALLS.get(2).equals("asyncH:m3"), "async+handler recorded third");
        check(CALLS.get(3).equals("oneway:m4"), "oneway recorded fourth");
        // invokeAsync 的 Response get()（已完成任务，非阻塞）
        check("async-done".equals(r.get()), "Response.get returns completed value");
        check(r.isDone(), "Response.isDone true");
        check(!r.isCancelled(), "Response.isCancelled false");
    }
}

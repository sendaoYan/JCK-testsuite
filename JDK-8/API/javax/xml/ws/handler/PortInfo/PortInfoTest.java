/*
 * @test
 * @summary javax.xml.ws.handler.PortInfo conformance: interface face, self-implementation triple round-trip, runtime PortInfo delivered to HandlerResolver for SOAP 1.1 and SOAP 1.2 ports.
 * @run main PortInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;


public class PortInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static String srcToString(Source s) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(s, new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class EchoProvider implements Provider<Source> {
        public Source invoke(Source req) {
            return new StreamSource(new java.io.StringReader("<r xmlns=\"urn:t\">ok</r>"));
        }
    }

    static PortInfo captured;

    static class CaptureResolver implements HandlerResolver {
        public List<Handler> getHandlerChain(PortInfo portInfo) {
            captured = portInfo;
            List<Handler> l = new ArrayList<Handler>();
            l.add(new LogicalHandler<LogicalMessageContext>() {
                public boolean handleMessage(LogicalMessageContext ctx) { return true; }
                public boolean handleFault(LogicalMessageContext ctx) { return true; }
                public void close(MessageContext ctx) { }
            });
            return l;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.PortInfo");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getServiceName"), "has method getServiceName");
        check(names.contains("getPortName"), "has method getPortName");
        check(names.contains("getBindingID"), "has method getBindingID");
        testInterfaceFace(c);
        testSelfImplementation();
        testRuntimePortInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 接口面：恰 3 个方法（svc/port QName + bindingID String），无继承接口
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 3, "exactly 3 declared methods: " + dm.length);
        for (Method m : dm) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
            check(m.getParameterTypes().length == 0, m.getName() + " no args");
        }
        check(c.getMethod("getServiceName").getReturnType() == QName.class, "getServiceName returns QName");
        check(c.getMethod("getPortName").getReturnType() == QName.class, "getPortName returns QName");
        check(c.getMethod("getBindingID").getReturnType() == String.class, "getBindingID returns String");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        // getHandlerChain 的形参类型恰为 PortInfo
        check(HandlerResolver.class.getMethod("getHandlerChain", PortInfo.class) != null,
              "HandlerResolver.getHandlerChain takes PortInfo");
    }

    // 自造实现：三元组回读
    static void testSelfImplementation() {
        final QName svc = new QName("urn:t", "svc");
        final QName port = new QName("urn:t", "p");
        final String binding = SOAPBinding.SOAP11HTTP_BINDING;
        PortInfo pi = new PortInfo() {
            public QName getServiceName() { return svc; }
            public QName getPortName() { return port; }
            public String getBindingID() { return binding; }
        };
        check(svc.equals(pi.getServiceName()), "self service name round-trip");
        check(port.equals(pi.getPortName()), "self port name round-trip");
        check(binding.equals(pi.getBindingID()), "self binding id round-trip");
        // null 三元组也可表达（接口无约束）
        PortInfo nulls = new PortInfo() {
            public QName getServiceName() { return null; }
            public QName getPortName() { return null; }
            public String getBindingID() { return null; }
        };
        check(nulls.getServiceName() == null && nulls.getPortName() == null && nulls.getBindingID() == null,
              "self null triple expressible");
        // 三元组不同的另一个实例
        PortInfo other = new PortInfo() {
            public QName getServiceName() { return new QName("urn:t", "svc"); }
            public QName getPortName() { return port; }
            public String getBindingID() { return SOAPBinding.SOAP12HTTP_BINDING; }
        };
        check(other.getServiceName().equals(pi.getServiceName()), "equal service names");
        check(!other.getBindingID().equals(pi.getBindingID()), "different binding ids");
    }

    // 真实通道：运行时交付给 resolver 的 PortInfo（SOAP 1.1 与 SOAP 1.2 两个端口）
    static void testRuntimePortInfo() throws Exception {
        com.sun.net.httpserver.HttpServer server =
            com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        int port = server.getAddress().getPort();
        Endpoint ep = Endpoint.create(new EchoProvider());
        server.start();
        ep.publish(server.createContext("/ws"));
        try {
            QName svcName = new QName("urn:t", "svc");
            QName port11 = new QName("urn:t", "p11");
            QName port12 = new QName("urn:t", "p12");
            Service svc = Service.create(svcName);
            CaptureResolver r = new CaptureResolver();
            svc.setHandlerResolver(r);
            svc.addPort(port11, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            svc.addPort(port12, SOAPBinding.SOAP12HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d11 = svc.createDispatch(port11, Source.class, Service.Mode.PAYLOAD);
            check(captured != null, "PortInfo captured for SOAP 1.1 port");
            check(svcName.equals(captured.getServiceName()),
                  "runtime PortInfo service name: " + captured.getServiceName());
            check(port11.equals(captured.getPortName()),
                  "runtime PortInfo port name: " + captured.getPortName());
            check(SOAPBinding.SOAP11HTTP_BINDING.equals(captured.getBindingID()),
                  "runtime PortInfo binding id SOAP 1.1: " + captured.getBindingID());
            String resp = srcToString(d11.invoke(new StreamSource(new java.io.StringReader(
                "<q xmlns=\"urn:t\">1</q>"))));
            check(resp.contains("ok"), "SOAP 1.1 dispatch through captured port works");
            check(captured.getServiceName() != null, "PortInfo still populated after invoke");

            Dispatch<Source> d12 = svc.createDispatch(port12, Source.class, Service.Mode.PAYLOAD);
            check(port12.equals(captured.getPortName()),
                  "PortInfo switches to SOAP 1.2 port: " + captured.getPortName());
            check(SOAPBinding.SOAP12HTTP_BINDING.equals(captured.getBindingID()),
                  "PortInfo binding id reflects SOAP 1.2: " + captured.getBindingID());
            check(svcName.equals(captured.getServiceName()),
                  "service name unchanged across ports: " + captured.getServiceName());
            // 注：端点按 SOAP 1.1 发布，1.2 请求会 415；此处仅验证 PortInfo 元数据，不发起 1.2 调用
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}

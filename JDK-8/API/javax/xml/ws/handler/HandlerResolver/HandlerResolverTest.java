/*
 * @test
 * @summary javax.xml.ws.handler.HandlerResolver conformance: interface face, Service resolver registration identity, real resolver consultation with PortInfo and chain order on a live dispatch.
 * @run main HandlerResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
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


public class HandlerResolverTest {
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

    static int providerCalls;

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class EchoProvider implements Provider<Source> {
        public Source invoke(Source req) {
            providerCalls++;
            return new StreamSource(new java.io.StringReader("<r xmlns=\"urn:t\">ok</r>"));
        }
    }

    static int resolverCalls;
    static PortInfo lastPortInfo;
    static List<String> order = new ArrayList<String>();
    static boolean useEmptyChain;

    static class RecordingLogical implements LogicalHandler<LogicalMessageContext> {
        public boolean handleMessage(LogicalMessageContext ctx) { order.add("LH"); return true; }
        public boolean handleFault(LogicalMessageContext ctx) { order.add("LH-fault"); return true; }
        public void close(MessageContext ctx) { order.add("LH-close"); }
    }

    static class RecordingSoap implements javax.xml.ws.handler.soap.SOAPHandler<javax.xml.ws.handler.soap.SOAPMessageContext> {
        public java.util.Set<QName> getHeaders() { return new java.util.HashSet<QName>(); }
        public boolean handleMessage(javax.xml.ws.handler.soap.SOAPMessageContext ctx) { order.add("SH"); return true; }
        public boolean handleFault(javax.xml.ws.handler.soap.SOAPMessageContext ctx) { order.add("SH-fault"); return true; }
        public void close(MessageContext ctx) { order.add("SH-close"); }
    }

    static class MyResolver implements HandlerResolver {
        public List<Handler> getHandlerChain(PortInfo portInfo) {
            resolverCalls++;
            lastPortInfo = portInfo;
            if (useEmptyChain) return new ArrayList<Handler>();
            List<Handler> l = new ArrayList<Handler>();
            l.add(new RecordingLogical());
            l.add(new RecordingSoap());
            return l;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.HandlerResolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getHandlerChain"), "has method getHandlerChain");
        testInterfaceFace(c);
        testServiceRegistration();
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 接口面：恰 1 个声明方法，签名 (PortInfo) -> List<Handler>
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 1, "exactly 1 declared method: " + dm.length);
        Method m = dm[0];
        check(Modifier.isAbstract(m.getModifiers()), "getHandlerChain abstract");
        check(Modifier.isPublic(m.getModifiers()), "getHandlerChain public");
        check(m.getReturnType() == java.util.List.class, "returns java.util.List");
        check(m.getParameterTypes().length == 1, "single parameter");
        check(m.getParameterTypes()[0] == PortInfo.class, "parameter is PortInfo");
        // 直接以接口引用调用自造实现（空链 / 双 handler 链）
        HandlerResolver empty = new HandlerResolver() {
            public List<Handler> getHandlerChain(PortInfo pi) { return Collections.<Handler>emptyList(); }
        };
        check(empty.getHandlerChain(null).isEmpty(), "self resolver can return empty list");
        HandlerResolver two = new MyResolver();
        List<Handler> chain = two.getHandlerChain(null);
        check(chain.size() == 2, "self resolver returns 2 handlers");
        check(chain.get(0) instanceof Handler && chain.get(1) instanceof Handler,
              "chain elements are Handlers");
        // 返回的 List 由调用方消费，允许不可变
        HandlerResolver unmod = new HandlerResolver() {
            public List<Handler> getHandlerChain(PortInfo pi) {
                return Collections.<Handler>unmodifiableList(new ArrayList<Handler>());
            }
        };
        check(unmod.getHandlerChain(null).isEmpty(), "unmodifiable empty chain is legal");
    }

    // Service 注册语义：默认 null，设置后回读同一实例
    static void testServiceRegistration() {
        Service svc = Service.create(new QName("urn:t", "svc"));
        check(svc.getHandlerResolver() == null, "default handler resolver is null (no WSDL service)");
        MyResolver r = new MyResolver();
        svc.setHandlerResolver(r);
        check(svc.getHandlerResolver() == r, "setHandlerResolver/getHandlerResolver identity");
        // 允许重置为 null
        svc.setHandlerResolver(null);
        check(svc.getHandlerResolver() == null, "setHandlerResolver(null) clears resolver");
        svc.setHandlerResolver(r);
        check(svc.getHandlerResolver() == r, "re-set resolver identity");
        // 不同 Service 实例互不影响
        Service other = Service.create(new QName("urn:t", "svc"));
        check(other.getHandlerResolver() == null, "second service unaffected by first resolver");
    }

    // 真实通道：resolver 在 Dispatch 创建时被咨询恰一次，PortInfo 为运行时真实实例
    static void testRealChannel() throws Exception {
        com.sun.net.httpserver.HttpServer server =
            com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        int port = server.getAddress().getPort();
        Endpoint ep = Endpoint.create(new EchoProvider());
        server.start();
        ep.publish(server.createContext("/ws"));
        try {
            QName svcName = new QName("urn:t", "svc");
            QName portName = new QName("urn:t", "p");
            Service svc = Service.create(svcName);
            MyResolver r = new MyResolver();
            svc.setHandlerResolver(r);
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            // 实测：RI 在 addPort 即咨询 resolver（链随绑定构建）
            int afterAddPort = resolverCalls;
            check(afterAddPort >= 1, "resolver consulted at port/binding creation: " + afterAddPort);
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            check(resolverCalls >= afterAddPort, "dispatch creation does not reduce consultations");
            int afterCreate = resolverCalls;
            check(lastPortInfo != null, "resolver receives a real PortInfo");
            check(lastPortInfo.getServiceName().equals(svcName), "PortInfo service name matches service QName");
            check(lastPortInfo.getPortName().equals(portName), "PortInfo port name matches port QName");
            check(SOAPBinding.SOAP11HTTP_BINDING.equals(lastPortInfo.getBindingID()),
                  "PortInfo binding id is SOAP 1.1 HTTP binding");

            order.clear();
            String resp = srcToString(d.invoke(new StreamSource(new java.io.StringReader("<q xmlns=\"urn:t\">1</q>"))));
            check(resp.contains("ok"), "invoke through resolver-provided chain succeeds");
            check(resolverCalls == afterCreate, "resolver NOT re-consulted per invoke");
            check(providerCalls == 1, "provider hit once");
            // 顺序：出站 LH->SH，入站 SH->LH
            check(order.size() == 6, "chain order log has 4 msg + 2 close entries: " + order);
            check(order.get(0).equals("LH") && order.get(1).equals("SH"), "outbound order logical->soap");
            check(order.get(2).equals("SH") && order.get(3).equals("LH"), "inbound order soap->logical");
            check(order.get(4).equals("SH-close") || order.get(5).equals("SH-close"), "soap close in tail");
            // 链可经 Binding 再观察
            check(d.getBinding().getHandlerChain().size() == 2, "binding handler chain has 2 entries");

            // 空链 resolver：无 handler 运行，invoke 仍成功
            useEmptyChain = true;
            Service svc2 = Service.create(svcName);
            svc2.setHandlerResolver(r);
            svc2.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d2 = svc2.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            check(resolverCalls > afterCreate, "resolver consulted again for the new service/dispatch");
            check(d2.getBinding().getHandlerChain().isEmpty(), "empty resolver yields empty chain");
            order.clear();
            String resp2 = srcToString(d2.invoke(new StreamSource(new java.io.StringReader("<q xmlns=\"urn:t\">2</q>"))));
            check(resp2.contains("ok"), "invoke with empty chain succeeds");
            check(order.isEmpty(), "no handler callbacks with empty chain");
            useEmptyChain = false;
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}

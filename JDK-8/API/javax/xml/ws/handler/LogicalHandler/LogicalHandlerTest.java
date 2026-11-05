/*
 * @test
 * @summary javax.xml.ws.handler.LogicalHandler conformance: Handler subinterface bound to LogicalMessageContext, marker face, self implementation and real chain instantiation.
 * @run main LogicalHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.LogicalMessage;
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


public class LogicalHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 自造 LogicalMessageContext（payload 保存为 Source） ----
    static class FakeLogicalMessageContext extends HashMap<String, Object> implements LogicalMessageContext {
        final Source payload;
        FakeLogicalMessageContext(Source payload) { this.payload = payload; }
        public LogicalMessage getMessage() {
            return new LogicalMessage() {
                public Source getPayload() { return payload; }
                public Object getPayload(javax.xml.bind.JAXBContext ctx) { return payload; }
                public void setPayload(Source s) { }
                public void setPayload(Object o, javax.xml.bind.JAXBContext ctx) { }
            };
        }
        public void setScope(String key, Scope scope) { }
        public Scope getScope(String key) { return Scope.HANDLER; }
    }

    static class MyLogicalHandler implements LogicalHandler<LogicalMessageContext> {
        int msgCalls, faultCalls, closeCalls;
        Object lastContext;
        Object lastMessage;
        List<Boolean> outboundFlags = new ArrayList<Boolean>();
        public boolean handleMessage(LogicalMessageContext ctx) {
            msgCalls++;
            lastContext = ctx;
            lastMessage = ctx.getMessage();
            outboundFlags.add((Boolean) ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
            return true;
        }
        public boolean handleFault(LogicalMessageContext ctx) { faultCalls++; return true; }
        public void close(MessageContext ctx) { closeCalls++; }
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

    static MyLogicalHandler realHandler;

    static class Resolver implements HandlerResolver {
        public List<Handler> getHandlerChain(PortInfo pi) {
            List<Handler> l = new ArrayList<Handler>();
            realHandler = new MyLogicalHandler();
            l.add(realHandler);
            return l;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.LogicalHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.Handler"), "implements javax.xml.ws.handler.Handler");
        testInterfaceFace(c);
        testSelfImplementation();
        testRealChain();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 接口面：零声明方法的标记接口，类型参数上界 LogicalMessageContext
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 0, "marker interface: no own methods: " + dm.length);
        check(c.getInterfaces().length == 1, "exactly one superinterface");
        check(c.getInterfaces()[0] == javax.xml.ws.handler.Handler.class,
              "direct superinterface is Handler");
        // 泛型上界：C extends LogicalMessageContext
        java.lang.reflect.TypeVariable<?>[] tv = c.getTypeParameters();
        check(tv.length == 1, "one type variable");
        check(tv[0].getName().equals("C"), "type variable named C");
        check(tv[0].getBounds().length == 1
              && tv[0].getBounds()[0] == LogicalMessageContext.class,
              "C bounded by LogicalMessageContext");
        // 继承而来的三个 Handler 方法（擦除后形参为 MessageContext）
        check(c.getMethod("handleMessage", MessageContext.class).getReturnType() == boolean.class,
              "inherited handleMessage returns boolean");
        check(c.getMethod("handleFault", MessageContext.class).getReturnType() == boolean.class,
              "inherited handleFault returns boolean");
        check(c.getMethod("close", MessageContext.class).getReturnType() == void.class,
              "inherited close(MessageContext) returns void");
        // 不含 SOAP 家族方法
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(!names.contains("getHeaders"), "no getHeaders on LogicalHandler");
    }

    // 自造实现：回调与 LogicalMessage 可回读
    static void testSelfImplementation() throws Exception {
        MyLogicalHandler h = new MyLogicalHandler();
        Source payload = new StreamSource(new java.io.StringReader("<p xmlns=\"urn:t\">x</p>"));
        FakeLogicalMessageContext mc = new FakeLogicalMessageContext(payload);
        mc.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);
        check(h.handleMessage(mc) == true, "self handleMessage returns true");
        check(h.msgCalls == 1, "handleMessage recorded");
        check(h.lastContext == mc, "context instance recorded");
        check(h.lastMessage instanceof LogicalMessage, "getMessage visible through context");
        check(srcToString(((LogicalMessage) h.lastMessage).getPayload()).contains("<p "),
              "payload source round-trips through self context");
        check(h.outboundFlags.get(0) == Boolean.TRUE, "outbound flag read from context map");
        check(h.handleFault(mc) == true, "self handleFault returns true");
        check(h.faultCalls == 1, "handleFault recorded");
        h.close(mc);
        check(h.closeCalls == 1, "close recorded");
        // LogicalHandler 可赋给 Handler<LogicalMessageContext> 与原始 Handler
        Handler<LogicalMessageContext> typed = h;
        Handler<?> raw = h;
        check(typed != null && raw != null, "assignable to Handler (generic and raw)");
    }

    // 真实链路：LogicalHandler 收到运行时真实 LogicalMessageContext
    static void testRealChain() throws Exception {
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
            svc.setHandlerResolver(new Resolver());
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            String resp = srcToString(d.invoke(new StreamSource(new java.io.StringReader("<q xmlns=\"urn:t\">1</q>"))));
            check(resp.contains("ok"), "invoke with logical handler succeeds");
            check(providerCalls == 1, "provider hit once");
            check(realHandler.msgCalls == 2, "logical handler ran for request and response");
            check(realHandler.lastContext instanceof LogicalMessageContext,
                  "runtime passes a real LogicalMessageContext");
            check(((LogicalMessageContext) realHandler.lastContext).getMessage() != null,
                  "real LogicalMessage obtainable");
            check(realHandler.outboundFlags.size() == 2, "outbound flag observed twice");
            check(realHandler.outboundFlags.get(0) == Boolean.TRUE, "request direction outbound=true");
            check(realHandler.outboundFlags.get(1) == Boolean.FALSE, "response direction outbound=false");
            check(realHandler.closeCalls == 1, "close called once");
            // 载荷可在入站分支内改写（第二方向读到改写值）
            check(realHandler.lastMessage != null, "last LogicalMessage retained after invoke");
            check(srcToString(d.invoke(new StreamSource(new java.io.StringReader(
                "<q xmlns=\"urn:t\">2</q>")))).contains("ok"), "second invoke through chain");
            check(providerCalls == 2, "provider hit again on second invoke");
            check(realHandler.closeCalls == 2, "close called per invoke (chain torn down)");
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}

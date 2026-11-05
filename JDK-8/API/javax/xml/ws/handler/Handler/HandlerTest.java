/*
 * @test
 * @summary javax.xml.ws.handler.Handler conformance: interface face, generic handler lifecycle (handleMessage/handleFault/close), false-return blocking semantics and fault path on a real provider endpoint.
 * @run main HandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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


public class HandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // ---- 自造最小 MessageContext（HashMap 支撑，scope 按规范默认 HANDLER） ----
    static class FakeMessageContext extends HashMap<String, Object> implements MessageContext {
        private final Map<String, Scope> scopes = new HashMap<String, Scope>();
        public void setScope(String key, Scope scope) {
            if (!containsKey(key)) throw new IllegalArgumentException("Property " + key + " does not exist.");
            scopes.put(key, scope);
        }
        public Scope getScope(String key) {
            if (!containsKey(key)) throw new IllegalArgumentException("Property " + key + " does not exist.");
            return scopes.containsKey(key) ? scopes.get(key) : Scope.HANDLER;
        }
    }

    // ---- 自造泛型 Handler：记录生命周期调用 ----
    static class RecordingHandler implements Handler<MessageContext> {
        int msgCalls, faultCalls, closeCalls;
        List<Object> seenContexts = new ArrayList<Object>();
        boolean returnTrue = true;
        public boolean handleMessage(MessageContext ctx) {
            msgCalls++;
            seenContexts.add(ctx);
            return returnTrue;
        }
        public boolean handleFault(MessageContext ctx) {
            faultCalls++;
            return true;
        }
        public void close(MessageContext ctx) {
            closeCalls++;
        }
    }

    // ---- 真实闭环：Provider 端点（PAYLOAD 模式） ----
    static int providerCalls;
    static String providerSaw;

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class EchoProvider implements Provider<Source> {
        public Source invoke(Source req) {
            providerCalls++;
            providerSaw = srcToString(req);
            if (providerThrow) throw new RuntimeException("boom");
            return new StreamSource(new java.io.StringReader("<r xmlns=\"urn:t\">ok</r>"));
        }
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

    static RecordingHandler logical;
    static RecordingHandler soap;
    static boolean providerThrow;

    static class Resolver implements HandlerResolver {
        public List<Handler> getHandlerChain(PortInfo pi) {
            List<Handler> l = new ArrayList<Handler>();
            logical = new RecordingHandler();
            soap = new RecordingHandler();
            l.add(new LHAdapter());
            l.add(new SHAdapter());
            return l;
        }
    }

    static class LHAdapter implements LogicalHandler<LogicalMessageContext> {
        public boolean handleMessage(LogicalMessageContext ctx) { return logical.handleMessage(ctx); }
        public boolean handleFault(LogicalMessageContext ctx) { return logical.handleFault(ctx); }
        public void close(MessageContext ctx) { logical.close(ctx); }
    }

    static class SHAdapter implements javax.xml.ws.handler.soap.SOAPHandler<javax.xml.ws.handler.soap.SOAPMessageContext> {
        public java.util.Set<QName> getHeaders() { return new java.util.HashSet<QName>(); }
        public boolean handleMessage(javax.xml.ws.handler.soap.SOAPMessageContext ctx) { return soap.handleMessage(ctx); }
        public boolean handleFault(javax.xml.ws.handler.soap.SOAPMessageContext ctx) { return soap.handleFault(ctx); }
        public void close(MessageContext ctx) { soap.close(ctx); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.Handler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("handleMessage"), "has method handleMessage");
        check(names.contains("handleFault"), "has method handleFault");
        check(names.contains("close"), "has method close");
        testInterfaceFace(c);
        testSelfImplementation();
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 接口方法面：恰 3 个声明方法，全部 public abstract
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 3, "exactly 3 declared methods: " + dm.length);
        int abstractCount = 0;
        for (Method m : dm) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        }
        check(abstractCount == 3, "all 3 methods abstract");
        Method hm = c.getMethod("handleMessage", MessageContext.class);
        check(hm.getReturnType() == boolean.class, "handleMessage returns boolean");
        check(hm.getParameterTypes().length == 1, "handleMessage single param");
        Method hf = c.getMethod("handleFault", MessageContext.class);
        check(hf.getReturnType() == boolean.class, "handleFault returns boolean");
        Method cl = c.getMethod("close", MessageContext.class);
        check(cl.getReturnType() == void.class, "close returns void");
        check(cl.getParameterTypes()[0] == MessageContext.class, "close takes raw MessageContext");
        // 类型参数 C 上界为 MessageContext
        java.lang.reflect.TypeVariable<?>[] tv = c.getTypeParameters();
        check(tv.length == 1, "one type variable C");
        check(tv[0].getName().equals("C"), "type variable named C");
        check(tv[0].getBounds().length == 1
              && tv[0].getBounds()[0] == MessageContext.class,
              "C bounded by MessageContext");
        // getHeaders 不在 Handler 上（归 SOAPHandler）
        check(!names(c).contains("getHeaders"), "getHeaders not on Handler (SOAPHandler only)");
    }

    static java.util.Set<String> names(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) s.add(m.getName());
        return s;
    }

    // 自造实现：三个回调行为可回读
    static void testSelfImplementation() {
        RecordingHandler h = new RecordingHandler();
        FakeMessageContext mc = new FakeMessageContext();
        mc.put("k", "v");
        check(h.handleMessage(mc) == true, "self handleMessage returns configured value");
        check(h.msgCalls == 1, "self handleMessage recorded");
        check(h.seenContexts.get(0) == mc, "self handler receives exact context instance");
        check(h.handleFault(mc) == true, "self handleFault returns true");
        check(h.faultCalls == 1, "self handleFault recorded");
        h.close(mc);
        check(h.closeCalls == 1, "self close recorded");
        check(mc.size() == 1, "close has no side effect on context");
        // 返回 false 合法（返回值由实现决定）
        RecordingHandler f = new RecordingHandler();
        f.returnTrue = false;
        check(f.handleMessage(mc) == false, "self handleMessage can return false");
        // Handler<MessageContext> 可作为原始 Handler 使用
        Handler<?> raw = h;
        check(raw != null, "generic handler assignable to raw Handler");
    }

    // 真实通道：localhost:0 Provider 端点 + Dispatch + handler 链
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
            svc.setHandlerResolver(new Resolver());
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);

            // 正常一次 invoke：请求/响应各触发一次 handleMessage，close 恰一次
            String resp = srcToString(d.invoke(new StreamSource(new java.io.StringReader("<q xmlns=\"urn:t\">1</q>"))));
            check(resp.contains("ok"), "invoke succeeds with handlers in chain");
            check(providerCalls == 1, "provider invoked exactly once");
            check(logical.msgCalls == 2, "logical handleMessage called for request+response: " + logical.msgCalls);
            check(soap.msgCalls == 2, "soap handleMessage called for request+response: " + soap.msgCalls);
            check(logical.closeCalls == 1, "logical close exactly once per invoke");
            check(soap.closeCalls == 1, "soap close exactly once per invoke");
            check(logical.faultCalls == 0, "no handleFault on success");
            check(soap.faultCalls == 0, "no soap handleFault on success");
            check(logical.seenContexts.get(0) instanceof MessageContext, "handler sees MessageContext instance");

            // 服务端抛错：客户端收 SOAPFaultException，handleFault+close 被调
            providerThrow = true;
            try {
                d.invoke(new StreamSource(new java.io.StringReader("<q xmlns=\"urn:t\">2</q>")));
                check(false, "provider fault must propagate");
            } catch (javax.xml.ws.soap.SOAPFaultException sfe) {
                check(sfe.getFault() != null, "fault path yields SOAPFaultException with fault");
                check(sfe.getFault().getFaultString() != null
                      && sfe.getFault().getFaultString().contains("boom"),
                      "fault string carries server cause");
            }
            providerThrow = false;
            check(logical.faultCalls == 1, "logical handleFault invoked once on fault");
            check(soap.faultCalls == 1, "soap handleFault invoked once on fault");
            check(logical.closeCalls == 2, "close called again after fault path");

            // handleMessage 返回 false（出站）：阻断后续 handler 与网络发送，方向反转
            final AtomicInteger beforeProvider = new AtomicInteger(providerCalls);
            logical.returnTrue = false;
            String r3 = srcToString(d.invoke(new StreamSource(new java.io.StringReader("<q xmlns=\"urn:t\">3</q>"))));
            check(r3.contains("urn:t"), "false-return invoke returns a message");
            check(providerCalls == beforeProvider.get(), "provider NOT called when handler returns false");
            check(logical.closeCalls == 3, "close still called after false-return");
        } finally {
            ep.stop();
            server.stop(0);
        }
        check(!ep.isPublished(), "endpoint stopped after closure");
    }
}

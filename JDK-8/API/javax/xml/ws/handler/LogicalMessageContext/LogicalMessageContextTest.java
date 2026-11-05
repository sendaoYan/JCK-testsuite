/*
 * @test
 * @summary javax.xml.ws.handler.LogicalMessageContext conformance: MessageContext subinterface, LogicalMessage payload access (Source and JAXB forms) with in-place payload rewrite visible to provider and client.
 * @run main LogicalMessageContextTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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


public class LogicalMessageContextTest {
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
            StringWriter sw = new StringWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(s, new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 注意：根元素在命名空间 urn:t 下，子字段必须显式声明 namespace 才能解回
    @XmlRootElement(name = "res", namespace = "urn:t")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Res {
        @XmlElement(name = "value", namespace = "urn:t")
        String value;
    }

    static int providerCalls;
    static String providerSaw;

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class EchoProvider implements Provider<Source> {
        public Source invoke(Source req) {
            providerCalls++;
            providerSaw = srcToString(req);
            return new StreamSource(new StringReader("<res xmlns=\"urn:t\"><value>pong</value></res>"));
        }
    }

    static LogicalMessageContext outboundCtx, inboundCtx;

    static Object inboundBean;

    static class MyHandler implements LogicalHandler<LogicalMessageContext> {
        public boolean handleMessage(LogicalMessageContext ctx) {
            if (Boolean.TRUE.equals(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
                outboundCtx = ctx;
                try {
                    ctx.getMessage().setPayload(new StreamSource(new StringReader(
                        "<replaced xmlns=\"urn:t\"><value>from-handler</value></replaced>")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                inboundCtx = ctx;
                try {
                    JAXBContext jc = JAXBContext.newInstance(Res.class);
                    inboundBean = ctx.getMessage().getPayload(jc);
                    // 入站 setPayload(bean, ctx) 改写：客户端将收到改写后的响应
                    Res replace = new Res();
                    replace.value = "client-sees-this";
                    ctx.getMessage().setPayload(replace, jc);
                    Object again = ctx.getMessage().getPayload(jc);
                    check(again instanceof Res && "client-sees-this".equals(((Res) again).value),
                          "inbound setPayload(Object) then getPayload(JAXBContext) round-trips");
                    check(inboundBean instanceof Res && "pong".equals(((Res) inboundBean).value),
                          "inbound payload decoded as JAXB bean (original provider value)");
                    check(inboundCtx == ctx, "same context instance both directions (reused)");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        public boolean handleFault(LogicalMessageContext ctx) { return true; }
        public void close(MessageContext ctx) { }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.LogicalMessageContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.MessageContext"), "implements javax.xml.ws.handler.MessageContext");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMessage"), "has method getMessage");
        testInterfaceFace(c);
        testLogicalMessageFace();
        testSelfImplementation();
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 接口面：恰 1 个声明方法 getMessage() 返回 LogicalMessage
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 1, "exactly 1 declared method: " + dm.length);
        Method gm = dm[0];
        check(gm.getName().equals("getMessage"), "declared method is getMessage");
        check(gm.getReturnType() == LogicalMessage.class, "getMessage returns LogicalMessage");
        check(gm.getParameterTypes().length == 0, "getMessage takes no args");
        check(Modifier.isAbstract(gm.getModifiers()), "getMessage abstract");
        // 继承 Map 与 scope 语义
        check(c.getMethod("setScope", String.class, MessageContext.Scope.class).getReturnType() == void.class,
              "inherits setScope");
        check(c.getMethod("getScope", String.class).getReturnType() == MessageContext.Scope.class,
              "inherits getScope");
        check(c.getMethod("put", Object.class, Object.class) != null, "inherits Map.put");
    }

    // LogicalMessage 方法面：4 个 payload 访问形态
    static void testLogicalMessageFace() throws Exception {
        Method[] ms = LogicalMessage.class.getMethods();
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) names.add(m.getName());
        check(names.contains("getPayload"), "LogicalMessage.getPayload");
        check(names.contains("setPayload"), "LogicalMessage.setPayload");
        check(LogicalMessage.class.getMethod("getPayload").getReturnType() == Source.class,
              "getPayload() returns Source");
        check(LogicalMessage.class.getMethod("getPayload", JAXBContext.class).getReturnType() == Object.class,
              "getPayload(JAXBContext) returns Object");
        check(LogicalMessage.class.getMethod("setPayload", Source.class).getReturnType() == void.class,
              "setPayload(Source) void");
        check(LogicalMessage.class.getMethod("setPayload", Object.class, JAXBContext.class).getReturnType() == void.class,
              "setPayload(Object,JAXBContext) void");
        check(LogicalMessage.class.isInterface(), "LogicalMessage is an interface");
    }

    // 自造实现：getMessage 与 payload Source 闭环
    static class FakeLogicalMessageContext extends HashMap<String, Object> implements LogicalMessageContext {
        Source payload = new StreamSource(new StringReader("<p xmlns=\"urn:t\">seed</p>"));
        public LogicalMessage getMessage() {
            return new LogicalMessage() {
                public Source getPayload() { return payload; }
                public Object getPayload(JAXBContext ctx) { return null; }
                public void setPayload(Source s) { payload = s; }
                public void setPayload(Object o, JAXBContext ctx) { }
            };
        }
        public void setScope(String key, Scope scope) { }
        public Scope getScope(String key) { return Scope.HANDLER; }
    }

    static void testSelfImplementation() {
        FakeLogicalMessageContext mc = new FakeLogicalMessageContext();
        LogicalMessage lm = mc.getMessage();
        check(lm != null, "self getMessage non-null");
        check(srcToString(lm.getPayload()).contains("seed"), "self payload seed readable");
        mc.getMessage().setPayload(new StreamSource(new StringReader("<q xmlns=\"urn:t\">new</q>")));
        check(srcToString(mc.getMessage().getPayload()).contains("new"),
              "setPayload replaces subsequent getPayload");
        mc.put("k", "v");
        check("v".equals(mc.get("k")), "Map put/get through LogicalMessageContext");
    }

    // 真实通道：出站改写 provider 可见；入站 JAXB 解出 bean；再改写客户端可见
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
            svc.setHandlerResolver(new HandlerResolver() {
                public List<Handler> getHandlerChain(PortInfo pi) {
                    List<Handler> l = new ArrayList<Handler>();
                    l.add(new MyHandler());
                    return l;
                }
            });
            svc.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost:" + port + "/ws");
            Dispatch<Source> d = svc.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
            String resp = srcToString(d.invoke(new StreamSource(new StringReader(
                "<q xmlns=\"urn:t\"><arg0>orig</arg0></q>"))));
            check(resp.contains("client-sees-this"), "invoke round-trips (rewritten response): " + resp);
            check(providerCalls == 1, "provider hit once");
            check(providerSaw.contains("from-handler"),
                  "outbound setPayload(Source) reached provider payload: " + providerSaw);
            check(outboundCtx instanceof LogicalMessageContext, "outbound context captured");
            check(outboundCtx.getMessage() != null, "outbound LogicalMessage non-null");
            // 出站 payload 原文可读
            // 入站：getPayload(JAXBContext) 解出 Res bean
            check(inboundCtx != null, "inbound context captured");
            check(inboundBean instanceof Res, "inbound getPayload(JAXBContext) returns Res bean: " + inboundBean);
            check("pong".equals(((Res) inboundBean).value), "bean field equals server value");
            check(resp.contains("client-sees-this"),
                  "inbound setPayload(Object) rewrite is what the client received: " + resp);
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}

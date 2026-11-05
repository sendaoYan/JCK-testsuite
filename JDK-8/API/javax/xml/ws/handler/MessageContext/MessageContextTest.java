/*
 * @test
 * @summary javax.xml.ws.handler.MessageContext conformance: full standard property constant table, Map contract, scope semantics (default HANDLER, APPLICATION switch, absent key) on a self implementation and on a live handler context.
 * @run main MessageContextTest
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


public class MessageContextTest {
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

    static MessageContext outboundCtx, inboundCtx;
    static Object flagOutbound, flagInbound;
    static final Map<String, Object> snapOut = new HashMap<String, Object>();
    static final Map<String, Object> snapIn = new HashMap<String, Object>();
    static final String[] SNAP_KEYS = {
        MessageContext.WSDL_SERVICE, MessageContext.WSDL_PORT, MessageContext.WSDL_INTERFACE,
        MessageContext.WSDL_OPERATION, MessageContext.WSDL_DESCRIPTION,
        MessageContext.HTTP_RESPONSE_CODE, MessageContext.HTTP_RESPONSE_HEADERS,
        MessageContext.REFERENCE_PARAMETERS, MessageContext.SERVLET_REQUEST,
        MessageContext.SERVLET_RESPONSE, MessageContext.SERVLET_CONTEXT,
        MessageContext.QUERY_STRING, MessageContext.PATH_INFO,
        MessageContext.INBOUND_MESSAGE_ATTACHMENTS
    };

    static class MyHandler implements LogicalHandler<LogicalMessageContext> {
        public boolean handleMessage(LogicalMessageContext ctx) {
            // 属性值必须在回调内快照：上下文实例在两个方向间复用并清空
            Map<String, Object> snap;
            if (Boolean.TRUE.equals(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
                outboundCtx = ctx;
                flagOutbound = ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                snap = snapOut;
            } else {
                inboundCtx = ctx;
                flagInbound = ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                snap = snapIn;
            }
            for (String k : SNAP_KEYS) snap.put(k, ctx.get(k));
            return true;
        }
        public boolean handleFault(LogicalMessageContext ctx) { return true; }
        public void close(MessageContext ctx) { }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.handler.MessageContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.util.Map"), "implements java.util.Map");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setScope"), "has method setScope");
        check(names.contains("getScope"), "has method getScope");
        testConstants(c);
        testInterfaceFace(c);
        testSelfImplementation();
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 常量面：18 个标准属性串逐串全等（javap 实测清单）
    static void testConstants(Class<?> c) throws Exception {
        check(MessageContext.MESSAGE_OUTBOUND_PROPERTY.equals("javax.xml.ws.handler.message.outbound"),
              "MESSAGE_OUTBOUND_PROPERTY");
        check(MessageContext.INBOUND_MESSAGE_ATTACHMENTS.equals("javax.xml.ws.binding.attachments.inbound"),
              "INBOUND_MESSAGE_ATTACHMENTS");
        check(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS.equals("javax.xml.ws.binding.attachments.outbound"),
              "OUTBOUND_MESSAGE_ATTACHMENTS");
        check(MessageContext.WSDL_DESCRIPTION.equals("javax.xml.ws.wsdl.description"), "WSDL_DESCRIPTION");
        check(MessageContext.WSDL_SERVICE.equals("javax.xml.ws.wsdl.service"), "WSDL_SERVICE");
        check(MessageContext.WSDL_PORT.equals("javax.xml.ws.wsdl.port"), "WSDL_PORT");
        check(MessageContext.WSDL_INTERFACE.equals("javax.xml.ws.wsdl.interface"), "WSDL_INTERFACE");
        check(MessageContext.WSDL_OPERATION.equals("javax.xml.ws.wsdl.operation"), "WSDL_OPERATION");
        check(MessageContext.HTTP_RESPONSE_CODE.equals("javax.xml.ws.http.response.code"), "HTTP_RESPONSE_CODE");
        check(MessageContext.HTTP_REQUEST_HEADERS.equals("javax.xml.ws.http.request.headers"), "HTTP_REQUEST_HEADERS");
        check(MessageContext.HTTP_RESPONSE_HEADERS.equals("javax.xml.ws.http.response.headers"), "HTTP_RESPONSE_HEADERS");
        check(MessageContext.HTTP_REQUEST_METHOD.equals("javax.xml.ws.http.request.method"), "HTTP_REQUEST_METHOD");
        check(MessageContext.SERVLET_REQUEST.equals("javax.xml.ws.servlet.request"), "SERVLET_REQUEST");
        check(MessageContext.SERVLET_RESPONSE.equals("javax.xml.ws.servlet.response"), "SERVLET_RESPONSE");
        check(MessageContext.SERVLET_CONTEXT.equals("javax.xml.ws.servlet.context"), "SERVLET_CONTEXT");
        check(MessageContext.QUERY_STRING.equals("javax.xml.ws.http.request.querystring"), "QUERY_STRING");
        check(MessageContext.PATH_INFO.equals("javax.xml.ws.http.request.pathinfo"), "PATH_INFO");
        check(MessageContext.REFERENCE_PARAMETERS.equals("javax.xml.ws.reference.parameters"), "REFERENCE_PARAMETERS");
        // 常量字段：public static final String，恰 18 个
        int n = 0;
        for (java.lang.reflect.Field f : c.getFields()) {
            if (f.getType() == String.class && Modifier.isStatic(f.getModifiers())) n++;
        }
        check(n == 18, "exactly 18 static String constants: " + n);
        // 两两互异
        String[] all = {
            MessageContext.MESSAGE_OUTBOUND_PROPERTY, MessageContext.INBOUND_MESSAGE_ATTACHMENTS,
            MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, MessageContext.WSDL_DESCRIPTION,
            MessageContext.WSDL_SERVICE, MessageContext.WSDL_PORT, MessageContext.WSDL_INTERFACE,
            MessageContext.WSDL_OPERATION, MessageContext.HTTP_RESPONSE_CODE,
            MessageContext.HTTP_REQUEST_HEADERS, MessageContext.HTTP_RESPONSE_HEADERS,
            MessageContext.HTTP_REQUEST_METHOD, MessageContext.SERVLET_REQUEST,
            MessageContext.SERVLET_RESPONSE, MessageContext.SERVLET_CONTEXT,
            MessageContext.QUERY_STRING, MessageContext.PATH_INFO,
            MessageContext.REFERENCE_PARAMETERS
        };
        // 上面数组 18 项（含 HTTP_REQUEST_METHOD），补充互异校验
        java.util.Set<String> distinct = new java.util.HashSet<String>();
        for (String s : all) distinct.add(s);
        check(distinct.size() == 18, "all 18 constant strings pairwise distinct: " + distinct.size());
    }

    // 接口面：两个 scope 方法签名
    static void testInterfaceFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 2, "exactly 2 declared methods: " + dm.length);
        Method ss = c.getMethod("setScope", String.class, MessageContext.Scope.class);
        check(ss.getReturnType() == void.class, "setScope returns void");
        check(Modifier.isAbstract(ss.getModifiers()), "setScope abstract");
        Method gs = c.getMethod("getScope", String.class);
        check(gs.getReturnType() == MessageContext.Scope.class, "getScope returns Scope");
        check(Modifier.isAbstract(gs.getModifiers()), "getScope abstract");
        // 原始类型：Map<String,Object>
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == java.util.Map.class,
              "superinterface is java.util.Map");
    }

    // 自造实现：规范 scope 语义（默认 HANDLER / APPLICATION 切换 / 未定义 key IAE）
    static class SpecMessageContext extends HashMap<String, Object> implements MessageContext {
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

    static void testSelfImplementation() {
        SpecMessageContext mc = new SpecMessageContext();
        mc.put("k1", "v1");
        check("v1".equals(mc.get("k1")), "Map put/get round-trip");
        check(mc.getScope("k1") == MessageContext.Scope.HANDLER, "default scope is HANDLER");
        mc.setScope("k1", MessageContext.Scope.APPLICATION);
        check(mc.getScope("k1") == MessageContext.Scope.APPLICATION, "setScope(APPLICATION) round-trip");
        mc.setScope("k1", MessageContext.Scope.HANDLER);
        check(mc.getScope("k1") == MessageContext.Scope.HANDLER, "setScope back to HANDLER");
        // 未 put 的 key
        try {
            mc.getScope("absent");
            check(false, "getScope of absent key must throw");
        } catch (IllegalArgumentException e) {
            check(true, "getScope of absent key -> IllegalArgumentException");
        }
        try {
            mc.setScope("absent", MessageContext.Scope.APPLICATION);
            check(false, "setScope of absent key must throw");
        } catch (IllegalArgumentException e) {
            check(true, "setScope of absent key -> IllegalArgumentException");
        }
        // remove 后 scope 亦不可见
        mc.remove("k1");
        try {
            mc.getScope("k1");
            check(false, "getScope after remove must throw");
        } catch (IllegalArgumentException e) {
            check(true, "getScope after remove -> IllegalArgumentException");
        }
        // Map 全套契约
        mc.put("a", "1");
        mc.put("b", null);
        check(mc.containsKey("a") && mc.containsValue(null), "containsKey/containsValue with null value");
        check("1".equals(mc.put("a", "2")), "put returns old value");
        check(mc.size() >= 2, "size reflects entries");
        check(mc.keySet().contains("b"), "keySet view");
        check(mc.remove("b") == null, "remove of null-valued key returns null");
        check(!mc.containsKey("b"), "removed key gone");
        // null key 按 HashMap 语义允许
        mc.put(null, "nv");
        check("nv".equals(mc.get(null)), "null key stored (HashMap semantics)");
        mc.clear();
        check(mc.isEmpty(), "clear empties context");
    }

    // 真实通道：运行时上下文中的标准属性与 scope 语义
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
            String resp = srcToString(d.invoke(new StreamSource(new java.io.StringReader("<q xmlns=\"urn:t\">1</q>"))));
            check(resp.contains("ok"), "invoke succeeds");

            // 方向标志：出站 true / 入站 false（回调内捕获）
            check(Boolean.TRUE.equals(flagOutbound), "outbound callback saw message.outbound=true");
            check(Boolean.FALSE.equals(flagInbound), "inbound callback saw message.outbound=false");

            // WSDL 族属性：由 Service 元数据填充
            check(svcName.equals(snapOut.get(MessageContext.WSDL_SERVICE)),
                  "WSDL_SERVICE equals service QName: " + snapOut.get(MessageContext.WSDL_SERVICE));
            // 实测：无 WSDL 的 dispatch 上下文只知道 service QName，port 名不可得
            check(snapOut.get(MessageContext.WSDL_PORT) == null,
                  "WSDL_PORT null for no-WSDL dispatch (service-only metadata): " + snapOut.get(MessageContext.WSDL_PORT));
            check(snapOut.get(MessageContext.WSDL_OPERATION) == null,
                  "WSDL_OPERATION null for dispatch (no WSDL operation metadata)");
            check(snapOut.get(MessageContext.WSDL_INTERFACE) == null,
                  "WSDL_INTERFACE null for dispatch");
            check(snapOut.get(MessageContext.WSDL_DESCRIPTION) == null,
                  "WSDL_DESCRIPTION null for no-WSDL service");

            // HTTP 属性：响应侧填充
            check(Integer.valueOf(200).equals(snapIn.get(MessageContext.HTTP_RESPONSE_CODE)),
                  "HTTP_RESPONSE_CODE == 200 inbound: " + snapIn.get(MessageContext.HTTP_RESPONSE_CODE));
            Object rh = snapIn.get(MessageContext.HTTP_RESPONSE_HEADERS);
            check(rh instanceof Map && !((Map<?, ?>) rh).isEmpty(),
                  "HTTP_RESPONSE_HEADERS non-empty map inbound");
            Object rp = snapIn.get(MessageContext.REFERENCE_PARAMETERS);
            check(rp != null, "REFERENCE_PARAMETERS present inbound");

            // 非 HTTP/Servlet 环境不出现的属性
            check(snapOut.get(MessageContext.SERVLET_REQUEST) == null, "SERVLET_REQUEST absent on client");
            check(snapOut.get(MessageContext.SERVLET_RESPONSE) == null, "SERVLET_RESPONSE absent on client");
            check(snapOut.get(MessageContext.SERVLET_CONTEXT) == null, "SERVLET_CONTEXT absent on client");
            check(snapOut.get(MessageContext.QUERY_STRING) == null, "QUERY_STRING absent on client");
            check(snapOut.get(MessageContext.PATH_INFO) == null, "PATH_INFO absent on client");
            check(snapIn.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS) == null
                  || snapIn.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS) instanceof Map,
                  "INBOUND_MESSAGE_ATTACHMENTS absent or a Map when no attachments (recorded)");

            // 运行时上下文的 Map/scope 语义（实测 RI）
            inboundCtx.put("my.key", "my.value");
            check("my.value".equals(inboundCtx.get("my.key")), "put/get on live context");
            check(inboundCtx.getScope("my.key") == MessageContext.Scope.HANDLER,
                  "live default scope HANDLER");
            inboundCtx.setScope("my.key", MessageContext.Scope.APPLICATION);
            check(inboundCtx.getScope("my.key") == MessageContext.Scope.APPLICATION,
                  "live setScope(APPLICATION) round-trip");
            try {
                inboundCtx.getScope("no.such.key");
                check(false, "live getScope absent must throw");
            } catch (IllegalArgumentException e) {
                check(true, "live getScope absent -> IllegalArgumentException (message: " + e.getMessage() + ")");
            }
            try {
                inboundCtx.setScope(null, MessageContext.Scope.APPLICATION);
                check(false, "live setScope(null key) must throw");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null && e.getMessage().contains("null"),
                      "live setScope(null key) -> IllegalArgumentException mentioning null (RI)");
            }
            // null scope：本 RI 容忍不抛（记录实测）
            boolean threw = false;
            try {
                inboundCtx.setScope("my.key", null);
            } catch (Exception e) {
                threw = true;
            }
            check(!threw, "live setScope(key, null) tolerated by this RI (recorded)");
        } finally {
            ep.stop();
            server.stop(0);
        }
    }
}

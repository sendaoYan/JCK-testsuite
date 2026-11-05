/*
 * @test
 * @summary javax.xml.ws.spi.http.HttpHandler conformance: abstract class hierarchy and methods, self handler dispatch, and the real RI handler installed by Endpoint.publish(HttpContext) driven through an in-process exchange.
 * @run main HttpHandlerTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.spi.http.HttpExchange;
import javax.xml.ws.spi.http.HttpHandler;


public class HttpHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @WebServiceProvider(serviceName = "svc", targetNamespace = "urn:t", portName = "p")
    @ServiceMode(value = Service.Mode.PAYLOAD)
    public static class EchoProvider implements Provider<Source> {
        public Source invoke(Source req) {
            return new javax.xml.transform.stream.StreamSource(
                new java.io.StringReader("<r xmlns=\"urn:t\">ok</r>"));
        }
    }

    // 自造上下文 + 交换（驱动 handle 用）
    static class MyContext extends HttpContext {
        final Map<String, Object> attrs = new HashMap<String, Object>();
        HttpHandler h;
        public String getPath() { return "/t"; }
        public Object getAttribute(String name) { return attrs.get(name); }
        public Set<String> getAttributeNames() { return attrs.keySet(); }
        public void setHandler(HttpHandler handler) { h = handler; }
    }

    static class SimpleExchange extends HttpExchange {
        final Map<String, List<String>> reqHeaders =
            new java.util.TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        final Map<String, List<String>> resHeaders =
            new java.util.TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        final Map<String, Object> attrs = new HashMap<String, Object>();
        final ByteArrayOutputStream resBody = new ByteArrayOutputStream();
        final byte[] reqBody;
        int status = -1;
        boolean closed;

        SimpleExchange(byte[] body) { this.reqBody = body; }

        public Map<String, List<String>> getRequestHeaders() { return reqHeaders; }
        public String getRequestHeader(String name) {
            List<String> l = reqHeaders.get(name);
            return l == null || l.isEmpty() ? null : l.get(0);
        }
        public Map<String, List<String>> getResponseHeaders() { return resHeaders; }
        public String getRequestURI() { return "/t"; }
        public String getContextPath() { return "/t"; }
        public String getRequestMethod() { return "POST"; }
        public HttpContext getHttpContext() { return Holder.ctx; }
        public void close() { closed = true; }
        public java.io.InputStream getRequestBody() { return new ByteArrayInputStream(reqBody); }
        public java.io.OutputStream getResponseBody() { return resBody; }
        public void setStatus(int s) { status = s; }
        public java.net.InetSocketAddress getRemoteAddress() {
            return new java.net.InetSocketAddress("localhost", 11111);
        }
        public java.net.InetSocketAddress getLocalAddress() {
            return new java.net.InetSocketAddress("localhost", 22222);
        }
        public String getProtocol() { return "HTTP/1.1"; }
        public String getScheme() { return "http"; }
        public String getPathInfo() { return null; }
        public String getQueryString() { return null; }
        public Object getAttribute(String name) { return attrs.get(name); }
        public Set<String> getAttributeNames() { return attrs.keySet(); }
        public java.security.Principal getUserPrincipal() { return null; }
        public boolean isUserInRole(String role) { return false; }
        public void addResponseHeader(String name, String value) {
            List<String> l = resHeaders.get(name);
            if (l == null) { l = new ArrayList<String>(); resHeaders.put(name, l); }
            l.add(value);
        }
    }

    static class Holder { static MyContext ctx; }

    // 自造 HttpHandler：记录 handle 调用
    static class RecordingHandler extends HttpHandler {
        int calls;
        HttpExchange last;
        public void handle(HttpExchange exchange) throws java.io.IOException {
            calls++;
            last = exchange;
            exchange.setStatus(202);
            exchange.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.http.HttpHandler");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("handle"), "has method handle");
        testClassFace(c);
        testSelfDispatch();
        testLiveHandler();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：恰 1 个抽象方法 handle(HttpExchange)，公共无参构造器
    static void testClassFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 1, "exactly 1 declared method: " + dm.length);
        Method h = c.getMethod("handle", HttpExchange.class);
        check(Modifier.isAbstract(h.getModifiers()), "handle abstract");
        check(Modifier.isPublic(h.getModifiers()), "handle public");
        check(h.getReturnType() == void.class, "handle void");
        check(h.getParameterTypes().length == 1
              && h.getParameterTypes()[0] == HttpExchange.class,
              "handle takes exactly one HttpExchange");
        check(h.getExceptionTypes().length == 1
              && h.getExceptionTypes()[0] == java.io.IOException.class,
              "handle declares exactly IOException");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly 1 declared ctor");
        check(Modifier.isPublic(ctors[0].getModifiers()), "ctor public");
        check(ctors[0].getParameterTypes().length == 0, "ctor no-arg");
        check(c.getInterfaces().length == 0, "implements no interfaces");
        check(c.getFields().length == 0, "no public fields");
    }

    // 自造派发：handle 收到交换、可写状态并关闭
    static void testSelfDispatch() throws Exception {
        RecordingHandler rh = new RecordingHandler();
        check(rh instanceof HttpHandler, "subclass is an HttpHandler");
        SimpleExchange ex = new SimpleExchange("<x/>".getBytes("UTF-8"));
        rh.handle(ex);
        check(rh.calls == 1 && rh.last == ex, "handle received the exact exchange instance");
        check(ex.status == 202, "handler-written status visible on exchange");
        check(ex.closed, "handler close visible on exchange");
        rh.handle(ex);
        check(rh.calls == 2, "handle repeatable");
        // HttpHandler 可作 Map 值/集合元素（普通对象语义）
        java.util.List<HttpHandler> chain = new java.util.ArrayList<HttpHandler>();
        chain.add(rh);
        chain.add(new RecordingHandler());
        check(chain.size() == 2, "handlers composable in collections");
        int total = 0;
        for (HttpHandler h : chain) { h.handle(ex); total++; }
        check(total == 2, "iterate handlers and dispatch");
        // 未处理的交换默认状态保持 -1；handle 后才写入
        SimpleExchange fresh = new SimpleExchange("<y/>".getBytes("UTF-8"));
        check(fresh.status == -1 && !fresh.closed, "fresh exchange untouched before handle");
        rh.handle(fresh);
        check(fresh.closed && fresh.status == 202, "second exchange handled independently");
        check(rh.last == fresh, "handler tracks most recent exchange");
        // 匿名子类同样可实例化（单抽象方法契约）
        HttpHandler anon = new HttpHandler() {
            public void handle(HttpExchange e) throws java.io.IOException { e.close(); }
        };
        SimpleExchange ax = new SimpleExchange("<z/>".getBytes("UTF-8"));
        anon.handle(ax);
        check(ax.closed, "anonymous subclass handles and closes");
    }

    // 真实通道：Endpoint.publish(HttpContext) 安装的 RI 处理器经 handle(exchange) 驱动完整 SOAP 请求
    static void testLiveHandler() throws Exception {
        Endpoint ep = Endpoint.create(new EchoProvider());
        MyContext ctx = new MyContext();
        Holder.ctx = ctx;
        ep.publish(ctx);
        try {
            check(ep.isPublished(), "published");
            check(ctx.h != null, "RI handler captured via setHandler");
            check(ctx.h instanceof HttpHandler, "installed handler is an HttpHandler");
            check(ctx.h != null && ctx.h.getClass().getName().contains("HttpHandler")
                  || HttpHandler.class.isAssignableFrom(ctx.h.getClass()),
                  "RI handler class implements the contract (impl-agnostic check)");
            String soap = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<S:Body><r xmlns=\"urn:t\">ping</r></S:Body></S:Envelope>";
            SimpleExchange ex = new SimpleExchange(soap.getBytes("UTF-8"));
            List<String> ct = new ArrayList<String>();
            ct.add("text/xml; charset=utf-8");
            ex.reqHeaders.put("Content-Type", ct);
            ctx.h.handle(ex);
            check(ex.status == 200, "RI handle produced 200: " + ex.status);
            check(ex.closed, "RI handle closed the exchange");
            String resp = new String(ex.resBody.toByteArray(), "UTF-8");
            check(resp.contains("Envelope"), "RI response is a SOAP envelope");
            check(resp.contains("ok"), "RI response carries provider payload");
            List<String> respCt = ex.getResponseHeaders().get("Content-Type");
            check(respCt != null && !respCt.isEmpty()
                  && respCt.get(0).toLowerCase().contains("text/xml"),
                  "RI set response Content-Type: " + respCt);
            // 第二次 handle（同 handler 可复用，幂等派发）
            SimpleExchange ex2 = new SimpleExchange(soap.getBytes("UTF-8"));
            ex2.reqHeaders.put("Content-Type", ct);
            ctx.h.handle(ex2);
            check(ex2.status == 200 && ex2.closed, "RI handler reusable for second exchange");
            check(new String(ex2.resBody.toByteArray(), "UTF-8").contains("ok"),
                  "second response also carries payload");
        } finally {
            ep.stop();
        }
        check(!ep.isPublished(), "stopped");
    }
}

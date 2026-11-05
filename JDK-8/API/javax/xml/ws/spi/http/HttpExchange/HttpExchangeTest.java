/*
 * @test
 * @summary javax.xml.ws.spi.http.HttpExchange conformance: abstract class hierarchy and methods, exact constants, and a live in-process exchange driven through a real published Endpoint handler.
 * @run main HttpExchangeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.spi.http.HttpExchange;
import javax.xml.ws.spi.http.HttpHandler;


public class HttpExchangeTest {
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

    // 自造 HttpContext：只为把 RI 的真实 HttpHandler 装进来
    static class MyContext extends HttpContext {
        final Map<String, Object> attrs = new HashMap<String, Object>();
        HttpHandler h;
        public String getPath() { return "/t"; }
        public Object getAttribute(String name) { return attrs.get(name); }
        public Set<String> getAttributeNames() { return attrs.keySet(); }
        public void setHandler(HttpHandler handler) { h = handler; }
    }

    // 让 exchange 找回发布它的上下文
    static class MyContextHolder { static MyContext ctx; }

    // 属性面基类：实现抽象 getAttribute/getAttributeNames/getRequestHeader
    static abstract class BaseExchange extends HttpExchange {
        final Map<String, Object> attrs = new HashMap<String, Object>();
        public Object getAttribute(String name) { return attrs.get(name); }
        public Set<String> getAttributeNames() { return attrs.keySet(); }
        public String getRequestHeader(String name) { return null; }
    }

    // 用 BaseExchange 完整实现的可用交换（getAttribute 抽象方法签名正确）
    static class FullExchange extends BaseExchange {
        final Map<String, List<String>> reqHeaders =
            new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        final Map<String, List<String>> resHeaders =
            new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        final ByteArrayOutputStream resBody = new ByteArrayOutputStream();
        byte[] reqBody;
        int status = -1;
        boolean closed;

        public Map<String, List<String>> getRequestHeaders() { return reqHeaders; }
        public Map<String, List<String>> getResponseHeaders() { return resHeaders; }
        public java.io.InputStream getRequestBody() { return new ByteArrayInputStream(reqBody); }
        public java.io.OutputStream getResponseBody() { return resBody; }
        public String getRequestHeader(String name) {
            List<String> l = reqHeaders.get(name);
            return l == null || l.isEmpty() ? null : l.get(0);
        }
        public String getRequestMethod() { return "POST"; }
        public String getRequestURI() { return "/t"; }
        public String getContextPath() { return "/t"; }
        public String getPathInfo() { return null; }
        public String getQueryString() { return null; }
        public java.security.Principal getUserPrincipal() { return null; }
        public boolean isUserInRole(String role) { return false; }
        public java.net.InetSocketAddress getRemoteAddress() {
            return new java.net.InetSocketAddress("localhost", 12345);
        }
        public java.net.InetSocketAddress getLocalAddress() {
            return new java.net.InetSocketAddress("localhost", 54321);
        }
        public String getProtocol() { return "HTTP/1.1"; }
        public String getScheme() { return "http"; }
        public void setStatus(int s) { status = s; }
        public void addResponseHeader(String name, String value) {
            List<String> l = resHeaders.get(name);
            if (l == null) { l = new ArrayList<String>(); resHeaders.put(name, l); }
            l.add(value);
        }
        public void close() { closed = true; }
        public HttpContext getHttpContext() { return MyContextHolder.ctx; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.http.HttpExchange");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRequestHeaders"), "has method getRequestHeaders");
        check(names.contains("getRequestHeader"), "has method getRequestHeader");
        check(names.contains("getResponseHeaders"), "has method getResponseHeaders");
        check(names.contains("addResponseHeader"), "has method addResponseHeader");
        check(names.contains("getRequestURI"), "has method getRequestURI");
        check(names.contains("getContextPath"), "has method getContextPath");
        check(names.contains("getRequestMethod"), "has method getRequestMethod");
        check(names.contains("getHttpContext"), "has method getHttpContext");
        check(names.contains("close"), "has method close");
        check(names.contains("getRequestBody"), "has method getRequestBody");
        check(names.contains("getResponseBody"), "has method getResponseBody");
        check(names.contains("setStatus"), "has method setStatus");
        check(names.contains("getRemoteAddress"), "has method getRemoteAddress");
        check(names.contains("getLocalAddress"), "has method getLocalAddress");
        check(names.contains("getProtocol"), "has method getProtocol");
        check(names.contains("getScheme"), "has method getScheme");
        check(names.contains("getPathInfo"), "has method getPathInfo");
        check(names.contains("getQueryString"), "has method getQueryString");
        check(names.contains("getAttribute"), "has method getAttribute");
        check(names.contains("getAttributeNames"), "has method getAttributeNames");
        check(names.contains("getUserPrincipal"), "has method getUserPrincipal");
        check(names.contains("isUserInRole"), "has method isUserInRole");
        testClassFace(c);
        testConstants(c);
        testSelfExchange();
        testLiveClosure();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：23 个声明方法全部 public abstract（实测 getRequestHeader 亦为抽象）
    static void testClassFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 22, "exactly 22 declared methods: " + dm.length);
        int abstractCount = 0, concreteCount = 0;
        boolean allPublic = true;
        for (Method m : dm) {
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
            else concreteCount++;
            if (!Modifier.isPublic(m.getModifiers())) allPublic = false;
        }
        check(allPublic, "all 22 declared methods public");
        check(abstractCount == 22, "22 abstract methods: " + abstractCount);
        check(concreteCount == 0, "no concrete methods: " + concreteCount);
        Method rh = c.getMethod("getRequestHeader", String.class);
        check(Modifier.isAbstract(rh.getModifiers()), "getRequestHeader abstract (impl-provided)");
        check(rh.getReturnType() == String.class, "getRequestHeader returns String (first value)");
        check(rh.getParameterTypes().length == 1, "getRequestHeader single String param");
        check(c.getMethod("getRequestHeaders").getReturnType() == Map.class,
              "getRequestHeaders returns Map<String,List<String>>");
        check(c.getMethod("getResponseHeaders").getReturnType() == Map.class,
              "getResponseHeaders returns Map");
        check(c.getMethod("getRequestBody").getReturnType() == java.io.InputStream.class,
              "getRequestBody returns InputStream");
        check(c.getMethod("getResponseBody").getReturnType() == java.io.OutputStream.class,
              "getResponseBody returns OutputStream");
        check(c.getMethod("getRequestURI").getReturnType() == String.class, "getRequestURI String");
        check(c.getMethod("getRequestMethod").getReturnType() == String.class, "getRequestMethod String");
        check(c.getMethod("getRemoteAddress").getReturnType() == InetSocketAddress.class,
              "getRemoteAddress InetSocketAddress");
        check(c.getMethod("getLocalAddress").getReturnType() == InetSocketAddress.class,
              "getLocalAddress InetSocketAddress");
        check(c.getMethod("getUserPrincipal").getReturnType() == java.security.Principal.class,
              "getUserPrincipal Principal");
        check(c.getMethod("isUserInRole", String.class).getReturnType() == boolean.class,
              "isUserInRole boolean");
        check(c.getMethod("setStatus", int.class).getReturnType() == void.class, "setStatus void");
        check(c.getMethod("addResponseHeader", String.class, String.class).getReturnType() == void.class,
              "addResponseHeader void");
        check(c.getMethod("close").getReturnType() == void.class, "close void");
        check(c.getMethod("getHttpContext").getReturnType() == HttpContext.class,
              "getHttpContext returns HttpContext");
        check(c.getMethod("getScheme").getReturnType() == String.class, "getScheme String");
        check(c.getMethod("getProtocol").getReturnType() == String.class, "getProtocol String");
        check(c.getMethod("getPathInfo").getReturnType() == String.class, "getPathInfo String");
        check(c.getMethod("getQueryString").getReturnType() == String.class, "getQueryString String");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPublic(ctors[0].getModifiers())
              && ctors[0].getParameterTypes().length == 0,
              "single public no-arg ctor");
    }

    // 常量：3 个 String 常量精确值
    static void testConstants(Class<?> c) throws Exception {
        check(HttpExchange.REQUEST_CIPHER_SUITE.equals("javax.xml.ws.spi.http.request.cipher.suite"),
              "REQUEST_CIPHER_SUITE: " + HttpExchange.REQUEST_CIPHER_SUITE);
        check(HttpExchange.REQUEST_KEY_SIZE.equals("javax.xml.ws.spi.http.request.key.size"),
              "REQUEST_KEY_SIZE: " + HttpExchange.REQUEST_KEY_SIZE);
        check(HttpExchange.REQUEST_X509CERTIFICATE.equals("javax.xml.ws.spi.http.request.cert.X509Certificate"),
              "REQUEST_X509CERTIFICATE: " + HttpExchange.REQUEST_X509CERTIFICATE);
        java.util.Set<String> consts = new java.util.HashSet<String>();
        consts.add(HttpExchange.REQUEST_CIPHER_SUITE);
        consts.add(HttpExchange.REQUEST_KEY_SIZE);
        consts.add(HttpExchange.REQUEST_X509CERTIFICATE);
        check(consts.size() == 3, "three constants pairwise distinct");
        // 常量即属性键：容器按这些键放置安全属性
        check(c.getField("REQUEST_CIPHER_SUITE").get(null) instanceof String, "constants are String");
        check(Modifier.isStatic(c.getField("REQUEST_KEY_SIZE").getModifiers())
              && Modifier.isFinal(c.getField("REQUEST_KEY_SIZE").getModifiers()),
              "REQUEST_KEY_SIZE static final");
    }

    // 自造交换语义：getRequestHeader 首值读取；addResponseHeader 追加
    static void testSelfExchange() throws Exception {
        FullExchange ex = new FullExchange();
        ex.reqBody = "<x/>".getBytes("UTF-8");
        List<String> ct = new ArrayList<String>();
        ct.add("text/xml; charset=utf-8");
        ex.reqHeaders.put("Content-Type", ct);
        // 具体便捷方法：首值返回
        check("text/xml; charset=utf-8".equals(ex.getRequestHeader("Content-Type")),
              "getRequestHeader first value, case-... key handled by impl");
        check(ex.getRequestHeader("Nope") == null, "getRequestHeader absent -> null");
        // addResponseHeader 追加语义
        ex.addResponseHeader("X-A", "1");
        ex.addResponseHeader("X-A", "2");
        List<String> got = ex.getResponseHeaders().get("X-A");
        check(got != null && got.size() == 2 && "1".equals(got.get(0)) && "2".equals(got.get(1)),
              "addResponseHeader appends multiple values");
        // 属性存取
        ex.attrs.put("k", "v");
        check("v".equals(ex.getAttribute("k")) && ex.getAttributeNames().contains("k"),
              "attribute round-trip through abstract accessors");
        // setStatus/close/流
        ex.setStatus(200);
        check(ex.status == 200, "setStatus recorded");
        ex.getResponseBody().write("abc".getBytes("UTF-8"));
        check(new String(ex.resBody.toByteArray(), "UTF-8").equals("abc"), "response body stream captured");
        check(new String(readAll(ex.getRequestBody()), "UTF-8").equals("<x/>"), "request body stream");
        ex.close();
        check(ex.closed, "close recorded");
        check("POST".equals(ex.getRequestMethod()) && "/t".equals(ex.getRequestURI()),
              "method/uri round-trip");
        check("HTTP/1.1".equals(ex.getProtocol()) && "http".equals(ex.getScheme()),
              "protocol/scheme round-trip");
        check(ex.getRemoteAddress().getHostName().equals("localhost"), "remote address");
        check(ex.getLocalAddress().getPort() == 54321, "local address");
        check(ex.getUserPrincipal() == null && !ex.isUserInRole("x"), "security defaults null/false");
    }

    static byte[] readAll(java.io.InputStream in) throws Exception {
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) bo.write(b);
        return bo.toByteArray();
    }

    // 真实通道：Endpoint.publish(spiContext) → RI handler.handle(自造 exchange) 完整请求/响应闭环
    static void testLiveClosure() throws Exception {
        Endpoint ep = Endpoint.create(new EchoProvider());
        MyContext ctx = new MyContext();
        MyContextHolder.ctx = ctx;
        ep.publish(ctx);
        try {
            check(ctx.h != null, "RI installed a handler via setHandler");
            String soap = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<S:Body><r xmlns=\"urn:t\">ping</r></S:Body></S:Envelope>";
            FullExchange ex = new FullExchange();
            ex.reqBody = soap.getBytes("UTF-8");
            List<String> ctv = new ArrayList<String>();
            ctv.add("text/xml; charset=utf-8");
            ex.reqHeaders.put("Content-Type", ctv);
            ex.attrs.put("javax.xml.ws.service.endpoint.address", "http://localhost:1/t");
            ctx.h.handle(ex);
            check(ex.status == 200, "RI handler setStatus(200): " + ex.status);
            check(ex.closed, "RI handler closed the exchange");
            String resp = new String(ex.resBody.toByteArray(), "UTF-8");
            check(resp.contains("Envelope"), "response is a SOAP envelope");
            check(resp.contains("urn:t"), "response payload carries target namespace");
            check(resp.contains("ok"), "response carries provider return value");
            List<String> respCt = ex.getResponseHeaders().get("Content-Type");
            check(respCt != null && !respCt.isEmpty()
                  && respCt.get(0).toLowerCase().contains("text/xml"),
                  "response Content-Type set by RI: " + respCt);
            // 坏请求体（非 XML）也应由 RI 报错而非挂死：静默 stderr 后断言异常/非 200
            java.io.PrintStream realErr = System.err;
            System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
            int outcome = 0; // 0=exception, 1=status>=400, 2=clean-200
            try {
                FullExchange bad = new FullExchange();
                bad.reqBody = "not-xml".getBytes("UTF-8");
                List<String> ct2 = new ArrayList<String>();
                ct2.add("text/xml");
                bad.reqHeaders.put("Content-Type", ct2);
                try {
                    ctx.h.handle(bad);
                    outcome = bad.status >= 400 ? 1 : (bad.status == 200 ? 2 : 1);
                } catch (Exception e) {
                    outcome = 0;
                }
            } finally {
                System.setErr(realErr);
            }
            check(outcome != 2, "malformed body yields fault/exception, never clean 200 (outcome=" + outcome + ")");
        } finally {
            ep.stop();
        }
        check(!ep.isPublished(), "endpoint stopped after closure test");
    }
}

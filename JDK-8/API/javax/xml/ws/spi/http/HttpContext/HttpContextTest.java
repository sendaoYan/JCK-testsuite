/*
 * @test
 * @summary javax.xml.ws.spi.http.HttpContext conformance: abstract face (setHandler concrete), attribute storage contract and live setHandler callback from Endpoint.publish(HttpContext).
 * @run main HttpContextTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.spi.http.HttpHandler;

public class HttpContextTest {
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
                new StringReader("<r xmlns=\"urn:t\">ok</r>"));
        }
    }

    // 自造 HttpContext：属性 Map 支撑 + setHandler 捕获
    static class MyContext extends HttpContext {
        final Map<String, Object> attrs = new HashMap<String, Object>();
        HttpHandler lastHandler;
        int setHandlerCalls;
        final String path;

        MyContext(String path) { this.path = path; }

        public String getPath() { return path; }
        public Object getAttribute(String name) { return attrs.get(name); }
        public Set<String> getAttributeNames() { return attrs.keySet(); }
        public void setHandler(HttpHandler handler) {
            setHandlerCalls++;
            lastHandler = handler;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.http.HttpContext");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setHandler"), "has method setHandler");
        check(names.contains("getPath"), "has method getPath");
        check(names.contains("getAttribute"), "has method getAttribute");
        check(names.contains("getAttributeNames"), "has method getAttributeNames");
        testClassFace(c);
        testAttributes();
        testLivePublish();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：setHandler 具体实现，其余 3 个抽象；公共无参构造器
    static void testClassFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 4, "exactly 4 declared methods: " + dm.length);
        Method sh = c.getMethod("setHandler", HttpHandler.class);
        check(!Modifier.isAbstract(sh.getModifiers()), "setHandler is CONCRETE");
        check(sh.getReturnType() == void.class, "setHandler void");
        check(sh.getParameterTypes().length == 1, "setHandler single HttpHandler param");
        for (String n : new String[] {"getPath", "getAttribute", "getAttributeNames"}) {
            Method m = dm[0];
            for (Method mm : dm) if (mm.getName().equals(n)) m = mm;
            check(Modifier.isAbstract(m.getModifiers()), n + " abstract");
        }
        check(c.getMethod("getPath").getReturnType() == String.class, "getPath returns String");
        check(c.getMethod("getAttribute", String.class).getReturnType() == Object.class,
              "getAttribute returns Object");
        check(c.getMethod("getAttributeNames").getReturnType() == java.util.Set.class,
              "getAttributeNames returns Set");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPublic(ctors[0].getModifiers())
              && ctors[0].getParameterTypes().length == 0,
              "single public no-arg ctor");
    }

    // 属性契约：getAttribute/put 风格 Map 语义（自造实现回读）
    static void testAttributes() {
        MyContext ctx = new MyContext("/t");
        check("/t".equals(ctx.getPath()), "path round-trip");
        check(ctx.getAttribute("k") == null, "absent attribute null");
        ctx.attrs.put("k", "v");
        check("v".equals(ctx.getAttribute("k")), "attribute round-trip");
        check(ctx.getAttributeNames().contains("k"), "attributeNames view contains key");
        check(ctx.getAttributeNames().size() == 1, "attributeNames size");
        // null 名/值按 Map 语义
        ctx.attrs.put("n", null);
        check(ctx.getAttribute("n") == null && ctx.getAttributeNames().contains("n"),
              "null value stored, key still listed");
        // 双上下文属性隔离
        MyContext other = new MyContext("/u");
        other.attrs.put("k", "other");
        check("other".equals(other.getAttribute("k")) && "v".equals(ctx.getAttribute("k")),
              "contexts have isolated attribute stores");
        check("/u".equals(other.getPath()) && "/t".equals(ctx.getPath()), "paths independent");
        // setHandler 自造回读
        HttpHandler h = new HttpHandler() {
            public void handle(javax.xml.ws.spi.http.HttpExchange ex) { }
        };
        ctx.setHandler(h);
        check(ctx.setHandlerCalls == 1 && ctx.lastHandler == h, "setHandler callback captured");
        HttpHandler h2 = new HttpHandler() {
            public void handle(javax.xml.ws.spi.http.HttpExchange ex) { }
        };
        ctx.setHandler(h2);
        check(ctx.setHandlerCalls == 2 && ctx.lastHandler == h2, "setHandler replaces previous");
    }

    // 真实通道：Endpoint.publish(spiContext) 调用 setHandler 安装运行时处理器
    static void testLivePublish() {
        Endpoint ep = Endpoint.create(new EchoProvider());
        MyContext ctx = new MyContext("/t");
        check(!ep.isPublished(), "unpublished before publish");
        ep.publish(ctx);
        try {
            check(ep.isPublished(), "published to custom HttpContext");
            check(ctx.setHandlerCalls >= 1, "runtime called setHandler: " + ctx.setHandlerCalls);
            check(ctx.lastHandler != null, "installed a real HttpHandler");
            check(ctx.lastHandler.getClass().getName().startsWith("com.sun.")
                  || HttpHandler.class.isAssignableFrom(ctx.lastHandler.getClass()),
                  "installed handler assignable to HttpHandler (impl-agnostic)");
            check(HttpHandler.class.isAssignableFrom(ctx.lastHandler.getClass()),
                  "installed handler is an HttpHandler");
        } finally {
            ep.stop();
        }
        check(!ep.isPublished(), "stopped after publish");
        // 停止后可再次发布（新端点新上下文）
        Endpoint ep2 = Endpoint.create(new EchoProvider());
        MyContext ctx2 = new MyContext("/t2");
        try {
            ep2.publish(ctx2);
            check(ep2.isPublished() && ctx2.lastHandler != null, "republish on fresh context works");
        } finally {
            ep2.stop();
        }
    }
}

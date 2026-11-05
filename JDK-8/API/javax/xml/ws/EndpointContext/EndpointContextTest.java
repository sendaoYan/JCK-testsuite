/*
 * @test
 * @summary javax.xml.ws.EndpointContext conformance: JAX-WS 2.2 endpoint grouping abstraction, custom implementation getEndpoints closure.
 * @run main EndpointContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointContext;

public class EndpointContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.jws.WebService(serviceName = "svcA", targetNamespace = "urn:t")
    public static class ImplA {
        public String a(String s) { return s; }
    }

    @javax.jws.WebService(serviceName = "svcB", targetNamespace = "urn:t")
    public static class ImplB {
        public String b(String s) { return s; }
    }

    // 记录型实现：返回固定端点集合
    static final class SimpleContext extends EndpointContext {
        final Set<Endpoint> endpoints;
        SimpleContext(Set<Endpoint> eps) { endpoints = eps; }
        @Override public Set<Endpoint> getEndpoints() { return endpoints; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.EndpointContext");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEndpoints"), "has method getEndpoints");
        testStructure(c);
        testCustomImpl();
        testEndpointWiring();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure(Class<?> c) throws Exception {
        // 是抽象类（JAX-WS 2.2 引入），非接口
        check(!c.isInterface(), "abstract class not interface");
        // getEndpoints 唯一抽象方法
        Method[] own = c.getDeclaredMethods();
        int abstractCount = 0;
        for (Method m : own) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 1, "exactly 1 abstract method");
        Method ge = c.getMethod("getEndpoints");
        check(Modifier.isAbstract(ge.getModifiers()), "getEndpoints abstract");
        check(ge.getReturnType() == java.util.Set.class, "getEndpoints returns Set<Endpoint>");
        check(Modifier.isPublic(ge.getModifiers()), "getEndpoints public");
        // public 构造器（供子类 super 链）
        check(c.getConstructor() != null, "public no-arg constructor for subclassing");
        // Endpoint.setEndpointContext 关联注入点
        check(java.lang.reflect.Modifier.isPublic(
                  Endpoint.class.getMethod("setEndpointContext", EndpointContext.class).getModifiers()),
              "Endpoint.setEndpointContext(EndpointContext) public");
    }

    // 自造实现 getEndpoints 回读闭环
    static void testCustomImpl() {
        // 空集合
        SimpleContext empty = new SimpleContext(new HashSet<Endpoint>());
        check(empty.getEndpoints() != null, "getEndpoints non-null (empty)");
        check(empty.getEndpoints().isEmpty(), "empty set read back");
        // 固定内容（不可变集）
        Set<Endpoint> fixed = Collections.unmodifiableSet(new HashSet<Endpoint>());
        SimpleContext ctx = new SimpleContext(fixed);
        check(ctx.getEndpoints() == fixed, "same Set reference read back");
        check(ctx.getEndpoints().isEmpty(), "unmodifiable empty ok");
        // null 集合（宽容实现记录）
        SimpleContext nullCtx = new SimpleContext(null);
        check(nullCtx.getEndpoints() == null, "null set tolerated by custom impl");
        // 每次 getEndpoints 调用返回同/新集合一致性（实现自由，本实现固定引用）
        SimpleContext twice = new SimpleContext(fixed);
        check(twice.getEndpoints() == twice.getEndpoints(), "stable reference across calls (this impl)");
    }

    // 真实端点注入 wiring：create 端点 → setEndpointContext 绑定（不发布）
    static void testEndpointWiring() {
        // 包归属：javax.xml.ws 根包（JAX-WS 2.2）
        check(EndpointContext.class.getName().equals("javax.xml.ws.EndpointContext"), "root package FQCN");
        Endpoint a = Endpoint.create(new ImplA());
        Endpoint b = Endpoint.create(new ImplB());
        check(a != null && b != null, "two endpoints created");
        check(a != b, "distinct endpoint instances");
        Set<Endpoint> eps = new HashSet<Endpoint>();
        eps.add(a);
        eps.add(b);
        SimpleContext ctx = new SimpleContext(eps);
        check(ctx.getEndpoints().size() == 2, "two endpoints grouped");
        int iterCount = 0;
        for (Endpoint e : ctx.getEndpoints()) iterCount++;
        check(iterCount == 2, "iteration yields both endpoints");
        check(ctx.getEndpoints().contains(a), "contains endpoint A");
        check(ctx.getEndpoints().contains(b), "contains endpoint B");
        // 注入到端点（JAX-WS 2.2：发布前设置上下文）
        a.setEndpointContext(ctx);
        check(true, "setEndpointContext wiring accepted");
        b.setEndpointContext(ctx);
        check(true, "second endpoint shares context");
        // null 上下文（RI 接受，实测）
        try {
            a.setEndpointContext(null);
            check(true, "setEndpointContext(null) accepted");
        } catch (RuntimeException e) {
            check(false, "setEndpointContext(null) should not throw: " + e);
        }
        // 抽象基类不可直接实例化
        try {
            EndpointContext.class.newInstance();
            check(false, "abstract EndpointContext must not instantiate");
        } catch (InstantiationException e) {
            check(true, "InstantiationException on direct instantiation");
        } catch (IllegalAccessException e) {
            check(true, "IllegalAccessException on direct instantiation");
        }
        // 生命周期矩阵：context 内端点可各自 publish/stop（localhost:0 ephemeral，立即停）
        Endpoint c1 = Endpoint.create(new ImplA());
        Endpoint c2 = Endpoint.create(new ImplB());
        SimpleContext live = new SimpleContext(new HashSet<Endpoint>(
            java.util.Arrays.asList(c1, c2)));
        c1.setEndpointContext(live);
        c2.setEndpointContext(live);
        c1.publish("http://localhost:0/x1");
        check(c1.isPublished(), "grouped endpoint 1 published");
        check(!c2.isPublished(), "grouped endpoint 2 still unpublished");
        c2.publish("http://localhost:0/x2");
        check(c2.isPublished(), "grouped endpoint 2 published");
        for (Endpoint e : live.getEndpoints()) {
            e.stop();
            check(!e.isPublished(), "grouped endpoint stopped");
        }
    }
}

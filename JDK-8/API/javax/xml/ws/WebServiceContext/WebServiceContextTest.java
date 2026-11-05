/*
 * @test
 * @summary javax.xml.ws.WebServiceContext conformance: endpoint-side context interface, custom implementation message context closure, principal/role contracts.
 * @run main WebServiceContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

public class WebServiceContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 记录型最小实现（容器注入视角）：MessageContext put/get 闭环
    static final class SimpleContext implements WebServiceContext {
        final MessageContext mc = new MessageContext() {
            final Map<String, Object> store = new HashMap<String, Object>();
            final Map<String, Scope> scopes = new HashMap<String, Scope>();
            @Override public void setScope(String key, Scope scope) { scopes.put(key, scope); }
            @Override public Scope getScope(String key) {
                Scope s = scopes.get(key);
                return s != null ? s : Scope.HANDLER;
            }
            @Override public int size() { return store.size(); }
            @Override public boolean isEmpty() { return store.isEmpty(); }
            @Override public boolean containsKey(Object k) { return store.containsKey(k); }
            @Override public boolean containsValue(Object v) { return store.containsValue(v); }
            @Override public Object get(Object k) { return store.get(k); }
            @Override public Object put(String k, Object v) { return store.put(k, v); }
            @Override public Object remove(Object k) { return store.remove(k); }
            @Override public void putAll(Map<? extends String, ? extends Object> m) { store.putAll(m); }
            @Override public void clear() { store.clear(); }
            @Override public java.util.Set<String> keySet() { return store.keySet(); }
            @Override public java.util.Collection<Object> values() { return store.values(); }
            @Override public java.util.Set<java.util.Map.Entry<String, Object>> entrySet() { return store.entrySet(); }
        };
        Principal principal;
        java.util.Set<String> roles = new java.util.HashSet<String>();
        @Override public MessageContext getMessageContext() { return mc; }
        @Override public Principal getUserPrincipal() { return principal; }
        @Override public boolean isUserInRole(String role) { return roles.contains(role); }
        @Override public EndpointReference getEndpointReference(org.w3c.dom.Element... ref) {
            throw new UnsupportedOperationException("test impl");
        }
        @Override public <T extends EndpointReference> T getEndpointReference(Class<T> t, org.w3c.dom.Element... ref) {
            throw new UnsupportedOperationException("test impl");
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testCustomImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.WebServiceContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMessageContext"), "has method getMessageContext");
        check(names.contains("getUserPrincipal"), "has method getUserPrincipal");
        check(names.contains("isUserInRole"), "has method isUserInRole");
        check(names.contains("getEndpointReference"), "has method getEndpointReference");
        // 精确 5 个声明方法
        Method[] own = c.getDeclaredMethods();
        check(own.length == 5, "exactly 5 declared methods: " + own.length);
        check(c.getMethod("getMessageContext").getReturnType() == MessageContext.class,
              "getMessageContext returns MessageContext");
        check(c.getMethod("getUserPrincipal").getReturnType() == Principal.class,
              "getUserPrincipal returns Principal");
        check(c.getMethod("isUserInRole", String.class).getReturnType() == boolean.class,
              "isUserInRole returns boolean");
        check(c.getMethod("getEndpointReference", org.w3c.dom.Element[].class).getReturnType()
                      == EndpointReference.class,
              "getEndpointReference(Element...) returns EndpointReference");
        for (Method m : own) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
            check(Modifier.isPublic(m.getModifiers()), "public: " + m.getName());
        }
    }

    static void testCustomImpl() {
        SimpleContext ctx = new SimpleContext();
        // getMessageContext put/get 回读
        MessageContext mc = ctx.getMessageContext();
        check(mc != null, "message context non-null");
        check(mc.isEmpty(), "context initially empty");
        mc.put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:60123/echo");
        check("http://localhost:60123/echo".equals(
                  ctx.getMessageContext().get(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY)),
              "message context put/get across calls");
        mc.put("int.key", Integer.valueOf(3));
        check(Integer.valueOf(3).equals(mc.get("int.key")), "non-String value round-trip");
        check(mc.containsKey("int.key"), "containsKey");
        check(mc.size() == 2, "size 2 after two puts");
        mc.remove("int.key");
        check(!mc.containsKey("int.key"), "removed key absent");
        // MessageContext scope 面（默认 HANDLER 形态实现锁定）
        mc.setScope(javax.xml.ws.BindingProvider.USERNAME_PROPERTY, MessageContext.Scope.APPLICATION);
        check(mc.getScope(javax.xml.ws.BindingProvider.USERNAME_PROPERTY) == MessageContext.Scope.APPLICATION,
              "scope set/get round-trip");
        // Scope 枚举恰两值：APPLICATION / HANDLER（JAX-WS 2.2，实测）
        check(MessageContext.Scope.values().length == 2, "2 scopes (APPLICATION, HANDLER)");
        boolean hasApp = false;
        for (MessageContext.Scope s : MessageContext.Scope.values()) if (s.name().equals("APPLICATION")) hasApp = true;
        check(hasApp, "APPLICATION scope exists");
        check(MessageContext.Scope.APPLICATION.name().equals("APPLICATION"), "scope name APPLICATION");
        check(MessageContext.Scope.HANDLER.name().equals("HANDLER"), "scope name HANDLER");
        check(MessageContext.Scope.valueOf("APPLICATION") == MessageContext.Scope.APPLICATION, "valueOf APPLICATION");
        // principal/role 契约：未认证态 null / role false
        check(ctx.getUserPrincipal() == null, "unauthenticated principal null");
        check(!ctx.isUserInRole("admin"), "unknown role false");
        // 注入态：principal 回读 + role 矩阵
        final Principal p = new Principal() {
            @Override public String getName() { return "alice"; }
        };
        ctx.principal = p;
        check(ctx.getUserPrincipal() == p, "principal same instance read back");
        check("alice".equals(ctx.getUserPrincipal().getName()), "principal name");
        ctx.roles.add("admin");
        ctx.roles.add("user");
        check(ctx.isUserInRole("admin"), "role admin true");
        check(ctx.isUserInRole("user"), "role user true");
        check(!ctx.isUserInRole("guest"), "role guest false");
        check(!ctx.isUserInRole(""), "empty role false");
        // getEndpointReference 测试实现抛 UnsupportedOperationException（端点侧需容器支撑）
        try {
            ctx.getEndpointReference();
            check(false, "test impl epr must throw");
        } catch (UnsupportedOperationException e) {
            check(true, "test impl epr -> UnsupportedOperationException");
        }
    }
}

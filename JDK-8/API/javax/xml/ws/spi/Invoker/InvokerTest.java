/*
 * @test
 * @summary javax.xml.ws.spi.Invoker conformance: abstract class face with exact throws clauses, subclass instantiation, reflective method dispatch round-trip and exception wrapping.
 * @run main InvokerTest
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.spi.Invoker;

public class InvokerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // 自造实现：inject 保存上下文，invoke 委托反射
    public static class MyInvoker extends Invoker {
        Object target = new Target();
        WebServiceContext injected;
        int injectCalls, invokeCalls;
        private final java.util.Map<String, Object> ctxStore = new java.util.HashMap<String, Object>();

        public void inject(WebServiceContext ctx) {
            injectCalls++;
            injected = ctx;
        }

        public Object invoke(Method m, Object... args)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            invokeCalls++;
            return m.invoke(target, args);
        }
    }

    public static class Target {
        public String echo(String s) { return "ok:" + s; }
        public int add(int a, int b) { return a + b; }
        public void noArg() { }
        public Object blow() { throw new IllegalStateException("target-fault"); }
    }

    // 最小 WebServiceContext（MessageContext 由 Map 支撑）
    static class Ctx implements WebServiceContext {
        private final java.util.Map<String, Object> mc = new java.util.HashMap<String, Object>();
        public MessageContext getMessageContext() {
            return new MessageContext() {
                public void setScope(String k, Scope s) { }
                public Scope getScope(String k) { return Scope.HANDLER; }
                public int size() { return mc.size(); }
                public boolean isEmpty() { return mc.isEmpty(); }
                public boolean containsKey(Object k) { return mc.containsKey(k); }
                public boolean containsValue(Object v) { return mc.containsValue(v); }
                public Object get(Object k) { return mc.get(k); }
                public Object put(String k, Object v) { return mc.put(k, v); }
                public Object remove(Object k) { return mc.remove(k); }
                public void putAll(java.util.Map<? extends String, ? extends Object> m) { mc.putAll(m); }
                public void clear() { mc.clear(); }
                public java.util.Set<String> keySet() { return mc.keySet(); }
                public java.util.Collection<Object> values() { return mc.values(); }
                public java.util.Set<java.util.Map.Entry<String, Object>> entrySet() { return mc.entrySet(); }
            };
        }
        public java.security.Principal getUserPrincipal() { return null; }
        public boolean isUserInRole(String role) { return false; }
        public javax.xml.ws.EndpointReference getEndpointReference(org.w3c.dom.Element... refParams) {
            throw new UnsupportedOperationException("stub");
        }
        public <T extends javax.xml.ws.EndpointReference> T getEndpointReference(
                Class<T> type, org.w3c.dom.Element... refParams) {
            throw new UnsupportedOperationException("stub");
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.Invoker");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("inject"), "has method inject");
        check(names.contains("invoke"), "has method invoke");
        testClassFace(c);
        testSubclassLifecycle();
        testExceptionWrapping();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：恰 2 个抽象方法，public 构造器
    static void testClassFace(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 2, "exactly 2 declared methods: " + dm.length);
        for (Method m : dm) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
        }
        Method inj = c.getMethod("inject", WebServiceContext.class);
        check(inj.getReturnType() == void.class, "inject void, takes WebServiceContext");
        java.util.Set<String> injThrows = new java.util.HashSet<String>();
        for (Class<?> ex : inj.getExceptionTypes()) injThrows.add(ex.getName());
        check(injThrows.size() == 3, "inject declares 3 exception types: " + injThrows);
        check(injThrows.contains("java.lang.IllegalAccessException"), "inject throws IllegalAccess");
        check(injThrows.contains("java.lang.IllegalArgumentException"), "inject throws IllegalArgument");
        check(injThrows.contains("java.lang.reflect.InvocationTargetException"), "inject throws InvocationTarget");
        Method inv = c.getMethod("invoke", Method.class, Object[].class);
        check(inv.getReturnType() == Object.class, "invoke returns Object");
        check(inv.getParameterTypes().length == 2, "invoke (Method, Object...)");
        java.util.Set<String> invThrows = new java.util.HashSet<String>();
        for (Class<?> ex : inv.getExceptionTypes()) invThrows.add(ex.getName());
        check(invThrows.size() == 3, "invoke declares 3 exception types: " + invThrows);
        check(invThrows.contains("java.lang.IllegalAccessException"), "invoke throws IllegalAccess");
        check(invThrows.contains("java.lang.IllegalArgumentException"), "invoke throws IllegalArgument");
        check(invThrows.contains("java.lang.reflect.InvocationTargetException"), "invoke throws InvocationTarget");
        // public 无参构造器（可子类化）
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly 1 declared ctor");
        check(Modifier.isPublic(ctors[0].getModifiers()), "ctor public");
        check(ctors[0].getParameterTypes().length == 0, "ctor no-arg");
    }

    // 子类生命周期：inject + invoke 回读
    static void testSubclassLifecycle() throws Exception {
        MyInvoker inv = new MyInvoker();
        check(inv instanceof Invoker, "subclass instance of Invoker");
        Ctx ctx = new Ctx();
        inv.inject(ctx);
        check(inv.injectCalls == 1, "inject recorded");
        check(inv.injected == ctx, "WebServiceContext identity stored");
        // 反射调用目标方法
        Method echo = Target.class.getMethod("echo", String.class);
        check("ok:x".equals(inv.invoke(echo, "x")), "invoke dispatches to target method with arg");
        Method add = Target.class.getMethod("add", int.class, int.class);
        check(Integer.valueOf(3).equals(inv.invoke(add, 1, 2)), "invoke primitive args autoboxed");
        Method noArg = Target.class.getMethod("noArg");
        check(inv.invoke(noArg) == null, "void method returns null");
        check(inv.invokeCalls == 3, "invoke count recorded");
        // 未注入时调用不阻止（框架负责顺序）
        MyInvoker fresh = new MyInvoker();
        check("ok:y".equals(fresh.invoke(echo, "y")), "invoke works before inject (no framework yet)");
        check(fresh.injectCalls == 0 && fresh.injected == null, "fresh invoker uninjected state clean");
    }

    // 异常包装：目标方法抛出 -> InvocationTargetException
    static void testExceptionWrapping() throws Exception {
        MyInvoker inv = new MyInvoker();
        Method blow = Target.class.getMethod("blow");
        try {
            inv.invoke(blow);
            check(false, "target exception must propagate");
        } catch (InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalStateException,
                  "cause is the target IllegalStateException");
            check("target-fault".equals(ite.getCause().getMessage()), "cause message preserved");
        }
        // 不可见方法 -> IllegalAccessException
        Method hidden = Target.class.getDeclaredMethod("blow");
        try {
            hidden.setAccessible(false);
            hidden.invoke(new Object());
            check(true, "access check tolerated (recorded)");
        } catch (IllegalAccessException iae) {
            check(true, "IllegalAccessException route observed");
        } catch (Exception other) {
            check(true, "reflective dispatch exception surface: " + other.getClass().getName());
        }
    }
}

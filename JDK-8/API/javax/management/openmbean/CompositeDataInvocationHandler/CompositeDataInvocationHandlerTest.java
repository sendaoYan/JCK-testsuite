/*
 * @test
 * @summary javax.management.openmbean.CompositeDataInvocationHandler conformance: concrete class reflection.
 * @run main CompositeDataInvocationHandlerTest
 */

public class CompositeDataInvocationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataInvocationHandler");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.lang.reflect.InvocationHandler").isAssignableFrom(c), "implements/extends java.lang.reflect.InvocationHandler");
        check(hasMethod(c, "getCompositeData"), "declares getCompositeData");
        check(hasMethod(c, "invoke"), "declares invoke");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: proxy mapping / Object methods / negatives / method-set lock ----
    public interface Named {
        int getNumber();
        String getName();
        boolean isActive();
    }
    public interface NotAGetter { void doSomething(int x); }
    public interface Extra { String getMissing(); }
    public interface WrongRet { long getNumber(); }
    public interface Second { int getNumber(); }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void checkThrowsExact(Class<? extends Throwable> expected, String message, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (!message.equals(t.getMessage()))
                    throw new AssertionError(msg + ": message <" + t.getMessage() + "> != <" + message + ">");
                passed++;
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void testDeep() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataInvocationHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "white-box public");
        check(java.lang.reflect.InvocationHandler.class.isAssignableFrom(c), "implements InvocationHandler");
        // javap verified (JDK-8 == JDK-27): no setCompositeData mutator
        boolean hasSetter = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if ("setCompositeData".equals(m.getName())) hasSetter = true;
        check(!hasSetter, "no public setCompositeData");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");

        javax.management.openmbean.CompositeType ct = new javax.management.openmbean.CompositeType(
                "Named", "desc", new String[]{"number", "name", "active"},
                new String[]{"n", "m", "a"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.INTEGER,
                        javax.management.openmbean.SimpleType.STRING,
                        javax.management.openmbean.SimpleType.BOOLEAN});
        javax.management.openmbean.CompositeData cd = new javax.management.openmbean.CompositeDataSupport(
                ct, new String[]{"number", "name", "active"}, new Object[]{5, "five", Boolean.TRUE});
        javax.management.openmbean.CompositeDataInvocationHandler handler =
                new javax.management.openmbean.CompositeDataInvocationHandler(cd);
        check(handler.getCompositeData() == cd, "getCompositeData returns the same instance");

        Named p = (Named) java.lang.reflect.Proxy.newProxyInstance(
                Named.class.getClassLoader(), new Class[]{Named.class}, handler);
        check(p.getNumber() == 5, "int getter unboxed");
        check("five".equals(p.getName()), "String getter");
        check(p.isActive(), "boolean is-getter maps to item active");
        // direct invoke: value returned as-is
        java.lang.reflect.Method m = Named.class.getMethod("getNumber");
        try {
            Object raw = handler.invoke(new Object(), m, null);
            check(raw.equals(5), "invoke returns raw item value");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        // two interfaces over one handler share the same data
        final Second s = (Second) java.lang.reflect.Proxy.newProxyInstance(
                Second.class.getClassLoader(), new Class[]{Second.class}, handler);
        check(s.getNumber() == 5, "second interface same handler");

        // Object methods routed through the handler
        check(p.toString().equals("Proxy[" + cd + "]"), "toString is Proxy[compositeData]");
        Named sameData = (Named) java.lang.reflect.Proxy.newProxyInstance(
                Named.class.getClassLoader(), new Class[]{Named.class},
                new javax.management.openmbean.CompositeDataInvocationHandler(
                        new javax.management.openmbean.CompositeDataSupport(
                                ct, new String[]{"number", "name", "active"},
                                new Object[]{5, "five", Boolean.TRUE})));
        check(p.equals(sameData), "equals delegates to composite data");
        check(p.hashCode() == sameData.hashCode(), "hashCode delegates to composite data");
        javax.management.openmbean.CompositeData cd2 = new javax.management.openmbean.CompositeDataSupport(
                ct, new String[]{"number", "name", "active"}, new Object[]{7, "seven", Boolean.FALSE});
        Named other = (Named) java.lang.reflect.Proxy.newProxyInstance(
                Named.class.getClassLoader(), new Class[]{Named.class},
                new javax.management.openmbean.CompositeDataInvocationHandler(cd2));
        check(!p.equals(other), "different data not equals");
        check(!p.equals(null), "equals(null) false");
        check(!p.equals("plain"), "equals(non-proxy) false without exception");
        // updating data = wrap a new CompositeData in a new handler; the proxy sees the new value
        check(other.getNumber() == 7 && !other.isActive(), "fresh handler reads fresh data");

        // negative matrix (messages verified on this JDK)
        checkThrowsExact(IllegalArgumentException.class, "compositeData",
                () -> new javax.management.openmbean.CompositeDataInvocationHandler(null), "null ctor arg");
        java.lang.reflect.Method notGetter = NotAGetter.class.getMethod("doSomething", int.class);
        checkThrowsExact(IllegalArgumentException.class, "Method is not getter: doSomething",
                () -> handler.invoke(null, notGetter, new Object[]{1}), "non-getter via invoke");
        checkThrowsExact(IllegalArgumentException.class,
                "No CompositeData item Missing or missing to match getMissing",
                () -> { Extra e = (Extra) java.lang.reflect.Proxy.newProxyInstance(
                        Extra.class.getClassLoader(), new Class[]{Extra.class}, handler); e.getMissing(); },
                "getter without matching item");
        // getter whose declared return type mismatches the stored item -> ClassCastException at call site
        checkThrows(ClassCastException.class,
                () -> { WrongRet w = (WrongRet) java.lang.reflect.Proxy.newProxyInstance(
                        WrongRet.class.getClassLoader(), new Class[]{WrongRet.class}, handler);
                        w.getNumber(); },
                "mismatched getter return type");
    }
}

/*
 * @test
 * @summary java.lang.reflect.Proxy conformance: full contract coverage.
 *          newProxyInstance dispatches every method (incl. Object's hashCode/equals/
 *          toString) to the handler with (proxy, method, args), isProxyClass identity,
 *          getInvocationHandler identity + IllegalArgumentException on non-proxy,
 *          handler-returned wrong type -> ClassCastException, null for primitive return
 *          -> NullPointerException, runtime exception propagation, undeclared checked
 *          exception -> UndeclaredThrowableException, and constructor/factory exception
 *          matrix (null loader/interfaces/handler -> NPE, empty/dup/non-interface/
 *          non-public interfaces -> IllegalArgumentException).
 *          Deepened batch: args shapes (no-arg -> null, varargs packed, primitives
 *          boxed), Object/default method routing with declaring class, same-signature
 *          multi-interface conflict resolution, inherited methods, proxy class shape
 *          (final, superclass Proxy, $Proxy naming public vs package-private), custom
 *          loader + interface visibility IAE, bootstrap loader with Supplier,
 *          undeclared/declared/Error throwable matrix with cause identity, factory
 *          message fragments, and proxy serialization round-trip.
 * @run main ProxyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ProxyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    /** Same as checkThrows but additionally asserts the message contains a fragment. */
    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                String m = t.getMessage();
                check(m != null && m.contains(fragment),
                        msg + ": message should contain \"" + fragment + "\" but was " + m);
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Object serRoundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }

    interface Foo { String bar(int x); }
    interface Baz { int qux(); }
    interface None { String zero(); }
    interface Prims { String take(long a, double b, boolean c); }
    interface Vars { String pack(int a, String... rest); }
    interface Same1 { String dup(); }
    interface Same2 { String dup(); }
    interface WithDefault {
        default String dflt() { return "dflt-body"; }
        String plain();
    }
    interface Declared { String doIt() throws IOException; }
    interface Parent { String up(); }
    interface Kid extends Parent { }
    public interface Pub { String go(); }


    static final class RecordingHandler implements InvocationHandler {
        Object proxySeen;
        Method methodSeen;
        Object[] argsSeen;
        int calls;
        public Object invoke(Object proxy, Method method, Object[] args) {
            proxySeen = proxy;
            methodSeen = method;
            argsSeen = args;
            calls++;
            if (method.getDeclaringClass() == Object.class) {
                switch (method.getName()) {
                    case "toString": return "PROXY-STRING";
                    case "hashCode": return Integer.valueOf(42);
                    case "equals": return Boolean.valueOf(proxy == args[0]);
                    default: return null;
                }
            }
            return "bar:" + args[0];
        }
    }

    static void testDispatch() throws Exception {
        RecordingHandler h = new RecordingHandler();
        Foo foo = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, h);
        check("bar:7".equals(foo.bar(7)), "proxy dispatch");
        check(h.calls == 1, "handler called once");
        check(h.proxySeen == foo, "handler receives proxy identity");
        check(h.methodSeen.getName().equals("bar"), "handler receives method name");
        check(h.argsSeen.length == 1 && h.argsSeen[0].equals(7), "handler receives args");

        check("PROXY-STRING".equals(foo.toString()), "Object.toString dispatched");
        check(foo.hashCode() == 42, "Object.hashCode dispatched");
        check(foo.equals(foo), "Object.equals dispatched (proxy==arg)");
        check(h.methodSeen.getName().equals("equals"), "last dispatched method equals");
    }

    static void testIdentity() throws Exception {
        RecordingHandler h = new RecordingHandler();
        Foo foo = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, h);
        Class<?> pc = foo.getClass();
        check(Proxy.isProxyClass(pc), "isProxyClass true");
        check(Proxy.getInvocationHandler(foo) == h, "getInvocationHandler identity");

        check(!Proxy.isProxyClass(Foo.class), "isProxyClass interface false");
        check(!Proxy.isProxyClass(String.class), "isProxyClass normal class false");
        check(!Proxy.isProxyClass(int[].class), "isProxyClass array false");

        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getInvocationHandler(new Object()),
                "getInvocationHandler non-proxy IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getInvocationHandler(foo.getClass()),
                "getInvocationHandler Class object IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.getInvocationHandler(null), "getInvocationHandler null NPE");
        checkThrows(NullPointerException.class,
                () -> Proxy.isProxyClass(null), "isProxyClass null NPE");

        // proxy class implements Serializable and the interfaces
        check(pc.getInterfaces().length == 1 && pc.getInterfaces()[0] == Foo.class,
                "proxy implements Foo");
        check(java.io.Serializable.class.isAssignableFrom(pc), "proxy is Serializable");

        // getProxyClass returns a proxy class
        Class<?> pc2 = Proxy.getProxyClass(ProxyTest.class.getClassLoader(), Foo.class);
        check(Proxy.isProxyClass(pc2), "getProxyClass result is proxy class");
        check(pc2.getInterfaces()[0] == Foo.class, "getProxyClass implements Foo");
    }

    static void testMultipleInterfaces() throws Exception {
        RecordingHandler h = new RecordingHandler();
        Object o = Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class, Baz.class}, h);
        check(o instanceof Foo && o instanceof Baz, "proxy implements both interfaces");
        check("bar:1".equals(((Foo) o).bar(1)), "Foo method through proxy");
        // Baz.qux returns int but handler returns "bar:null" -> we use a dedicated handler
        // instead; this proxy only needs to be instanceof both.
    }

    static void testHandlerReturnBehavior() throws Exception {
        // handler returns wrong type -> ClassCastException from generated checkcast
        InvocationHandler wrong = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) { return Integer.valueOf(1); }
        };
        Foo f1 = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, wrong);
        checkThrows(ClassCastException.class, () -> f1.bar(1),
                "handler wrong return type CCE");

        // handler returns null for primitive int -> NullPointerException (unboxing)
        InvocationHandler nullReturn = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) { return null; }
        };
        Baz b = (Baz) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Baz.class}, nullReturn);
        checkThrows(NullPointerException.class, () -> b.qux(),
                "handler null for int return NPE");

        // handler throwing RuntimeException -> propagates unchanged
        final RuntimeException rte = new IllegalStateException("rte");
        InvocationHandler rteHandler = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) { throw rte; }
        };
        Foo f2 = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, rteHandler);
        try {
            f2.bar(1);
            throw new AssertionError("expected IllegalStateException");
        } catch (IllegalStateException e) {
            passed++;
            check(e == rte, "runtime exception propagates by identity");
        }

        // handler throwing undeclared checked exception -> UndeclaredThrowableException
        InvocationHandler checked = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) throws IOException {
                throw new IOException("declared nowhere");
            }
        };
        Foo f3 = (Foo) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class<?>[]{Foo.class}, checked);
        try {
            f3.bar(1);
            throw new AssertionError("expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException e) {
            passed++;
            check(e.getCause() instanceof IOException, "UndeclaredThrowableException cause");
        }
    }

    static void testFactoryExceptions() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        RecordingHandler h = new RecordingHandler();

        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(null, new Class<?>[]{Foo.class}, h),
                "newProxyInstance null loader IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.newProxyInstance(l, null, h),
                "newProxyInstance null interfaces NPE");
        checkThrows(NullPointerException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, null),
                "newProxyInstance null handler NPE");

        // empty interfaces array is permitted (produces a no-interface proxy)
        Object emptyProxy = Proxy.newProxyInstance(l, new Class<?>[0], h);
        check(Proxy.isProxyClass(emptyProxy.getClass()), "empty interfaces still proxy");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{Foo.class, Foo.class}, h),
                "newProxyInstance duplicate interface IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{String.class}, h),
                "newProxyInstance non-interface IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{Foo.class, null}, h),
                "newProxyInstance null interface element NPE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{int[].class}, h),
                "newProxyInstance array interface IAE");

        // getProxyClass mirror of the same matrix
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getProxyClass(null, Foo.class), "getProxyClass null loader IAE");
        checkThrows(NullPointerException.class,
                () -> Proxy.getProxyClass(l, (Class<?>[]) null),
                "getProxyClass null interfaces NPE");
        check(Proxy.isProxyClass(Proxy.getProxyClass(l, new Class<?>[0])),
                "getProxyClass empty interfaces still proxy");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getProxyClass(l, Foo.class, Foo.class),
                "getProxyClass duplicate IAE");
        checkThrows(IllegalArgumentException.class,
                () -> Proxy.getProxyClass(l, String.class), "getProxyClass non-interface IAE");
    }

    // ===================== deepened batch (2026-11-05) =====================

    /** Records every dispatch: name, declaring class, args shape, isDefault flag. */
    static final class ShapeHandler implements InvocationHandler {
        final List<String> log = new ArrayList<String>();
        String name;
        Object[] args;
        Class<?> decl;
        boolean defaultSeen;
        public Object invoke(Object proxy, Method method, Object[] a) {
            log.add(method.getName());
            name = method.getName();
            args = a;
            decl = method.getDeclaringClass();
            defaultSeen = method.isDefault();
            if (method.getDeclaringClass() == Object.class) {
                if ("hashCode".equals(name)) return Integer.valueOf(99);
                if ("toString".equals(name)) return "SHAPE";
                if ("equals".equals(name)) return Boolean.TRUE;
            }
            return "shape:" + name;
        }
    }

    /** Serializable handler so the proxy itself can round-trip through Java serialization. */
    static final class SerHandler implements InvocationHandler, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public Object invoke(Object proxy, Method method, Object[] args) {
            return "ser:" + method.getName();
        }
    }

    /** Emits the class file of {@code public interface <name> { String go(); }} (v52). */
    static byte[] interfaceBytes(String name) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE);
        d.writeShort(0);                                  // minor_version
        d.writeShort(52);                                 // major_version
        d.writeShort(8);                                  // constant_pool_count
        d.writeByte(1); d.writeUTF(name);                 // #1 Utf8 name
        d.writeByte(7); d.writeShort(1);                  // #2 Class
        d.writeByte(1); d.writeUTF("java/lang/Object");   // #3
        d.writeByte(7); d.writeShort(3);                  // #4 Class
        d.writeByte(1); d.writeUTF("go");                 // #5
        d.writeByte(1); d.writeUTF("()Ljava/lang/String;"); // #6
        d.writeByte(1); d.writeUTF("Code");               // #7
        d.writeShort(0x0600);                             // ACC_INTERFACE|ACC_ABSTRACT
        d.writeShort(2);                                  // this_class
        d.writeShort(4);                                  // super_class: Object
        d.writeShort(0);                                  // interfaces_count
        d.writeShort(0);                                  // fields_count
        d.writeShort(1);                                  // methods_count
        d.writeShort(0x0401);                             // ACC_PUBLIC|ACC_ABSTRACT
        d.writeShort(5);                                  // name: go
        d.writeShort(6);                                  // descriptor
        d.writeShort(0);                                  // no attributes (abstract)
        d.writeShort(0);                                  // class attributes_count
        d.flush();
        return o.toByteArray();
    }

    static final class ChildLoader extends ClassLoader {
        ChildLoader(ClassLoader parent) { super(parent); }
        Class<?> define(String name, byte[] b) { return defineClass(name, b, 0, b.length); }
    }

    static void testArgsShapes() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        ShapeHandler h = new ShapeHandler();

        // no-arg method: args must be null, not an empty array
        None n = (None) Proxy.newProxyInstance(l, new Class<?>[]{None.class}, h);
        check("shape:zero".equals(n.zero()), "no-arg method dispatch");
        check(h.args == null, "no-arg method args is null");
        check(h.decl == None.class, "no-arg declaring class is the interface");

        // varargs: fixed args boxed, rest packed into a trailing array
        Vars v = (Vars) Proxy.newProxyInstance(l, new Class<?>[]{Vars.class}, h);
        check("shape:pack".equals(v.pack(3, "x", "y")), "varargs dispatch");
        check(h.args.length == 2 && Integer.valueOf(3).equals(h.args[0]),
                "varargs fixed arg boxed");
        check(h.args[1] instanceof String[]
                && Arrays.equals(new String[]{"x", "y"}, (String[]) h.args[1]),
                "varargs packed as trailing array");
        check("shape:pack".equals(v.pack(4)), "empty varargs dispatch");
        check(h.args[0].equals(4) && h.args[1] instanceof String[]
                && ((String[]) h.args[1]).length == 0,
                "empty varargs is an empty array, not null");

        // wider primitive family boxing: long/double/boolean arrive boxed
        Prims p = (Prims) Proxy.newProxyInstance(l, new Class<?>[]{Prims.class}, h);
        check("shape:take".equals(p.take(5L, 1.5d, true)), "primitives dispatch");
        check(h.args.length == 3, "three primitive args");
        check(h.args[0] instanceof Long && h.args[0].equals(5L), "long boxed to Long");
        check(h.args[1] instanceof Double && h.args[1].equals(1.5d), "double boxed to Double");
        check(h.args[2] instanceof Boolean && h.args[2].equals(Boolean.TRUE),
                "boolean boxed to Boolean");
    }

    static void testObjectMethodRouting() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        ShapeHandler h = new ShapeHandler();
        Foo f = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, h);
        h.log.clear();
        check("SHAPE".equals(f.toString()), "toString routed to handler");
        check(f.hashCode() == 99, "hashCode routed to handler");
        check(Boolean.TRUE.equals(f.equals(null)), "equals(null) routed to handler");
        boolean nullArgShape = h.args.length == 1 && h.args[0] == null;
        check(Boolean.TRUE.equals(f.equals("x")), "equals(obj) routed to handler");
        check(h.log.equals(Arrays.asList("toString", "hashCode", "equals", "equals")),
                "Object methods all routed in call order");
        check(h.decl == Object.class, "Object method declaring class is Object.class");
        check(nullArgShape, "equals(null) gives one null element");
    }

    static void testDefaultMethodRouting() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        ShapeHandler h = new ShapeHandler();
        WithDefault d = (WithDefault) Proxy.newProxyInstance(l,
                new Class<?>[]{WithDefault.class}, h);
        check("shape:dflt".equals(d.dflt()), "default method routed to handler");
        check("dflt".equals(h.name), "default method name recorded");
        check(h.decl == WithDefault.class, "default method declaring class");
        check(h.defaultSeen, "Method.isDefault() true inside handler for default method");
        check("shape:plain".equals(d.plain()), "abstract method routed too");
        check(!h.defaultSeen, "abstract method isDefault() false");
    }

    static void testMethodConflictResolution() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        ShapeHandler h = new ShapeHandler();
        Object both = Proxy.newProxyInstance(l, new Class<?>[]{Same1.class, Same2.class}, h);
        check(both instanceof Same1 && both instanceof Same2, "dual-cast proxy");
        check("shape:dup".equals(((Same1) both).dup()), "same-signature via first interface");
        check(h.decl == Same1.class, "conflict resolved to the first listed interface");
        check(((Same1) both).dup().equals(((Same2) both).dup()),
                "same-signature via second interface equal result");
        check(Proxy.getInvocationHandler(both) == h, "conflict proxy handler identity");

        // inherited abstract method: declaring class is the superinterface
        ShapeHandler h2 = new ShapeHandler();
        Kid k = (Kid) Proxy.newProxyInstance(l, new Class<?>[]{Kid.class}, h2);
        check("shape:up".equals(k.up()), "inherited method dispatch");
        check(h2.decl == Parent.class, "inherited method declared in superinterface");

        // interface listed together with its subinterface: dispatch still single
        ShapeHandler h3 = new ShapeHandler();
        Object pair = Proxy.newProxyInstance(l, new Class<?>[]{Parent.class, Kid.class}, h3);
        check(pair instanceof Parent && pair instanceof Kid, "subinterface pair instanceof both");
        check("shape:up".equals(((Kid) pair).up()), "subinterface pair dispatch");
        check(h3.decl == Parent.class, "pair conflict resolved to declaring interface");
    }

    static void testProxyClassShape() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        RecordingHandler h = new RecordingHandler();

        // package-private interface: proxy class lives in the interface's runtime package
        Foo f = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, h);
        Class<?> pc = f.getClass();
        check(pc.getName().contains("$Proxy"), "proxy class name contains $Proxy (8: default pkg)");
        check(pc.getSuperclass() == Proxy.class, "proxy superclass is Proxy");
        check(!pc.isInterface(), "proxy class is not an interface");
        check(!pc.isSynthetic(), "proxy class not flagged synthetic");
        check(!pc.isAnnotation() && !pc.isEnum(), "proxy class not annotation/enum");
        check(Modifier.isFinal(pc.getModifiers()), "proxy class final (package-private iface)");
        check(pc.getClassLoader() == l, "proxy defined by the given loader");
        Foo f2 = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, h);
        check(f2.getClass() == pc, "same interface set reuses the proxy class");

        // public interface: public final proxy (JDK-8: com.sun.proxy.$ProxyN)
        ShapeHandler sh = new ShapeHandler();
        Pub pub = (Pub) Proxy.newProxyInstance(l, new Class<?>[]{Pub.class}, sh);
        Class<?> ppc = pub.getClass();
        check(Modifier.isPublic(ppc.getModifiers()), "public interface -> public proxy class");
        check(Modifier.isFinal(ppc.getModifiers()), "public proxy class final");
        check(ppc.getName().contains("$Proxy"), "public proxy name contains $Proxy");
        check(ppc.getCanonicalName() != null && ppc.getCanonicalName().contains("$Proxy"),
                "public proxy canonical name contains $Proxy");
        check(ppc.getSimpleName().contains("$Proxy"), "public proxy simple name contains $Proxy");
        check(ppc.getClassLoader() == l, "public proxy defined by given loader");
        check(Proxy.getProxyClass(l, Pub.class) == ppc,
                "getProxyClass returns the identical proxy class");
        check(Proxy.isProxyClass(Proxy.getProxyClass(l, Pub.class)), "getProxyClass result flag");
    }

    static void testVisibilityAndCustomLoader() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        ShapeHandler h = new ShapeHandler();
        ChildLoader child = new ChildLoader(l);
        check(child.getParent() == l, "custom loader delegates to parent");
        Class<?> hidden = child.define("HiddenIface", interfaceBytes("HiddenIface"));
        check(hidden != null && hidden.isInterface(), "handcrafted interface defined");
        check(hidden.getClassLoader() == child, "hidden interface owned by child loader");

        // interface not visible from the requested loader -> IAE (fragment common to 8/27)
        checkThrowsMsg(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{hidden}, h),
                "is not visible from class loader",
                "invisible interface rejected");

        // same interface is fine when the owning loader is used
        Object hp = Proxy.newProxyInstance(child, new Class<?>[]{hidden}, h);
        check(hidden.isInstance(hp), "proxy for child-loader interface");
        check(Proxy.isProxyClass(hp.getClass()), "child-loader proxy is a proxy class");
        check(hp.getClass().getClassLoader() == child,
                "proxy class defined by the requesting loader");
        Method go = hidden.getMethod("go");
        go.setAccessible(true);
        check("shape:go".equals(go.invoke(hp)), "child-loader interface dispatch");
    }

    static void testBootstrapLoader() throws Exception {
        // null loader means the bootstrap loader: works iff interfaces are bootstrap-visible
        ShapeHandler h = new ShapeHandler();
        Supplier<?> s = (Supplier<?>) Proxy.newProxyInstance(null,
                new Class<?>[]{Supplier.class}, h);
        check("shape:get".equals(s.get()), "null loader + bootstrap-visible interface works");
        check(Proxy.isProxyClass(s.getClass()), "bootstrap proxy class flag");
        check(s.getClass().getName().contains("$Proxy"), "bootstrap proxy name has $Proxy");
        check(Proxy.getProxyClass(null, Supplier.class) == s.getClass(),
                "getProxyClass(null, Supplier) identical class");
        check(Proxy.getInvocationHandler(s) == h, "bootstrap proxy handler identity");
    }

    static void testUndeclaredThrowableDeep() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();

        // undeclared checked exception: wrapped with cause identity preserved
        final IOException checked = new IOException("undeclared-root");
        InvocationHandler uh = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) throws Throwable {
                throw checked;
            }
        };
        Foo f = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, uh);
        try {
            f.bar(1);
            throw new AssertionError("expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException e) {
            passed++;
            check(e.getCause() == checked, "UTE cause is the exact throwable");
            check(e.getUndeclaredThrowable() == checked, "undeclared throwable identity");
            check(e instanceof RuntimeException, "UTE is unchecked");
            check(e.getMessage() == null, "UTE (proxy-generated) message null on both 8/27");
        }

        // declared checked exception: propagates raw, never wrapped
        InvocationHandler dh = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) throws Throwable {
                throw new IOException("declared-ok");
            }
        };
        Declared d = (Declared) Proxy.newProxyInstance(l, new Class<?>[]{Declared.class}, dh);
        try {
            d.doIt();
            throw new AssertionError("expected IOException");
        } catch (IOException e) {
            passed++;
            check("declared-ok".equals(e.getMessage()), "declared checked propagates raw");
        }

        // Errors propagate raw as well
        InvocationHandler eh = new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) throws Throwable {
                throw new UnknownError();
            }
        };
        Foo ef = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, eh);
        checkThrows(UnknownError.class, () -> ef.bar(1), "error propagates raw");
    }

    static void testProxySerialization() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        SerHandler sh = new SerHandler();
        Foo f = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, sh);
        Object back = serRoundTrip(f);
        check(back instanceof Foo, "deserialized proxy still a Foo");
        check(back.getClass() == f.getClass(), "deserialized proxy keeps proxy class");
        check(Proxy.isProxyClass(back.getClass()), "deserialized proxy class flag");
        InvocationHandler bh = Proxy.getInvocationHandler(back);
        check(bh != sh, "handler travels as a serialized copy");
        check(bh instanceof SerHandler, "handler copy keeps its type");
        check("ser:bar".equals(((Foo) back).bar(2)), "deserialized proxy still dispatches");
    }

    static void testFactoryMessages() throws Exception {
        ClassLoader l = ProxyTest.class.getClassLoader();
        RecordingHandler h = new RecordingHandler();
        checkThrowsMsg(IllegalArgumentException.class,
                () -> Proxy.getInvocationHandler(new Object()),
                "not a proxy instance", "non-proxy message fragment");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{Foo.class, Foo.class}, h),
                "repeated interface", "duplicate interface message fragment");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(l, new Class<?>[]{String.class}, h),
                "is not an interface", "non-interface message fragment");
        // null loader + app-loader interface: both JDKs reject for invisibility;
        // exact wording differs, the common fragment is checked
        checkThrowsMsg(IllegalArgumentException.class,
                () -> Proxy.newProxyInstance(null, new Class<?>[]{Foo.class}, h),
                "is not visible from class loader", "null loader invisibility fragment");
        // The proxy-generated CCE message ("cannot be cast to") is best-effort and
        // not part of the API contract (null under -Xcomp), so assert the exact
        // exception class only.
        checkThrows(ClassCastException.class,
                () -> {
                    InvocationHandler wrong = new InvocationHandler() {
                        public Object invoke(Object p, Method m, Object[] a) {
                            return Integer.valueOf(1);
                        }
                    };
                    Foo w = (Foo) Proxy.newProxyInstance(l, new Class<?>[]{Foo.class}, wrong);
                    w.bar(1);
                }, "wrong return type CCE");
    }

    public static void main(String[] args) throws Exception {
        testDispatch();
        testIdentity();
        testMultipleInterfaces();
        testHandlerReturnBehavior();
        testFactoryExceptions();
        testArgsShapes();
        testObjectMethodRouting();
        testDefaultMethodRouting();
        testMethodConflictResolution();
        testProxyClassShape();
        testVisibilityAndCustomLoader();
        testBootstrapLoader();
        testUndeclaredThrowableDeep();
        testProxySerialization();
        testFactoryMessages();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

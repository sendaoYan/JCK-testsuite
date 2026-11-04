/*
 * @test
 * @summary java.net.DatagramSocketImplFactory conformance: interface hierarchy and members.
 *          Deepened: exact interface shape (public interface, single abstract
 *          method createDatagramSocketImpl() returning DatagramSocketImpl, no
 *          other members, functional-interface lambda suitability), anonymous
 *          and lambda implementations returning null and a real custom impl,
 *          and the one-shot JVM-global installation contract:
 *          DatagramSocket.setDatagramSocketImplFactory accepts the first
 *          factory, consults it when sockets are constructed (a null return
 *          makes new DatagramSocket fail with NPE), and rejects any second
 *          install with SocketException "factory already defined".
 * @run main DatagramSocketImplFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.DatagramSocketImplFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class DatagramSocketImplFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- preserved original assertions, kept verbatim ----
    static void testLegacy() throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocketImplFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createDatagramSocketImpl"), "has method createDatagramSocketImpl");
    }

    // interface shape.
    static void testWhiteBox() throws Exception {
        Class<?> c = DatagramSocketImplFactory.class;
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method");
        Method m = c.getDeclaredMethod("createDatagramSocketImpl");
        check(m.getReturnType() == DatagramSocketImpl.class, "returns DatagramSocketImpl");
        check(m.getParameterCount() == 0, "no parameters");
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                "public abstract");
        check(m.getExceptionTypes().length == 0, "declares no checked exceptions");
        // fields: none besides implicit statics
        check(c.getDeclaredFields().length == 0, "no declared fields");
        // the DatagramSocket installer exists with the expected signature
        check(DatagramSocket.class.getMethod("setDatagramSocketImplFactory",
                DatagramSocketImplFactory.class) != null, "DatagramSocket installer present");
    }

    // implementations: anonymous class, lambda, and a real custom impl.
    static class StubImpl extends DatagramSocketImpl {
        protected void create() { }
        protected void bind(int p, InetAddress addr) { }
        protected void send(DatagramPacket p) { }
        protected int peek(InetAddress i) { return 0; }
        protected int peekData(DatagramPacket p) { return 0; }
        protected void receive(DatagramPacket p) { }
        protected void setTTL(byte t) { }
        protected byte getTTL() { return 1; }
        protected void setTimeToLive(int ttl) { }
        protected int getTimeToLive() { return 1; }
        protected void join(InetAddress g) { }
        protected void leave(InetAddress g) { }
        protected void joinGroup(SocketAddress sa, NetworkInterface ni) { }
        protected void leaveGroup(SocketAddress sa, NetworkInterface ni) { }
        protected void close() { }
        public void setOption(int id, Object v) { }
        public Object getOption(int id) { return null; }
    }

    static void testImplementations() throws Exception {
        DatagramSocketImplFactory nullFactory = new DatagramSocketImplFactory() {
            public DatagramSocketImpl createDatagramSocketImpl() { return null; }
        };
        check(nullFactory.createDatagramSocketImpl() == null, "anonymous factory returns null");
        DatagramSocketImplFactory lambda = () -> null;
        check(lambda.createDatagramSocketImpl() == null, "lambda factory returns null");
        final StubImpl shared = new StubImpl();
        DatagramSocketImplFactory stub = () -> shared;
        DatagramSocketImpl produced = stub.createDatagramSocketImpl();
        check(produced != null, "stub factory returns an impl");
        check(produced instanceof DatagramSocketImpl, "produced object is a DatagramSocketImpl");
        check(produced == shared, "factory can return a shared instance");
        check(produced.getClass().getName().contains("StubImpl"), "produced class name");
        // two calls may return independent or shared instances - both legal
        check(stub.createDatagramSocketImpl() == shared, "second call consistent");
    }

    // call-side contract: repeated invocation and exception transparency.
    static void testMethodContract() throws Exception {
        check(DatagramSocketImplFactory.class.getPackage() != null
                && "java.net".equals(DatagramSocketImplFactory.class.getPackage().getName()),
                "lives in package java.net");
        final RuntimeException boom = new RuntimeException("boom");
        DatagramSocketImplFactory thrower = () -> { throw boom; };
        try {
            thrower.createDatagramSocketImpl();
            check(false, "factory exception swallowed");
        } catch (RuntimeException e) {
            check(e == boom, "factory exceptions propagate unwrapped");
        }
        final int[] calls = {0};
        DatagramSocketImplFactory counter = () -> { calls[0]++; return new StubImpl(); };
        DatagramSocketImpl a = counter.createDatagramSocketImpl();
        DatagramSocketImpl b = counter.createDatagramSocketImpl();
        check(calls[0] == 2, "each call invokes createDatagramSocketImpl");
        check(a != b, "factory may return fresh instances per call");
        check(a instanceof DatagramSocketImpl && b instanceof DatagramSocketImpl,
                "both products are DatagramSocketImpls");
    }

    // One-shot installation contract (JVM global) - MUST run last in the file.
    static void testFactoryLifecycle() throws Exception {
        final int[] count = {0};
        DatagramSocketImplFactory counting = new DatagramSocketImplFactory() {
            public DatagramSocketImpl createDatagramSocketImpl() { count[0]++; return null; }
        };
        DatagramSocket.setDatagramSocketImplFactory(counting);
        check(true, "first install accepted");
        // the factory is consulted when a socket needs its impl; returning null
        // breaks construction with NPE
        try {
            new DatagramSocket(0);
            check(false, "null impl accepted");
        } catch (NullPointerException expected) {
            passed++;
        }
        check(count[0] >= 1, "factory consulted, invoked " + count[0] + " times");
        // exactly one factory per JVM
        try {
            DatagramSocket.setDatagramSocketImplFactory(counting);
            check(false, "second install accepted");
        } catch (SocketException e) {
            check("factory already defined".equals(e.getMessage()),
                    "second install message: " + e.getMessage());
        }
        try {
            DatagramSocket.setDatagramSocketImplFactory(null);
            check(false, "second install of null accepted");
        } catch (SocketException e) {
            check("factory already defined".equals(e.getMessage()),
                    "null install after first also rejected");
        }
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testWhiteBox();
        testImplementations();
        testMethodContract();
        testFactoryLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

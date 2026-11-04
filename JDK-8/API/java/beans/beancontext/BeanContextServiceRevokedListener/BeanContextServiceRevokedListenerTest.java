/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceRevokedListener conformance: white-box structure + black-box behavior.
 *          Exact interface shape (single serviceRevoked, direct extension of java.util.EventListener,
 *          not Serializable); the real flow through BeanContextServicesSupport: a revoked listener
 *          handed to getService is notified exactly once per revoke with exact serviceClass, the
 *          invalidate flag mirroring revokeNow (false and true), and the context as source; the
 *          in-use probe record - after a revoke(now=true) a subsequent getService resolves to null
 *          (there is no ServiceUnavailableException path in this API); one notification per
 *          outstanding requestor entry; a re-acquired reference after revoke(false)+re-add works;
 *          and a throwing listener propagates its RuntimeException out of revokeService.
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextServiceRevokedListenerTest
 */

import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextServiceProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BeanContextServiceRevokedListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static String methodNames(Class<?> c) {
        List<String> l = new ArrayList<String>();
        for (Method m : c.getDeclaredMethods()) l.add(m.getName());
        Collections.sort(l);
        return l.toString();
    }

    public interface MyService { String name(); }

    public static class MyProvider implements BeanContextServiceProvider {
        int getC, relC;
        final Object svc = new Object();
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { getC++; return svc; }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { relC++; }
        public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
            return Collections.emptyList().iterator();
        }
    }

    public static class MyRevokedListener implements BeanContextServiceRevokedListener {
        int n;
        BeanContextServiceRevokedEvent last;
        RuntimeException boom;
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { n++; last = e; if (boom != null) throw boom; }
    }

    public static class MyServicesListener implements java.beans.beancontext.BeanContextServicesListener {
        int availableCount, revokedCount;
        public void serviceAvailable(java.beans.beancontext.BeanContextServiceAvailableEvent e) { availableCount++; }
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { revokedCount++; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceRevokedListener");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(java.util.EventListener.class.isAssignableFrom(c), "implements/extends java.util.EventListener");
        check(hasMethod(c, "serviceRevoked"), "declares serviceRevoked");
        // exact inventory
        check(methodNames(c).equals("[serviceRevoked]"), "exact single-method inventory [serviceRevoked]");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
        Class<?>[] direct = c.getInterfaces();
        check(direct.length == 1 && direct[0] == java.util.EventListener.class,
                "direct superinterface is exactly java.util.EventListener");
        Method m = c.getDeclaredMethods()[0];
        check(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                "serviceRevoked is public abstract");
        check(m.getReturnType() == void.class, "serviceRevoked returns void");
        Class<?>[] ps = m.getParameterTypes();
        check(ps.length == 1 && ps[0] == BeanContextServiceRevokedEvent.class,
                "serviceRevoked takes exactly BeanContextServiceRevokedEvent");
        check(m.getExceptionTypes().length == 0, "serviceRevoked declares no checked exceptions");
        check(!java.io.Serializable.class.isAssignableFrom(c), "listener interface is not Serializable");
        // subsumption: BeanContextServicesListener extends this interface
        check(BeanContextServiceRevokedListener.class.isAssignableFrom(
                java.beans.beancontext.BeanContextServicesListener.class),
                "BeanContextServicesListener subsumes this interface");
        // the hub context implements it (via its interfaces) and can be used polymorphically
        check(new BeanContextServicesSupport() instanceof BeanContextServiceRevokedListener,
                "BeanContextServicesSupport is a BeanContextServiceRevokedListener");
        // deprecation status on this JDK
        check(c.getAnnotation(Deprecated.class) == null, "not deprecated on JDK 8");
    }

    static void testBlackBox() throws Exception {
        final int[] n = new int[1];
        BeanContextServiceRevokedListener l = new BeanContextServiceRevokedListener() {
            public void serviceRevoked(BeanContextServiceRevokedEvent e) { n[0]++; }
        };
        l.serviceRevoked(new BeanContextServiceRevokedEvent(new BeanContextServicesSupport(), Object.class, true));
        check(n[0] == 1, "serviceRevoked dispatch");
        // independent repeated dispatch
        l.serviceRevoked(new BeanContextServiceRevokedEvent(new BeanContextServicesSupport(), Object.class, false));
        check(n[0] == 2, "serviceRevoked dispatched a second time");
        // two listener instances are independent counters
        MyRevokedListener a = new MyRevokedListener();
        MyRevokedListener b = new MyRevokedListener();
        BeanContextServiceRevokedEvent e = new BeanContextServiceRevokedEvent(new BeanContextServicesSupport(), MyService.class, true);
        a.serviceRevoked(e);
        check(a.n == 1 && b.n == 0, "only the invoked listener counts");
        b.serviceRevoked(e);
        check(a.n == 1 && b.n == 1, "each listener keeps its own count");
        check(a.last == b.last && a.last == e, "the same event instance can be handed to many listeners");
    }

    static void testRealFlow() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        MyProvider p = new MyProvider();
        bcss.addService(MyService.class, p);
        BeanContextChildSupport child = new BeanContextChildSupport();
        bcss.add(child);
        MyRevokedListener rl = new MyRevokedListener();
        Object requestor = new Object();
        check(rl.n == 0, "no notifications before any revoke");
        Object svc = bcss.getService(child, requestor, MyService.class, null, rl);
        check(svc == p.svc && rl.n == 0, "request alone fires no revoke event");
        // revokeNow=false: listener notified once, invalidate=false
        bcss.revokeService(MyService.class, p, false);
        check(rl.n == 1, "listener notified exactly once on revoke");
        check(rl.last.getServiceClass() == MyService.class, "event serviceClass exact");
        check(!rl.last.isCurrentServiceInvalidNow(), "invalidate flag mirrors revokeNow=false");
        check(rl.last.getSourceAsBeanContextServices() == bcss, "event source is the context");
        check(rl.last.isServiceClass(MyService.class), "event matches the revoked class");
        // re-register and re-acquire: the same listener can serve another cycle
        bcss.addService(MyService.class, p);
        bcss.getService(child, requestor, MyService.class, null, rl);
        bcss.revokeService(MyService.class, p, true);
        check(rl.n == 2, "second cycle notifies again");
        check(rl.last.isCurrentServiceInvalidNow(), "invalidate flag mirrors revokeNow=true");
        // the provider itself is never called at revoke time
        check(p.relC == 0, "provider sees no releaseService from the revoke flow");
        // after a revoke(now=true) the service is simply gone: getService resolves to null;
        // there is NO ServiceUnavailableException in this API (probe-verified on both JDKs)
        check(bcss.getService(child, requestor, MyService.class, null, rl) == null,
                "post-revoke getService resolves to null, no ServiceUnavailableException");
        // one notification per outstanding requestor entry
        BeanContextServicesSupport b2 = new BeanContextServicesSupport();
        MyProvider p2 = new MyProvider();
        b2.addService(MyService.class, p2);
        BeanContextChildSupport c2 = new BeanContextChildSupport();
        b2.add(c2);
        MyRevokedListener shared = new MyRevokedListener();
        b2.getService(c2, new Object(), MyService.class, null, shared);
        b2.getService(c2, new Object(), MyService.class, null, shared);
        check(shared.n == 0, "two outstanding requestors, no events yet");
        b2.revokeService(MyService.class, p2, true);
        check(shared.n == 2, "one event per outstanding requestor entry");
        // a listener registered via getService is NOT notified for other services' revocations
        BeanContextServicesSupport b3 = new BeanContextServicesSupport();
        MyProvider p3 = new MyProvider();
        b3.addService(MyService.class, p3);
        b3.addService(Integer.class, p3);
        BeanContextChildSupport c3 = new BeanContextChildSupport();
        b3.add(c3);
        MyRevokedListener onlyMine = new MyRevokedListener();
        b3.getService(c3, new Object(), MyService.class, null, onlyMine);
        b3.revokeService(Integer.class, p3, true);
        check(onlyMine.n == 0, "not notified for an unrelated service");
        b3.revokeService(MyService.class, p3, true);
        check(onlyMine.n == 1, "notified for the service it holds");
    }

    static void testExceptionPropagation() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        final MyProvider p = new MyProvider();
        bcss.addService(MyService.class, p);
        BeanContextChildSupport child = new BeanContextChildSupport();
        bcss.add(child);
        final MyRevokedListener boom = new MyRevokedListener();
        boom.boom = new IllegalStateException("revoked-listener-boom");
        bcss.getService(child, new Object(), MyService.class, null, boom);
        checkThrows(IllegalStateException.class, new ThrowingRunnable() {
            public void run() { bcss.revokeService(MyService.class, p, true); } },
                "listener exception propagates out of revokeService");
        check(!bcss.hasService(MyService.class), "revocation still applied despite the throw");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealFlow();
        testExceptionPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

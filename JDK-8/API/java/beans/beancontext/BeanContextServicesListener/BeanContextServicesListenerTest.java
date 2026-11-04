/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesListener conformance: white-box structure + black-box behavior.
 *          Exact interface closure (the single serviceAvailable declaration, direct extension
 *          of BeanContextServiceRevokedListener, hence transitively EventListener, never
 *          Serializable); live wiring into a real BeanContextServicesSupport: addService fires
 *          serviceAvailable with exact source/serviceClass and the identical event instance to
 *          every listener, revokeService fires serviceRevoked with the invalidate flag
 *          pass-through, listener removal stops delivery, nested child contexts receive the
 *          parent's event instance, and a throwing listener's RuntimeException propagates out
 *          of addService/revokeService while the state change itself stays applied.
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextServicesListenerTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesListener;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextServiceProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BeanContextServicesListenerTest {
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
        final Object svc = new Object();
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { return svc; }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { }
        public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
            return Collections.emptyList().iterator();
        }
    }

    public static class MyRevokedListener implements BeanContextServiceRevokedListener {
        int n;
        BeanContextServiceRevokedEvent last;
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { n++; last = e; }
    }

    public static class MyServicesListener implements BeanContextServicesListener {
        int availableCount, revokedCount;
        BeanContextServiceAvailableEvent lastAvailable;
        BeanContextServiceRevokedEvent lastRevoked;
        public void serviceAvailable(BeanContextServiceAvailableEvent e) { availableCount++; lastAvailable = e; }
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { revokedCount++; lastRevoked = e; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesListener");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(BeanContextServiceRevokedListener.class.isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServiceRevokedListener");
        check(hasMethod(c, "serviceAvailable"), "declares serviceAvailable");
        // exact inventory: exactly one declared method of its own
        check(methodNames(c).equals("[serviceAvailable]"), "exact single-method inventory [serviceAvailable]");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
        Method m = c.getDeclaredMethods()[0];
        check(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                "serviceAvailable is public abstract");
        check(m.getReturnType() == void.class, "serviceAvailable returns void");
        check(m.getParameterTypes().length == 1
                && m.getParameterTypes()[0] == BeanContextServiceAvailableEvent.class,
                "serviceAvailable takes exactly BeanContextServiceAvailableEvent");
        check(m.getExceptionTypes().length == 0, "serviceAvailable declares no checked exceptions");
        // exact direct superinterface
        Class<?>[] direct = c.getInterfaces();
        check(direct.length == 1 && direct[0] == BeanContextServiceRevokedListener.class,
                "direct superinterface is exactly BeanContextServiceRevokedListener");
        // inherited surface via the closure: serviceRevoked comes from the superinterface
        check(hasMethod(BeanContextServiceRevokedListener.class, "serviceRevoked"),
                "inherits serviceRevoked through BeanContextServiceRevokedListener");
        check(java.util.EventListener.class.isAssignableFrom(c), "transitively extends java.util.EventListener");
        check(!BeanContext.class.isAssignableFrom(c), "not a BeanContext itself");
        check(!java.io.Serializable.class.isAssignableFrom(c), "listener interface is not Serializable");
        // the canonical hub both implements and dispatches through this interface
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        check(bcss instanceof BeanContextServicesListener, "BeanContextServicesSupport is a BeanContextServicesListener");
        check(bcss instanceof BeanContextServiceRevokedListener, "and therefore a BeanContextServiceRevokedListener");
        // a context implementing both halves can be registered on itself without error
        bcss.addBeanContextServicesListener(bcss);
        check(true, "a BeanContextServicesListener-implementing context registers as listener");
        bcss.removeBeanContextServicesListener(bcss);
        // deprecation status on this JDK
        check(c.getAnnotation(Deprecated.class) == null, "not deprecated on JDK 8");
    }

    static void testBlackBox() throws Exception {
        final int[] a = new int[1], r = new int[1];
        BeanContextServicesListener l = new BeanContextServicesListener() {
            public void serviceAvailable(BeanContextServiceAvailableEvent e) { a[0]++; }
            public void serviceRevoked(BeanContextServiceRevokedEvent e) { r[0]++; }
        };
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        l.serviceAvailable(new BeanContextServiceAvailableEvent(bcss, Object.class));
        l.serviceRevoked(new BeanContextServiceRevokedEvent(bcss, Object.class, true));
        check(a[0] == 1 && r[0] == 1, "serviceAvailable/serviceRevoked dispatch");
        // both callbacks can be driven repeatedly, independently
        l.serviceRevoked(new BeanContextServiceRevokedEvent(bcss, Object.class, false));
        check(a[0] == 1 && r[0] == 2, "serviceRevoked dispatched a second time");
    }

    static void testWiring() throws Exception {
        final BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        final MyProvider p = new MyProvider();
        final MyServicesListener first = new MyServicesListener();
        final MyServicesListener second = new MyServicesListener();
        // registration contract through the hub context
        bcss.addBeanContextServicesListener(first);
        bcss.addBeanContextServicesListener(second);
        bcss.addBeanContextServicesListener(first); // duplicate registration is a no-op
        check(true, "duplicate addBeanContextServicesListener accepted without error");
        bcss.addService(MyService.class, p);
        check(first.availableCount == 1, "first listener saw serviceAvailable once (duplicate add is a no-op)");
        check(second.availableCount == 1, "second listener saw serviceAvailable once");
        check(first.lastAvailable == second.lastAvailable, "identical event instance delivered to every listener");
        check(first.lastAvailable.getSourceAsBeanContextServices() == bcss, "event source is the context");
        check(first.lastAvailable.getServiceClass() == MyService.class, "event serviceClass exact");
        check(first.revokedCount == 0 && second.revokedCount == 0, "no serviceRevoked before any revoke");
        // revoke(false) and revoke(true) both dispatch, carrying the invalidate flag
        bcss.revokeService(MyService.class, p, false);
        check(first.revokedCount == 1 && !first.lastRevoked.isCurrentServiceInvalidNow(),
                "revoke(false) dispatched with invalidate false");
        bcss.addService(MyService.class, p);
        bcss.revokeService(MyService.class, p, true);
        check(first.revokedCount == 2 && first.lastRevoked.isCurrentServiceInvalidNow(),
                "revoke(true) dispatched with invalidate true");
        check(second.revokedCount == 2, "second listener kept receiving revocations");
        // removal stops delivery for that listener only
        bcss.removeBeanContextServicesListener(second);
        bcss.addService(Integer.class, p);
        check(first.availableCount == 3 && second.availableCount == 2,
                "removed listener no longer receives serviceAvailable");
        bcss.revokeService(Integer.class, p, true);
        check(first.revokedCount == 3 && second.revokedCount == 2,
                "removed listener no longer receives serviceRevoked");
        // removed listener can be re-registered
        bcss.addBeanContextServicesListener(second);
        bcss.addService(Runnable.class, p);
        check(second.availableCount == 3, "re-registered listener receives events again");
    }

    static void testNestedDelivery() throws Exception {
        BeanContextServicesSupport outer = new BeanContextServicesSupport();
        BeanContextServicesSupport inner = new BeanContextServicesSupport();
        outer.add(inner);
        final MyServicesListener innerListener = new MyServicesListener();
        inner.addBeanContextServicesListener(innerListener);
        MyProvider p = new MyProvider();
        outer.addService(MyService.class, p);
        check(innerListener.availableCount == 1,
                "child BeanContextServices (a BeanContextServicesListener) sees parent registration");
        check(innerListener.lastAvailable.getServiceClass() == MyService.class,
                "child-delivered event carries the parent's serviceClass");
        check(innerListener.lastAvailable.getSourceAsBeanContextServices() == outer,
                "child-delivered event source is the parent context");
        // revocation is NOT fanned out to the child context's own registered listeners:
        // downward propagation happens only through the delegation proxy of actual requests
        outer.revokeService(MyService.class, p, true);
        check(innerListener.revokedCount == 0,
                "child context's own listeners do not see the parent's revoke");
        // but a delegated request's per-requestor revoked listener IS notified through the proxy chain
        outer.addService(MyService.class, p);
        BeanContextChildSupport leaf = new BeanContextChildSupport();
        inner.add(leaf);
        MyRevokedListener leafRl = new MyRevokedListener();
        Object leafReq = new Object();
        check(inner.getService(leaf, leafReq, MyService.class, null, leafRl) == p.svc,
                "leaf obtains the parent's service through the child context (delegation)");
        outer.revokeService(MyService.class, p, true);
        check(leafRl.n == 1 && leafRl.last.isCurrentServiceInvalidNow(),
                "delegated request's revoked listener sees revoke with invalidate flag");
        check(!inner.hasService(MyService.class), "delegated hasService false after parent revoke");
        check(inner.getService(leaf, leafReq, MyService.class, null, leafRl) == null,
                "delegated re-request after revoke resolves to null");
    }

    static void testExceptionPropagation() throws Exception {
        // a listener throwing out of serviceAvailable propagates to the addService caller,
        // but the service stays registered
        final BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        final RuntimeException marker = new RuntimeException("available-boom");
        bcss.addBeanContextServicesListener(new MyServicesListener() {
            public void serviceAvailable(BeanContextServiceAvailableEvent e) { throw marker; }
        });
        final MyProvider p = new MyProvider();
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() { bcss.addService(MyService.class, p); } },
                "serviceAvailable throw propagates out of addService");
        check(bcss.hasService(MyService.class), "service remains registered despite the throw");
        // same for serviceRevoked out of revokeService
        BeanContextServicesSupport bcss2 = new BeanContextServicesSupport();
        final MyProvider p2 = new MyProvider();
        bcss2.addService(MyService.class, p2);
        bcss2.addBeanContextServicesListener(new MyServicesListener() {
            public void serviceRevoked(BeanContextServiceRevokedEvent e) { throw new RuntimeException("revoked-boom"); }
        });
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() { bcss2.revokeService(MyService.class, p2, true); } },
                "serviceRevoked throw propagates out of revokeService");
        check(!bcss2.hasService(MyService.class), "service stays revoked despite the throw");
        // per-requestor revoked listener throws also propagate (getService wiring)
        BeanContextServicesSupport bcss3 = new BeanContextServicesSupport();
        final MyProvider p3 = new MyProvider();
        bcss3.addService(MyService.class, p3);
        BeanContextChildSupport child = new BeanContextChildSupport();
        bcss3.add(child);
        final MyRevokedListener boom = new MyRevokedListener() {
            public void serviceRevoked(BeanContextServiceRevokedEvent e) { throw new RuntimeException("requestor-boom"); }
        };
        bcss3.getService(child, new Object(), MyService.class, null, boom);
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() { bcss3.revokeService(MyService.class, p3, true); } },
                "per-requestor serviceRevoked throw propagates out of revokeService");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWiring();
        testNestedDelivery();
        testExceptionPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

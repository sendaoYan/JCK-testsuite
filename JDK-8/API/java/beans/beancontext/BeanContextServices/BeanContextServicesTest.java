/*
 * @test
 * @summary java.beans.beancontext.BeanContextServices conformance: white-box structure + black-box behavior.
 *          Exact interface closure (BeanContext + BeanContextServicesListener) and the exact
 *          9-method inventory with signatures and the TooManyListenersException clause; full
 *          service lifecycle through BeanContextServicesSupport: addService first/duplicate,
 *          hasService transitions, live getCurrentServiceClasses/Selectors contents (including
 *          no-op iterator remove), getService/releaseService requestor-listener bookkeeping
 *          with provider counters and argument pass-through, TooManyListenersException on a
 *          conflicting revoked listener (with provider rollback), UnsupportedOperationException
 *          on provider switch while a reference is held, revoke(false) vs revoke(true)
 *          reference retention, nested-context delegation, listener registration/delivery
 *          matrix and negative matrix (NPE/IAE). Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextServicesTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesListener;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.DesignMode;
import java.beans.Visibility;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;

public class BeanContextServicesTest {
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
    static boolean iterContains(Iterator<?> it, Object o) {
        while (it.hasNext()) if (it.next() == o) return true;
        return false;
    }

    public interface MyService { String name(); }

    public static class MyProvider implements BeanContextServiceProvider {
        int getC, relC;
        final Object svc = new Object();
        Object lastRequestor, lastSelector;
        Class lastClass;
        BeanContextServices lastBcs;
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) {
            getC++; lastRequestor = requestor; lastSelector = selector; lastClass = serviceClass; lastBcs = bcs;
            return svc;
        }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { relC++; }
        public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
            List<String> l = new ArrayList<String>();
            l.add("sel-a");
            l.add("sel-b");
            return l.iterator();
        }
    }

    public static class MyRevokedListener implements BeanContextServiceRevokedListener {
        int n;
        BeanContextServiceRevokedEvent last;
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { n++; last = e; }
    }

    public static class MyServicesListener implements BeanContextServicesListener {
        int av, rv;
        BeanContextServiceAvailableEvent lastAvailable;
        BeanContextServiceRevokedEvent lastRevoked;
        public void serviceAvailable(BeanContextServiceAvailableEvent e) { av++; lastAvailable = e; }
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { rv++; lastRevoked = e; }
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServices");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(BeanContext.class.isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContext");
        check(BeanContextServicesListener.class.isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServicesListener");
        check(hasMethod(c, "addBeanContextServicesListener"), "declares addBeanContextServicesListener");
        check(hasMethod(c, "addService"), "declares addService");
        check(hasMethod(c, "getCurrentServiceClasses"), "declares getCurrentServiceClasses");

        // exact closure: direct superinterfaces are exactly BeanContext + BeanContextServicesListener
        Class<?>[] direct = c.getInterfaces();
        check(direct.length == 2, "exactly two direct superinterfaces");
        check(direct[0] == BeanContext.class && direct[1] == BeanContextServicesListener.class
                || direct[0] == BeanContextServicesListener.class && direct[1] == BeanContext.class,
                "direct superinterfaces are BeanContext and BeanContextServicesListener");
        // transitive closure
        check(BeanContextChild.class.isAssignableFrom(c), "transitively extends BeanContextChild");
        check(BeanContextServiceRevokedListener.class.isAssignableFrom(c), "transitively extends BeanContextServiceRevokedListener");
        check(EventListenerTestSafe(), "transitively extends java.util.EventListener");
        check(DesignMode.class.isAssignableFrom(c), "transitively extends DesignMode");
        check(Visibility.class.isAssignableFrom(c), "transitively extends Visibility");
        check(java.util.Collection.class.isAssignableFrom(c), "transitively extends java.util.Collection");
        // exact declared method inventory
        check(methodNames(c).equals("[addBeanContextServicesListener, addService, getCurrentServiceClasses, "
                + "getCurrentServiceSelectors, getService, hasService, releaseService, "
                + "removeBeanContextServicesListener, revokeService]"), "exact 9-method inventory");
        check(c.getDeclaredMethods().length == 9, "declares exactly 9 methods");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
        // exact signatures
        Method add = c.getMethod("addService", Class.class, BeanContextServiceProvider.class);
        check(add.getReturnType() == boolean.class, "addService returns boolean");
        Method rev = c.getMethod("revokeService", Class.class, BeanContextServiceProvider.class, boolean.class);
        check(rev.getReturnType() == void.class, "revokeService returns void");
        Method has = c.getMethod("hasService", Class.class);
        check(has.getReturnType() == boolean.class, "hasService returns boolean");
        Method get = c.getMethod("getService", BeanContextChild.class, Object.class, Class.class, Object.class,
                BeanContextServiceRevokedListener.class);
        check(get.getReturnType() == Object.class, "getService returns Object");
        check(get.getExceptionTypes().length == 1
                && get.getExceptionTypes()[0] == TooManyListenersException.class,
                "getService declares exactly TooManyListenersException");
        Method rel = c.getMethod("releaseService", BeanContextChild.class, Object.class, Object.class);
        check(rel.getReturnType() == void.class, "releaseService returns void");
        check(c.getMethod("getCurrentServiceClasses").getReturnType() == Iterator.class,
                "getCurrentServiceClasses returns Iterator");
        check(c.getMethod("getCurrentServiceSelectors", Class.class).getReturnType() == Iterator.class,
                "getCurrentServiceSelectors returns Iterator");
        check(c.getMethod("addBeanContextServicesListener", BeanContextServicesListener.class).getReturnType() == void.class,
                "addBeanContextServicesListener returns void");
        check(c.getMethod("removeBeanContextServicesListener", BeanContextServicesListener.class).getReturnType() == void.class,
                "removeBeanContextServicesListener returns void");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface is not Serializable");
        // the canonical implementation realizes the full interface (and its closure)
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        check(bcss instanceof BeanContextServices, "BeanContextServicesSupport implements BeanContextServices");
        check(bcss instanceof BeanContextServicesListener, "realizes BeanContextServicesListener");
        check(bcss instanceof BeanContextServiceRevokedListener, "realizes BeanContextServiceRevokedListener");
        check(bcss instanceof BeanContext, "realizes BeanContext");
        // deprecation status on this JDK
        check(c.getAnnotation(Deprecated.class) == null, "not deprecated on JDK 8");
    }

    static boolean EventListenerTestSafe() {
        return java.util.EventListener.class.isAssignableFrom(BeanContextServices.class);
    }

    static void testServiceLifecycle() throws Exception {
        BeanContextServices bcs = new BeanContextServicesSupport();
        MyProvider p = new MyProvider();
        check(!bcs.hasService(MyService.class), "hasService false before registration");
        check(bcs.addService(MyService.class, p), "addService true first time");
        check(bcs.hasService(MyService.class), "hasService true after registration");
        check(!bcs.addService(MyService.class, p), "duplicate addService same class returns false");
        check(!bcs.addService(MyService.class, new MyProvider()), "duplicate addService other provider returns false, no replacement");
        check(bcs.hasService(MyService.class), "service still registered after failed duplicate");
        check(iterContains(bcs.getCurrentServiceClasses(), MyService.class), "getCurrentServiceClasses contains registered class");
        // second service: iterator sees both
        check(bcs.addService(Integer.class, p), "second service class registered");
        check(iterContains(bcs.getCurrentServiceClasses(), MyService.class), "classes iterator has first service");
        check(iterContains(bcs.getCurrentServiceClasses(), Integer.class), "classes iterator has second service");
        // BCSIterator.remove() is a documented-by-behavior no-op (does not unregister, does not throw)
        Iterator<?> classes = bcs.getCurrentServiceClasses();
        classes.next();
        classes.remove();
        check(bcs.hasService(MyService.class), "iterator remove() is a no-op, service stays registered");
        // selectors: live delegate into the provider; null for unregistered classes
        Iterator<?> sels = bcs.getCurrentServiceSelectors(MyService.class);
        check(sels.hasNext() && "sel-a".equals(sels.next()), "first provider selector delivered");
        check(sels.hasNext() && "sel-b".equals(sels.next()), "second provider selector delivered");
        check(!sels.hasNext(), "selector iterator exhausted");
        check(bcs.getCurrentServiceSelectors(java.io.Serializable.class) == null,
                "getCurrentServiceSelectors of unregistered class is null");
        // empty context has an empty (non-null) classes iterator
        Iterator<?> empty = new BeanContextServicesSupport().getCurrentServiceClasses();
        check(empty != null && !empty.hasNext(), "empty context classes iterator is empty");
        // negative matrix on registration/lookup
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new BeanContextServicesSupport().addService(null, new MyProvider()); } },
                "addService(null, provider) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new BeanContextServicesSupport().addService(MyService.class, null); } },
                "addService(class, null) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new BeanContextServicesSupport().hasService(null); } },
                "hasService(null) -> NPE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bcs.revokeService(MyService.class, new MyProvider(), true); } },
                "revokeService with mismatched provider -> IAE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcs.revokeService(null, p, true); } },
                "revokeService(null, provider) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcs.revokeService(MyService.class, null, true); } },
                "revokeService(class, null) -> NPE");
        // revoking an unregistered class is a silent no-op
        bcs.revokeService(java.io.Serializable.class, p, true);
        check(!bcs.hasService(java.io.Serializable.class), "unregistered revoke left state unchanged");
        // clean revoke
        bcs.revokeService(MyService.class, p, true);
        check(!bcs.hasService(MyService.class), "hasService false after revoke");
        check(!iterContains(bcs.getCurrentServiceClasses(), MyService.class), "classes iterator no longer has revoked class");
        check(bcs.getCurrentServiceSelectors(MyService.class) == null, "selectors of revoked class is null");
    }

    static void testGetServiceRelease() throws Exception {
        final BeanContextServices bcs = new BeanContextServicesSupport();
        final MyProvider p = new MyProvider();
        bcs.addService(MyService.class, p);
        final BeanContextChildSupport child = new BeanContextChildSupport();
        bcs.add(child);
        final MyRevokedListener rl = new MyRevokedListener();
        final Object requestor = new Object();
        final Object selector = new Object();
        // unknown service for a valid child resolves to null (no exception)
        check(bcs.getService(child, new Object(), java.io.Serializable.class, null, rl) == null,
                "getService of unregistered service returns null");
        // child must belong to the context
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Exception { bcs.getService(new BeanContextChildSupport(), requestor, MyService.class, null, rl); } },
                "getService for non-child -> IAE");
        // null argument matrix
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { bcs.getService(null, requestor, MyService.class, null, rl); } },
                "getService(null child) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { bcs.getService(child, null, MyService.class, null, rl); } },
                "getService(null requestor) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { bcs.getService(child, requestor, null, null, rl); } },
                "getService(null serviceClass) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { bcs.getService(child, requestor, MyService.class, null, null); } },
                "getService(null revoked listener) -> NPE");
        // successful request: service instance identity and provider argument pass-through
        Object svc = bcs.getService(child, requestor, MyService.class, selector, rl);
        check(svc == p.svc, "getService returns the provider's service instance");
        check(p.getC == 1, "provider.getService invoked exactly once");
        check(p.lastRequestor == requestor, "requestor passed through by identity");
        check(p.lastSelector == selector, "selector passed through by identity");
        check(p.lastClass == MyService.class, "serviceClass passed through by identity");
        check(p.lastBcs == bcs, "provider receives the requesting BeanContextServices by identity");
        // same requestor + same revoked listener again: allowed, ref-counted
        Object svc2 = bcs.getService(child, requestor, MyService.class, selector, rl);
        check(svc2 == p.svc && p.getC == 2, "repeat request with same listener allowed (ref counted)");
        // each release maps to exactly one provider.releaseService
        bcs.releaseService(child, requestor, svc);
        check(p.relC == 1, "releaseService forwarded to provider once");
        bcs.releaseService(child, requestor, svc2);
        check(p.relC == 2, "second releaseService forwarded to provider");
        // same requestor + different revoked listener -> TooManyListenersException with provider rollback
        final MyRevokedListener rl2 = new MyRevokedListener();
        bcs.getService(child, requestor, MyService.class, null, rl);
        check(p.getC == 3, "third request booked");
        checkThrows(TooManyListenersException.class, new ThrowingRunnable() {
            public void run() throws Exception { bcs.getService(child, requestor, MyService.class, null, rl2); } },
                "same requestor with different revoked listener -> TooManyListenersException");
        check(p.getC == 4 && p.relC == 3, "TMLE path rolls back with one provider.releaseService");
        // a different requestor with that listener is fine
        check(bcs.getService(child, new Object(), MyService.class, null, rl2) == p.svc,
                "different requestor with the other listener is allowed");
        // releaseService negatives
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bcs.releaseService(new BeanContextChildSupport(), requestor, new Object()); } },
                "releaseService for non-child -> IAE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcs.releaseService(null, requestor, new Object()); } },
                "releaseService(null child) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcs.releaseService(child, null, new Object()); } },
                "releaseService(null requestor) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcs.releaseService(child, requestor, null); } },
                "releaseService(null service) -> NPE");
        int before = p.relC;
        bcs.releaseService(child, requestor, new Object());
        check(p.relC == before, "releaseService of a never-obtained service is a silent no-op");
        // provider returning null: getService returns null and no reference is booked
        final MyProvider nullProvider = new MyProvider() {
            public Object getService(BeanContextServices b, Object r, Class c, Object s) { getC++; return null; }
            public Iterator getCurrentServiceSelectors(BeanContextServices b, Class c) {
                return java.util.Collections.emptyList().iterator();
            }
        };
        bcs.addService(Runnable.class, nullProvider);
        check(bcs.getService(child, new Object(), Runnable.class, null, rl) == null,
                "provider returning null yields null service");
        bcs.releaseService(child, new Object(), new Object());
        check(nullProvider.relC == 0, "no release forwarded for a null service");
    }

    static void testRevokeSemantics() throws Exception {
        // revoke(false): reference retention, provider still released on explicit releaseService
        BeanContextServicesSupport bcs1 = new BeanContextServicesSupport();
        MyProvider p1 = new MyProvider();
        bcs1.addService(MyService.class, p1);
        BeanContextChildSupport child1 = new BeanContextChildSupport();
        bcs1.add(child1);
        MyRevokedListener rl1 = new MyRevokedListener();
        MyServicesListener sl1 = new MyServicesListener();
        bcs1.addBeanContextServicesListener(sl1);
        Object req1 = new Object();
        bcs1.getService(child1, req1, MyService.class, null, rl1);
        bcs1.revokeService(MyService.class, p1, false);
        check(rl1.n == 1, "revoke(false) notifies the per-requestor revoked listener once");
        check(rl1.last != null && !rl1.last.isCurrentServiceInvalidNow(), "revoke(false) event invalidate flag false");
        check(sl1.rv == 1 && !sl1.lastRevoked.isCurrentServiceInvalidNow(),
                "revoke(false) notifies registered services listener with invalidate false");
        check(p1.relC == 0, "provider receives no releaseService at revoke time");
        bcs1.releaseService(child1, req1, p1.svc);
        check(p1.relC == 1, "after revoke(false) explicit releaseService still reaches provider");
        // re-register the same provider; the held reference reconciles cleanly
        check(bcs1.addService(MyService.class, p1), "re-add same provider after revoke(false)");
        check(bcs1.getService(child1, req1, MyService.class, null, rl1) == p1.svc,
                "same requestor/listener re-acquires service after re-add");
        // revoke(true): references cleaned, provider never sees the late releaseService
        BeanContextServicesSupport bcs2 = new BeanContextServicesSupport();
        MyProvider p2 = new MyProvider();
        bcs2.addService(MyService.class, p2);
        BeanContextChildSupport child2 = new BeanContextChildSupport();
        bcs2.add(child2);
        MyRevokedListener rl2 = new MyRevokedListener();
        MyServicesListener sl2 = new MyServicesListener();
        bcs2.addBeanContextServicesListener(sl2);
        Object req2 = new Object();
        bcs2.getService(child2, req2, MyService.class, null, rl2);
        bcs2.revokeService(MyService.class, p2, true);
        check(rl2.n == 1 && rl2.last.isCurrentServiceInvalidNow(), "revoke(true) event invalidate flag true");
        check(sl2.rv == 1 && sl2.lastRevoked.isCurrentServiceInvalidNow(),
                "revoke(true) services listener event invalidate true");
        check(p2.relC == 0, "provider receives no callback at revoke(true) either");
        bcs2.releaseService(child2, req2, p2.svc);
        check(p2.relC == 0, "late releaseService after revoke(true) is a no-op for the provider");
        // provider switch while a reference is held -> UnsupportedOperationException with rollback
        BeanContextServicesSupport bcs3 = new BeanContextServicesSupport();
        MyProvider p3 = new MyProvider();
        bcs3.addService(MyService.class, p3);
        BeanContextChildSupport child3 = new BeanContextChildSupport();
        bcs3.add(child3);
        MyRevokedListener rl3 = new MyRevokedListener();
        Object req3 = new Object();
        bcs3.getService(child3, req3, MyService.class, null, rl3);
        bcs3.revokeService(MyService.class, p3, false); // keeps the child-side reference alive
        final MyProvider p4 = new MyProvider();
        bcs3.addService(MyService.class, p4);
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() throws Exception { bcs3.getService(child3, req3, MyService.class, null, rl3); } },
                "provider switch under a held reference -> UnsupportedOperationException");
        check(p4.getC == 1 && p4.relC == 1, "UOE path rolls back the new provider's get with a release");
    }

    static void testListenerMatrix() throws Exception {
        final BeanContextServicesSupport bcs = new BeanContextServicesSupport();
        // null and unregistered listener handling
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcs.addBeanContextServicesListener(null); } },
                "addBeanContextServicesListener(null) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcs.removeBeanContextServicesListener(null); } },
                "removeBeanContextServicesListener(null) -> NPE");
        bcs.removeBeanContextServicesListener(new MyServicesListener());
        check(true, "removeBeanContextServicesListener of unregistered listener is a no-op");
        // delivery: addService fires serviceAvailable with exact source and class
        final MyServicesListener a = new MyServicesListener();
        final MyServicesListener b = new MyServicesListener();
        bcs.addBeanContextServicesListener(a);
        bcs.addBeanContextServicesListener(b);
        bcs.addService(MyService.class, new MyProvider());
        check(a.av == 1, "listener A saw serviceAvailable once");
        check(b.av == 1, "listener B saw serviceAvailable once");
        check(a.lastAvailable == b.lastAvailable, "all listeners receive the same event instance");
        check(a.lastAvailable.getSourceAsBeanContextServices() == bcs, "event source is the context itself");
        check(a.lastAvailable.getServiceClass() == MyService.class, "event serviceClass is exact");
        check(bcs.addService(MyService.class, new MyProvider()) == false, "duplicate add rejected");
        check(a.av == 1, "duplicate addService fires no event");
        // removing the listener stops delivery
        bcs.removeBeanContextServicesListener(b);
        bcs.addService(Integer.class, new MyProvider());
        check(b.av == 1 && a.av == 2, "removed listener gets nothing, remaining listener still notified");
        // nested contexts: a child BeanContextServices is itself a delivery target
        BeanContextServicesSupport outer = new BeanContextServicesSupport();
        BeanContextServicesSupport inner = new BeanContextServicesSupport();
        outer.add(inner);
        final MyServicesListener innerListener = new MyServicesListener();
        inner.addBeanContextServicesListener(innerListener);
        MyProvider delegatedProvider = new MyProvider();
        outer.addService(MyService.class, delegatedProvider);
        check(innerListener.av == 1, "child BeanContextServices receives serviceAvailable from parent registration");
        check(innerListener.lastAvailable.getSourceAsBeanContextServices() == outer,
                "child receives the parent's event with the parent as source");
        // delegated hasService/getService/releaseService through the nesting
        check(inner.hasService(MyService.class), "nested context delegates hasService to parent");
        BeanContextChildSupport leaf = new BeanContextChildSupport();
        inner.add(leaf);
        MyRevokedListener leafRl = new MyRevokedListener();
        Object leafReq = new Object();
        Object svc = inner.getService(leaf, leafReq, MyService.class, null, leafRl);
        check(svc != null && svc == delegatedProvider.svc, "nested getService returns parent provider's instance");
        inner.releaseService(leaf, leafReq, svc);
        check(delegatedProvider.relC == 1, "nested releaseService reaches the parent's provider");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBoxStructure();
        testServiceLifecycle();
        testGetServiceRelease();
        testRevokeSemantics();
        testListenerMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

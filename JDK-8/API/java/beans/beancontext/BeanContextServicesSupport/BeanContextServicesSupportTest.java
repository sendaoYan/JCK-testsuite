/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport conformance:
 *          structure (extends BeanContextSupport implements BeanContextServices),
 *          ctor matrix (5 overloads, peer semantics, locale/designTime), service
 *          registration matrix (duplicate rejection, exact service class set,
 *          selectors incl. null-selector iterator NPE record), getService lifecycle
 *          (provider called on every request, TooManyListenersException on differing
 *          revoked listener, selector/bcs pass-through, ref-counted release, silent
 *          over-release), revoke matrix (invalidate flag, event fields, provider
 *          mismatch IAE, post-revoke lookup returns null), services listener registry
 *          (NPE, dedup, pass-through and suppression), parent context delegation
 *          (nested hasService, late registration propagation, re-issued revoke events),
 *          inherited BeanContextSupport collection contract (UOE mutators, null
 *          handling), serialization round-trip (only serializable providers survive).
 * @run main BeanContextServicesSupportTest
 */

import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesListener;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class BeanContextServicesSupportTest {
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
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- self-written service fixtures (headless, no AWT) ----
    public interface Svc { String name(); }
    public static class SvcImpl implements Svc, Serializable {
        private static final long serialVersionUID = 1L;
        public String name() { return "svc"; }
    }
    /** Provider returning a singleton service; counts every callback and records args. */
    public static class SvcProvider implements BeanContextServiceProvider, Serializable {
        private static final long serialVersionUID = 1L;
        int getCnt, relCnt, selCnt;
        Object lastBcs, lastRequestor, lastSelector;
        Class<?> lastServiceClass;
        final Svc svc = new SvcImpl();
        boolean nullSelectors = true;
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) {
            getCnt++; lastBcs = bcs; lastRequestor = requestor; lastSelector = selector; lastServiceClass = serviceClass;
            return svc;
        }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { relCnt++; }
        public Iterator<Svc> getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
            selCnt++; return nullSelectors ? null : new ArrayList<Svc>().iterator();
        }
    }
    /** Non-Serializable provider: its registration must not survive serialization. */
    public static class PlainProvider implements BeanContextServiceProvider {
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { return new Object(); }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { }
        public Iterator<Object> getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) { return new ArrayList<Object>().iterator(); }
    }
    public static class RevokedL implements BeanContextServiceRevokedListener {
        int count; BeanContextServiceRevokedEvent last;
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { count++; last = e; }
    }
    public static class SvcsL implements BeanContextServicesListener {
        int avail, rev;
        BeanContextServiceAvailableEvent lastAvail;
        BeanContextServiceRevokedEvent lastRev;
        public void serviceAvailable(BeanContextServiceAvailableEvent e) { avail++; lastAvail = e; }
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { rev++; lastRev = e; }
    }
    public static class MemberBean extends BeanContextChildSupport {
        private static final long serialVersionUID = 1L;
    }
    static class MemL implements BeanContextMembershipListener {
        int added, removed; BeanContextMembershipEvent lastAdd, lastRem;
        public void childrenAdded(BeanContextMembershipEvent e) { added++; lastAdd = e; }
        public void childrenRemoved(BeanContextMembershipEvent e) { removed++; lastRem = e; }
    }

    // ---- original structural assertions (kept) ----
    static void testExistingReflection() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextSupport".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextSupport");
        check(Class.forName("java.beans.beancontext.BeanContextServices").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServices");
        check(hasMethod(c, "addBeanContextServicesListener"), "declares addBeanContextServicesListener");
        check(hasMethod(c, "addService"), "declares addService");
        check(hasMethod(c, "getBeanContextServicesPeer"), "declares getBeanContextServicesPeer");
    }

    static void testStructure() throws Exception {
        Class<?> c = BeanContextServicesSupport.class;
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(BeanContextServicesListener.class.isAssignableFrom(c), "is a BeanContextServicesListener");
        check(BeanContextServiceRevokedListener.class.isAssignableFrom(c), "is a BeanContextServiceRevokedListener");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -8494482757288719206L, "stable serialVersionUID");
        String[] ops = {
            "addBeanContextServicesListener", "removeBeanContextServicesListener", "addService",
            "revokeService", "hasService", "getService", "releaseService",
            "getCurrentServiceClasses", "getCurrentServiceSelectors",
            "serviceAvailable", "serviceRevoked", "initialize", "getBeanContextServicesPeer",
            "createBCSChild", "createBCSSServiceProvider", "childJustRemovedHook",
            "releaseBeanContextResources", "initializeBeanContextResources",
            "fireServiceAdded", "fireServiceRevoked", "getChildBeanContextServicesListener",
            "bcsPreSerializationHook", "bcsPreDeserializationHook",
        };
        for (String op : ops) check(hasMethod(c, op), "declares " + op);
        // getService signature: 5 params, throws TooManyListenersException
        boolean found = false, throwsTMLE = false;
        for (Method m : c.getDeclaredMethods()) {
            if ("getService".equals(m.getName()) && m.getParameterTypes().length == 5) {
                found = true;
                Class<?>[] ps = m.getParameterTypes();
                check(ps[0] == java.beans.beancontext.BeanContextChild.class, "getService param0 BeanContextChild");
                check(ps[2] == Class.class, "getService param2 Class");
                check(ps[4] == BeanContextServiceRevokedListener.class, "getService param4 revoked listener");
                for (Class<?> ex : m.getExceptionTypes())
                    if (ex == java.util.TooManyListenersException.class) throwsTMLE = true;
            }
        }
        check(found, "getService(BeanContextChild,Object,Class,Object,BeanContextServiceRevokedListener) declared");
        check(throwsTMLE, "getService declares TooManyListenersException");
    }

    static void testConstructors() throws Exception {
        check(BeanContextServicesSupport.class.getDeclaredConstructors().length == 5, "five ctor overloads");
        BeanContextServicesSupport b = new BeanContextServicesSupport();
        check(b.getBeanContextServicesPeer() == b, "no-arg ctor: peer is self");
        check(b.size() == 0 && b.isEmpty(), "no-arg ctor: empty");
        check(!b.isDesignTime(), "no-arg ctor: not design time");
        BeanContextServicesSupport peer = new BeanContextServicesSupport();
        BeanContextServicesSupport withPeer = new BeanContextServicesSupport(peer);
        check(withPeer.getBeanContextServicesPeer() == peer, "peer ctor returns peer");
        BeanContextServicesSupport loc = new BeanContextServicesSupport(null, Locale.FRENCH);
        check(loc.getBeanContextServicesPeer() == loc, "peer+locale ctor: null peer means self");
        check(loc.getLocale() == Locale.FRENCH, "locale ctor stores locale");
        check(!loc.isDesignTime(), "peer+locale ctor: design time default false");
        BeanContextServicesSupport dt = new BeanContextServicesSupport(null, Locale.FRENCH, true);
        check(dt.isDesignTime(), "3-arg ctor honors designTime");
        BeanContextServicesSupport full = new BeanContextServicesSupport(null, Locale.ITALIAN, true, true);
        check(full.isDesignTime() && full.getLocale() == Locale.ITALIAN, "4-arg ctor honors locale+designTime");
        for (Constructor<?> k : BeanContextServicesSupport.class.getDeclaredConstructors())
            check(Modifier.isPublic(k.getModifiers()), "ctor public: " + k.getParameterCount() + " args");
    }

    static void testRegistrationMatrix() throws Exception {
        BeanContextServicesSupport b = new BeanContextServicesSupport();
        Iterator<?> empty = b.getCurrentServiceClasses();
        check(empty != null && !empty.hasNext(), "no services: empty iterator (not null)");
        check(!b.hasService(Svc.class), "hasService false before registration");
        check(b.getCurrentServiceSelectors(Svc.class) == null, "selectors null for unregistered service");
        SvcProvider sp = new SvcProvider();
        SvcsL sl = new SvcsL();
        b.addBeanContextServicesListener(sl);
        check(b.addService(Svc.class, sp), "addService returns true");
        check(sl.avail == 1, "serviceAvailable fired on addService");
        check(sl.lastAvail.getServiceClass() == Svc.class, "available event service class exact");
        check(sl.lastAvail.getSourceAsBeanContextServices() == b, "available event source is peer");
        check(!b.addService(Svc.class, sp), "duplicate addService same provider rejected");
        SvcProvider other = new SvcProvider();
        check(!b.addService(Svc.class, other), "duplicate addService different provider rejected");
        check(b.hasService(Svc.class), "hasService true after registration");
        check(!b.hasService(String.class), "hasService false for other class");
        check(b.addService(String.class, other), "second distinct service class accepted");
        check(sl.avail == 2, "second available event fired");
        Set<Object> classes = new HashSet<Object>();
        Iterator<?> it = b.getCurrentServiceClasses();
        while (it.hasNext()) classes.add(it.next());
        check(classes.size() == 2 && classes.contains(Svc.class) && classes.contains(String.class),
                "getCurrentServiceClasses yields exactly the registered set");
        sp.nullSelectors = true;
        final Iterator<?> nullIter = b.getCurrentServiceSelectors(Svc.class);
        check(sp.selCnt == 1, "selectors routed to registered provider");
        check(nullIter != null, "selectors iterator non-null wrapper");
        // provider returned null: wrapper iterator throws NPE on use (recorded)
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { nullIter.hasNext(); }
        }, "null-selector iterator hasNext");
        sp.nullSelectors = false;
        Iterator<?> selIt = b.getCurrentServiceSelectors(Svc.class);
        check(selIt != null && !selIt.hasNext(), "empty-selector iterator iterates zero elements");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { b.addService(null, sp); }
        }, "addService(null class)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { b.addService(Svc.class, null); }
        }, "addService(null provider)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { b.hasService(null); }
        }, "hasService(null)");
    }

    static void testGetServiceSemantics() throws Exception {
        BeanContextServicesSupport b = new BeanContextServicesSupport();
        SvcProvider sp = new SvcProvider();
        sp.nullSelectors = false;
        b.addService(Svc.class, sp);
        MemberBean child = new MemberBean();
        check(b.add(child), "member added");
        RevokedL rl = new RevokedL();
        Object requestor = new Object();
        Object selector = new Object();
        Object s1 = b.getService(child, requestor, Svc.class, selector, rl);
        check(s1 == sp.svc, "getService returns provider's service instance");
        check(sp.getCnt == 1, "provider getService called once");
        check(sp.lastBcs == b, "provider receives the context peer as bcs");
        check(sp.lastRequestor == requestor, "provider receives requestor");
        check(sp.lastSelector == selector, "provider receives selector");
        check(sp.lastServiceClass == Svc.class, "provider receives service class");
        Object s2 = b.getService(child, requestor, Svc.class, selector, rl);
        check(s2 == s1, "same singleton service instance on repeat request");
        check(sp.getCnt == 2, "no per-requestor provider caching: provider called again");
        // differing revoked listener for same requestor+service
        RevokedL rl2 = new RevokedL();
        final BeanContextServicesSupport fb = b;
        final MemberBean fchild = child;
        final Object freq = requestor;
        final RevokedL frl2 = rl2;
        checkThrows(java.util.TooManyListenersException.class, new ThrowingRunnable() {
            public void run() throws Exception { fb.getService(fchild, freq, Svc.class, null, frl2); }
        }, "getService with different revoked listener for same requestor");
        check(sp.getCnt == 3, "provider consulted before TooManyListenersException");
        check(sp.relCnt == 1, "bookkeeping unwound: provider releaseService after TMLE");
        // same listener object again is fine
        Object s3 = b.getService(child, requestor, Svc.class, null, rl);
        check(s3 == s1 && sp.getCnt == 4, "same listener accepted again");
        // different requestor with same listener is fine
        Object s4 = b.getService(child, new Object(), Svc.class, null, rl);
        check(s4 == s1 && sp.getCnt == 5, "different requestor accepted");
        // release is ref-counted: three refs for requestor (s1, s2, s3), one for the other requestor (s4)
        int relBase = sp.relCnt;
        b.releaseService(child, requestor, s1);
        check(sp.relCnt == relBase + 1, "releaseService forwarded to provider");
        b.releaseService(child, requestor, s1);
        check(sp.relCnt == relBase + 2, "second release still forwarded (ref remained)");
        b.releaseService(child, requestor, s1);
        check(sp.relCnt == relBase + 3, "third release still forwarded (ref remained)");
        b.releaseService(child, requestor, s1);
        check(sp.relCnt == relBase + 3, "over-release beyond refs is silent for provider");
        // negative matrix
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { fb.getService(null, freq, Svc.class, null, frl2); }
        }, "getService(null child)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { fb.getService(fchild, null, Svc.class, null, frl2); }
        }, "getService(null requestor)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { fb.getService(fchild, freq, null, null, frl2); }
        }, "getService(null service class)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { fb.getService(fchild, freq, Svc.class, null, null); }
        }, "getService(null revoked listener)");
        final MemberBean stranger = new MemberBean();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Exception { fb.getService(stranger, freq, Svc.class, null, frl2); }
        }, "getService for non-member child");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { fb.releaseService(stranger, freq, new Object()); }
        }, "releaseService for non-member child");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { fb.releaseService(null, freq, new Object()); }
        }, "releaseService(null child)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { fb.releaseService(fchild, null, new Object()); }
        }, "releaseService(null requestor)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { fb.releaseService(fchild, freq, null); }
        }, "releaseService(null service)");
        // service from a provider returning null is simply not granted
        final class NullProvider implements BeanContextServiceProvider {
            public Object getService(BeanContextServices bcs, Object req, Class sc, Object sel) { return null; }
            public void releaseService(BeanContextServices bcs, Object req, Object svc) { }
            public Iterator<Object> getCurrentServiceSelectors(BeanContextServices bcs, Class sc) { return new ArrayList<Object>().iterator(); }
        }
        b.addService(String.class, new NullProvider());
        Object none = b.getService(child, requestor, String.class, null, rl);
        check(none == null, "provider returning null yields null service (no exception)");
    }

    static void testRevokeMatrix() throws Exception {
        final BeanContextServicesSupport b = new BeanContextServicesSupport();
        final SvcProvider sp = new SvcProvider();
        sp.nullSelectors = false;
        b.addService(Svc.class, sp);
        SvcsL sl = new SvcsL();
        b.addBeanContextServicesListener(sl);
        MemberBean child = new MemberBean();
        b.add(child);
        RevokedL rl = new RevokedL();
        Object requestor = new Object();
        Object svc = b.getService(child, requestor, Svc.class, null, rl);
        check(svc != null, "service granted");
        // revoke with revokeCurrentServicesNow=false
        b.revokeService(Svc.class, sp, false);
        check(rl.count == 1, "requestor revoked listener notified");
        check(rl.last.getServiceClass() == Svc.class, "revoked event service class exact");
        check(rl.last.isServiceClass(Svc.class) && !rl.last.isServiceClass(String.class), "revoked event isServiceClass");
        check(!rl.last.isCurrentServiceInvalidNow(), "invalidate=false: isCurrentServiceInvalidNow false");
        check(rl.last.getSourceAsBeanContextServices() == b, "revoked event source is peer");
        check(sl.rev == 1, "services listener also notified of revoke");
        check(!b.hasService(Svc.class), "revocation removes the service registration");
        check(b.getService(child, requestor, Svc.class, null, rl) == null,
                "getService after revoke returns null (no exception)");
        // re-register the same class+provider after revoke
        check(b.addService(Svc.class, sp), "re-register after revoke succeeds");
        check(b.hasService(Svc.class), "hasService restored");
        Object svc2 = b.getService(child, requestor, Svc.class, null, rl);
        check(svc2 == sp.svc, "service granted again after re-register");
        // revoke with revokeCurrentServicesNow=true
        b.revokeService(Svc.class, sp, true);
        check(rl.count == 2, "second revoke notified");
        check(rl.last.isCurrentServiceInvalidNow(), "invalidate=true: isCurrentServiceInvalidNow true");
        // provider mismatch and negatives
        b.addService(Svc.class, sp);
        final SvcProvider wrong = new SvcProvider();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { b.revokeService(Svc.class, wrong, true); }
        }, "revokeService with mismatched provider");
        b.revokeService(String.class, sp, true); // unregistered class: silent no-op
        check(true, "revokeService of unregistered class is silent");
        check(b.hasService(Svc.class), "mismatched revoke did not remove service");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { b.revokeService(null, sp, true); }
        }, "revokeService(null class)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { b.revokeService(Svc.class, null, true); }
        }, "revokeService(null provider)");
    }

    static void testListenerRegistry() throws Exception {
        final BeanContextServicesSupport b = new BeanContextServicesSupport();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { b.addBeanContextServicesListener(null); }
        }, "addBeanContextServicesListener(null)");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { b.removeBeanContextServicesListener(null); }
        }, "removeBeanContextServicesListener(null)");
        SvcsL sl = new SvcsL();
        b.addBeanContextServicesListener(sl);
        b.addBeanContextServicesListener(sl);
        b.addBeanContextServicesListener(sl);
        BeanContextServiceAvailableEvent ae = new BeanContextServiceAvailableEvent(b, String.class);
        b.serviceAvailable(ae);
        check(sl.avail == 1, "duplicate listener registrations collapse to one");
        check(sl.lastAvail == ae, "the very same event object is delivered");
        BeanContextServiceRevokedEvent re = new BeanContextServiceRevokedEvent(b, String.class, false);
        b.serviceRevoked(re);
        check(sl.rev == 1 && sl.lastRev == re, "serviceRevoked pass-through to listeners");
        // registering a service fires serviceAvailable to the services listeners
        SvcProvider sp = new SvcProvider();
        b.addService(Svc.class, sp);
        check(sl.avail == 2, "addService notifies services listener");
        // suppression when this context already provides the class itself
        b.serviceAvailable(new BeanContextServiceAvailableEvent(b, Svc.class));
        check(sl.avail == 2, "serviceAvailable suppressed for locally provided class");
        b.serviceRevoked(new BeanContextServiceRevokedEvent(b, Svc.class, false));
        check(sl.rev == 1, "serviceRevoked suppressed for locally provided class");
        b.removeBeanContextServicesListener(sl);
        b.serviceAvailable(new BeanContextServiceAvailableEvent(b, String.class));
        check(sl.avail == 2, "removed listener no longer notified");
        SvcsL stranger = new SvcsL();
        b.removeBeanContextServicesListener(stranger);
        check(true, "removing an unregistered listener is a no-op");
        // events expose their delegates
        BeanContextServiceAvailableEvent e2 = new BeanContextServiceAvailableEvent(b, Svc.class);
        check(e2.getCurrentServiceSelectors() != null, "available event selectors delegate to source context");
        check(e2.getSourceAsBeanContextServices() == b, "available event source as services");
        BeanContextServiceRevokedEvent r2 = new BeanContextServiceRevokedEvent(b, Svc.class, true);
        check(r2.isServiceClass(Svc.class) && r2.isCurrentServiceInvalidNow(), "revoked event accessors");
    }

    static void testParentDelegation() throws Exception {
        BeanContextServicesSupport parent = new BeanContextServicesSupport();
        BeanContextServicesSupport childCtx = new BeanContextServicesSupport();
        SvcsL childL = new SvcsL();
        childCtx.addBeanContextServicesListener(childL);
        SvcProvider psp = new SvcProvider();
        psp.nullSelectors = false;
        check(parent.addService(Svc.class, psp), "parent registers service");
        check(!childCtx.hasService(Svc.class), "before nesting: child cannot see parent service");
        check(parent.add(childCtx), "child context nested into parent");
        check(childCtx.hasService(Svc.class), "after nesting: hasService delegates to parent");
        check(!childCtx.hasService(String.class), "delegation does not invent services");
        Set<Object> own = new HashSet<Object>();
        Iterator<?> it = childCtx.getCurrentServiceClasses();
        while (it.hasNext()) own.add(it.next());
        check(own.isEmpty(), "getCurrentServiceClasses lists only own services (not delegated)");
        check(childCtx.getCurrentServiceSelectors(Svc.class) == null, "selectors null for delegated-only service");
        MemberBean member = new MemberBean();
        childCtx.add(member);
        RevokedL mrl = new RevokedL();
        Object requestor = new Object();
        Object ds = childCtx.getService(member, requestor, Svc.class, null, mrl);
        check(ds == psp.svc, "delegated getService returns parent provider's service");
        check(psp.getCnt == 1, "parent provider consulted via delegation");
        childCtx.releaseService(member, requestor, ds);
        check(psp.relCnt == 1, "delegated releaseService reaches parent provider");
        childCtx.releaseService(member, new Object(), ds);
        check(psp.relCnt == 1, "delegated release with wrong requestor is silent");
        // late parent registration propagates serviceAvailable into the nested context
        check(parent.addService(String.class, psp), "parent late-registers second service");
        check(childL.avail == 1, "nested context listener saw serviceAvailable");
        check(childL.lastAvail.getServiceClass() == String.class, "propagated event service class");
        check(childL.lastAvail.getSourceAsBeanContextServices() == parent, "propagated event source is parent");
        check(childCtx.hasService(String.class), "hasService sees late-registered parent service");
        // revoking on the parent re-issues a revoked event from the child context;
        // the released reference above is gone, so acquire a fresh delegated one first
        Object ds2 = childCtx.getService(member, requestor, Svc.class, null, mrl);
        check(ds2 == psp.svc && psp.getCnt == 2, "fresh delegated grant before revoke probe");
        parent.revokeService(String.class, psp, true);
        check(mrl.count == 0, "listener holding only Svc refs not notified for String revoke");
        parent.revokeService(Svc.class, psp, true);
        check(mrl.count == 1, "delegated requestor notified of parent revoke");
        check(mrl.last.getServiceClass() == Svc.class, "re-issued event service class exact");
        check(mrl.last.isCurrentServiceInvalidNow(), "re-issued event keeps invalidate=true");
        check(mrl.last.getSourceAsBeanContextServices() == childCtx, "re-issued event source is the nested context");
        check(!childCtx.hasService(Svc.class), "delegated visibility gone after parent revoke");
    }

    static void testInheritedCollectionContract() throws Exception {
        final BeanContextServicesSupport b = new BeanContextServicesSupport();
        MemL ml = new MemL();
        b.addBeanContextMembershipListener(ml);
        MemberBean c1 = new MemberBean();
        MemberBean c2 = new MemberBean();
        check(b.add(c1) && b.add(c2), "add members");
        check(ml.added == 2, "childrenAdded fired per add");
        check(ml.lastAdd.size() == 1 && ml.lastAdd.contains(c2), "membership event carries added child");
        check(ml.lastAdd.getBeanContext() == b, "membership event source context");
        check(b.size() == 2 && !b.isEmpty(), "size/isEmpty");
        check(b.contains(c1) && b.contains(c2), "contains members");
        check(b.containsAll(java.util.Arrays.asList(new Object[]{c1, c2})), "containsAll");
        check(b.toArray().length == 2, "toArray length");
        int seen = 0;
        for (Iterator<?> i = b.iterator(); i.hasNext(); i.next()) seen++;
        check(seen == 2, "iterator visits all members");
        check(b.remove(c1), "remove member");
        check(ml.removed == 1 && ml.lastRem.contains(c1), "childrenRemoved fired");
        check(!b.contains(c1) && b.size() == 1, "state after remove");
        check(!b.remove(c1), "removing non-member returns false");
        check(ml.removed == 1, "no event for non-member remove");
        check(!b.contains(null), "contains(null) false");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { b.add(null); }
        }, "add(null)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { b.remove(null); }
        }, "remove(null)");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { b.clear(); }
        }, "clear unsupported");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { b.addAll(new ArrayList<Object>()); }
        }, "addAll unsupported");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { b.removeAll(new ArrayList<Object>()); }
        }, "removeAll unsupported");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { b.retainAll(new ArrayList<Object>()); }
        }, "retainAll unsupported");
    }

    static void testSerializationRoundTrip() throws Exception {
        BeanContextServicesSupport b = new BeanContextServicesSupport();
        MemberBean member = new MemberBean();
        b.add(member);
        SvcProvider serProv = new SvcProvider();
        serProv.nullSelectors = false;
        b.addService(Svc.class, serProv);
        b.addService(String.class, new PlainProvider());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(b);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BeanContextServicesSupport d = (BeanContextServicesSupport) ois.readObject();
        ois.close();
        check(d.getBeanContextServicesPeer() == d, "deserialized peer is self");
        check(d.size() == 1, "deserialized member count");
        check(!d.contains(member), "members deserialize as copies (identity not kept)");
        boolean typeOk = false;
        for (Iterator<?> i = d.iterator(); i.hasNext(); ) typeOk = i.next() instanceof MemberBean;
        check(typeOk, "deserialized member type preserved");
        check(d.hasService(Svc.class), "serializable provider survives round-trip");
        check(!d.hasService(String.class), "non-serializable provider registration lost (recorded)");
        boolean found = false;
        for (Iterator<?> i = d.getCurrentServiceClasses(); i.hasNext(); )
            if (i.next() == Svc.class) found = true;
        check(found, "deserialized service classes list Svc");
        check(d.getCurrentServiceSelectors(Svc.class) != null, "deserialized selectors iterator");
        Object grant = d.getService((java.beans.beancontext.BeanContextChild) d.iterator().next(),
                new Object(), Svc.class, null, new RevokedL());
        check(grant instanceof Svc, "deserialized context grants services via copied provider");
    }

    public static void main(String[] args) throws Exception {
        testExistingReflection();
        testStructure();
        testConstructors();
        testRegistrationMatrix();
        testGetServiceSemantics();
        testRevokeMatrix();
        testListenerRegistry();
        testParentDelegation();
        testInheritedCollectionContract();
        testSerializationRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.beans.beancontext.BeanContextProxy conformance: white-box structure + black-box behavior.
 *          Note: the whole java.beans.beancontext package carries Deprecated(forRemoval=true, since=23) on
 *          JDK 23+ but remains fully functional; structural deprecation assertions cover that.
 *          Exact interface shape (single getBeanContextProxy() returning BeanContextChild - note
 *          the method name is getBeanContextProxy, NOT getBeanContext); the real semantics through
 *          BeanContextSupport: adding a proxy composite enrolls BOTH the proxy and its peer
 *          BeanContextChild as members (contains/size/membership event contents), invokes
 *          setBeanContext on the peer, removing either removes both; the negative matrix
 *          (getBeanContextProxy() == null -> NPE at add time; a class implementing BOTH
 *          BeanContextChild and BeanContextProxy -> IAE); and the recorded fact that
 *          BeanContextSupport/BeanContextServicesSupport do NOT themselves implement the
 *          proxy interface.
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextProxyTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextProxy;
import java.beans.beancontext.BeanContextSupport;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeanContextProxyTest {
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
        java.util.Collections.sort(l);
        return l.toString();
    }

    // composite bean: NOT a BeanContextChild itself, exposes one through the proxy interface
    public static class ProxyBean implements BeanContextProxy {
        final BeanContextChildSupport peer = new BeanContextChildSupport();
        public BeanContextChild getBeanContextProxy() { return peer; }
    }

    // a proxy whose getBeanContextProxy() yields null
    public static class NullProxyBean implements BeanContextProxy {
        public BeanContextChild getBeanContextProxy() { return null; }
    }

    // forbidden combination: implementing both BeanContextChild and BeanContextProxy
    public static class DualRoleBean extends BeanContextChildSupport implements BeanContextProxy {
        public BeanContextChild getBeanContextProxy() { return this; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextProxy");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getBeanContextProxy"), "declares getBeanContextProxy");
        // exact inventory: exactly one method, and it is NOT getBeanContext
        check(methodNames(c).equals("[getBeanContextProxy]"), "exact single-method inventory [getBeanContextProxy]");
        check(!hasMethod(c, "getBeanContext"), "does NOT declare getBeanContext (that is BeanContextChild's method)");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Method m = c.getMethod("getBeanContextProxy");
        check(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                "getBeanContextProxy is public abstract");
        check(m.getReturnType() == BeanContextChild.class, "returns BeanContextChild");
        check(m.getParameterTypes().length == 0, "takes no arguments");
        check(m.getExceptionTypes().length == 0, "declares no checked exceptions");
        check(!java.util.EventListener.class.isAssignableFrom(c), "not an EventListener");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface is not Serializable");
        // the two roles are distinct: neither interface subsumes the other
        check(!BeanContextChild.class.isAssignableFrom(c), "proxy is not a BeanContextChild subtype");
        check(!BeanContextProxy.class.isAssignableFrom(BeanContextChild.class),
                "BeanContextChild is not a proxy subtype");
        // record: the built-in contexts do NOT implement the proxy interface themselves
        check(!BeanContextProxy.class.isAssignableFrom(BeanContextSupport.class),
                "BeanContextSupport does not implement BeanContextProxy");
        check(!BeanContextProxy.class.isAssignableFrom(java.beans.beancontext.BeanContextServicesSupport.class),
                "BeanContextServicesSupport does not implement BeanContextProxy");
        // deprecation status on this JDK: whole package deprecated for removal since 23, still functional
        Deprecated dep = c.getAnnotation(Deprecated.class);
        check(dep != null, "deprecated on JDK 27");
        check(dep.forRemoval(), "deprecated forRemoval=true");
        check("23".equals(dep.since()), "deprecated since=23");
    }

    static void testOriginalWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextProxy");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final BeanContextChild peer = new BeanContextChildSupport();
        BeanContextProxy p = new BeanContextProxy() {
            public BeanContextChild getBeanContextProxy() { return peer; }
        };
        check(peer == p.getBeanContextProxy(), "getBeanContextProxy");
        // repeated invocation returns the same peer (a proxy is a stable delegate)
        check(p.getBeanContextProxy() == p.getBeanContextProxy(), "stable peer identity");
        // null is a legal direct return (validation happens at BeanContextSupport.add time)
        check(new NullProxyBean().getBeanContextProxy() == null, "null return allowed by the interface itself");
    }

    static void testCompositeMembership() throws Exception {
        final BeanContextSupport bc = new BeanContextSupport();
        final ProxyBean pb = new ProxyBean();
        final List<Object> added = new ArrayList<Object>();
        final List<Object> removed = new ArrayList<Object>();
        bc.addBeanContextMembershipListener(new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) {
                for (Iterator<?> i = e.iterator(); i.hasNext(); ) added.add(i.next());
            }
            public void childrenRemoved(BeanContextMembershipEvent e) {
                for (Iterator<?> i = e.iterator(); i.hasNext(); ) removed.add(i.next());
            }
        });
        check(bc.size() == 0, "empty context before add");
        check(bc.add(pb), "adding a proxy composite succeeds");
        // BOTH the proxy and its peer become members
        check(bc.contains(pb), "proxy itself is a member");
        check(bc.contains(pb.peer), "peer BeanContextChild is a member too");
        check(bc.size() == 2, "membership counts both entries");
        check(added.size() == 2 && added.contains(pb) && added.contains(pb.peer),
                "membership event carries exactly the proxy and the peer");
        // setBeanContext was routed to the peer, not to the proxy
        check(pb.peer.getBeanContext() == bc, "peer learned the context via setBeanContext");
        check(pb.peer.getBeanContext() instanceof BeanContext, "peer context typed BeanContext");
        // removing the proxy removes both entries
        check(bc.remove(pb), "removing via the proxy succeeds");
        check(!bc.contains(pb) && !bc.contains(pb.peer), "both entries removed together");
        check(bc.size() == 0, "context empty again");
        check(removed.size() == 2 && removed.contains(pb) && removed.contains(pb.peer),
                "removal event carries both entries");
        // re-add, then remove via the PEER removes both as well
        bc.add(pb);
        check(bc.size() == 2, "re-added composite occupies two slots");
        check(bc.remove(pb.peer), "removing via the peer succeeds");
        check(bc.size() == 0 && !bc.contains(pb), "peer removal clears the proxy entry too");
    }

    static void testNegativeMatrix() throws Exception {
        // a proxy yielding null fails at add time with a dedicated NPE
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new BeanContextSupport().add(new NullProxyBean()); } },
                "add of a proxy whose getBeanContextProxy() is null -> NPE");
        // a class playing both roles is rejected with IAE
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new BeanContextSupport().add(new DualRoleBean()); } },
                "add of a BeanContextChild+BeanContextProxy dual role -> IAE");
        // a plain BeanContextChildSupport (no proxy role) still adds normally
        BeanContextSupport bc = new BeanContextSupport();
        BeanContextChildSupport plain = new BeanContextChildSupport();
        check(bc.add(plain), "plain child unaffected by the proxy rules");
        check(bc.size() == 1 && plain.getBeanContext() == bc, "plain child single membership");
        // peer itself is a plain BeanContextChild and can be added directly elsewhere
        BeanContextSupport other = new BeanContextSupport();
        ProxyBean pb = new ProxyBean();
        check(other.add(pb.peer), "the peer can be used standalone as a child");
        check(other.size() == 1, "standalone peer occupies one slot");
        check(pb.peer.getBeanContext() == other, "standalone peer learns its context");
    }

    public static void main(String[] args) throws Exception {
        testOriginalWhiteBox();
        testWhiteBox();
        testBlackBox();
        testCompositeMembership();
        testNegativeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

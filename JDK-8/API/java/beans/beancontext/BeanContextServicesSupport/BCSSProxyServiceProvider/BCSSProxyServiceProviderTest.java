/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSProxyServiceProvider
 *          conformance: protected inner (non-static) class implementing
 *          BeanContextServiceProvider and BeanContextServiceRevokedListener; structure
 *          (exact member set, package-private ctor taking the nesting context, private
 *          nestingCtxt field, four public methods), lifecycle (proxy created when a
 *          BeanContextServicesSupport is nested under a BeanContextServices parent, not
 *          created under a plain BeanContextSupport parent, cleared on unnesting only
 *          when members exist - recorded source quirk), and delegated behavior
 *          (getService/releaseService forwarding to the parent provider, serviceRevoked
 *          re-issuing events from the nested context on parent revocation).
 * @run main BCSSProxyServiceProviderTest
 */

import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BCSSProxyServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    public interface Svc { String name(); }
    public static class SvcProvider implements BeanContextServiceProvider {
        int getCnt, relCnt;
        final Svc svc = new Svc() { public String name() { return "svc"; } };
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { getCnt++; return svc; }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { relCnt++; }
        public Iterator<Svc> getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) { return new ArrayList<Svc>().iterator(); }
    }
    static class RevokedL implements BeanContextServiceRevokedListener {
        int count; BeanContextServiceRevokedEvent last;
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { count++; last = e; }
    }
    public static class MemberBean extends BeanContextChildSupport {
        private static final long serialVersionUID = 1L;
    }
    /** Exposes the protected proxy field so its lifecycle is observable. */
    public static class ProbingBCSS extends BeanContextServicesSupport {
        boolean hasProxy() { return proxy != null; }
        String proxyClassName() { return proxy == null ? null : proxy.getClass().getName(); }
    }

    // ---- original structural assertions (kept) ----
    static void testExistingReflection() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSProxyServiceProvider");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.beans.beancontext.BeanContextServiceProvider").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServiceProvider");
        check(Class.forName("java.beans.beancontext.BeanContextServiceRevokedListener").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServiceRevokedListener");
        check(hasMethod(c, "getCurrentServiceSelectors"), "declares getCurrentServiceSelectors");
        check(hasMethod(c, "getService"), "declares getService");
        check(hasMethod(c, "releaseService"), "declares releaseService");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSProxyServiceProvider");
        check(Modifier.isProtected(c.getModifiers()), "protected class");
        check(!Modifier.isStatic(c.getModifiers()), "inner class (non-static)");
        check(c.isMemberClass(), "member class");
        check(c.getEnclosingClass() == BeanContextServicesSupport.class, "declared in BeanContextServicesSupport");
        check(c.getSuperclass() == Object.class, "direct subclass of Object");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        Class<?>[] ifaces = c.getInterfaces();
        check(ifaces.length == 2, "exactly two interfaces");
        Set<String> names = new HashSet<String>();
        for (Class<?> i : ifaces) names.add(i.getName());
        check(names.contains("java.beans.beancontext.BeanContextServiceProvider")
                && names.contains("java.beans.beancontext.BeanContextServiceRevokedListener"),
                "implements provider and revoked-listener interfaces");
        // exactly the four methods, all public and non-static
        Set<String> methods = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.size() == 4, "exactly four declared methods");
        check(methods.contains("getService") && methods.contains("releaseService")
                && methods.contains("getCurrentServiceSelectors") && methods.contains("serviceRevoked"),
                "declares getService/releaseService/getCurrentServiceSelectors/serviceRevoked");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
            check(!Modifier.isStatic(m.getModifiers()), m.getName() + " non-static");
            check(!Modifier.isAbstract(m.getModifiers()), m.getName() + " concrete");
        }
        // signatures
        for (Method m : c.getDeclaredMethods()) {
            Class<?>[] ps = m.getParameterTypes();
            if ("getService".equals(m.getName()))
                check(ps.length == 4 && ps[0] == BeanContextServices.class && ps[2] == Class.class,
                        "getService(bcs, requestor, class, selector)");
            if ("releaseService".equals(m.getName()))
                check(ps.length == 3 && ps[0] == BeanContextServices.class,
                        "releaseService(bcs, requestor, service)");
            if ("getCurrentServiceSelectors".equals(m.getName()))
                check(ps.length == 2 && ps[0] == BeanContextServices.class && ps[1] == Class.class,
                        "getCurrentServiceSelectors(bcs, class)");
            if ("serviceRevoked".equals(m.getName()))
                check(ps.length == 1 && ps[0] == BeanContextServiceRevokedEvent.class && m.getReturnType() == void.class,
                        "serviceRevoked(event):void");
        }
        // fields: private nestingCtxt + synthetic outer reference
        Set<String> fields = new HashSet<String>();
        for (Field f : c.getDeclaredFields()) fields.add(f.getName());
        check(fields.size() == 2, "exactly nestingCtxt and the outer reference");
        check(fields.contains("nestingCtxt"), "declares nestingCtxt");
        Field nf = c.getDeclaredField("nestingCtxt");
        check(Modifier.isPrivate(nf.getModifiers()), "nestingCtxt private");
        check(nf.getType() == BeanContextServices.class, "nestingCtxt typed BeanContextServices");
        // ctor: package-private, (BeanContextServicesSupport, BeanContextServices)
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single ctor");
        check(ctors[0].getModifiers() == 0, "ctor package-private (not constructible outside the package)");
        Class<?>[] cps = ctors[0].getParameterTypes();
        check(cps.length == 2 && cps[1] == BeanContextServices.class, "ctor takes outer ref plus nesting context");
    }

    static void testProxyLifecycle() throws Exception {
        ProbingBCSS child = new ProbingBCSS();
        check(!child.hasProxy(), "no proxy before nesting");
        check(child.proxyClassName() == null, "no proxy class name before nesting");
        BeanContextServicesSupport parent = new BeanContextServicesSupport();
        parent.add(child);
        check(child.hasProxy(), "proxy created when nested under a BeanContextServices parent");
        check("java.beans.beancontext.BeanContextServicesSupport$BCSSProxyServiceProvider".equals(child.proxyClassName()),
                "proxy is the exact BCSSProxyServiceProvider class");
        // with a member present, unnesting clears the proxy
        child.add(new MemberBean());
        parent.remove(child);
        check(!child.hasProxy(), "proxy cleared on unnest when members present");
        // recorded source quirk: with zero members releaseBeanContextResources returns
        // early and the proxy field is NOT cleared
        ProbingBCSS empty = new ProbingBCSS();
        BeanContextServicesSupport parent2 = new BeanContextServicesSupport();
        parent2.add(empty);
        check(empty.hasProxy(), "proxy created for memberless nested context");
        parent2.remove(empty);
        check(empty.hasProxy(), "recorded: memberless unnest keeps the stale proxy");
        // plain (non-services) parent never creates a proxy
        ProbingBCSS underPlain = new ProbingBCSS();
        BeanContextSupport plain = new BeanContextSupport();
        plain.add(underPlain);
        check(!underPlain.hasProxy(), "no proxy under a plain BeanContextSupport parent");
        plain.remove(underPlain);
        // direct setBeanContext path also installs the proxy
        ProbingBCSS direct = new ProbingBCSS();
        BeanContextServicesSupport p3 = new BeanContextServicesSupport();
        direct.setBeanContext(p3);
        check(direct.hasProxy(), "setBeanContext installs the proxy");
    }

    static void testDelegatedBehavior() throws Exception {
        BeanContextServicesSupport parent = new BeanContextServicesSupport();
        SvcProvider psp = new SvcProvider();
        parent.addService(Svc.class, psp);
        ProbingBCSS child = new ProbingBCSS();
        parent.add(child);
        check(child.hasProxy(), "delegation active");
        MemberBean member = new MemberBean();
        child.add(member);
        RevokedL rl = new RevokedL();
        Object requestor = new Object();
        // getService is forwarded through the proxy to the parent provider
        Object svc = child.getService(member, requestor, Svc.class, null, rl);
        check(svc == psp.svc, "proxy forwards getService to the parent provider");
        check(psp.getCnt == 1, "parent provider consulted exactly once");
        // releaseService is forwarded as well
        child.releaseService(member, requestor, svc);
        check(psp.relCnt == 1, "proxy forwards releaseService to the parent provider");
        // parent revocation re-issues the event from the nested context (proxy serviceRevoked);
        // the released reference is gone, so acquire a fresh delegated one first
        Object svc2 = child.getService(member, requestor, Svc.class, null, rl);
        check(svc2 == psp.svc && psp.getCnt == 2, "fresh delegated grant before revoke probe");
        parent.revokeService(Svc.class, psp, true);
        check(rl.count == 1, "proxy serviceRevoked notifies delegated requestor");
        check(rl.last.getServiceClass() == Svc.class, "re-issued event service class exact");
        check(rl.last.isCurrentServiceInvalidNow(), "re-issued event keeps invalidate=true");
        check(rl.last.getSourceAsBeanContextServices() == child, "re-issued event source is the nested context");
        check(!child.hasService(Svc.class), "delegated visibility gone after parent revoke");
    }

    public static void main(String[] args) throws Exception {
        testExistingReflection();
        testStructure();
        testProxyLifecycle();
        testDelegatedBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

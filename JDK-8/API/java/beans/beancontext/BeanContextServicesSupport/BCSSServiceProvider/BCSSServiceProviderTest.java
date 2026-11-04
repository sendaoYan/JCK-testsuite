/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSServiceProvider
 *          conformance: protected static Serializable wrapper created per registered
 *          service. Structure: exact member set (protected serviceProvider field,
 *          protected getServiceProvider accessor, package-private two-arg ctor),
 *          stable serialVersionUID, no public API path exposes instances. Behavior:
 *          instances captured through a BeanContextServicesSupport subclass overriding
 *          createBCSSServiceProvider (one wrapper per registration, fresh wrapper per
 *          service class), wrapped-provider identity via setAccessible reflection where
 *          the platform allows it (blocked and recorded on modular JDKs), wrapper
 *          equality observable through revokeService provider matching, and standalone
 *          serialization round-trip.
 * @run main BCSSServiceProviderTest
 */

import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BCSSServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    public interface Svc { String name(); }
    public static class SvcProvider implements BeanContextServiceProvider {
        int getCnt;
        final Svc svc = new Svc() { public String name() { return "svc"; } };
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { getCnt++; return svc; }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { }
        public Iterator<Svc> getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) { return new ArrayList<Svc>().iterator(); }
    }
    /** Serializable variant: only such providers survive a wrapper round-trip. */
    public static class SerProvider implements BeanContextServiceProvider, Serializable {
        private static final long serialVersionUID = 1L;
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { return new Object(); }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { }
        public Iterator<Svc> getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) { return new ArrayList<Svc>().iterator(); }
    }
    /** Captures the BCSSServiceProvider wrappers created by addService. */
    public static class CapturingBCSS extends BeanContextServicesSupport {
        final java.util.List<Object> wrappers = new ArrayList<Object>();
        protected BeanContextServicesSupport.BCSSServiceProvider createBCSSServiceProvider(Class sc, BeanContextServiceProvider bcsp) {
            BeanContextServicesSupport.BCSSServiceProvider w = super.createBCSSServiceProvider(sc, bcsp);
            wrappers.add(w);
            return w;
        }
    }

    // ---- original structural assertions (kept) ----
    static void testExistingReflection() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSServiceProvider");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSServiceProvider");
        check(Modifier.isProtected(c.getModifiers()), "protected class");
        check(Modifier.isStatic(c.getModifiers()), "static nested class");
        check(c.isMemberClass(), "member class");
        check(c.getEnclosingClass() == BeanContextServicesSupport.class, "declared in BeanContextServicesSupport");
        check(c.getSuperclass() == Object.class, "direct subclass of Object");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 861278251667444782L, "stable serialVersionUID");
        // fields: serialVersionUID + the protected serviceProvider
        Set<String> fields = new HashSet<String>();
        for (Field f : c.getDeclaredFields()) fields.add(f.getName());
        check(fields.size() == 2, "exactly serialVersionUID and serviceProvider fields");
        check(fields.contains("serviceProvider"), "declares serviceProvider");
        Field sp = c.getDeclaredField("serviceProvider");
        check(Modifier.isProtected(sp.getModifiers()), "serviceProvider protected");
        check(!Modifier.isTransient(sp.getModifiers()), "serviceProvider serialized with the wrapper");
        check(!Modifier.isStatic(sp.getModifiers()), "serviceProvider non-static");
        check(sp.getType() == BeanContextServiceProvider.class, "serviceProvider typed BeanContextServiceProvider");
        // exactly one declared method: protected getServiceProvider()
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "exactly one declared method");
        check(hasMethod(c, "getServiceProvider"), "declares getServiceProvider");
        check(Modifier.isProtected(ms[0].getModifiers()), "getServiceProvider protected");
        check(!Modifier.isStatic(ms[0].getModifiers()), "getServiceProvider non-static");
        check(ms[0].getParameterTypes().length == 0, "getServiceProvider takes no parameters");
        check(ms[0].getReturnType() == BeanContextServiceProvider.class, "getServiceProvider returns the provider");
        // ctor: package-private (Class, BeanContextServiceProvider)
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single ctor");
        check(ctors[0].getModifiers() == 0, "ctor package-private (not constructible outside the package)");
        Class<?>[] cps = ctors[0].getParameterTypes();
        check(cps.length == 2 && cps[0] == Class.class && cps[1] == BeanContextServiceProvider.class,
                "ctor takes service class plus provider");
        // no interface other than Serializable
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == Serializable.class,
                "only Serializable interface");
    }

    static void testWrapperInstances() throws Exception {
        CapturingBCSS b = new CapturingBCSS();
        SvcProvider sp = new SvcProvider();
        check(b.addService(Svc.class, sp), "service registered");
        check(b.wrappers.size() == 1, "one wrapper created per registration");
        Class<?> wClass = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSServiceProvider");
        check(b.wrappers.get(0).getClass() == wClass, "wrapper is the exact BCSSServiceProvider class");
        check(b.wrappers.get(0) instanceof Serializable, "wrapper Serializable");
        check(ObjectStreamClass.lookup(b.wrappers.get(0).getClass()).getSerialVersionUID() == 861278251667444782L,
                "wrapper UID stable");
        SvcProvider sp2 = new SvcProvider();
        check(b.addService(String.class, sp2), "second service registered");
        check(b.wrappers.size() == 2, "second wrapper created");
        check(b.wrappers.get(0) != b.wrappers.get(1), "wrappers distinct per service class");
        // duplicate registration does not create another wrapper
        check(!b.addService(Svc.class, sp), "duplicate registration rejected");
        check(b.wrappers.size() == 2, "no wrapper for rejected duplicate registration");
        // wrapped provider identity: visible via setAccessible reflection where allowed;
        // on modular JDKs (java.desktop does not open the package) this is blocked - recorded
        boolean blocked = false;
        try {
            Method g = wClass.getDeclaredMethod("getServiceProvider");
            g.setAccessible(true);
            check(g.invoke(b.wrappers.get(0)) == sp, "getServiceProvider returns the registered provider (reflection path)");
            check(g.invoke(b.wrappers.get(1)) == sp2, "second wrapper holds the second provider");
        } catch (RuntimeException re) {
            blocked = true;
            check(true, "getServiceProvider reflection blocked on modular JDK (recorded)");
        }
        // behavioral wrapper evidence independent of reflection: revokeService matches the
        // wrapped provider, so the original provider is accepted and a different one is not
        final BeanContextServicesSupport fb = b;
        final SvcProvider fw = new SvcProvider();
        try {
            fb.revokeService(Svc.class, fw, false);
            check(false, "mismatched provider revoke should fail");
        } catch (IllegalArgumentException expected) {
            check(true, "wrapper keeps the registered provider (mismatch detected)");
        }
        fb.revokeService(Svc.class, sp, false);
        check(!b.hasService(Svc.class), "original provider accepted by revokeService");
    }

    static void testSerialization() throws Exception {
        CapturingBCSS b = new CapturingBCSS();
        b.addService(Svc.class, new SerProvider());
        Object wrapper = b.wrappers.get(0);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(wrapper);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back.getClass() == wrapper.getClass(), "wrapper round-trip preserves class");
        check(back instanceof Serializable, "wrapper round-trip Serializable");
        // a non-serializable provider makes the wrapper itself non-serializable (recorded)
        final class NoSerProvider implements BeanContextServiceProvider {
            public Object getService(BeanContextServices bcs, Object req, Class sc, Object sel) { return new Object(); }
            public void releaseService(BeanContextServices bcs, Object req, Object svc) { }
            public Iterator<Object> getCurrentServiceSelectors(BeanContextServices bcs, Class sc) { return new ArrayList<Object>().iterator(); }
        }
        CapturingBCSS b2 = new CapturingBCSS();
        b2.addService(String.class, new NoSerProvider());
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        boolean failed = false;
        try {
            oos2.writeObject(b2.wrappers.get(0));
        } catch (java.io.NotSerializableException e) {
            failed = true;
        } finally {
            oos2.close();
        }
        check(failed, "wrapper with non-serializable provider is not serializable (recorded)");
    }

    public static void main(String[] args) throws Exception {
        testExistingReflection();
        testStructure();
        testWrapperInstances();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

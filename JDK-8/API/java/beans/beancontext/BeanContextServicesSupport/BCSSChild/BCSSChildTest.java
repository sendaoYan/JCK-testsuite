/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSChild conformance:
 *          nested per-child bookkeeping class. Structure: protected inner (non-static)
 *          class extending BeanContextSupport.BCSChild, Serializable with stable
 *          serialVersionUID, package-private ctor and helper methods, two nested
 *          package-private reference-holder classes, transient service bookkeeping
 *          fields. Behavior: instances obtained through a BeanContextServicesSupport
 *          subclass overriding createBCSChild (one fresh instance per add, replaced on
 *          re-add), independent per-child service bookkeeping observed via provider
 *          release counters, and standalone serialization round-trip.
 * @run main BCSSChildTest
 */

import java.beans.beancontext.BeanContextChildSupport;
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

public class BCSSChildTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- service fixture: counts releases so per-child bookkeeping is observable ----
    public interface Svc { String name(); }
    public static class SvcProvider implements BeanContextServiceProvider {
        int getCnt, relCnt;
        final Svc svc = new Svc() { public String name() { return "svc"; } };
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { getCnt++; return svc; }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { relCnt++; }
        public Iterator<Svc> getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) { return new ArrayList<Svc>().iterator(); }
    }
    static class RevokedL implements BeanContextServiceRevokedListener {
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { }
    }
    public static class MemberBean extends BeanContextChildSupport {
        private static final long serialVersionUID = 1L;
    }
    /** Captures the BCSSChild instances created by the context. */
    public static class CapturingBCSS extends BeanContextServicesSupport {
        final java.util.List<BeanContextSupport.BCSChild> created = new ArrayList<BeanContextSupport.BCSChild>();
        protected BeanContextSupport.BCSChild createBCSChild(Object targetChild, Object peer) {
            BeanContextSupport.BCSChild c = super.createBCSChild(targetChild, peer);
            created.add(c);
            return c;
        }
    }

    // ---- original structural assertions (kept) ----
    static void testExistingReflection() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSChild");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextSupport$BCSChild".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextSupport$BCSChild");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSChild");
        Class<?> bcsChildClass = Class.forName("java.beans.beancontext.BeanContextSupport$BCSChild");
        check(bcsChildClass.isAssignableFrom(c), "assignable to BeanContextSupport.BCSChild");
        check(Modifier.isProtected(c.getModifiers()), "protected class");
        check(!Modifier.isPublic(c.getModifiers()), "not public");
        check(!Modifier.isStatic(c.getModifiers()), "inner class (non-static)");
        check(c.isMemberClass(), "member class");
        check("BCSSChild".equals(c.getSimpleName()), "simple name");
        check(c.getEnclosingClass() == BeanContextServicesSupport.class, "declared in BeanContextServicesSupport");
        check(c.getDeclaringClass() == BeanContextServicesSupport.class, "declaring class");
        check(Serializable.class.isAssignableFrom(c), "Serializable");
        check(Serializable.class.isAssignableFrom(c.getSuperclass()), "superclass Serializable");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -3263851306889194873L, "stable serialVersionUID");
        // fields: constant + two transient bookkeeping maps + synthetic outer ref
        Set<String> fields = new HashSet<String>();
        for (Field f : c.getDeclaredFields()) fields.add(f.getName());
        check(fields.contains("serialVersionUID"), "declares serialVersionUID");
        check(fields.contains("serviceClasses"), "declares serviceClasses map");
        check(fields.contains("serviceRequestors"), "declares serviceRequestors map");
        check(fields.size() == 4, "exactly serialVersionUID/serviceClasses/serviceRequestors/outer-ref fields");
        for (Field f : c.getDeclaredFields()) {
            if ("serviceClasses".equals(f.getName()) || "serviceRequestors".equals(f.getName()))
                check(Modifier.isTransient(f.getModifiers()), f.getName() + " transient");
        }
        // methods: exactly the five package-private helpers
        Set<String> methods = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.size() == 5, "exactly five declared methods");
        check(methods.contains("usingService") && methods.contains("releaseService")
                && methods.contains("revokeService") && methods.contains("cleanupReferences")
                && methods.contains("revokeAllDelegatedServicesNow"), "declares the five bookkeeping helpers");
        for (Method m : c.getDeclaredMethods()) {
            int mm = m.getModifiers();
            check(!Modifier.isPublic(mm) && !Modifier.isProtected(mm) && !Modifier.isPrivate(mm),
                    "helper " + m.getName() + " package-private");
            check(!Modifier.isStatic(mm), "helper " + m.getName() + " non-static");
        }
        // usingService throws TooManyListenersException and UnsupportedOperationException
        boolean tmle = false, uoe = false;
        for (Method m : c.getDeclaredMethods()) {
            if ("usingService".equals(m.getName())) {
                check(m.getParameterTypes().length == 6, "usingService takes six parameters");
                for (Class<?> ex : m.getExceptionTypes()) {
                    if (ex == java.util.TooManyListenersException.class) tmle = true;
                    if (ex == UnsupportedOperationException.class) uoe = true;
                }
            }
        }
        check(tmle, "usingService declares TooManyListenersException");
        check(uoe, "usingService declares UnsupportedOperationException");
        // ctor: package-private, (BeanContextServicesSupport, Object, Object)
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single ctor");
        check(ctors[0].getModifiers() == 0, "ctor package-private (not constructible outside the package)");
        check(ctors[0].getParameterTypes().length == 3, "ctor takes outer ref + child + peer");
        // nested reference holders
        Set<String> nested = new HashSet<String>();
        for (Class<?> n : c.getDeclaredClasses()) nested.add(n.getSimpleName());
        check(nested.size() == 2 && nested.contains("BCSSCServiceClassRef") && nested.contains("BCSSCServiceRef"),
                "two nested reference-holder classes");
        for (Class<?> n : c.getDeclaredClasses())
            check(n.getModifiers() == 0, "nested " + n.getSimpleName() + " package-private");
    }

    static void testInstances() throws Exception {
        CapturingBCSS b = new CapturingBCSS();
        MemberBean m1 = new MemberBean();
        b.add(m1);
        check(b.created.size() == 1, "one BCSSChild created per add");
        Class<?> bcsc = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSChild");
        // element static type is the protected BCSChild: use Object locals to inspect
        Object first = b.created.get(0);
        check(first.getClass() == bcsc, "createBCSChild produces the exact BCSSChild class");
        check(first instanceof Serializable, "created child is Serializable");
        MemberBean m2 = new MemberBean();
        b.add(m2);
        Object second = b.created.get(1);
        check(first != second, "distinct instance per member");
        b.remove(m1);
        b.add(m1);
        check(b.created.size() == 3, "re-add creates a fresh BCSSChild");
        check(second != (Object) b.created.get(2), "fresh instance distinct from old one");
        check(b.contains(m1) && b.contains(m2) && b.size() == 2, "members tracked after re-add");
        // standalone serialization round-trip of a captured instance
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(first);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back.getClass() == bcsc, "round-trip preserves BCSSChild class");
        check(back instanceof Serializable, "round-trip instance Serializable");
    }

    static void testPerChildBookkeeping() throws Exception {
        CapturingBCSS b = new CapturingBCSS();
        SvcProvider sp = new SvcProvider();
        b.addService(Svc.class, sp);
        MemberBean m1 = new MemberBean();
        MemberBean m2 = new MemberBean();
        b.add(m1);
        b.add(m2);
        RevokedL rl = new RevokedL();
        Object s1 = b.getService(m1, new Object(), Svc.class, null, rl);
        Object s2 = b.getService(m2, new Object(), Svc.class, null, rl);
        check(s1 == sp.svc && s2 == sp.svc, "both members granted the service");
        check(sp.getCnt == 2, "provider consulted once per member");
        // removing one member releases exactly that member's reference
        int base = sp.relCnt;
        b.remove(m1);
        check(sp.relCnt == base + 1, "member removal releases only its own service references");
        check(b.size() == 1 && b.contains(m2), "other member untouched");
        b.remove(m2);
        check(sp.relCnt == base + 2, "second member removal releases its reference");
        check(b.created.size() == 2, "two BCSSChild instances were bookkeeping the two members");
    }

    public static void main(String[] args) throws Exception {
        testExistingReflection();
        testStructure();
        testInstances();
        testPerChildBookkeeping();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

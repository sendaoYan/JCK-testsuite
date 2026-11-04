/*
 * @test
 * @summary java.beans.beancontext.BeanContextSupport.BCSChild conformance: white-box structure + black-box behavior.
 *          Structure: protected non-static inner class of BeanContextSupport, implements
 *          Serializable, single package-private 3-arg ctor, package-private accessor
 *          method set, stable serialVersionUID. Behavior exercised through the public
 *          BeanContextSupport surface: one wrapper per child, BeanContextProxy children
 *          get a linked wrapper pair (both entries added/removed atomically, inner child
 *          nested, null proxy -> NPE) and wrappers survive serialization round-trip.
 * @run main BCSChildTest
 */

import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextProxy;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BCSChildTest {
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

    static class ProxyChild implements BeanContextProxy, java.io.Serializable {
        BeanContextChild inner = new BeanContextChildSupport();
        public BeanContextChild getBeanContextProxy() { return inner; }
    }

    static class NullProxyChild implements BeanContextProxy {
        public BeanContextChild getBeanContextProxy() { return null; }
    }

    static class Rec implements BeanContextMembershipListener {
        int added, removed;
        Object[] lastAdded, lastRemoved;
        public void childrenAdded(BeanContextMembershipEvent e) { added++; lastAdded = e.toArray(); }
        public void childrenRemoved(BeanContextMembershipEvent e) { removed++; lastRemoved = e.toArray(); }
    }

    static BeanContextSupport roundTrip(BeanContextSupport bc) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(bc);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BeanContextSupport back = (BeanContextSupport) ois.readObject();
        ois.close();
        return back;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport$BCSChild");
        int m = c.getModifiers();
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!Modifier.isFinal(m), "not final");
        check(Modifier.isProtected(m), "protected class");
        check(!Modifier.isPublic(m), "not public");
        check(!Modifier.isStatic(m), "non-static inner class (holds this$0)");
        check(Modifier.isPublic(c.getSuperclass().getModifiers()) && c.getSuperclass() == Object.class, "extends Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check("java.beans.beancontext.BeanContextSupport".equals(c.getDeclaringClass().getName()),
              "declared inside BeanContextSupport");
        check(c.getName().endsWith("BeanContextSupport$BCSChild"), "binary name is nested");
        // single package-private ctor with 3 params (outer ref, child, proxy peer)
        check(c.getDeclaredConstructors().length == 1, "single declared ctor");
        java.lang.reflect.Constructor<?> ct = c.getDeclaredConstructors()[0];
        check(!Modifier.isPublic(ct.getModifiers()) && !Modifier.isProtected(ct.getModifiers()), "ctor package-private");
        Class<?>[] params = ct.getParameterTypes();
        check(params.length == 3, "ctor has 3 parameters");
        check("java.beans.beancontext.BeanContextSupport".equals(params[0].getName()), "ctor param 0 is the outer BCS");
        check(params[1] == Object.class && params[2] == Object.class, "ctor params 1/2 are Object child/proxyPeer");
        // package-private accessor surface (no public methods of its own)
        List<String> names = new ArrayList<String>();
        boolean allPackagePrivate = true;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) {
            names.add(mm.getName());
            if (Modifier.isPublic(mm.getModifiers()) || Modifier.isProtected(mm.getModifiers())) allPackagePrivate = false;
        }
        java.util.Collections.sort(names);
        check(names.equals(Arrays.asList("getChild", "getProxyPeer", "isProxyPeer", "isRemovePending", "setRemovePending")),
              "declared methods exactly the package-private accessors: " + names);
        check(allPackagePrivate, "accessors are package-private");
        // fields: child, proxyPeer, removePending + serialVersionUID
        List<String> fields = new ArrayList<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fields.add(f.getName());
        check(fields.contains("child") && fields.contains("proxyPeer") && fields.contains("removePending"),
              "declared fields child/proxyPeer/removePending: " + fields);
        java.lang.reflect.Field suid = c.getDeclaredField("serialVersionUID");
        check(Modifier.isStatic(suid.getModifiers()) && Modifier.isFinal(suid.getModifiers()), "serialVersionUID static final");
        // stable serialVersionUID (JDK 1.7 FCS value)
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -5815286101609939109L, "serialVersionUID stable");
    }

    static void testBlackBox() throws Exception {
        java.beans.beancontext.BeanContextSupport bc = new java.beans.beancontext.BeanContextSupport();
        check(bc.add("kid"), "add creates BCSChild internally");
        check(bc.size() == 1 && bc.contains("kid"), "child tracked");
        check("kid".equals(bc.iterator().next()), "iterator yields child");
        // duplicate add does not create a second wrapper
        check(!bc.add("kid"), "duplicate add false");
        check(bc.size() == 1, "one wrapper per child");
        // removing the child drops the wrapper
        check(bc.remove("kid") && bc.size() == 0, "remove drops wrapper");
    }

    static void testProxyPeerWrappers() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        Rec rec = new Rec();
        bc.addBeanContextMembershipListener(rec);
        ProxyChild pc = new ProxyChild();
        bc.add(pc);
        check(bc.size() == 2, "proxy child creates wrapper pair (outer + proxy peer)");
        check(bc.contains(pc), "outer child tracked");
        check(bc.contains(pc.inner), "proxy peer tracked as separate entry");
        check(rec.added == 1 && rec.lastAdded.length == 2, "childrenAdded carries both entries");
        check(pc.inner.getBeanContext() == bc, "proxy peer child nested in the context");
        // removing the outer removes both wrappers atomically
        bc.remove(pc);
        check(bc.size() == 0, "removing outer drops both wrappers");
        check(!bc.contains(pc.inner), "proxy peer entry gone");
        check(rec.removed == 1 && rec.lastRemoved.length == 2, "childrenRemoved carries both entries");
        // null getBeanContextProxy is rejected
        final BeanContextSupport bcn = new BeanContextSupport();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bcn.add(new NullProxyChild()); } }, "null getBeanContextProxy -> NPE");
        check(bcn.size() == 0, "null proxy leaves no entries");
    }

    static void testSerialization() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        bc.add("wrapped");
        BeanContextSupport back = roundTrip(bc);
        check(back.size() == 1, "wrapper survives serialization round-trip");
        check(back.contains("wrapped"), "wrapped child survives with the BCSChild");
        // proxy pair also survives
        BeanContextSupport bc2 = new BeanContextSupport();
        ProxyChild pc = new ProxyChild();
        bc2.add(pc);
        BeanContextSupport back2 = roundTrip(bc2);
        check(back2.size() == 2, "proxy wrapper pair survives round-trip");
        int proxys = 0, peers = 0;
        for (Object o : back2.toArray()) {
            if (o instanceof ProxyChild) proxys++;
            else if (o instanceof BeanContextChildSupport) peers++;
        }
        check(proxys == 1 && peers == 1, "both proxy entries survive as copies (1 outer + 1 peer), got " + proxys + "/" + peers);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testProxyPeerWrappers();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

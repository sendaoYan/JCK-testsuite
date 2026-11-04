/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceAvailableEvent conformance: white-box structure + black-box behavior.
 *          Exact class shape (extends BeanContextEvent, single public ctor (BeanContextServices, Class),
 *          protected non-transient serviceClass field, serialVersionUID); source/serviceClass
 *          identity; getCurrentServiceSelectors live delegation to the source context (provider
 *          selectors while registered, null after revoke - a live view, not a snapshot);
 *          null-source IAE from EventObject vs stored null serviceClass; propagation state
 *          inherited from BeanContextEvent; toString contract; and serialization round-trip
 *          (serviceClass and propagatedFrom survive, transient EventObject.source drops to null
 *          and getCurrentServiceSelectors then NPEs).
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextServiceAvailableEventTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextEvent;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesListener;
import java.beans.beancontext.BeanContextServicesSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BeanContextServiceAvailableEventTest {
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
    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }
    static Object deser(byte[] b) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    public interface MyService { String name(); }

    public static class MyProvider implements BeanContextServiceProvider {
        final Object svc = new Object();
        public Object getService(BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { return svc; }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) { }
        public Iterator getCurrentServiceSelectors(BeanContextServices bcs, Class serviceClass) {
            List<String> l = new ArrayList<String>();
            l.add("s-one");
            l.add("s-two");
            return l.iterator();
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
        public void serviceAvailable(BeanContextServiceAvailableEvent e) { availableCount++; lastAvailable = e; }
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { revokedCount++; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceAvailableEvent");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextEvent".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextEvent");
        check(hasMethod(c, "getCurrentServiceSelectors"), "declares getCurrentServiceSelectors");
        check(hasMethod(c, "getServiceClass"), "declares getServiceClass");
        check(hasMethod(c, "getSourceAsBeanContextServices"), "declares getSourceAsBeanContextServices");
        // exact declared method inventory
        check(methodNames(c).equals("[getCurrentServiceSelectors, getServiceClass, getSourceAsBeanContextServices]"),
                "exact 3-method inventory");
        check(c.getDeclaredMethods().length == 3, "declares exactly 3 methods");
        for (Method m : c.getDeclaredMethods())
            check(Modifier.isPublic(m.getModifiers()), "method " + m.getName() + " is public");
        // exactly one public constructor with exact parameter types
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "exactly one public constructor");
        Class<?>[] ps = ctors[0].getParameterTypes();
        check(ps.length == 2 && ps[0] == java.beans.beancontext.BeanContextServices.class && ps[1] == Class.class,
                "ctor (BeanContextServices, Class)");
        // protected serviceClass field, not transient -> survives serialization
        boolean foundProtected = false;
        for (Field f : c.getDeclaredFields()) {
            if ("serviceClass".equals(f.getName())) {
                foundProtected = true;
                check(Modifier.isProtected(f.getModifiers()), "serviceClass is protected");
                check(!Modifier.isTransient(f.getModifiers()), "serviceClass is not transient");
            }
        }
        check(foundProtected, "declares the serviceClass field");
        // serializable with the stable serialVersionUID
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -5333985775656400778L,
                "serialVersionUID stable (-5333985775656400778)");
        // inherited surface
        check(hasMethod(BeanContextEvent.class, "getBeanContext"), "inherits getBeanContext");
        check(hasMethod(BeanContextEvent.class, "setPropagatedFrom"), "inherits setPropagatedFrom");
        check(hasMethod(BeanContextEvent.class, "getPropagatedFrom"), "inherits getPropagatedFrom");
        check(hasMethod(BeanContextEvent.class, "isPropagated"), "inherits isPropagated");
        // deprecation status on this JDK
        check(c.getAnnotation(Deprecated.class) == null, "not deprecated on JDK 8");
    }

    static void testBlackBox() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        bcss.addService(Object.class, new BeanContextServiceProvider() {
            public Object getService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Class serviceClass, Object selector) { return new Object(); }
            public void releaseService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Object service) {}
            public Iterator getCurrentServiceSelectors(java.beans.beancontext.BeanContextServices bcs, Class serviceClass) { return Collections.emptyList().iterator(); }
        });
        BeanContextServiceAvailableEvent e = new BeanContextServiceAvailableEvent(bcss, Object.class);
        check(bcss == e.getSourceAsBeanContextServices(), "getSourceAsBeanContextServices");
        check(Object.class == e.getServiceClass(), "getServiceClass");
        check(bcss == e.getBeanContext(), "getBeanContext");
        check(e.getCurrentServiceSelectors() != null, "getCurrentServiceSelectors non-null");
    }

    static void testSemantics() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        MyProvider p = new MyProvider();
        MyServicesListener sl = new MyServicesListener();
        bcss.addBeanContextServicesListener(sl);
        // the real flow: addService fires an event with this exact shape
        bcss.addService(MyService.class, p);
        BeanContextServiceAvailableEvent e = sl.lastAvailable;
        check(e != null, "registration delivers an available event");
        check(e.getSource() == bcss, "getSource identity");
        check(e.getSourceAsBeanContextServices() == bcss, "getSourceAsBeanContextServices identity");
        check(e.getBeanContext() == bcss, "getBeanContext identity");
        check(e.getBeanContext() instanceof BeanContext, "getBeanContext type is BeanContext");
        check(e.getServiceClass() == MyService.class, "getServiceClass exact");
        // selectors delegate LIVE to the source context: provider contents while registered
        Iterator<?> sels = e.getCurrentServiceSelectors();
        check("s-one".equals(sels.next()) && "s-two".equals(sels.next()) && !sels.hasNext(),
                "selectors are the provider's, in order");
        // after revoke the same event resolves selectors to null: live view, not a snapshot
        bcss.revokeService(MyService.class, p, true);
        check(e.getCurrentServiceSelectors() == null,
                "selectors of the same event become null once the service is revoked (live delegation)");
        // an unregistered class also resolves to null via the source
        BeanContextServiceAvailableEvent other = new BeanContextServiceAvailableEvent(bcss, java.io.Serializable.class);
        check(other.getCurrentServiceSelectors() == null, "selectors for never-registered class null");
        // a context with the class registered returns non-null for a hand-made event
        BeanContextServicesSupport b2 = new BeanContextServicesSupport();
        b2.addService(Integer.class, p);
        check(new BeanContextServiceAvailableEvent(b2, Integer.class).getCurrentServiceSelectors() != null,
                "hand-made event over a registered context sees selectors");
    }

    static void testNullMatrix() throws Exception {
        final BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        // null source: EventObject contract - IllegalArgumentException, not NPE
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new BeanContextServiceAvailableEvent(null, MyService.class); } },
                "null source -> IAE (EventObject 'null source')");
        // null serviceClass: accepted and stored, surfaces through every accessor
        BeanContextServiceAvailableEvent e = new BeanContextServiceAvailableEvent(bcss, null);
        check(e.getServiceClass() == null, "null serviceClass stored and returned");
        check(e.getSourceAsBeanContextServices() == bcss, "source unaffected by null serviceClass");
        check(e.getCurrentServiceSelectors() == null, "selectors resolve to null via source lookup");
        check(!e.isPropagated(), "propagation state unaffected");
        check(e.toString() != null, "toString non-null with null serviceClass");
    }

    static void testPropagation() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        BeanContextServiceAvailableEvent e = new BeanContextServiceAvailableEvent(bcss, MyService.class);
        check(!e.isPropagated(), "fresh event is not propagated");
        check(e.getPropagatedFrom() == null, "propagatedFrom defaults to null");
        BeanContextSupportHolder outer = new BeanContextSupportHolder();
        e.setPropagatedFrom(outer.context);
        check(e.isPropagated(), "marked propagated");
        check(e.getPropagatedFrom() == outer.context, "propagatedFrom identity round trip");
        e.setPropagatedFrom(null);
        check(!e.isPropagated() && e.getPropagatedFrom() == null, "setPropagatedFrom(null) resets state");
        // toString mentions the class name and the source
        BeanContextServiceAvailableEvent t = new BeanContextServiceAvailableEvent(bcss, MyService.class);
        check(t.toString().startsWith("java.beans.beancontext.BeanContextServiceAvailableEvent[source="),
                "toString starts with class name and source");
        check(t.toString().contains(String.valueOf(bcss)), "toString contains the source");
    }

    static final class BeanContextSupportHolder {
        final java.beans.beancontext.BeanContextSupport context = new java.beans.beancontext.BeanContextSupport();
    }

    static void testSerialization() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        BeanContextServiceAvailableEvent e = new BeanContextServiceAvailableEvent(bcss, MyService.class);
        e.setPropagatedFrom(new java.beans.beancontext.BeanContextSupport());
        BeanContextServiceAvailableEvent back = (BeanContextServiceAvailableEvent) deser(ser(e));
        check(back != null && back != e, "round trip returns a distinct instance");
        check(back.getServiceClass() == MyService.class, "serviceClass survives round trip");
        check(back.isPropagated(), "propagatedFrom survives round trip");
        check(back.getPropagatedFrom() != null, "propagatedFrom value survives");
        check(back.getSource() == null, "EventObject source is transient -> null after round trip");
        check(back.getSourceAsBeanContextServices() == null, "getSourceAsBeanContextServices null after round trip");
        check(back.getBeanContext() == null, "getBeanContext null after round trip");
        // with a nulled source, selector delegation dereferences null -> NPE (verified on both JDKs)
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { back.getCurrentServiceSelectors(); } },
                "getCurrentServiceSelectors after round trip -> NPE (transient source)");
        // an unpropagated event stays unpropagated
        BeanContextServiceAvailableEvent plain = (BeanContextServiceAvailableEvent)
                deser(ser(new BeanContextServiceAvailableEvent(bcss, Integer.class)));
        check(!plain.isPropagated(), "unpropagated event stays unpropagated");
        check(plain.getServiceClass() == Integer.class, "other service classes survive too");
        check(plain.toString() != null, "deserialized toString still works");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSemantics();
        testNullMatrix();
        testPropagation();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

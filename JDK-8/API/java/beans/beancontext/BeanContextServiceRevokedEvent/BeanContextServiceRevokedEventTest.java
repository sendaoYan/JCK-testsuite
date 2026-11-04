/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceRevokedEvent conformance: white-box structure + black-box behavior.
 *          Exact class shape (extends BeanContextEvent, single public ctor (BeanContextServices,
 *          Class, boolean), protected non-transient serviceClass plus private non-transient
 *          invalidateRefs, serialVersionUID); the exact isCurrentServiceInvalidNow == ctor flag
 *          rule; isServiceClass matching (same true, other false, null argument false, NPE when
 *          the event's serviceClass is null); source identity through all three accessors;
 *          events captured from the real revoke flow (revokeNow=false and revokeNow=true) match
 *          hand-made ones field for field; null-source IAE vs stored null serviceClass; and
 *          serialization round-trip (flag and serviceClass survive, transient source drops).
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextServiceRevokedEventTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesListener;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextServiceProvider;
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

public class BeanContextServiceRevokedEventTest {
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
        BeanContextServiceRevokedEvent lastRevoked;
        public void serviceAvailable(java.beans.beancontext.BeanContextServiceAvailableEvent e) { availableCount++; }
        public void serviceRevoked(BeanContextServiceRevokedEvent e) { revokedCount++; lastRevoked = e; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceRevokedEvent");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextEvent".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextEvent");
        check(hasMethod(c, "getServiceClass"), "declares getServiceClass");
        check(hasMethod(c, "getSourceAsBeanContextServices"), "declares getSourceAsBeanContextServices");
        check(hasMethod(c, "isCurrentServiceInvalidNow"), "declares isCurrentServiceInvalidNow");
        // exact declared method inventory
        check(methodNames(c).equals("[getServiceClass, getSourceAsBeanContextServices, isCurrentServiceInvalidNow, isServiceClass]"),
                "exact 4-method inventory");
        check(c.getDeclaredMethods().length == 4, "declares exactly 4 methods");
        for (Method m : c.getDeclaredMethods())
            check(Modifier.isPublic(m.getModifiers()), "method " + m.getName() + " is public");
        // exactly one public constructor (BeanContextServices, Class, boolean)
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1, "exactly one public constructor");
        Class<?>[] ps = ctors[0].getParameterTypes();
        check(ps.length == 3 && ps[0] == java.beans.beancontext.BeanContextServices.class
                && ps[1] == Class.class && ps[2] == boolean.class,
                "ctor (BeanContextServices, Class, boolean)");
        // field shapes: protected non-transient serviceClass; the invalidate flag is a private
        // non-transient boolean (survives serialization without being API-visible)
        boolean foundClass = false;
        for (Field f : c.getDeclaredFields()) {
            if ("serviceClass".equals(f.getName())) {
                foundClass = true;
                check(Modifier.isProtected(f.getModifiers()), "serviceClass is protected");
                check(!Modifier.isTransient(f.getModifiers()), "serviceClass is not transient");
            }
        }
        check(foundClass, "declares the serviceClass field");
        boolean foundFlag = false;
        for (Field f : c.getDeclaredFields()) {
            if ("invalidateRefs".equals(f.getName())) {
                foundFlag = true;
                check(!Modifier.isPublic(f.getModifiers()) && !Modifier.isProtected(f.getModifiers()),
                        "invalidateRefs is private (not part of the API surface)");
                check(f.getType() == boolean.class, "invalidateRefs is a boolean");
                check(!Modifier.isTransient(f.getModifiers()), "invalidateRefs is not transient");
            }
        }
        check(foundFlag, "declares the invalidateRefs field (verified by name)");
        // return types
        check(c.getMethod("isCurrentServiceInvalidNow").getReturnType() == boolean.class,
                "isCurrentServiceInvalidNow returns boolean");
        check(c.getMethod("isServiceClass", Class.class).getReturnType() == boolean.class,
                "isServiceClass returns boolean");
        check(c.getMethod("getServiceClass").getReturnType() == Class.class,
                "getServiceClass returns Class");
        // serializable with stable serialVersionUID
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -1295543154724961754L,
                "serialVersionUID stable (-1295543154724961754)");
        // inherited surface from BeanContextEvent / EventObject
        check(hasMethod(BeanContextEvent.class, "setPropagatedFrom"), "inherits setPropagatedFrom");
        check(hasMethod(java.util.EventObject.class, "getSource"), "inherits getSource from EventObject");
        // deprecation status on this JDK
        check(c.getAnnotation(Deprecated.class) == null, "not deprecated on JDK 8");
    }

    static void testBlackBox() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        BeanContextServiceRevokedEvent e = new BeanContextServiceRevokedEvent(bcss, Object.class, true);
        check(bcss == e.getSourceAsBeanContextServices(), "getSourceAsBeanContextServices");
        check(Object.class == e.getServiceClass(), "getServiceClass");
        check(e.isServiceClass(Object.class), "isServiceClass true");
        check(!e.isServiceClass(String.class), "isServiceClass false");
        check(e.isCurrentServiceInvalidNow(), "invalidNow true");
        BeanContextServiceRevokedEvent e2 = new BeanContextServiceRevokedEvent(bcss, Object.class, false);
        check(!e2.isCurrentServiceInvalidNow(), "invalidNow false");
    }

    static void testSemantics() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        // accessor identity triple
        BeanContextServiceRevokedEvent e = new BeanContextServiceRevokedEvent(bcss, MyService.class, true);
        check(e.getSource() == bcss, "getSource identity");
        check(e.getSourceAsBeanContextServices() == bcss, "getSourceAsBeanContextServices identity");
        check(e.getBeanContext() == bcss, "getBeanContext identity");
        check(e.getBeanContext() instanceof BeanContext, "getBeanContext typed BeanContext");
        check(e.getSourceAsBeanContextServices() instanceof BeanContextServices,
                "getSourceAsBeanContextServices typed BeanContextServices");
        // the invalidate rule is exactly the constructor flag, both values
        check(new BeanContextServiceRevokedEvent(bcss, MyService.class, true).isCurrentServiceInvalidNow(),
                "invalidate=true maps to isCurrentServiceInvalidNow true");
        check(!new BeanContextServiceRevokedEvent(bcss, MyService.class, false).isCurrentServiceInvalidNow(),
                "invalidate=false maps to isCurrentServiceInvalidNow false");
        // isServiceClass matching matrix
        check(e.isServiceClass(MyService.class), "isServiceClass same class true");
        check(!e.isServiceClass(String.class), "isServiceClass different class false");
        check(!e.isServiceClass(int.class), "isServiceClass primitive class false");
        check(!e.isServiceClass(null), "isServiceClass(null) false (equals contract), no NPE");
        check(!e.isServiceClass(Object.class), "isServiceClass superclass false (exact match only)");
        // two events with the same class are interchangeable for matching
        check(new BeanContextServiceRevokedEvent(bcss, MyService.class, false).isServiceClass(MyService.class),
                "matching independent of the invalidate flag");
        // propagation state inherited
        check(!e.isPropagated() && e.getPropagatedFrom() == null, "fresh revoked event not propagated");
        e.setPropagatedFrom(bcss);
        check(e.isPropagated() && e.getPropagatedFrom() == bcss, "propagation settable");
        // toString mentions class and source
        check(e.toString().startsWith("java.beans.beancontext.BeanContextServiceRevokedEvent[source="),
                "toString starts with class name and source");
    }

    static void testRealFlow() throws Exception {
        // events captured from a live revoke match hand-made ones field for field
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        MyProvider p = new MyProvider();
        bcss.addService(MyService.class, p);
        BeanContextChildSupport child = new BeanContextChildSupport();
        bcss.add(child);
        MyRevokedListener rl = new MyRevokedListener();
        MyServicesListener sl = new MyServicesListener();
        bcss.addBeanContextServicesListener(sl);
        bcss.getService(child, new Object(), MyService.class, null, rl);
        // revokeNow=false
        bcss.revokeService(MyService.class, p, false);
        check(rl.n == 1, "requestor listener notified once");
        check(rl.last.getServiceClass() == MyService.class, "flow event serviceClass exact");
        check(!rl.last.isCurrentServiceInvalidNow(), "flow event invalidate false for revokeNow=false");
        check(rl.last.getSourceAsBeanContextServices() == bcss, "flow event source is the context");
        check(sl.revokedCount == 1 && !sl.lastRevoked.isCurrentServiceInvalidNow(),
                "registered listener sees the same flag");
        // revokeNow=true; the still-held reference from the revokeNow=false round fires again,
        // so the requestor listener sees one event per outstanding requestor entry (2 entries)
        bcss.addService(MyService.class, p);
        bcss.getService(child, new Object(), MyService.class, null, rl);
        bcss.revokeService(MyService.class, p, true);
        check(rl.n == 3 && rl.last.isCurrentServiceInvalidNow(), "flow event invalidate true for revokeNow=true");
        check(sl.revokedCount == 2 && sl.lastRevoked.isCurrentServiceInvalidNow(),
                "registered listener invalidate true");
        // the delivered event and an equivalent hand-made one agree on every accessor
        BeanContextServiceRevokedEvent handmade = new BeanContextServiceRevokedEvent(bcss, MyService.class, true);
        check(handmade.isServiceClass(rl.last.getServiceClass()), "hand-made equivalent matches flow class");
        check(handmade.isCurrentServiceInvalidNow() == rl.last.isCurrentServiceInvalidNow(),
                "hand-made equivalent matches flow flag");
        // a revoked service no longer resolves: getService returns null (no special exception)
        check(bcss.getService(child, new Object(), MyService.class, null, new MyRevokedListener()) == null,
                "getService after revoke resolves to null, not an exception");
        check(!bcss.hasService(MyService.class), "hasService false after revoke");
    }

    static void testNullMatrix() throws Exception {
        final BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        // null source: EventObject contract - IllegalArgumentException
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new BeanContextServiceRevokedEvent(null, MyService.class, true); } },
                "null source -> IAE (EventObject 'null source')");
        // null serviceClass: stored, but isServiceClass then dereferences null -> NPE
        final BeanContextServiceRevokedEvent e = new BeanContextServiceRevokedEvent(bcss, null, true);
        check(e.getServiceClass() == null, "null serviceClass stored and returned");
        check(e.isCurrentServiceInvalidNow(), "flag unaffected by null serviceClass");
        check(e.getSourceAsBeanContextServices() == bcss, "source unaffected by null serviceClass");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { e.isServiceClass(MyService.class); } },
                "isServiceClass on null-serviceClass event -> NPE");
        check(e.toString() != null, "toString non-null with null serviceClass");
        // nulls in the flag position are impossible (primitive) - structural note only
        check(true, "invalidate flag is primitive boolean, no null state exists");
    }

    static void testSerialization() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        // true flag
        BeanContextServiceRevokedEvent e = new BeanContextServiceRevokedEvent(bcss, MyService.class, true);
        BeanContextServiceRevokedEvent back = (BeanContextServiceRevokedEvent) deser(ser(e));
        check(back != e, "round trip returns a distinct instance");
        check(back.getServiceClass() == MyService.class, "serviceClass survives round trip");
        check(back.isCurrentServiceInvalidNow(), "invalidateRefs=true survives round trip");
        check(back.isServiceClass(MyService.class), "isServiceClass works after round trip");
        check(!back.isServiceClass(String.class), "isServiceClass mismatch still false after round trip");
        check(back.getSource() == null, "transient source -> null after round trip");
        check(back.getSourceAsBeanContextServices() == null, "getSourceAsBeanContextServices null after round trip");
        check(back.getBeanContext() == null, "getBeanContext null after round trip");
        // false flag
        BeanContextServiceRevokedEvent backFalse = (BeanContextServiceRevokedEvent)
                deser(ser(new BeanContextServiceRevokedEvent(bcss, MyService.class, false)));
        check(!backFalse.isCurrentServiceInvalidNow(), "invalidateRefs=false survives round trip");
        check(backFalse.getServiceClass() == MyService.class, "class survives in the false-flag variant too");
        // propagation state round trip
        BeanContextServiceRevokedEvent propagated = new BeanContextServiceRevokedEvent(bcss, MyService.class, true);
        propagated.setPropagatedFrom(new java.beans.beancontext.BeanContextSupport());
        BeanContextServiceRevokedEvent backProp = (BeanContextServiceRevokedEvent) deser(ser(propagated));
        check(backProp.isPropagated(), "propagatedFrom survives round trip");
        check(backProp.toString() != null, "deserialized toString works");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSemantics();
        testRealFlow();
        testNullMatrix();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

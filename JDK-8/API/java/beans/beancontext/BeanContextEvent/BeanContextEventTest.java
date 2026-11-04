/*
 * @test
 * @summary java.beans.beancontext.BeanContextEvent conformance: white-box structure + black-box behavior.
 *          Abstract EventObject subclass with a single protected (BeanContext) ctor;
 *          declared method surface incl. synchronized propagation accessors;
 *          getBeanContext/getSource identity; propagatedFrom default null, isPropagated
 *          is exactly (propagatedFrom != null) -- setting it to the source itself still
 *          counts as propagated; setPropagatedFrom(null) allowed; abstract instantiation
 *          blocked; protected propagatedFrom field; null source -> IAE; concrete subevent
 *          hierarchy (membership/service events assignable); serialization round-trip via
 *          a concrete subclass (transient source, surviving propagatedFrom).
 * @run main BeanContextEventTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextEvent;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BeanContextEventTest {
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

    static class E extends BeanContextEvent { E(BeanContext bc) { super(bc); } }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }
    static <T> T deser(byte[] b) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
        @SuppressWarnings("unchecked") T t = (T) ois.readObject();
        ois.close();
        return t;
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextEvent");
        check(!c.isInterface(), "not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(java.util.EventObject.class.isAssignableFrom(c), "extends EventObject");
        check("java.util.EventObject".equals(c.getSuperclass().getName()), "superclass java.util.EventObject");
        check(hasMethod(c, "getBeanContext"), "declares getBeanContext");
        check(hasMethod(c, "getPropagatedFrom"), "declares getPropagatedFrom");
        check(hasMethod(c, "isPropagated"), "declares isPropagated");
        check(hasMethod(c, "setPropagatedFrom"), "declares setPropagatedFrom");
        // single protected ctor taking a BeanContext
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single declared ctor");
        check(Modifier.isProtected(ctors[0].getModifiers()), "ctor protected");
        Class<?>[] params = ctors[0].getParameterTypes();
        check(params.length == 1 && params[0].getName().equals("java.beans.beancontext.BeanContext"),
              "ctor takes a BeanContext source");
        // propagation accessors are synchronized
        boolean syncGet = false, syncSet = false, syncIs = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getPropagatedFrom") && Modifier.isSynchronized(m.getModifiers())) syncGet = true;
            if (m.getName().equals("setPropagatedFrom") && Modifier.isSynchronized(m.getModifiers())) syncSet = true;
            if (m.getName().equals("isPropagated") && Modifier.isSynchronized(m.getModifiers())) syncIs = true;
        }
        check(syncGet && syncSet && syncIs, "propagation accessors are synchronized");
        // protected propagatedFrom field of type BeanContext
        Field f = c.getDeclaredField("propagatedFrom");
        check(f.getType() == BeanContext.class, "propagatedFrom field type BeanContext");
        check(Modifier.isProtected(f.getModifiers()), "propagatedFrom protected");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 7267998073569045052L, "serialVersionUID stable");
        // abstract instantiation blocked
        checkThrows(java.lang.InstantiationException.class, new ThrowingRunnable() {
            public void run() throws Exception { BeanContextEvent.class.newInstance(); } },
            "abstract class cannot be instantiated");
    }

    static void testBlackBox() throws Exception {
        BeanContext bc = new BeanContextSupport();
        E e = new E(bc);
        check(bc == e.getBeanContext(), "getBeanContext");
        check(e.getSource() == bc, "getSource identity");
        check(e.getPropagatedFrom() == null, "propagatedFrom null default");
        check(!e.isPropagated(), "not propagated default");
        BeanContext bc2 = new BeanContextSupport();
        e.setPropagatedFrom(bc2);
        check(bc2 == e.getPropagatedFrom(), "propagatedFrom set");
        check(e.isPropagated(), "isPropagated true");
    }

    static void testPropagationSemantics() throws Exception {
        BeanContext bc = new BeanContextSupport();
        E e = new E(bc);
        // isPropagated is exactly (propagatedFrom != null): even the source itself counts
        e.setPropagatedFrom(bc);
        check(e.getPropagatedFrom() == bc, "propagatedFrom set to the source itself");
        check(e.isPropagated(), "source-set propagatedFrom still counts as propagated");
        // setPropagatedFrom(null) is allowed and clears propagation
        e.setPropagatedFrom(null);
        check(e.getPropagatedFrom() == null, "propagatedFrom reset to null");
        check(!e.isPropagated(), "cleared propagation");
        // repeated sets are last-wins
        BeanContext a = new BeanContextSupport();
        BeanContext b = new BeanContextSupport();
        e.setPropagatedFrom(a);
        e.setPropagatedFrom(b);
        check(e.getPropagatedFrom() == b && e.isPropagated(), "last set wins");
        // getBeanContext always reflects the constructor source, independent of propagation
        check(e.getBeanContext() == bc, "getBeanContext unaffected by propagation");
        // the event stays usable as a plain EventObject
        check(e.getSource() == bc, "getSource unaffected by propagation");
        // toString is inherited and does not throw
        check(e.toString() != null, "toString works");
    }

    static void testNullSource() throws Exception {
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new E(null); } },
            "null source -> IAE (EventObject contract)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new BeanContextMembershipEvent(null, new Object[0]); } },
            "null source on a concrete subclass -> IAE");
    }

    static void testSubeventHierarchy() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextEvent");
        check(c.isAssignableFrom(BeanContextMembershipEvent.class), "membership event is a BeanContextEvent");
        check(c.isAssignableFrom(BeanContextServiceAvailableEvent.class), "service available event is a BeanContextEvent");
        check(c.isAssignableFrom(BeanContextServiceRevokedEvent.class), "service revoked event is a BeanContextEvent");
        // a membership event carries the propagation machinery of the base class
        BeanContextSupport src = new BeanContextSupport();
        BeanContextMembershipEvent me = new BeanContextMembershipEvent(src, new Object[]{"c"});
        BeanContext other = new BeanContextSupport();
        me.setPropagatedFrom(other);
        check(me.isPropagated() && me.getPropagatedFrom() == other, "subevent propagation inherited");
        // base-typed reference sees the base surface
        BeanContextEvent typed = me;
        check(typed.getBeanContext() == src, "base-typed getBeanContext");
        check(typed.getSource() == src, "base-typed getSource");
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        BeanContextServiceAvailableEvent sae = new BeanContextServiceAvailableEvent(bcss, Object.class);
        BeanContextEvent typedSae = sae;
        check(typedSae.getBeanContext() == bcss, "service event getBeanContext");
        check(!typedSae.isPropagated(), "service event default not propagated");
    }

    static void testSerialization() throws Exception {
        BeanContextSupport src = new BeanContextSupport();
        BeanContext from = new BeanContextSupport();
        BeanContextMembershipEvent e = new BeanContextMembershipEvent(src, new Object[]{"s"});
        e.setPropagatedFrom(from);
        BeanContextMembershipEvent back = deser(ser(e));
        check(back.getSource() == null, "source transient: null after round-trip");
        check(back.getBeanContext() == null, "getBeanContext null after round-trip");
        check(back.isPropagated(), "propagation state survives");
        check(back.getPropagatedFrom() != null && back.getPropagatedFrom() != from,
              "propagatedFrom survives as a deserialized copy");
        check(back.contains("s"), "subclass payload survives");
        // unpropagated event round-trips as unpropagated
        BeanContextMembershipEvent plain = deser(
                ser(new BeanContextMembershipEvent(src, new Object[]{"p"})));
        check(!plain.isPropagated() && plain.getPropagatedFrom() == null, "unpropagated stays unpropagated");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBlackBox();
        testPropagationSemantics();
        testNullSource();
        testSubeventHierarchy();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

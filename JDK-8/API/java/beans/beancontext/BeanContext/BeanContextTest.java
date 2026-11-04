/*
 * @test
 * @summary java.beans.beancontext.BeanContext conformance: white-box structure + black-box behavior.
 *          Exact interface closure (extends BeanContextChild, Collection, DesignMode,
 *          Visibility -- and nothing else), exact declared-method inventory (5 methods),
 *          globalHierarchyLock contract (static final Object, stable identity), declared
 *          throws clauses, and behavioral semantics exercised polymorphically through the
 *          interface type via BeanContextSupport: Collection surface, DesignMode surface,
 *          Visibility surface, membership registration/delivery, instantiateChild, and
 *          resource delegation (null/non-member NPE/IAE, member lookup succeeds).
 * @run main BeanContextTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeanContextTest {
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
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    public static class Kid extends BeanContextSupport {}

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContext");
        check(c.isInterface(), "is an interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        // exact direct super-interface closure
        Set<String> direct = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) direct.add(i.getName());
        check(direct.equals(new HashSet<String>(Arrays.asList(
                "java.beans.beancontext.BeanContextChild",
                "java.util.Collection",
                "java.beans.DesignMode",
                "java.beans.Visibility"))), "exact super-interfaces: " + direct);
        check(Class.forName("java.beans.beancontext.BeanContextChild").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextChild");
        check(Class.forName("java.util.Collection").isAssignableFrom(c), "implements/extends java.util.Collection");
        check(Class.forName("java.beans.DesignMode").isAssignableFrom(c), "implements/extends java.beans.DesignMode");
        check(Class.forName("java.beans.Visibility").isAssignableFrom(c), "implements/extends java.beans.Visibility");
        check(!BeanContextServices.class.isAssignableFrom(c), "BeanContext does not extend BeanContextServices");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface itself is not Serializable");
        check(!java.util.EventListener.class.isAssignableFrom(c), "not an EventListener");
        // exact declared method inventory (5 bean-context-specific methods)
        List<String> names = new ArrayList<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        java.util.Collections.sort(names);
        check(names.size() == 5, "declares exactly 5 methods: " + names);
        check(names.equals(Arrays.asList("addBeanContextMembershipListener", "getResource",
                "getResourceAsStream", "instantiateChild", "removeBeanContextMembershipListener")),
              "exact declared method names: " + names);
        check(hasMethod(c, "addBeanContextMembershipListener"), "declares addBeanContextMembershipListener");
        check(hasMethod(c, "getResource"), "declares getResource");
        check(hasMethod(c, "getResourceAsStream"), "declares getResourceAsStream");
        // throws clauses
        boolean ioe = false, cnfe = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals("instantiateChild")) continue;
            for (Class<?> ex : m.getExceptionTypes()) {
                if (ex == java.io.IOException.class) ioe = true;
                if (ex == ClassNotFoundException.class) cnfe = true;
            }
        }
        check(ioe && cnfe, "instantiateChild declares IOException + ClassNotFoundException");
        boolean iae1 = false, iae2 = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getResourceAsStream") || m.getName().equals("getResource")) {
                for (Class<?> ex : m.getExceptionTypes()) if (ex == IllegalArgumentException.class) {
                    if (m.getName().equals("getResourceAsStream")) iae1 = true; else iae2 = true;
                }
            }
        }
        check(iae1 && iae2, "resource methods declare IllegalArgumentException");
    }

    static void testGlobalLock() throws Exception {
        check(BeanContext.globalHierarchyLock != null, "globalHierarchyLock non-null");
        java.lang.reflect.Field f = BeanContext.class.getField("globalHierarchyLock");
        int m = f.getModifiers();
        check(java.lang.reflect.Modifier.isStatic(m) && java.lang.reflect.Modifier.isFinal(m)
              && java.lang.reflect.Modifier.isPublic(m), "globalHierarchyLock public static final");
        check(f.getType() == Object.class, "globalHierarchyLock is a plain Object");
        check(f.get(null) == BeanContext.globalHierarchyLock, "same lock instance on every access");
        // the lock is usable as a monitor
        synchronized (BeanContext.globalHierarchyLock) {
            check(true, "globalHierarchyLock usable as monitor");
        }
    }

    static void testBehaviorThroughInterface() throws Exception {
        BeanContext bc = new BeanContextSupport();
        // Collection surface through the interface reference
        check(bc.isEmpty() && bc.size() == 0, "Collection surface: empty/size");
        check(bc.add("m1"), "Collection surface: add");
        check(bc.contains("m1") && bc.size() == 1, "Collection surface: contains/size");
        Set<Object> seen = new HashSet<Object>();
        for (Object o : bc) seen.add(o);
        check(seen.equals(new HashSet<Object>(Arrays.asList("m1"))), "Collection surface: for-each iteration");
        check(bc.toArray().length == 1, "Collection surface: toArray");
        check(bc.containsAll(Arrays.asList("m1")), "Collection surface: containsAll");
        check(bc.remove("m1") && bc.isEmpty(), "Collection surface: remove");
        // DesignMode surface
        check(!bc.isDesignTime(), "DesignMode surface: default runtime");
        bc.setDesignTime(true);
        check(bc.isDesignTime(), "DesignMode surface: setDesignTime");
        bc.setDesignTime(false);
        check(!bc.isDesignTime(), "DesignMode surface: back to runtime");
        // Visibility surface
        check(!bc.needsGui() && !bc.avoidingGui(), "Visibility surface: defaults");
        bc.okToUseGui();
        check(!bc.avoidingGui(), "Visibility surface: okToUseGui");
        bc.dontUseGui();
        check(!bc.needsGui(), "Visibility surface: dontUseGui (empty context)");
        bc.okToUseGui();
        // BeanContextChild surface: a context is itself nestable
        BeanContextSupport outer = new BeanContextSupport();
        BeanContextSupport inner = new BeanContextSupport();
        outer.add(inner);
        check(inner.getBeanContext() == outer, "BeanContextChild surface: nested context link");
        inner.setBeanContext(null);
        check(inner.getBeanContext() == null && outer.isEmpty(), "BeanContextChild surface: detach honored");
        // membership listener contract through the interface
        final int[] n = new int[1];
        BeanContextMembershipListener l = new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) { n[0]++; }
            public void childrenRemoved(BeanContextMembershipEvent e) {}
        };
        bc.addBeanContextMembershipListener(l);
        bc.removeBeanContextMembershipListener(l);
        check(n[0] == 0, "membership listener add/remove no fire");
        bc.addBeanContextMembershipListener(l);
        bc.add("kid");
        check(n[0] == 1, "childrenAdded delivered through interface");
        // instantiateChild
        Object o = bc.instantiateChild("java.lang.String");
        check(o instanceof String, "instantiateChild");
        check(bc.contains(o), "instantiateChild nests the bean");
    }

    static void testResources() throws Exception {
        final BeanContext bc = new BeanContextSupport();
        final BeanContextChild member = new BeanContextChildSupport();
        bc.add(member);
        final BeanContextChild nonMember = new BeanContextChildSupport();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.getResourceAsStream(null, null); } }, "getResourceAsStream(null, null) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.getResourceAsStream("x", null); } }, "getResourceAsStream(name, null) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.getResource("x", null); } }, "getResource(name, null child) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.getResource(null, member); } }, "getResource(null name) -> NPE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bc.getResourceAsStream("x", nonMember); } }, "getResourceAsStream(non-member) -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bc.getResource("x", nonMember); } }, "getResource(non-member) -> IAE");
        check(bc.getResourceAsStream("java/lang/String.class", member) != null, "member getResourceAsStream delegates to classloader");
        check(bc.getResource("java/lang/String.class", member) != null, "member getResource delegates to classloader");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testGlobalLock();
        testBehaviorThroughInterface();
        testResources();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

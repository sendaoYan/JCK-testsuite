/*
 * @test
 * @summary java.beans.beancontext.BeanContextChild conformance: white-box structure + black-box behavior.
 *          Exact declared-method inventory (6 methods, no super-interfaces), setBeanContext
 *          throws PropertyVetoException, and semantics exercised through
 *          BeanContextChildSupport: beanContext property default/get/set/detach,
 *          veto path leaves the property unchanged, exact propertyChange events
 *          (name/old/new/source), named-listener selectivity, duplicate registration
 *          fires twice while a single remove halves delivery, null-name and
 *          null-listener registrations are silent no-ops, and a delegation wrapper
 *          implementing the interface forwards the whole surface.
 * @run main BeanContextChildTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeanContextChildTest {
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

    /** Delegation wrapper: implements the interface by forwarding to a support object. */
    static class DelegatingChild implements BeanContextChild {
        final BeanContextChildSupport delegate = new BeanContextChildSupport();
        public void setBeanContext(BeanContext bc) throws PropertyVetoException { delegate.setBeanContext(bc); }
        public BeanContext getBeanContext() { return delegate.getBeanContext(); }
        public void addPropertyChangeListener(String name, PropertyChangeListener l) { delegate.addPropertyChangeListener(name, l); }
        public void removePropertyChangeListener(String name, PropertyChangeListener l) { delegate.removePropertyChangeListener(name, l); }
        public void addVetoableChangeListener(String name, VetoableChangeListener l) { delegate.addVetoableChangeListener(name, l); }
        public void removeVetoableChangeListener(String name, VetoableChangeListener l) { delegate.removeVetoableChangeListener(name, l); }
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChild");
        check(c.isInterface(), "is an interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getInterfaces().length == 0, "no direct super-interfaces");
        // exact method inventory
        List<String> names = new ArrayList<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        java.util.Collections.sort(names);
        check(names.size() == 6, "declares exactly 6 methods: " + names);
        check(names.equals(Arrays.asList("addPropertyChangeListener", "addVetoableChangeListener",
                "getBeanContext", "removePropertyChangeListener", "removeVetoableChangeListener",
                "setBeanContext")), "exact declared method names: " + names);
        check(hasMethod(c, "addPropertyChangeListener"), "declares addPropertyChangeListener");
        check(hasMethod(c, "addVetoableChangeListener"), "declares addVetoableChangeListener");
        check(hasMethod(c, "getBeanContext"), "declares getBeanContext");
        // setBeanContext declares PropertyVetoException
        boolean vetoDeclared = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals("setBeanContext")) continue;
            for (Class<?> ex : m.getExceptionTypes()) if (ex == PropertyVetoException.class) vetoDeclared = true;
        }
        check(vetoDeclared, "setBeanContext declares throws PropertyVetoException");
        // parameter signatures
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("addPropertyChangeListener")
                    || m.getName().equals("removePropertyChangeListener")) {
                Class<?>[] p = m.getParameterTypes();
                check(p.length == 2 && p[0] == String.class && p[1] == PropertyChangeListener.class,
                      m.getName() + "(String, PropertyChangeListener)");
            }
            if (m.getName().equals("addVetoableChangeListener")
                    || m.getName().equals("removeVetoableChangeListener")) {
                Class<?>[] p = m.getParameterTypes();
                check(p.length == 2 && p[0] == String.class && p[1] == VetoableChangeListener.class,
                      m.getName() + "(String, VetoableChangeListener)");
            }
        }
        // implementors
        check(BeanContextChildSupport.class.isAssignableFrom(BeanContextChildSupport.class)
              && BeanContextChild.class.isAssignableFrom(BeanContextChildSupport.class), "BeanContextChildSupport implements it");
        check(BeanContextChild.class.isAssignableFrom(BeanContext.class), "BeanContext extends it");
        check(BeanContextChild.class.isAssignableFrom(BeanContextSupport.class), "BeanContextSupport implements it");
    }

    static void testPropertySemantics() throws Exception {
        BeanContextChildSupport child = new BeanContextChildSupport();
        check(child.getBeanContext() == null, "beanContext null default");
        BeanContextSupport bc = new BeanContextSupport();
        // exact property event on attach
        final List<PropertyChangeEvent> evs = new ArrayList<PropertyChangeEvent>();
        child.addPropertyChangeListener("beanContext", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evs.add(e); }
        });
        child.setBeanContext(bc);
        check(child.getBeanContext() == bc, "setBeanContext/getBeanContext");
        check(evs.size() == 1, "attach fires one beanContext propertyChange");
        check("beanContext".equals(evs.get(0).getPropertyName()), "event name beanContext");
        check(evs.get(0).getOldValue() == null && evs.get(0).getNewValue() == bc, "event old=null new=bc");
        check(evs.get(0).getSource() == child, "event source is the child (its peer)");
        // same value: no event
        evs.clear();
        child.setBeanContext(bc);
        check(evs.isEmpty(), "setting the same context fires nothing");
        // detach: old=bc new=null
        child.setBeanContext(null);
        check(child.getBeanContext() == null, "setBeanContext(null) allowed");
        check(evs.size() == 1 && evs.get(0).getOldValue() == bc && evs.get(0).getNewValue() == null,
              "detach event old=bc new=null");
        // named listener selectivity
        final int[] other = {0};
        child.addPropertyChangeListener("unrelated", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { other[0]++; }
        });
        evs.clear();
        child.setBeanContext(bc);
        check(other[0] == 0, "listener of another property not notified");
        check(evs.size() == 1, "beanContext listener still notified");
        // veto path
        BeanContextChildSupport vetoed = new BeanContextChildSupport();
        vetoed.addVetoableChangeListener("beanContext", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                if (e.getNewValue() != null) throw new PropertyVetoException("no", e);
            }
        });
        try {
            vetoed.setBeanContext(bc);
            check(false, "veto must propagate");
        } catch (PropertyVetoException pve) {
            check(true, "setBeanContext veto -> PropertyVetoException");
        }
        check(vetoed.getBeanContext() == null, "property unchanged after veto");
        // null-name / null-listener registrations are silent no-ops
        final int[] got = {0};
        PropertyChangeListener counter = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { got[0]++; }
        };
        vetoed.addPropertyChangeListener(null, counter);
        vetoed.addPropertyChangeListener("p", null);
        vetoed.removePropertyChangeListener("p", null);
        vetoed.removePropertyChangeListener("p", counter); // never added for "p"
        vetoed.firePropertyChange("p", "a", "b");
        check(got[0] == 0, "null-name/null-listener registrations deliver nothing");
        // duplicate registration fires twice; single remove halves delivery
        BeanContextChildSupport dup = new BeanContextChildSupport();
        final int[] n = {0};
        PropertyChangeListener once = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { n[0]++; }
        };
        dup.addPropertyChangeListener("p", once);
        dup.addPropertyChangeListener("p", once);
        dup.firePropertyChange("p", "1", "2");
        check(n[0] == 2, "duplicate registration fires twice");
        dup.removePropertyChangeListener("p", once);
        n[0] = 0;
        dup.firePropertyChange("p", "1", "2");
        check(n[0] == 1, "one remove leaves one registration");
        // vetoable listener registration + remove
        BeanContextChildSupport vcs = new BeanContextChildSupport();
        final int[] vcalls = {0};
        VetoableChangeListener vcl = new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { vcalls[0]++; }
        };
        vcs.addVetoableChangeListener("q", vcl);
        vcs.fireVetoableChange("q", "a", "b");
        check(vcalls[0] == 1, "vetoable listener notified");
        vcs.removeVetoableChangeListener("q", vcl);
        vcs.fireVetoableChange("q", "a", "b");
        check(vcalls[0] == 1, "removed vetoable listener not notified");
    }

    static void testDelegationPattern() throws Exception {
        DelegatingChild child = new DelegatingChild();
        check(child.getBeanContext() == null, "delegating child default null");
        BeanContextSupport bc = new BeanContextSupport();
        final int[] n = {0};
        child.addPropertyChangeListener("beanContext", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { n[0]++; }
        });
        child.setBeanContext(bc);
        check(child.getBeanContext() == bc, "delegation forwards setBeanContext");
        check(n[0] == 1, "delegation forwards listener registration");
        child.setBeanContext(null);
        check(child.getBeanContext() == null && n[0] == 2, "delegation forwards detach");
        // typed as the interface everywhere
        BeanContextChild typed = child;
        check(typed.getBeanContext() == null, "interface-typed access works");
        typed.setBeanContext(bc);
        check(typed.getBeanContext() == bc, "interface-typed set/get works");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testPropertySemantics();
        testDelegationPattern();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

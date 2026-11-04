/*
 * @test
 * @summary java.beans.Customizer conformance: exact member set via
 *          reflection (setObject / add / removePropertyChangeListener,
 *          all abstract, void, no defaults), concrete customization flow
 *          with a nested implementation (setObject(null) accepted,
 *          identity recorded, last-set-wins), property-change wiring on
 *          the customizer's own PropertyChangeSupport (event fields,
 *          multi-listener notification, remove semantics, double-add
 *          double-notify, equal old/new suppressed), Introspector /
 *          BeanDescriptor customizer-class integration, and cross-type
 *          assignability (not EventListener / Serializable / unrelated
 *          bean interfaces). Headless: the customizer under test is
 *          deliberately not an AWT Component.
 * @run main CustomizerTest
 */

import java.awt.Component;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.SimpleBeanInfo;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class CustomizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Nested concrete customizer: records setObject, fires edits on its own PCS. */
    public static class BasicCustomizer implements Customizer {
        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        Object target;
        public void setObject(Object o) { target = o; }
        public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
        public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }
        /** Test hook: a customizer fires an edit event like a real GUI panel would. */
        public void fireEdit(String name, Object oldValue, Object newValue) {
            pcs.firePropertyChange(name, oldValue, newValue);
        }
    }

    /** Sample bean whose explicit BeanInfo publishes BasicCustomizer as its customizer class. */
    public static class PlainBean {
        public PlainBean() {}
        public String getName() { return "p"; }
    }
    public static class PlainBeanBeanInfo extends SimpleBeanInfo {
        public BeanDescriptor getBeanDescriptor() { return new BeanDescriptor(PlainBean.class, BasicCustomizer.class); }
    }

    static class RecordingListener implements PropertyChangeListener {
        final List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent e) { events.add(e); }
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Customizer.class;
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(c.getTypeParameters().length == 0, "not generic");
        check(c.getDeclaredAnnotations().length == 0, "no declared annotations");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 3, "exactly 3 declared methods");
        boolean hasSet = false, hasAdd = false, hasRemove = false;
        for (Method m : ms) {
            if (m.getName().equals("setObject")) hasSet = true;
            if (m.getName().equals("addPropertyChangeListener")) hasAdd = true;
            if (m.getName().equals("removePropertyChangeListener")) hasRemove = true;
        }
        check(hasSet && hasAdd && hasRemove, "member set is exactly setObject/add/removePropertyChangeListener");
        Method set = c.getDeclaredMethod("setObject", Object.class);
        check(set.getReturnType() == void.class, "setObject returns void");
        check(set.getParameterTypes().length == 1 && set.getParameterTypes()[0] == Object.class,
                "setObject takes a single Object");
        Method add = c.getDeclaredMethod("addPropertyChangeListener", PropertyChangeListener.class);
        check(add.getReturnType() == void.class, "addPropertyChangeListener returns void");
        check(add.getParameterTypes().length == 1, "addPropertyChangeListener takes one listener");
        Method remove = c.getDeclaredMethod("removePropertyChangeListener", PropertyChangeListener.class);
        check(remove.getReturnType() == void.class, "removePropertyChangeListener returns void");
        for (Method m : ms) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    m.getName() + " is public abstract");
            check(!m.isDefault(), m.getName() + " has no default body");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
            check(m.getDeclaredAnnotations().length == 0, m.getName() + " carries no annotations");
        }
    }

    static void testSetObjectContract() {
        BasicCustomizer cu = new BasicCustomizer();
        check(cu.target == null, "fresh customizer has no target");
        cu.setObject(null);
        check(cu.target == null, "setObject(null) accepted");
        Object bean = new Object();
        cu.setObject(bean);
        check(cu.target == bean, "setObject records target identity");
        Object other = new Object();
        cu.setObject(other);
        check(cu.target == other, "setObject replaces previous target");
        cu.setObject("text");
        check("text".equals(cu.target), "setObject accepts any object type");
        cu.setObject(Integer.valueOf(0));
        check(cu.target instanceof Integer, "setObject accepts wrapper targets");
    }

    static void testPropertyChangeWiring() {
        BasicCustomizer cu = new BasicCustomizer();
        RecordingListener l1 = new RecordingListener();
        RecordingListener l2 = new RecordingListener();
        cu.addPropertyChangeListener(l1);
        check(cu.pcs.getPropertyChangeListeners().length == 1, "one listener registered");
        cu.fireEdit("value", "a", "b");
        check(l1.events.size() == 1, "listener notified once");
        PropertyChangeEvent e = l1.events.get(0);
        check(e.getSource() == cu, "event source is the customizer itself");
        check("value".equals(e.getPropertyName()), "property name propagated");
        check("a".equals(e.getOldValue()) && "b".equals(e.getNewValue()), "old/new values propagated");
        cu.addPropertyChangeListener(l2);
        cu.fireEdit("value", "b", "c");
        check(l1.events.size() == 2 && l2.events.size() == 1, "both listeners notified");
        cu.removePropertyChangeListener(l1);
        cu.fireEdit("value", "c", "d");
        check(l1.events.size() == 2 && l2.events.size() == 2, "removed listener no longer notified");
        cu.removePropertyChangeListener(l1);
        cu.fireEdit("value", "d", "e");
        check(l1.events.size() == 2 && l2.events.size() == 3, "repeated remove is a no-op");
        cu.removePropertyChangeListener(new RecordingListener());
        check(l2.events.size() == 3, "removing a never-added listener is harmless");
        BasicCustomizer cu2 = new BasicCustomizer();
        RecordingListener dbl = new RecordingListener();
        cu2.addPropertyChangeListener(dbl);
        cu2.addPropertyChangeListener(dbl);
        cu2.fireEdit("value", null, "x");
        check(dbl.events.size() == 2, "double-added listener notified twice per edit");
        cu2.removePropertyChangeListener(dbl);
        cu2.fireEdit("value", "x", null);
        check(dbl.events.size() == 3, "single remove drops one of the two registrations");
        cu2.removePropertyChangeListener(dbl);
        cu2.fireEdit("value", null, "y");
        check(dbl.events.size() == 3, "second remove drops the remaining registration");
        check(cu2.pcs.getPropertyChangeListeners().length == 0, "no listeners left after both removes");
        BasicCustomizer cu3 = new BasicCustomizer();
        RecordingListener eq = new RecordingListener();
        cu3.addPropertyChangeListener(eq);
        cu3.fireEdit("value", "same", "same");
        check(eq.events.isEmpty(), "equal old/new edit suppressed by PropertyChangeSupport");
    }

    static void testIntrospectorIntegration() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(PlainBean.class);
        BeanDescriptor bd = bi.getBeanDescriptor();
        check(bd != null, "BeanDescriptor present");
        check(bd.getBeanClass() == PlainBean.class, "descriptor bean class");
        check(bd.getCustomizerClass() == BasicCustomizer.class,
                "explicit BeanInfo publishes the customizer class");
        BeanDescriptor bare = new BeanDescriptor(PlainBean.class);
        check(bare.getCustomizerClass() == null, "BeanDescriptor without customizer arg has null class");
    }

    static void testCrossTypeAssignability() {
        check(!EventListener.class.isAssignableFrom(Customizer.class), "Customizer is not an EventListener");
        check(!Serializable.class.isAssignableFrom(Customizer.class), "Customizer does not extend Serializable");
        check(!Customizer.class.isAssignableFrom(java.beans.DesignMode.class)
                && !java.beans.DesignMode.class.isAssignableFrom(Customizer.class), "unrelated to DesignMode");
        check(!Customizer.class.isAssignableFrom(java.beans.Visibility.class)
                && !java.beans.Visibility.class.isAssignableFrom(Customizer.class), "unrelated to Visibility");
        check(!java.beans.beancontext.BeanContext.class.isAssignableFrom(Customizer.class),
                "unrelated to BeanContext");
        check(!Component.class.isAssignableFrom(BasicCustomizer.class),
                "customizer under test is headless (javadoc expects a GUI component; only mechanics tested)");
    }

    static void testAnonymousImplementation() {
        final Object[] seen = new Object[1];
        Customizer anon = new Customizer() {
            public void setObject(Object o) { seen[0] = o; }
            public void addPropertyChangeListener(PropertyChangeListener l) {}
            public void removePropertyChangeListener(PropertyChangeListener l) {}
        };
        Object b = new Object();
        anon.setObject(b);
        check(seen[0] == b, "anonymous implementation dispatches setObject");
        check(anon instanceof Customizer, "anonymous implementation is a Customizer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBoxStructure();
        testSetObjectContract();
        testPropertyChangeWiring();
        testIntrospectorIntegration();
        testCrossTypeAssignability();
        testAnonymousImplementation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

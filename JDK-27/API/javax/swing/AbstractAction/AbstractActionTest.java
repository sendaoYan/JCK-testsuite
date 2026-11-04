/*
 * @test
 * @summary javax.swing.AbstractAction conformance: constructors, value map, property events, clone, serialization (headless-safe).
 * @run main AbstractActionTest
 */

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class AbstractActionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static class CountingAction extends AbstractAction {
        int fired;
        CountingAction() { super(); }
        CountingAction(String name) { super(name); }
        CountingAction(String name, ImageIcon icon) { super(name, icon); }
        public void actionPerformed(ActionEvent e) { fired++; }
        CountingAction copy() {
            try { return (CountingAction) super.clone(); }
            catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        }
    }

    // ---- existing shape assertions (preserved) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractAction", false, AbstractActionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.Action"), "implements javax.swing.Action");
        check(hasInterface(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "method getValue");
        check(names.contains("putValue"), "method putValue");
        check(names.contains("setEnabled"), "method setEnabled");
        check(names.contains("isEnabled"), "method isEnabled");
        check(names.contains("getKeys"), "method getKeys");
    }

    static void testConstructors() {
        CountingAction a0 = new CountingAction();
        check(a0.getValue(Action.NAME) == null, "no-arg: NAME null");
        check(a0.isEnabled(), "no-arg: enabled");
        CountingAction a1 = new CountingAction("nm");
        check("nm".equals(a1.getValue(Action.NAME)), "name ctor: NAME");
        ImageIcon icon = new ImageIcon(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
        CountingAction a2 = new CountingAction("nm2", icon);
        check("nm2".equals(a2.getValue(Action.NAME)), "name+icon ctor: NAME");
        check(a2.getValue(Action.SMALL_ICON) == icon, "name+icon ctor: SMALL_ICON same instance");
    }

    static void testValueMap() {
        CountingAction a = new CountingAction("x");
        check(a.getValue("anything") == null, "absent -> null");
        a.putValue(Action.MNEMONIC_KEY, Integer.valueOf(65));
        check(Integer.valueOf(65).equals(a.getValue(Action.MNEMONIC_KEY)), "Integer-typed value roundtrip");
        a.putValue("k", "v1");
        check("v1".equals(a.getValue("k")), "custom key put");
        a.putValue("k", "v2");
        check("v2".equals(a.getValue("k")), "overwrite value");
        check(a.getKeys() != null && a.getKeys().length == 3, "getKeys length (Name from ctor + 2 puts)");
        Set<Object> keySet = new HashSet<Object>(Arrays.asList((Object[]) a.getKeys()));
        check(keySet.contains("k"), "getKeys contains custom");
        check(keySet.contains(Action.MNEMONIC_KEY), "getKeys contains MNEMONIC_KEY");
        check(keySet.contains(Action.NAME), "getKeys contains NAME set by constructor");
        CountingAction empty = new CountingAction();
        check(empty.getKeys() == null, "getKeys null when no values");
        // actionPerformed dispatch
        a.actionPerformed(new ActionEvent(a, 1, "cmd"));
        check(a.fired == 1, "actionPerformed dispatched");
    }

    static void testPropertyChangeEvents() {
        CountingAction a = new CountingAction("x");
        check(a.getPropertyChangeListeners().length == 0, "no listeners initially");
        final List<java.beans.PropertyChangeEvent> evts =
                new ArrayList<java.beans.PropertyChangeEvent>();
        java.beans.PropertyChangeListener pcl = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) { evts.add(e); }
        };
        a.addPropertyChangeListener(pcl);
        check(a.getPropertyChangeListeners().length == 1, "listener added");
        a.setEnabled(false);
        check(evts.size() == 1, "setEnabled fires one event");
        check("enabled".equals(evts.get(0).getPropertyName()), "event property name is enabled");
        check(Boolean.FALSE.equals(evts.get(0).getNewValue()), "event newValue false");
        check(Boolean.TRUE.equals(evts.get(0).getOldValue()), "event oldValue true");
        a.setEnabled(false);
        check(evts.size() == 1, "same-value setEnabled fires nothing");
        a.putValue(Action.SHORT_DESCRIPTION, "tip");
        check(evts.size() == 2, "putValue fires");
        check(Action.SHORT_DESCRIPTION.equals(evts.get(1).getPropertyName()), "putValue event name is the key");
        check("tip".equals(evts.get(1).getNewValue()), "putValue event newValue");
        a.removePropertyChangeListener(pcl);
        check(a.getPropertyChangeListeners().length == 0, "listener removed");
        a.setEnabled(true);
        check(evts.size() == 2, "no events after removal");
    }

    static void testClone() {
        CountingAction a = new CountingAction("orig");
        a.putValue("custom", "v");
        a.setEnabled(false);
        CountingAction c = a.copy();
        check("orig".equals(c.getValue(Action.NAME)), "clone keeps NAME");
        check("v".equals(c.getValue("custom")), "clone keeps values");
        check(!c.isEnabled(), "clone keeps enabled state");
        // independence
        a.putValue("custom", "changed");
        check("v".equals(c.getValue("custom")), "clone value map independent of original");
        a.setEnabled(true);
        check(!c.isEnabled(), "clone enabled independent of original");
    }

    static void testSerialization() throws Exception {
        CountingAction a = new CountingAction("ser");
        a.putValue(Action.SHORT_DESCRIPTION, "d");
        a.setEnabled(false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bos);
        oo.writeObject(a);
        oo.close();
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof AbstractAction, "round-trip instanceof AbstractAction");
        AbstractAction ra = (AbstractAction) back;
        check("ser".equals(ra.getValue(Action.NAME)), "round-trip NAME preserved");
        check("d".equals(ra.getValue(Action.SHORT_DESCRIPTION)), "round-trip SHORT_DESCRIPTION preserved");
        check(!ra.isEnabled(), "round-trip enabled preserved");
        ra.setEnabled(true);
        check(ra.isEnabled(), "round-trip object still operable");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testConstructors();
        testValueMap();
        testPropertyChangeEvents();
        testClone();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

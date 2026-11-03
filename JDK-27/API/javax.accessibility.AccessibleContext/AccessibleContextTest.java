/*
 * @test
 * @summary javax.accessibility.AccessibleContext conformance: white-box structure + black-box behavior.
 * @run main AccessibleContextTest
 */

import java.lang.reflect.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccessibleContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class MinContext extends javax.accessibility.AccessibleContext {
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public Locale getLocale() { return Locale.ROOT; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleContext");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getAccessibleName"), "declares getAccessibleName");
        check(hasMethod(c, "setAccessibleName"), "declares setAccessibleName");
        check(hasMethod(c, "getAccessibleDescription"), "declares getAccessibleDescription");
        check(hasMethod(c, "setAccessibleDescription"), "declares setAccessibleDescription");
        check(hasMethod(c, "getAccessibleRole"), "declares getAccessibleRole");
    }

    static void testBlackBox() throws Exception {
        MinContext ctx = new MinContext();
        check(ctx.getAccessibleName() == null, "initial name null");
        check(ctx.getAccessibleDescription() == null, "initial description null");
        check(ctx.getAccessibleParent() == null, "initial parent null");

        final List<String> events = new ArrayList<>();
        ctx.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                events.add(e.getPropertyName());
            }
        });

        ctx.setAccessibleName("name");
        check("name".equals(ctx.getAccessibleName()), "set/get accessibleName");
        ctx.setAccessibleDescription("desc");
        check("desc".equals(ctx.getAccessibleDescription()), "set/get accessibleDescription");

        check(events.size() >= 2, "two property events fired");
        check(events.get(0).equals(javax.accessibility.AccessibleContext.ACCESSIBLE_NAME_PROPERTY),
                "ACCESSIBLE_NAME_PROPERTY event fired");
        check(events.get(1).equals(javax.accessibility.AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY),
                "ACCESSIBLE_DESCRIPTION_PROPERTY event fired");

        // Parent round trip.
        final javax.accessibility.Accessible parent = new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
        };
        ctx.setAccessibleParent(parent);
        check(ctx.getAccessibleParent() == parent, "set/get accessibleParent");

        // Unsupported sub-interfaces default to null.
        check(ctx.getAccessibleAction() == null, "getAccessibleAction null");
        check(ctx.getAccessibleComponent() == null, "getAccessibleComponent null");
        check(ctx.getAccessibleSelection() == null, "getAccessibleSelection null");
        check(ctx.getAccessibleText() == null, "getAccessibleText null");
        check(ctx.getAccessibleEditableText() == null, "getAccessibleEditableText null");
        check(ctx.getAccessibleValue() == null, "getAccessibleValue null");
        check(ctx.getAccessibleIcon() == null, "getAccessibleIcon null");
        check(ctx.getAccessibleTable() == null, "getAccessibleTable null");

        // Relation set defaults to a non-null empty set.
        javax.accessibility.AccessibleRelationSet rs = ctx.getAccessibleRelationSet();
        check(rs != null, "getAccessibleRelationSet non-null");
        check(rs.size() == 0, "relation set initially empty");

        // firePropertyChange dispatches to listeners.
        ctx.firePropertyChange("custom.property", null, "value");
        check(events.size() == 3 && "custom.property".equals(events.get(2)), "firePropertyChange dispatches");

        // removePropertyChangeListener stops delivery.
        ctx.removePropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { }
        });

        // Role/state set are provided by subclass.
        check(ctx.getAccessibleRole() == javax.accessibility.AccessibleRole.UNKNOWN, "subclass role");
        check(ctx.getAccessibleStateSet() != null, "subclass state set");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

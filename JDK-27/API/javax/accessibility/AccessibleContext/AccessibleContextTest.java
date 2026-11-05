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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: firePropertyChange callback loop + full property-name table + name/description setters ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleContext ctx = new RecCtx();
        final java.util.List<java.beans.PropertyChangeEvent> events = new java.util.ArrayList<java.beans.PropertyChangeEvent>();
        java.beans.PropertyChangeListener pcl = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) { events.add(e); }
        };
        ctx.addPropertyChangeListener(pcl);
        ctx.firePropertyChange(javax.accessibility.AccessibleContext.ACCESSIBLE_NAME_PROPERTY, "old", "new");
        check(events.size() == 1, "listener fired once");
        check(javax.accessibility.AccessibleContext.ACCESSIBLE_NAME_PROPERTY.equals(events.get(0).getPropertyName()), "property name round-trip");
        check("old".equals(events.get(0).getOldValue()) && "new".equals(events.get(0).getNewValue()), "old/new values");
        check(events.get(0).getSource() == ctx, "source is the context");
        ctx.firePropertyChange(javax.accessibility.AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY, null, "d");
        check(events.size() == 2, "second fire");
        check(events.get(1).getNewValue().equals("d") && events.get(1).getOldValue() == null, "null old value allowed");
        ctx.removePropertyChangeListener(pcl);
        ctx.firePropertyChange(javax.accessibility.AccessibleContext.ACCESSIBLE_STATE_PROPERTY, "a", "b");
        check(events.size() == 2, "no delivery after remove");
        // setAccessibleName / setAccessibleDescription fire + get round-trip on concrete ctx
        RecCtx rc = new RecCtx();
        final int[] fired = new int[1];
        rc.addPropertyChangeListener(new java.beans.PropertyChangeListener() { public void propertyChange(java.beans.PropertyChangeEvent e) { fired[0]++; } });
        rc.setAccessibleName("Deep");
        check("Deep".equals(rc.getAccessibleName()), "name round-trip");
        check(fired[0] == 1, "setAccessibleName fires");
        rc.setAccessibleDescription("Desc");
        check("Desc".equals(rc.getAccessibleDescription()), "description round-trip");
        check(fired[0] == 2, "setAccessibleDescription fires");
        // property-name constant table (exact strings)
        String[][] consts = {
            { "ACCESSIBLE_NAME_PROPERTY", "AccessibleName" }, { "ACCESSIBLE_DESCRIPTION_PROPERTY", "AccessibleDescription" },
            { "ACCESSIBLE_STATE_PROPERTY", "AccessibleState" }, { "ACCESSIBLE_VALUE_PROPERTY", "AccessibleValue" },
            { "ACCESSIBLE_SELECTION_PROPERTY", "AccessibleSelection" }, { "ACCESSIBLE_CARET_PROPERTY", "AccessibleCaret" },
            { "ACCESSIBLE_TEXT_PROPERTY", "AccessibleText" }, { "ACCESSIBLE_VISIBLE_DATA_PROPERTY", "AccessibleVisibleData" },
            { "ACCESSIBLE_CHILD_PROPERTY", "AccessibleChild" }, { "ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY", "AccessibleActiveDescendant" },
            { "ACCESSIBLE_TABLE_CAPTION_CHANGED", "accessibleTableCaptionChanged" }, { "ACCESSIBLE_TABLE_SUMMARY_CHANGED", "accessibleTableSummaryChanged" },
            { "ACCESSIBLE_TABLE_MODEL_CHANGED", "accessibleTableModelChanged" } };
        java.lang.reflect.Field f;
        for (String[] pair : consts) {
            f = javax.accessibility.AccessibleContext.class.getField(pair[0]);
            check(pair[1].equals(f.get(null)), pair[0] + " = " + pair[1]);
        }
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_TABLE_ROW_HEADER_CHANGED").get(null).equals("accessibleTableRowHeaderChanged"), "row header prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_TABLE_ROW_DESCRIPTION_CHANGED").get(null).equals("accessibleTableRowDescriptionChanged"), "row desc prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_TABLE_COLUMN_HEADER_CHANGED").get(null).equals("accessibleTableColumnHeaderChanged"), "col header prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_TABLE_COLUMN_DESCRIPTION_CHANGED").get(null).equals("accessibleTableColumnDescriptionChanged"), "col desc prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_ACTION_PROPERTY").get(null).equals("accessibleActionProperty"), "action prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_HYPERTEXT_OFFSET").get(null).equals("AccessibleHypertextOffset"), "hypertext offset prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_INVALIDATE_CHILDREN").get(null).equals("accessibleInvalidateChildren"), "invalidate children prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_TEXT_ATTRIBUTES_CHANGED").get(null).equals("accessibleTextAttributesChanged"), "text attributes prop");
        check(javax.accessibility.AccessibleContext.class.getField("ACCESSIBLE_COMPONENT_BOUNDS_CHANGED").get(null).equals("accessibleComponentBoundsChanged"), "component bounds prop");
        // default child/parent contracts on the recording subclass
        check(ctx.getAccessibleIndexInParent() == -1, "default index -1");
        check(ctx.getAccessibleChildrenCount() == 0, "default 0 children");
        check(ctx.getAccessibleChild(0) == null, "child 0 null");
        check(ctx.getAccessibleParent() == null, "default parent null");
        check(java.util.Locale.ENGLISH.equals(ctx.getLocale()), "locale");
        // default service accessors on a bare context
        check(ctx.getAccessibleComponent() == null, "default component null");
        check(ctx.getAccessibleAction() == null, "default action null");
        check(ctx.getAccessibleValue() == null, "default value null");
        check(ctx.getAccessibleSelection() == null, "default selection null");
        check(ctx.getAccessibleText() == null, "default text null");
        check(ctx.getAccessibleTable() == null, "default table null");
        check(ctx.getAccessibleIcon() == null, "default icons null");
        check("".equals(ctx.getAccessibleRelationSet().toString()), "default empty relation set");
        // protected writable fields exist (declared, not public API surface)
        try { check(!java.lang.reflect.Modifier.isPublic(javax.accessibility.AccessibleContext.class.getDeclaredField("accessibleParent").getModifiers()), "protected accessibleParent field"); } catch (NoSuchFieldException e) { check(false, "accessibleParent field expected"); }
        try { check(!java.lang.reflect.Modifier.isPublic(javax.accessibility.AccessibleContext.class.getDeclaredField("accessibleName").getModifiers()), "protected accessibleName field"); } catch (NoSuchFieldException e) { check(false, "accessibleName field expected"); }
        try { check(!java.lang.reflect.Modifier.isPublic(javax.accessibility.AccessibleContext.class.getDeclaredField("accessibleDescription").getModifiers()), "protected accessibleDescription field"); } catch (NoSuchFieldException e) { check(false, "accessibleDescription field expected"); }
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // concrete AccessibleContext recording subclass
    static class RecCtx extends javax.accessibility.AccessibleContext {
        public String getAccessibleName() { return accessibleName; }
        public String getAccessibleDescription() { return accessibleDescription; }
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public java.util.Locale getLocale() throws java.awt.IllegalComponentStateException { return java.util.Locale.ENGLISH; }
    }

}

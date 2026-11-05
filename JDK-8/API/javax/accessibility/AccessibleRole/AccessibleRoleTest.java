/*
 * @test
 * @summary javax.accessibility.AccessibleRole conformance: class reflection.
 * @run main AccessibleRoleTest
 */

public class AccessibleRoleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleRole");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("javax.accessibility.AccessibleBundle"), "superclass javax.accessibility.AccessibleBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full 63-constant table + display strings + equals/hashCode ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleRole[] roles = {
            javax.accessibility.AccessibleRole.ALERT, javax.accessibility.AccessibleRole.COLUMN_HEADER, javax.accessibility.AccessibleRole.CANVAS,
            javax.accessibility.AccessibleRole.COMBO_BOX, javax.accessibility.AccessibleRole.DESKTOP_ICON, javax.accessibility.AccessibleRole.HTML_CONTAINER,
            javax.accessibility.AccessibleRole.INTERNAL_FRAME, javax.accessibility.AccessibleRole.DESKTOP_PANE, javax.accessibility.AccessibleRole.OPTION_PANE,
            javax.accessibility.AccessibleRole.WINDOW, javax.accessibility.AccessibleRole.FRAME, javax.accessibility.AccessibleRole.DIALOG,
            javax.accessibility.AccessibleRole.COLOR_CHOOSER, javax.accessibility.AccessibleRole.DIRECTORY_PANE, javax.accessibility.AccessibleRole.FILE_CHOOSER,
            javax.accessibility.AccessibleRole.FILLER, javax.accessibility.AccessibleRole.HYPERLINK, javax.accessibility.AccessibleRole.ICON,
            javax.accessibility.AccessibleRole.LABEL, javax.accessibility.AccessibleRole.ROOT_PANE, javax.accessibility.AccessibleRole.GLASS_PANE,
            javax.accessibility.AccessibleRole.LAYERED_PANE, javax.accessibility.AccessibleRole.LIST, javax.accessibility.AccessibleRole.LIST_ITEM,
            javax.accessibility.AccessibleRole.MENU_BAR, javax.accessibility.AccessibleRole.POPUP_MENU, javax.accessibility.AccessibleRole.MENU,
            javax.accessibility.AccessibleRole.MENU_ITEM, javax.accessibility.AccessibleRole.SEPARATOR, javax.accessibility.AccessibleRole.PAGE_TAB_LIST,
            javax.accessibility.AccessibleRole.PAGE_TAB, javax.accessibility.AccessibleRole.PANEL, javax.accessibility.AccessibleRole.PROGRESS_BAR,
            javax.accessibility.AccessibleRole.PASSWORD_TEXT, javax.accessibility.AccessibleRole.PUSH_BUTTON, javax.accessibility.AccessibleRole.TOGGLE_BUTTON,
            javax.accessibility.AccessibleRole.CHECK_BOX, javax.accessibility.AccessibleRole.RADIO_BUTTON, javax.accessibility.AccessibleRole.ROW_HEADER,
            javax.accessibility.AccessibleRole.SCROLL_PANE, javax.accessibility.AccessibleRole.SCROLL_BAR, javax.accessibility.AccessibleRole.VIEWPORT,
            javax.accessibility.AccessibleRole.SLIDER, javax.accessibility.AccessibleRole.SPLIT_PANE, javax.accessibility.AccessibleRole.TABLE,
            javax.accessibility.AccessibleRole.TEXT, javax.accessibility.AccessibleRole.TREE, javax.accessibility.AccessibleRole.TOOL_BAR,
            javax.accessibility.AccessibleRole.TOOL_TIP, javax.accessibility.AccessibleRole.AWT_COMPONENT, javax.accessibility.AccessibleRole.SWING_COMPONENT,
            javax.accessibility.AccessibleRole.UNKNOWN, javax.accessibility.AccessibleRole.STATUS_BAR, javax.accessibility.AccessibleRole.DATE_EDITOR,
            javax.accessibility.AccessibleRole.SPIN_BOX, javax.accessibility.AccessibleRole.FONT_CHOOSER, javax.accessibility.AccessibleRole.GROUP_BOX,
            javax.accessibility.AccessibleRole.HEADER, javax.accessibility.AccessibleRole.FOOTER, javax.accessibility.AccessibleRole.PARAGRAPH,
            javax.accessibility.AccessibleRole.RULER, javax.accessibility.AccessibleRole.EDITBAR, javax.accessibility.AccessibleRole.PROGRESS_MONITOR };
        check(roles.length == 63, "63 role constants");
        for (javax.accessibility.AccessibleRole r : roles) check(r != null && r.toDisplayString(java.util.Locale.ENGLISH) != null, "role displayable " + r);
        check("alert".equals(javax.accessibility.AccessibleRole.ALERT.toDisplayString(java.util.Locale.ENGLISH)), "alert");
        check("push button".equals(javax.accessibility.AccessibleRole.PUSH_BUTTON.toDisplayString(java.util.Locale.ENGLISH)), "push button");
        check("push button".equals(javax.accessibility.AccessibleRole.PUSH_BUTTON.toString()), "toString = English display string");
        check("label".equals(javax.accessibility.AccessibleRole.LABEL.toDisplayString(java.util.Locale.ENGLISH)), "label");
        check("bouton".equals(javax.accessibility.AccessibleRole.PUSH_BUTTON.toDisplayString(java.util.Locale.FRENCH)), "French bouton");
        check(javax.accessibility.AccessibleRole.LABEL.equals(javax.accessibility.AccessibleRole.LABEL), "self equals");
        check(!javax.accessibility.AccessibleRole.LABEL.equals(javax.accessibility.AccessibleRole.PUSH_BUTTON), "distinct roles unequal");
        check(javax.accessibility.AccessibleRole.LABEL.hashCode() == javax.accessibility.AccessibleRole.LABEL.hashCode(), "hashCode stable");
        java.lang.reflect.Field kf = javax.accessibility.AccessibleBundle.class.getDeclaredField("key");
        try {
            kf.setAccessible(true);
            check("label".equals(kf.get(javax.accessibility.AccessibleRole.LABEL)), "protected key field = label (JDK-8 open reflection)");
            check(!java.lang.reflect.Modifier.isPublic(kf.getModifiers()), "key field protected");
            check(java.lang.reflect.Modifier.isProtected(kf.getModifiers()), "key protected modifier");
        } catch (RuntimeException e) {
            check(e.getClass().getName().endsWith("InaccessibleObjectException"), "JDK-9+ module java.desktop does not open protected key");
        }
        check(java.lang.reflect.Modifier.isPublic(javax.accessibility.AccessibleBundle.class.getDeclaredConstructors()[0].getModifiers()), "bundle ctor public");
        check(!java.lang.reflect.Modifier.isPublic(javax.accessibility.AccessibleRole.class.getDeclaredConstructors()[0].getModifiers()), "role ctor protected (subclass to define custom roles)");
        check(javax.accessibility.AccessibleRole.class.getSuperclass() == javax.accessibility.AccessibleBundle.class, "extends AccessibleBundle");
        // custom roles via protected-key ctor subclass; unknown key falls back to the key itself
        javax.accessibility.AccessibleRole custom = new javax.accessibility.AccessibleRole("deep-role") {};
        check("deep-role".equals(custom.toDisplayString(java.util.Locale.ENGLISH)), "custom role key passthrough (no bundle entry)");
        boolean serOK = true;
        try { serRoundTrip(javax.accessibility.AccessibleRole.LABEL); } catch (Exception e) { serOK = false; }
        check(!serOK, "Role is not serializable");
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

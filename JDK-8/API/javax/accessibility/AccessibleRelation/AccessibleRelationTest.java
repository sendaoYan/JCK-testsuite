/*
 * @test
 * @summary javax.accessibility.AccessibleRelation conformance: class reflection.
 * @run main AccessibleRelationTest
 */

import java.util.Locale;

public class AccessibleRelationTest {
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
        Locale.setDefault(Locale.ENGLISH);
        Class<?> c = Class.forName("javax.accessibility.AccessibleRelation");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("javax.accessibility.AccessibleBundle"), "superclass javax.accessibility.AccessibleBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getKey"), "method getKey");
        check(hasMethod(c, "getTarget"), "method getTarget");
        check(hasMethod(c, "setTarget"), "method setTarget");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: 12-key table + target set/replace + display string ----
    static void testContractDeep() throws Exception {
        check("childNodeOf".equals(javax.accessibility.AccessibleRelation.CHILD_NODE_OF), "childNodeOf");
        String[] keys = { "controlledBy", "controllerFor", "embeddedBy", "embeds", "flowsFrom", "flowsTo", "labeledBy", "labelFor", "memberOf", "parentWindowOf", "subwindowOf" };
        for (int i = 0; i < keys.length; i++) check(keys[i].equals(getRelKey(i)), "String constant " + keys[i]);
        check(javax.accessibility.AccessibleRelation.class.getField("CHILD_NODE_OF").get(null).equals("childNodeOf"), "CHILD_NODE_OF constant type String");
        javax.accessibility.AccessibleRelation r = new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.CHILD_NODE_OF, new Object[] { "k1", "k2" });
        check(javax.accessibility.AccessibleRelation.CHILD_NODE_OF.equals(r.getKey()), "getKey");
        Object[] tg = (Object[]) r.getTarget();
        check(tg.length == 2 && "k1".equals(tg[0]) && "k2".equals(tg[1]), "varargs targets");
        check("child node of".equals(r.toString()), "toString is display string");
        r.setTarget("k3");
        check(((Object[]) r.getTarget()).length == 1 && "k3".equals(((Object[]) r.getTarget())[0]), "setTarget replaces");
        javax.accessibility.AccessibleRelation single = new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.LABEL_FOR, "one");
        check(((Object[]) single.getTarget()).length == 1 && "one".equals(((Object[]) single.getTarget())[0]), "single-object target");
        javax.accessibility.AccessibleRelation noTarget = new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.LABEL_FOR);
        check(((Object[]) noTarget.getTarget()).length == 0, "no-target ctor gives empty array");
        check(javax.accessibility.AccessibleRelation.class.getField("CHILD_NODE_OF").getType() == String.class, "constants are String-typed");
    }
    static String getRelKey(int i) throws Exception {
        String[] names = { "CONTROLLED_BY", "CONTROLLER_FOR", "EMBEDDED_BY", "EMBEDS", "FLOWS_FROM", "FLOWS_TO", "LABELED_BY", "LABEL_FOR", "MEMBER_OF", "PARENT_WINDOW_OF", "SUBWINDOW_OF" };
        return (String) javax.accessibility.AccessibleRelation.class.getField(names[i]).get(null);
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

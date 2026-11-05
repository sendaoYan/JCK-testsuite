/*
 * @test
 * @summary javax.accessibility.AccessibleRelation conformance: white-box structure + black-box behavior.
 * @run main AccessibleRelationTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleRelationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleRelation");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == javax.accessibility.AccessibleBundle.class, "extends AccessibleBundle");
        check(hasMethod(c, "getKey"), "declares getKey");
        check(hasMethod(c, "getTarget"), "declares getTarget");
        check(hasMethod(c, "setTarget"), "declares setTarget");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleRelation r = new javax.accessibility.AccessibleRelation("labelFor");
        check("labelFor".equals(r.getKey()), "ctor key");
        check(r.getTarget().length == 0, "key-only ctor empty target");

        r.setTarget("target");
        check(r.getTarget().length == 1, "setTarget(Object) length 1");
        check("target".equals(r.getTarget()[0]), "setTarget(Object) value");

        r.setTarget(new Object[]{"a", "b"});
        Object[] t = r.getTarget();
        check(t.length == 2, "setTarget(Object[]) length 2");
        check("a".equals(t[0]) && "b".equals(t[1]), "setTarget(Object[]) values");

        // getTarget returns a defensive copy.
        t[0] = "MUTATED";
        check("a".equals(r.getTarget()[0]), "getTarget defensive copy");

        javax.accessibility.AccessibleRelation r2 = new javax.accessibility.AccessibleRelation("x", new Object[]{"y"});
        check("x".equals(r2.getKey()), "ctor key+array key");
        check(r2.getTarget().length == 1 && "y".equals(r2.getTarget()[0]), "ctor key+array target");

        javax.accessibility.AccessibleRelation r3 = new javax.accessibility.AccessibleRelation("z", "single");
        check(r3.getTarget().length == 1 && "single".equals(r3.getTarget()[0]), "ctor key+object target");

        // Localization of the relation key (from AccessibleBundle).
        javax.accessibility.AccessibleRelation labelFor = new javax.accessibility.AccessibleRelation(
                javax.accessibility.AccessibleRelation.LABEL_FOR);
        check("label for".equals(labelFor.toDisplayString(Locale.US)), "LABEL_FOR localized");

        // Constants hold the expected keys / property names.
        check("labelFor".equals(javax.accessibility.AccessibleRelation.LABEL_FOR), "LABEL_FOR constant");
        check("labelForProperty".equals(javax.accessibility.AccessibleRelation.LABEL_FOR_PROPERTY),
                "LABEL_FOR_PROPERTY constant");
        check("memberOf".equals(javax.accessibility.AccessibleRelation.MEMBER_OF), "MEMBER_OF constant");
        check("controllerFor".equals(javax.accessibility.AccessibleRelation.CONTROLLER_FOR), "CONTROLLER_FOR constant");
        check("controlledBy".equals(javax.accessibility.AccessibleRelation.CONTROLLED_BY), "CONTROLLED_BY constant");
        check("flowsTo".equals(javax.accessibility.AccessibleRelation.FLOWS_TO), "FLOWS_TO constant");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

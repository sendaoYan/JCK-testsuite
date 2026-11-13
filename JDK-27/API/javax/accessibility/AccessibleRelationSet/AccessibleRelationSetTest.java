/*
 * @test
 * @summary javax.accessibility.AccessibleRelationSet conformance: white-box structure + black-box behavior.
 * @run main AccessibleRelationSetTest
 */

import java.lang.reflect.*;

public class AccessibleRelationSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleRelationSet");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "remove"), "declares remove");
        check(hasMethod(c, "contains"), "declares contains");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "size"), "declares size");
        check(hasMethod(c, "clear"), "declares clear");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleRelationSet rs = new javax.accessibility.AccessibleRelationSet();
        check(rs.size() == 0, "empty size 0");
        check(rs.toArray().length == 0, "empty toArray length 0");
        check(!rs.contains("labelFor"), "empty contains false");
        check(rs.get("labelFor") == null, "empty get null");
        check(!rs.remove(new javax.accessibility.AccessibleRelation("x")), "remove absent false");

        javax.accessibility.AccessibleRelation r = new javax.accessibility.AccessibleRelation("labelFor", "a");
        check(rs.add(r), "add returns true");
        check(rs.size() == 1, "size 1 after add");
        check(rs.contains("labelFor"), "contains by key");
        check(rs.get("labelFor") == r, "get returns same instance");

        // Adding a relation with the same key merges targets.
        javax.accessibility.AccessibleRelation r2 = new javax.accessibility.AccessibleRelation("labelFor", "b");
        check(rs.add(r2), "add same-key returns true");
        check(rs.size() == 1, "same-key add keeps size 1");
        check(rs.get("labelFor").getTarget().length == 2, "same-key add merges targets");
        check("a".equals(rs.get("labelFor").getTarget()[0]), "merged target[0]");
        check("b".equals(rs.get("labelFor").getTarget()[1]), "merged target[1]");

        javax.accessibility.AccessibleRelation[] arr = rs.toArray();
        check(arr.length == 1, "toArray length 1");
        check(rs.toString() != null && rs.toString().length() > 0, "toString non-empty");

        javax.accessibility.AccessibleRelationSet rs2 = new javax.accessibility.AccessibleRelationSet(
                new javax.accessibility.AccessibleRelation[]{
                    new javax.accessibility.AccessibleRelation("a"),
                    new javax.accessibility.AccessibleRelation("b")});
        check(rs2.size() == 2, "ctor array size 2");
        rs2.addAll(new javax.accessibility.AccessibleRelation[]{ new javax.accessibility.AccessibleRelation("c") });
        check(rs2.size() == 3, "addAll size 3");

        check(rs.remove(r), "remove present true");
        check(rs.size() == 0, "size 0 after remove");
        rs2.clear();
        check(rs2.size() == 0, "clear size 0");
        check(rs2.toArray().length == 0, "clear toArray length 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: add-dup-replace/remove/contains/get/addAll/clear matrix ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleRelationSet rs = new javax.accessibility.AccessibleRelationSet();
        check(rs.size() == 0, "empty size 0");
        check("".equals(rs.toString()), "empty toString empty string");
        check(!rs.contains(javax.accessibility.AccessibleRelation.LABEL_FOR), "empty contains false");
        check(rs.get(javax.accessibility.AccessibleRelation.LABEL_FOR) == null, "empty get null");
        check(rs.toArray().length == 0, "empty toArray");
        rs.add(new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.LABEL_FOR, "x"));
        rs.add(new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.CONTROLLED_BY, "y"));
        check(rs.size() == 2, "2 relations");
        check(rs.contains(javax.accessibility.AccessibleRelation.LABEL_FOR), "contains");
        check("labelFor".equals(rs.get(javax.accessibility.AccessibleRelation.LABEL_FOR).getKey()), "get by key");
        check(rs.get("no-such-key") == null, "missing key get null");
        check(rs.contains("controlledBy"), "contains by key string");
        // AccessibleRelationSet.toString() joins each relation's display string,
        // which is resolved against Locale.getDefault(); pin English for the literal.
        java.util.Locale savedDefaultLocale = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            check("label for,controlledBy".equals(rs.toString()), "toString display strings [" + rs.toString() + "]");
        } finally {
            java.util.Locale.setDefault(savedDefaultLocale);
        }
        boolean dup = rs.add(new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.LABEL_FOR, "z"));
        check(dup, "re-adding same key returns true (merges targets)");
        check(rs.size() == 2, "size still 2 after merge");
        Object[] merged = (Object[]) rs.get(javax.accessibility.AccessibleRelation.LABEL_FOR).getTarget();
        check(merged.length == 2 && "x".equals(merged[0]) && "z".equals(merged[1]), "targets unioned in insertion order [" + java.util.Arrays.toString(merged) + "]");
        rs.add(new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.LABEL_FOR, new Object[] { "m1", "m2" }));
        Object[] merged2 = (Object[]) rs.get(javax.accessibility.AccessibleRelation.LABEL_FOR).getTarget();
        check(merged2.length == 4 && "m2".equals(merged2[3]), "multi-target add appends [" + java.util.Arrays.toString(merged2) + "]");
        check(rs.remove(rs.get(javax.accessibility.AccessibleRelation.LABEL_FOR)), "remove true");
        check(rs.size() == 1, "1 remains");
        check(!rs.remove(rs.get(javax.accessibility.AccessibleRelation.LABEL_FOR)), "double remove false");
        check(rs.toArray().length == 1, "toArray 1");
        check(rs.toArray() instanceof Object[], "toArray Object[] view");
        rs.addAll(new javax.accessibility.AccessibleRelation[] { new javax.accessibility.AccessibleRelation(javax.accessibility.AccessibleRelation.MEMBER_OF, "m") });
        check(rs.size() == 2, "addAll");
        rs.clear();
        check(rs.size() == 0, "clear");
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

/*
 * @test
 * @summary javax.accessibility.AccessibleRelationSet conformance: class reflection.
 * @run main AccessibleRelationSetTest
 */

import java.util.Locale;

public class AccessibleRelationSetTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleRelationSet");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "addAll"), "method addAll");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "toArray"), "method toArray");
        check(hasMethod(c, "toString"), "method toString");
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

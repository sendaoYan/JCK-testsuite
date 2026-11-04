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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

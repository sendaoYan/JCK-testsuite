/*
 * @test
 * @summary javax.accessibility.AccessibleRelationSet conformance: relation set.
 * @run main AccessibleRelationSetTest
 */

public class AccessibleRelationSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.accessibility.AccessibleRelationSet rs = new javax.accessibility.AccessibleRelationSet();
        check(rs.size() == 0, "size empty");
        javax.accessibility.AccessibleRelation r = new javax.accessibility.AccessibleRelation("labelFor");
        check(rs.add(r), "add");
        check(rs.size() == 1, "size 1");
        check(rs.contains("labelFor"), "contains");
        check(rs.get("labelFor") == r, "get");
        check(rs.toArray().length == 1, "toArray length");
        check(rs.remove(r), "remove");
        check(rs.size() == 0, "size after remove");
        javax.accessibility.AccessibleRelation[] in = new javax.accessibility.AccessibleRelation[]{ new javax.accessibility.AccessibleRelation("a"), new javax.accessibility.AccessibleRelation("b") };
        javax.accessibility.AccessibleRelationSet rs2 = new javax.accessibility.AccessibleRelationSet(in);
        check(rs2.size() == 2, "ctor array");
        check(rs2.toString() != null, "toString");
        rs2.addAll(new javax.accessibility.AccessibleRelation[]{ new javax.accessibility.AccessibleRelation("c") });
        check(rs2.size() == 3, "addAll");
        rs2.clear();
        check(rs2.size() == 0, "clear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

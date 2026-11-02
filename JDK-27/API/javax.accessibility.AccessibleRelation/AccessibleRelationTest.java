/*
 * @test
 * @summary javax.accessibility.AccessibleRelation conformance: accessible relation.
 * @run main AccessibleRelationTest
 */

public class AccessibleRelationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.accessibility.AccessibleRelation r = new javax.accessibility.AccessibleRelation("labelFor");
        check("labelFor".equals(r.getKey()), "getKey");
        r.setTarget("target");
        check(r.getTarget().length == 1, "setTarget(Object) length");
        r.setTarget(new Object[]{"a", "b"});
        check(r.getTarget().length == 2, "setTarget(Object[]) length");
        check("b".equals(r.getTarget()[1]), "target value");
        check("labelFor".equals(javax.accessibility.AccessibleRelation.LABEL_FOR), "LABEL_FOR");
        check("labelForProperty".equals(javax.accessibility.AccessibleRelation.LABEL_FOR_PROPERTY), "LABEL_FOR_PROPERTY");
        javax.accessibility.AccessibleRelation r2 = new javax.accessibility.AccessibleRelation("x", new Object[]{"y"});
        check("x".equals(r2.getKey()), "ctor key");
        check(r2.getTarget().length == 1, "ctor target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

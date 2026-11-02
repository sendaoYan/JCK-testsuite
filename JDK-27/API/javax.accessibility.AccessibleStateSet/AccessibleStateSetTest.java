/*
 * @test
 * @summary javax.accessibility.AccessibleStateSet conformance: state set.
 * @run main AccessibleStateSetTest
 */

public class AccessibleStateSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.accessibility.AccessibleStateSet ss = new javax.accessibility.AccessibleStateSet();
        check(ss.add(javax.accessibility.AccessibleState.ACTIVE), "add");
        check(ss.contains(javax.accessibility.AccessibleState.ACTIVE), "contains");
        check(ss.remove(javax.accessibility.AccessibleState.ACTIVE), "remove");
        check(!ss.contains(javax.accessibility.AccessibleState.ACTIVE), "removed");
        javax.accessibility.AccessibleStateSet ss2 = new javax.accessibility.AccessibleStateSet(new javax.accessibility.AccessibleState[]{ javax.accessibility.AccessibleState.ENABLED });
        check(ss2.contains(javax.accessibility.AccessibleState.ENABLED), "ctor array");
        check(ss2.toArray().length == 1, "toArray length");
        check(ss2.toString() != null, "toString");
        ss2.addAll(new javax.accessibility.AccessibleState[]{ javax.accessibility.AccessibleState.VISIBLE });
        check(ss2.contains(javax.accessibility.AccessibleState.VISIBLE), "addAll");
        ss2.clear();
        check(ss2.toArray().length == 0, "clear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

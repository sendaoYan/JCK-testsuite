/*
 * @test
 * @summary javax.accessibility.AccessibleStateSet conformance: white-box structure + black-box behavior.
 * @run main AccessibleStateSetTest
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class AccessibleStateSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleStateSet");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "remove"), "declares remove");
        check(hasMethod(c, "contains"), "declares contains");
        check(hasMethod(c, "toArray"), "declares toArray");
        check(hasMethod(c, "clear"), "declares clear");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleStateSet ss = new javax.accessibility.AccessibleStateSet();
        check(ss.toString() == null, "empty toString null");
        check(ss.toArray().length == 0, "empty toArray length 0");
        check(!ss.contains(javax.accessibility.AccessibleState.ACTIVE), "empty contains false");
        check(!ss.remove(javax.accessibility.AccessibleState.ACTIVE), "remove absent returns false");

        check(ss.add(javax.accessibility.AccessibleState.ACTIVE), "add first returns true");
        check(!ss.add(javax.accessibility.AccessibleState.ACTIVE), "add duplicate returns false");
        check(ss.contains(javax.accessibility.AccessibleState.ACTIVE), "contains after add");
        check(ss.toArray().length == 1, "toArray length 1");

        ss.add(javax.accessibility.AccessibleState.ENABLED);
        javax.accessibility.AccessibleState[] arr = ss.toArray();
        check(arr.length == 2, "two states toArray length 2");
        check(ss.toString().indexOf(',') >= 0, "two states toString comma-separated");

        check(ss.remove(javax.accessibility.AccessibleState.ACTIVE), "remove present returns true");
        check(!ss.contains(javax.accessibility.AccessibleState.ACTIVE), "removed absent");
        check(ss.contains(javax.accessibility.AccessibleState.ENABLED), "other state still present");

        javax.accessibility.AccessibleStateSet ss2 = new javax.accessibility.AccessibleStateSet(
                new javax.accessibility.AccessibleState[]{
                    javax.accessibility.AccessibleState.ENABLED,
                    javax.accessibility.AccessibleState.ENABLED,
                    javax.accessibility.AccessibleState.VISIBLE});
        check(ss2.toArray().length == 2, "ctor dedupes duplicates");
        check(ss2.contains(javax.accessibility.AccessibleState.ENABLED), "ctor contains ENABLED");
        check(ss2.contains(javax.accessibility.AccessibleState.VISIBLE), "ctor contains VISIBLE");

        ss2.addAll(new javax.accessibility.AccessibleState[]{ javax.accessibility.AccessibleState.FOCUSED });
        check(ss2.contains(javax.accessibility.AccessibleState.FOCUSED), "addAll adds");
        ss2.clear();
        check(ss2.toArray().length == 0, "clear empties");
        check(ss2.toString() == null, "cleared toString null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

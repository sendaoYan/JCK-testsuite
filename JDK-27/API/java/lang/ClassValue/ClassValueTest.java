/*
 * @test
 * @summary java.lang.ClassValue conformance: white-box structure + black-box behavior.
 * @run main ClassValueTest
 */

import java.lang.reflect.Modifier;

public class ClassValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ClassValue");
        check(Modifier.isAbstract(c.getModifiers()), "ClassValue is abstract");
        check(!c.isInterface(), "ClassValue is a class");
        check(Modifier.isProtected(c.getDeclaredMethod("computeValue", Class.class).getModifiers()),
              "computeValue is protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testClassValue() {
        final int[] computed = {0};
        ClassValue<String> cv = new ClassValue<String>() {
            protected String computeValue(Class<?> type) {
                computed[0]++;
                return type.getName();
            }
        };
        check("java.lang.String".equals(cv.get(String.class)), "get computes the value");
        check("java.lang.String".equals(cv.get(String.class)), "get caches the value");
        check(computed[0] == 1, "computeValue called exactly once per class");
        check("java.lang.Integer".equals(cv.get(Integer.class)), "get is per-class");
        check(computed[0] == 2, "computeValue called for a second class");
    }

    static void testRemove() {
        ClassValue<String> cv = new ClassValue<String>() {
            protected String computeValue(Class<?> type) { return type.getSimpleName(); }
        };
        check("String".equals(cv.get(String.class)), "get before remove");
        cv.remove(String.class);
        check("String".equals(cv.get(String.class)), "get recomputes after remove");
    }


    static void testBlackBox() throws Exception {
        testClassValue();
        testRemove();
    }

    // ===== 契约深化（2026-11）新增 fixtures =====
    static class CountingValue extends ClassValue<Integer> {
        int computes;
        final java.util.List<Class<?>> asked = new java.util.ArrayList<Class<?>>();
        protected Integer computeValue(Class<?> type) {
            computes++;
            asked.add(type);
            return Integer.valueOf(computes * 1000); // >127: fresh object outside the Integer cache
        }
    }

    enum Planet { MERCURY, VENUS }
    interface Shape {}
    static class Widget {}
    record Rec(int a) {}

    static void testComputeOnceAndIdentity() {
        CountingValue cv = new CountingValue();
        Integer v1 = cv.get(String.class);
        Integer v2 = cv.get(String.class);
        check(v1 != null && v1.intValue() == 1000, "first value comes from the first compute");
        check(v1 == v2, "get returns the identical cached object");
        check(cv.computes == 1, "computeValue called exactly once");
        check(cv.asked.get(0) == String.class, "computeValue receives the exact Class object");
        check(cv.get(String.class) == v1, "third get still returns the cached object");
        check(cv.computes == 1, "no extra compute after three gets");
    }

    static void testPerClassEntries() {
        CountingValue cv = new CountingValue();
        Class<?>[] keys = { String.class, Integer.class, Character.class, int.class,
                            String[].class, void.class, Planet.class, Shape.class,
                            Widget.class, Rec.class, new int[0].getClass() };
        for (int i = 0; i < keys.length; i++) check(cv.get(keys[i]) != null, "value computed for key " + i);
        check(cv.computes == keys.length, "exactly one compute per class");
        for (int i = 0; i < keys.length; i++) check(cv.get(keys[i]) != null, "second pass cached " + i);
        check(cv.computes == keys.length, "no recomputation on the second pass");
        for (int i = 0; i < keys.length; i++) check(cv.asked.get(i) == keys[i], "asked identity " + i);
    }

    static void testRemoveRecompute() {
        CountingValue cv = new CountingValue();
        Integer before = cv.get(String.class);
        check(cv.computes == 1, "one compute before remove");
        cv.remove(String.class);
        Integer after = cv.get(String.class);
        check(cv.computes == 2, "remove forces recomputation on next get");
        check(after.intValue() == 2000, "recomputed value differs from the original");
        check(after != before, "recomputed value is a new object");
        check(cv.get(String.class) == after, "recomputed value is cached");
        check(cv.computes == 2, "no further computes while cached");
        cv.remove(Integer.class); // never computed: harmless no-op
        check(cv.computes == 2, "remove of a never-computed class is a no-op");
        check(cv.get(Widget.class) != null, "other classes unaffected by remove");
        check(cv.computes == 3, "only Widget computed after the no-op remove");
        cv.remove(String.class);
        cv.remove(String.class); // second remove is also a no-op
        check(cv.get(String.class).intValue() == 4000, "double remove then recompute");
        check(cv.computes == 4, "final compute count");
    }

    static void testNullValueCached() {
        final int[] calls = {0};
        ClassValue<Object> nullCv = new ClassValue<Object>() {
            protected Object computeValue(Class<?> type) { calls[0]++; return null; }
        };
        check(nullCv.get(String.class) == null, "null value returned");
        check(calls[0] == 1, "null computed once");
        check(nullCv.get(String.class) == null, "null still returned on second get");
        check(calls[0] == 1, "null value is cached without recompute");
        nullCv.remove(String.class);
        check(nullCv.get(String.class) == null, "null again after remove");
        check(calls[0] == 2, "recompute happens after remove");
    }

    static void testExceptionNotCached() {
        final int[] calls = {0};
        ClassValue<String> cv = new ClassValue<String>() {
            protected String computeValue(Class<?> type) {
                calls[0]++;
                if (calls[0] == 1) throw new IllegalStateException("boom");
                return "recovered";
            }
        };
        try {
            cv.get(String.class);
            throw new AssertionError("computeValue exception: expected propagation");
        } catch (IllegalStateException e) {
            check("boom".equals(e.getMessage()), "exact exception relayed");
        }
        check(calls[0] == 1, "failing compute counted");
        check("recovered".equals(cv.get(String.class)), "second get retries computeValue");
        check(calls[0] == 2, "failure was not cached");
        check("recovered".equals(cv.get(String.class)), "successful value is cached");
        check(calls[0] == 2, "no recompute after success");
    }

    static void testIndependentInstances() {
        CountingValue a = new CountingValue();
        CountingValue b = new CountingValue();
        Integer va = a.get(String.class);
        Integer vb = b.get(String.class);
        check(va != vb, "different ClassValues compute independently");
        check(a.computes == 1 && b.computes == 1, "each instance computed once");
        check(a.get(String.class) == va && b.get(String.class) == vb,
              "each instance caches its own value");
        check(a.get(Integer.class) != null && b.get(Integer.class) != null,
              "both instances are per-class");
        check(a.computes == 2 && b.computes == 2, "per-instance compute counts");
        a.remove(String.class);
        check(a.get(String.class) != va, "removed instance recomputes on next get");
        check(b.get(String.class) == vb, "remove on one instance does not affect the other");
        check(a.computes == 3 && b.computes == 2, "only the removed instance recomputed");
    }

    static void testExoticKeys() {
        CountingValue cv = new CountingValue();
        Runnable r = () -> {};
        Integer v1 = cv.get(r.getClass());
        check(v1 != null, "lambda hidden class as key");
        check(cv.get(r.getClass()) == v1, "hidden class value cached");
        check(r.getClass().isHidden(), "lambda key is indeed a hidden class");
        Integer v2 = cv.get(Planet.class);
        check(v2 != null && cv.get(Planet.class) == v2, "enum class as key");
        Integer v3 = cv.get(Planet[].class);
        check(v3 != null && cv.get(Planet[].class) == v3, "enum array class as key");
        Integer v4 = cv.get(Shape.class);
        check(v4 != null && cv.get(Shape.class) == v4, "interface as key");
        Integer v5 = cv.get(int.class);
        check(v5 != null && cv.get(int.class) == v5, "primitive class as key");
        Integer v6 = cv.get(void.class);
        check(v6 != null && cv.get(void.class) == v6, "void class as key");
        Integer v7 = cv.get(Rec.class);
        check(v7 != null && cv.get(Rec.class) == v7, "record class as key");
        check(cv.computes == 7, "seven distinct keys computed exactly once each");
        Runnable r2 = () -> {};
        Integer v8 = cv.get(r2.getClass());
        check(v8 != null, "second lambda class is its own key");
        check(cv.computes == 8, "distinct lambdas get distinct entries");
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testComputeOnceAndIdentity();
        testPerClassEntries();
        testRemoveRecompute();
        testNullValueCached();
        testExceptionNotCached();
        testIndependentInstances();
        testExoticKeys();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

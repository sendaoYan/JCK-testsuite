/*
 * @test
 * @summary java.lang.reflect.AccessibleObject conformance: white-box structure + black-box behavior,
 *          plus the deepen batch: canAccess IAE message fragments and positive paths,
 *          trySetAccessible success/failure/idempotence, private-field deny/allow/re-deny
 *          cycle with message fragments, static-final write denial, repeatable-annotation
 *          container semantics and a mixed-kind batch including an Executable.
 * @run main AccessibleObjectTest
 */

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AccessibleObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            if (!expected.isInstance(t)) {
                throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            String m = t.getMessage();
            if (m == null || !m.contains(fragment)) {
                throw new AssertionError(msg + ": message <" + m
                        + "> does not contain <" + fragment + ">");
            }
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static class Sample {
        public int pub;

        @Deprecated
        public void marked() {
        }

        public static void stat() {
        }
    }

    // ---- fixtures added by the deepen batch ----

    static class StatFin {
        public static final String SF = "sf";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface PA {
        String value();
    }

    // PR declared before its container PC so PC's element type PR[] resolves
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(PC.class)
    @interface PR {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface PC {
        PR[] value();
    }

    static class AnnHolder {
        @PR("1")
        @PR("2")
        @PA("x")
        public void multi() {
        }
    }

    // second top-level nest fixture lives in AccessibleSeparate (bottom of file)

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Field f = Sample.class.getField("pub");
        check(!f.isAccessible(), "not accessible by default");
        f.setAccessible(true);
        check(f.isAccessible(), "setAccessible(true)");
        f.setAccessible(false);
        check(!f.isAccessible(), "setAccessible(false)");
        check(f.getAnnotations().length == 0, "getAnnotations");
        check(f.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations");
        check(!f.isAnnotationPresent(Deprecated.class), "isAnnotationPresent false");
        check(f.getAnnotation(Deprecated.class) == null, "getAnnotation null");
        AccessibleObject.setAccessible(new AccessibleObject[]{f}, true);
        check(f.isAccessible(), "static setAccessible(array)");
        check(!AccessibleObject.class.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(AccessibleObject.class.getModifiers()),
              "not abstract");
        boolean implAnnotatedElement = false;
        for (Class<?> i : AccessibleObject.class.getInterfaces())
            if (i.getName().equals("java.lang.reflect.AnnotatedElement")) implAnnotatedElement = true;
        check(implAnnotatedElement, "implements AnnotatedElement");
        check(AccessibleObject.class.isAssignableFrom(Field.class)
              && AccessibleObject.class.isAssignableFrom(Method.class)
              && AccessibleObject.class.isAssignableFrom(Constructor.class),
              "Field/Method/Constructor extend AccessibleObject");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. canAccess: public member of own class
        Sample s = new Sample();
        Field pub = Sample.class.getField("pub");
        check(pub.canAccess(s), "canAccess public instance field");
        check(!pub.isAccessible(), "canAccess does not require setAccessible");
        Method stat = Sample.class.getMethod("stat");
        check(stat.canAccess(null), "canAccess static member with null");
        try {
            stat.canAccess(s);
            check(false, "canAccess(non-null) on static member must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException canAccess non-null on static");
        }
        try {
            pub.canAccess(null);
            check(false, "canAccess(null) on instance member must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException canAccess(null) instance member");
        }
        try {
            pub.canAccess("not-a-Sample");
            check(false, "canAccess wrong obj must fail");
        } catch (IllegalArgumentException e) {
            check(true, "IllegalArgumentException canAccess wrong obj");
        }

        // 2. canAccess/trySetAccessible on a cross-nest private member
        Field hid = AccessibleSeparate.class.getDeclaredField("hidden");
        check(!hid.canAccess(new AccessibleSeparate()), "private member canAccess false");
        check(hid.trySetAccessible(), "trySetAccessible succeeds in same unnamed module");
        check(hid.canAccess(new AccessibleSeparate()), "canAccess true after trySetAccessible");
        check(hid.isAccessible(), "isAccessible mirrors override");
        hid.setAccessible(false);
        check(!hid.canAccess(new AccessibleSeparate()), "setAccessible(false) restores check");

        // 3. batch setAccessible over mixed member kinds
        Method mk = Sample.class.getMethod("marked");
        Constructor<?> ctor = Sample.class.getDeclaredConstructor();
        Field[] pvt = new Field[]{AccessibleSeparate.class.getDeclaredField("other")};
        AccessibleObject[] batch = {mk, ctor, pvt[0]};
        AccessibleObject.setAccessible(batch, true);
        for (AccessibleObject ao : batch) check(ao.isAccessible(), "batch setAccessible true");
        AccessibleObject.setAccessible(batch, false);
        for (AccessibleObject ao : batch) check(!ao.isAccessible(), "batch setAccessible false");

        // 4. annotations via the AccessibleObject contract
        check(mk.isAnnotationPresent(Deprecated.class), "isAnnotationPresent on method");
        check(mk.getAnnotation(Deprecated.class) != null, "getAnnotation Deprecated");
        check(mk.getAnnotations().length == 1, "getAnnotations length 1");
        check(mk.getDeclaredAnnotation(Deprecated.class) != null, "getDeclaredAnnotation");
        check(mk.getDeclaredAnnotations().length == 1, "getDeclaredAnnotations length 1");
        check(mk.getAnnotationsByType(Deprecated.class).length == 1, "getAnnotationsByType");
        check(mk.getDeclaredAnnotationsByType(Deprecated.class).length == 1,
              "getDeclaredAnnotationsByType");
        check(mk.getAnnotation(SafeVarargs.class) == null, "absent annotation null");
        check(!mk.isAnnotationPresent(SafeVarargs.class), "absent isAnnotationPresent false");

        // 5. behavior change driven by the flag: get before/after
        Field other = AccessibleSeparate.class.getDeclaredField("other");
        AccessibleSeparate inst = new AccessibleSeparate();
        try {
            other.get(inst);
            check(false, "get must be denied before setAccessible");
        } catch (IllegalAccessException e) {
            check(true, "IllegalAccessException before setAccessible");
        }
        other.setAccessible(true);
        check("o0".equals(other.get(inst)), "get after setAccessible");
        other.set(inst, "o1");
        check("o1".equals(other.get(inst)), "set/get after setAccessible");

        // 6. setAccessible on an unopened JDK class member is refused
        Field stringValue = String.class.getDeclaredField("value");
        try {
            stringValue.setAccessible(true);
            check(false, "java.base private setAccessible must be refused");
        } catch (java.lang.reflect.InaccessibleObjectException e) {
            check(true, "InaccessibleObjectException on unopened module");
        }
        check(!stringValue.trySetAccessible(), "trySetAccessible false on unopened module");
    }

    // ---- tests added by the deepen batch ----

    static void testApiSurfaceLocked27() throws Exception {
        check(AccessibleObject.class.getMethod("canAccess", Object.class) != null,
                "canAccess present on JDK-27");
        check(AccessibleObject.class.getMethod("trySetAccessible") != null,
                "trySetAccessible present on JDK-27");
    }

    static void testCanAccessMessages() throws Exception {
        Sample s = new Sample();
        Method stat = Sample.class.getMethod("stat");
        Method mk = Sample.class.getMethod("marked");
        check(mk.canAccess(s), "instance method with matching receiver");
        check(stat.canAccess(null), "static method with null");
        checkThrowsMsg(IllegalArgumentException.class, () -> mk.canAccess(null),
                "null object for", "null receiver on instance member IAE message");
        checkThrowsMsg(IllegalArgumentException.class, () -> mk.canAccess("str"),
                "is not an instance of", "wrong receiver type IAE message");
        checkThrowsMsg(IllegalArgumentException.class, () -> stat.canAccess(s),
                "non-null object for", "non-null receiver on static member IAE message");
    }

    static void testTrySetAccessiblePaths() throws Exception {
        // failure path returns false without throwing
        Field jf = String.class.getDeclaredField("value");
        check(!jf.trySetAccessible(), "trySetAccessible false on unopened module");
        check(!jf.isAccessible(), "flag untouched after failed trySetAccessible");
        // success path on own private member and idempotence
        Field own = AccessibleSeparate.class.getDeclaredField("hidden");
        check(own.trySetAccessible(), "trySetAccessible true on own class");
        check(own.trySetAccessible(), "trySetAccessible idempotent");
        check(own.isAccessible(), "isAccessible mirrors the override");
    }

    static void testPrivateFieldDeniedThenAllowed() throws Exception {
        AccessibleSeparate inst = new AccessibleSeparate();
        Field f = AccessibleSeparate.class.getDeclaredField("hidden");
        checkThrowsMsg(IllegalAccessException.class, () -> f.get(inst),
                "access a member of class", "denied private read message");
        f.setAccessible(true);
        check("h".equals(f.get(inst)), "read after setAccessible");
        f.setAccessible(false);
        checkThrowsMsg(IllegalAccessException.class, () -> f.get(inst),
                "access a member of class", "re-denied after setAccessible(false)");
    }

    static void testStaticFinalFieldSet() throws Exception {
        Field sf = StatFin.class.getDeclaredField("SF");
        check(sf.get(null).equals("sf"), "static final read ok without override");
        sf.setAccessible(true);
        checkThrowsMsg(IllegalAccessException.class, () -> sf.set(null, "x"),
                "Can not set static final", "static final write denied");
        check(sf.get(null).equals("sf"), "value unchanged after denial");
        sf.setAccessible(false);
    }

    static void testBatchMixedKinds() throws Exception {
        Field bf = Sample.class.getField("pub");
        Method bm = Sample.class.getMethod("marked");
        Constructor<?> bc = Sample.class.getDeclaredConstructor();
        Executable be = Sample.class.getMethod("stat");
        AccessibleObject[] arr = {bf, bm, bc, be};
        AccessibleObject.setAccessible(arr, true);
        check(bf.isAccessible() && bm.isAccessible() && bc.isAccessible()
                && be.isAccessible(), "batch flags all true incl. Executable");
        AccessibleObject.setAccessible(arr, false);
        check(!bf.isAccessible() && !bm.isAccessible() && !bc.isAccessible()
                && !be.isAccessible(), "batch flags all false");
        bm.setAccessible(true);
        check(bm.isAccessible() && !bf.isAccessible(), "flags independent");
    }

    static void testRepeatableAnnotations() throws Exception {
        Method m = AnnHolder.class.getMethod("multi");
        check(m.getAnnotationsByType(PR.class).length == 2,
                "getAnnotationsByType expands the container");
        check(m.getAnnotation(PR.class) == null,
                "single repeated annotation not directly present");
        check(m.getAnnotation(PC.class) != null,
                "container is directly present");
        check(m.getDeclaredAnnotationsByType(PR.class).length == 2,
                "getDeclaredAnnotationsByType expands the container");
        check(m.getAnnotationsByType(PA.class).length == 1,
                "non-repeatable annotation unaffected");
        check(m.getDeclaredAnnotations().length == 2,
                "declared = container + other annotation");
        check(!m.isAnnotationPresent(PR.class),
                "isAnnotationPresent does not see through the container");
        check(m.getAnnotationsByType(Deprecated.class).length == 0,
                "absent byType empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testApiSurfaceLocked27();
        testCanAccessMessages();
        testTrySetAccessiblePaths();
        testPrivateFieldDeniedThenAllowed();
        testStaticFinalFieldSet();
        testBatchMixedKinds();
        testRepeatableAnnotations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// second top-level class = a different nest: private members are not
// accessible to AccessibleObjectTest, exercising the access-control paths.
class AccessibleSeparate {
    private String hidden = "h";
    @SuppressWarnings("unused")
    private String other = "o0";
}

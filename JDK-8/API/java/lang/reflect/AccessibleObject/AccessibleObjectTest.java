/*
 * @test
 * @summary java.lang.reflect.AccessibleObject conformance: setAccessible/isAccessible and
 *          annotation accessors via a Field, plus the deepen batch: private-field deny /
 *          allow / re-deny cycle with message fragments, static-final write denial,
 *          batch flag flipping across Field/Method/Constructor, repeatable-annotation
 *          container semantics, the AccessibleObject type hierarchy and the JDK-8 API
 *          surface lock (no canAccess/trySetAccessible; String.value setAccessible works).
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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
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

    static class Sample {
        public int pub;
    }

    // ---- fixtures added by the deepen batch ----

    // JDK-8 has no nestmate access: a private nested field is a valid deny target.
    static class Priv {
        private int hidden = 7;
    }

    static class StatFin {
        public static final String SF = "sf";
    }

    static class Batch {
        public int v;

        public void go() {
        }

        public Batch() {
        }
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

    static void testAccessibleObject() throws Exception {
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
    }

    // ---- tests added by the deepen batch ----

    static void testApiSurfaceLocked() throws Exception {
        // canAccess/trySetAccessible were added in JDK 9 — absent on JDK-8.
        checkThrows(NoSuchMethodException.class,
                () -> AccessibleObject.class.getMethod("canAccess", Object.class),
                "no canAccess on JDK-8");
        checkThrows(NoSuchMethodException.class,
                () -> AccessibleObject.class.getMethod("trySetAccessible"),
                "no trySetAccessible on JDK-8");
    }

    static void testPrivateFieldDeniedThenAllowed() throws Exception {
        Priv p = new Priv();
        Field f = Priv.class.getDeclaredField("hidden");
        checkThrowsMsg(IllegalAccessException.class, () -> f.get(p),
                "access a member of class", "denied private read message");
        f.setAccessible(true);
        check(f.get(p).equals(7), "read after setAccessible");
        f.set(p, 8);
        check(p.hidden == 8, "reflective write visible to normal read");
        f.setAccessible(false);
        checkThrowsMsg(IllegalAccessException.class, () -> f.get(p),
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
        Field bf = Batch.class.getField("v");
        Method bm = Batch.class.getMethod("go");
        Constructor<?> bc = Batch.class.getConstructor();
        AccessibleObject[] arr = {bf, bm, bc};
        AccessibleObject.setAccessible(arr, true);
        check(bf.isAccessible() && bm.isAccessible() && bc.isAccessible(),
                "batch flags all true");
        AccessibleObject.setAccessible(arr, false);
        check(!bf.isAccessible() && !bm.isAccessible() && !bc.isAccessible(),
                "batch flags all false");
        bf.setAccessible(true);
        check(bf.isAccessible() && !bm.isAccessible(), "flags independent");
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

    static void testTypeHierarchy() throws Exception {
        check(AccessibleObject.class.isAssignableFrom(Field.class),
                "Field extends AccessibleObject");
        check(AccessibleObject.class.isAssignableFrom(Method.class),
                "Method extends AccessibleObject");
        check(AccessibleObject.class.isAssignableFrom(Executable.class),
                "Executable extends AccessibleObject");
        check(AccessibleObject.class.isAssignableFrom(Constructor.class),
                "Constructor extends AccessibleObject");
        boolean found = false;
        for (Class<?> i : AccessibleObject.class.getInterfaces()) {
            if (i.getName().equals("java.lang.reflect.AnnotatedElement")) found = true;
        }
        check(found, "implements AnnotatedElement");
        check(!AccessibleObject.class.isInterface(), "is a class not an interface");
    }

    static void testPlatformStringField() throws Exception {
        // JDK-8 has no module system: setAccessible on a java.lang private field
        // succeeds (JDK-9+ throws InaccessibleObjectException) — locked difference.
        Field v = String.class.getDeclaredField("value");
        v.setAccessible(true);
        check(v.isAccessible(), "JDK-8 String.value setAccessible succeeds");
        v.setAccessible(false);
        check(!v.isAccessible(), "flag restored");
    }

    public static void main(String[] args) throws Exception {
        testAccessibleObject();
        testApiSurfaceLocked();
        testPrivateFieldDeniedThenAllowed();
        testStaticFinalFieldSet();
        testBatchMixedKinds();
        testRepeatableAnnotations();
        testTypeHierarchy();
        testPlatformStringField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.reflect.AccessibleObject conformance: white-box structure + black-box behavior.
 * @run main AccessibleObjectTest
 */

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AccessibleObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int pub;

        @Deprecated
        public void marked() {
        }

        public static void stat() {
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

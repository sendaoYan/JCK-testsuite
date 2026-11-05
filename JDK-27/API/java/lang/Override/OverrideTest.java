/*
 * @test
 * @summary java.lang.Override conformance: white-box structure + black-box behavior
 *          (SOURCE retention, METHOD target, source-retained absence at runtime).
 * @run main OverrideTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class OverrideTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Base { void m() {} }
    static class Sub extends Base { @Override void m() {} }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        Class<?> c = Override.class;
        check(c.isAnnotation(), "Override is an annotation type");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.SOURCE,
              "SOURCE retention");
        boolean hasMethod = false;
        for (ElementType e : c.getAnnotation(Target.class).value()) {
            if (e == ElementType.METHOD) hasMethod = true;
        }
        check(hasMethod, "@Target includes METHOD");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSourceRetained() throws Exception {
        check(!Sub.class.getDeclaredMethod("m").isAnnotationPresent(Override.class),
              "@Override is source-retained (absent at runtime)");
    }

    // ==== Contract deepening (lcN1, 2026-11) ====
    interface Ifce { void im(); }
    static class IfaceImpl implements Ifce { @Override public void im() {} }
    static class AbsImpl extends AbstractBase { @Override void am() {} }
    static abstract class AbstractBase { abstract void am(); }
    static class Sub2 extends Sub { @Override public String toString() { return "Sub2"; } }

    static void testTargetExactSingleValue() {
        ElementType[] ts = Override.class.getAnnotation(Target.class).value();
        check(ts.length == 1, "@Target has exactly one element");
        check(ts[0] == ElementType.METHOD, "@Target is exactly {METHOD}");
        check(!java.util.Arrays.asList(ts).contains(ElementType.TYPE), "TYPE not allowed");
        check(!java.util.Arrays.asList(ts).contains(ElementType.CONSTRUCTOR),
              "CONSTRUCTOR not allowed");
    }

    static void testSourceInvisibilityEverywhere() throws Exception {
        check(Sub.class.getDeclaredMethod("m").getAnnotation(Override.class) == null,
              "getAnnotation(Override.class) == null on overriding method (SOURCE retention)");
        check(Sub.class.getDeclaredMethod("m").getAnnotations().length == 0,
              "no runtime-visible annotations on the @Override method");
        check(IfaceImpl.class.getDeclaredMethod("im").getAnnotation(Override.class) == null,
              "interface-implementation @Override also invisible");
        check(AbsImpl.class.getDeclaredMethod("am").getAnnotation(Override.class) == null,
              "abstract-override @Override also invisible");
        check(Sub2.class.getDeclaredMethod("toString").getAnnotation(Override.class) == null,
              "toString() @Override also invisible");
        check(Sub2.class.getDeclaredMethod("toString").isAnnotationPresent(Override.class) == false,
              "isAnnotationPresent false");
    }

    static void testNoMembersNoDocumented() {
        check(Override.class.getDeclaredMethods().length == 0, "Override declares no members");
        check(Override.class.getDeclaredFields().length == 0, "no fields");
        check(!Override.class.isAnnotationPresent(java.lang.annotation.Documented.class),
              "@Documented absent on Override");
        check(Override.class.getAnnotations().length == 2,
              "only two runtime-visible meta-annotations (Retention, Target)");
        boolean seenRetention = false, seenTarget = false;
        for (java.lang.annotation.Annotation a : Override.class.getAnnotations()) {
            if (a.annotationType() == java.lang.annotation.Retention.class) seenRetention = true;
            if (a.annotationType() == java.lang.annotation.Target.class) seenTarget = true;
        }
        check(seenRetention && seenTarget, "meta-annotations are exactly Retention and Target");
    }

    static void testStructuralExtras() {
        check(Override.class.isAnnotation(), "Override is an annotation type");
        check(Override.class.isInterface(), "annotation types are interfaces");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(Override.class),
              "assignable to Annotation");
        check("java.lang.Override".equals(Override.class.getName()), "binary name");
        check(Override.class.getAnnotation(Override.class) == null,
              "Override is not itself @Override-annotated");
        check(Override.class.getAnnotation(java.lang.annotation.Inherited.class) == null,
              "@Inherited absent");
        check(Override.class.getDeclaredClasses().length == 0, "no nested classes");
        check(Override.class.getDeclaredAnnotations().length == 2,
              "declared annotations count 2");
    }

    static void testOverrideSemanticsStillHold() {
        // behavioral sanity: the @Override-marked methods really do override
        check(new Sub2().toString().equals("Sub2"), "overridden toString dispatches to Sub2");
        check(!(new Sub().toString().equals("Sub2")), "super toString differs");
        Ifce f = new IfaceImpl();
        check(f != null, "interface instance created");
        AbstractBase b = new AbsImpl();
        check(b.getClass() == AbsImpl.class, "abstract extension instantiable via concrete subclass");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSourceRetained();
        testTargetExactSingleValue();
        testSourceInvisibilityEverywhere();
        testNoMembersNoDocumented();
        testStructuralExtras();
        testOverrideSemanticsStillHold();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

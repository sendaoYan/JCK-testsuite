/*
 * @test
 * @summary java.lang.annotation.Target conformance: white-box structure + black-box behavior,
 *          ElementType[] value(), self-targeted to ANNOTATION_TYPE, and permissive/restrictive
 *          target semantics.
 * @run main TargetTest
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TargetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures: restricted targets vs unrestricted ----
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface OnlyField {
    }

    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @interface MethodOrParam {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Anywhere {
    }

    static class Carrier {
        @OnlyField
        static int field;

        @MethodOrParam("m")
        void method(@MethodOrParam("p") int p) {
        }
    }

    @Anywhere
    static class TypeUse {
    }

    // an empty @Target: the annotation type is declarable but applicable nowhere
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface NothingAllowed {
    }

    // an annotation applicable to every JDK 27 target (12 element kinds)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
             ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE,
             ElementType.PACKAGE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE,
             ElementType.MODULE, ElementType.RECORD_COMPONENT})
    @Retention(RetentionPolicy.RUNTIME)
    @interface AllTargets {
    }

    @AllTargets
    static class Everywhere {

        @AllTargets
        static int anyField;

        @AllTargets
        Everywhere(@AllTargets int p) {
        }

        @AllTargets
        void anyMethod(@AllTargets int p) {
        }
    }

    @AllTargets
    @interface MetaUse {
    }

    // TYPE_USE-only annotation written in a type position (a type parameter)
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface TypeUseOnly {
    }

    static class GenericCarrier {
        <@TypeUseOnly T> void generic(T t) {
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Target");
        check(c.isAnnotation(), "Target is an annotation type");
        check(c.isAnnotationPresent(Retention.class), "has @Retention");
        check(c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
        check(c.isAnnotationPresent(Documented.class), "@Documented on Target");
        check(c.getDeclaredMethods().length == 1, "single member");
        check(c.getDeclaredMethod("value").getReturnType() == ElementType[].class,
              "value() returns ElementType[]");
        Target self = Target.class.getAnnotation(Target.class);
        check(self != null, "self-annotated");
        check(self.value().length == 1 && self.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Field f = Carrier.class.getDeclaredField("field");
        check(f.getAnnotation(OnlyField.class) != null, "@Target(FIELD) usable on field");

        Method m = Carrier.class.getDeclaredMethod("method", int.class);
        check(m.getAnnotation(MethodOrParam.class) != null, "@Target(METHOD,...) usable on method");
        check(m.getAnnotation(MethodOrParam.class).value().equals("m"), "method member value");
        check(m.getParameters()[0].getAnnotation(MethodOrParam.class) != null,
              "@Target(...,PARAMETER) usable on parameter");
        check(m.getParameters()[0].getAnnotation(MethodOrParam.class).value().equals("p"),
              "parameter member value");

        check(TypeUse.class.getAnnotation(Anywhere.class) != null,
              "no @Target: usable on type");

        Target tf = OnlyField.class.getAnnotation(Target.class);
        check(tf != null && tf.value().length == 1 && tf.value()[0] == ElementType.FIELD,
              "OnlyField declared targets readable");
        Target tmp = MethodOrParam.class.getAnnotation(Target.class);
        check(tmp != null && tmp.value().length == 2
              && tmp.value()[0] == ElementType.METHOD && tmp.value()[1] == ElementType.PARAMETER,
              "MethodOrParam targets in declared order");
        check(Anywhere.class.getAnnotation(Target.class) == null,
              "Anywhere has no @Target (unrestricted)");

        check(Carrier.class.getAnnotation(OnlyField.class) == null,
              "field-only annotation absent from class");
        check(f.getAnnotation(Anywhere.class) == null, "Anywhere not used on this field");
    }

    // ---- Part 3: deepened target semantics ----
    static void testDeepenedTargets() throws Exception {
        // empty @Target array is a legal declaration: readable as length 0
        Target empty = NothingAllowed.class.getAnnotation(Target.class);
        check(empty != null && empty.value().length == 0,
              "@Target({}) declares zero applicable targets");

        // the all-targets declaration covers exactly the JDK 27 set of 12
        Target all = AllTargets.class.getAnnotation(Target.class);
        check(all.value().length == 12, "all-targets declaration has 12 entries");
        boolean hasModule = false, hasRecord = false;
        for (ElementType e : all.value()) {
            if (e == ElementType.MODULE) hasModule = true;
            if (e == ElementType.RECORD_COMPONENT) hasRecord = true;
        }
        check(hasModule && hasRecord, "MODULE and RECORD_COMPONENT included");

        // an all-targets annotation is present on every kind of element
        check(Everywhere.class.getAnnotation(AllTargets.class) != null, "on type");
        Field anyField = Everywhere.class.getDeclaredField("anyField");
        check(anyField.getAnnotation(AllTargets.class) != null, "on field");
        java.lang.reflect.Constructor<?> ctor =
                Everywhere.class.getDeclaredConstructor(int.class);
        check(ctor.getAnnotation(AllTargets.class) != null, "on constructor");
        check(ctor.getParameters()[0].getAnnotation(AllTargets.class) != null,
              "on constructor parameter");
        Method anyMethod = Everywhere.class.getDeclaredMethod("anyMethod", int.class);
        check(anyMethod.getAnnotation(AllTargets.class) != null, "on method");
        check(anyMethod.getParameters()[0].getAnnotation(AllTargets.class) != null,
              "on method parameter");
        check(MetaUse.class.getAnnotation(AllTargets.class) != null, "on annotation type");

        // TYPE_USE-only annotation in a type position lands on the type parameter,
        // not on the method -- the declaration and type views are distinct
        Method generic = GenericCarrier.class.getDeclaredMethod("generic", Object.class);
        check(generic.getAnnotation(TypeUseOnly.class) == null,
              "type-position annotation not on the method declaration");
        check(generic.getTypeParameters()[0].getAnnotation(TypeUseOnly.class) != null,
              "type-position annotation visible on the type parameter");

        // value() arrays are defensively copied on every read
        Target tf = OnlyField.class.getAnnotation(Target.class);
        ElementType[] tfv = tf.value();
        tfv[0] = ElementType.METHOD;
        check(tf.value()[0] == ElementType.FIELD, "Target value() defensively copied");
        check(tf.value().length == 1, "Target value() length unchanged");

        // Target instances are value-based
        Target t1 = OnlyField.class.getAnnotation(Target.class);
        Target t2 = OnlyField.class.getAnnotation(Target.class);
        check(t1.equals(t2), "Target equal across fetches");
        check(t1.hashCode() == t2.hashCode(), "Target hashCode stable");
        check(!t1.equals(MethodOrParam.class.getAnnotation(Target.class)),
              "different target sets not equal");

        // the Target meta-annotation itself is not @Inherited and not repeatable
        check(!Target.class.isAnnotationPresent(java.lang.annotation.Inherited.class),
              "Target is not @Inherited");
        check(!Target.class.isAnnotationPresent(java.lang.annotation.Repeatable.class),
              "Target is not @Repeatable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepenedTargets();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

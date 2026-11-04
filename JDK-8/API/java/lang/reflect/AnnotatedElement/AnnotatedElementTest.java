/*
 * @test
 * @summary java.lang.reflect.AnnotatedElement conformance: annotation accessors,
 *          repeatable annotations flattened vs container view, inheritance semantics
 *          (present on subclass but not declared), declaration order stability,
 *          uniform contract across Class/Field/Method/Constructor/Parameter carriers,
 *          absent paths, and null argument -> NullPointerException.
 * @run main AnnotatedElementTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class AnnotatedElementTest {
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

    // ---- fixture annotations (self-made, runtime visible, several element kinds) ----

    @Repeatable(Tags.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @interface Tag {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @interface Tags {
        Tag[] value();
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface InhMark {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface PlainMark {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Ord1 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Ord2 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Ord3 {
    }

    @Deprecated
    static class Annotated {
    }

    static class Plain {
    }

    @Tag("x")
    @Tag("y")
    @Tag("z")
    @PlainMark
    static class Rep {
    }

    @Tag("only")
    static class Single {
    }

    @InhMark
    static class BaseAnno {
    }

    static class ChildAnno extends BaseAnno {
    }

    static class GrandChildAnno extends ChildAnno {
    }

    @PlainMark
    static class MixedChild extends BaseAnno {
    }

    @Ord1
    @Ord2
    @Ord3
    static class Ordered {
    }

    static class Carriers {
        @Tag("f1")
        public int field;

        @Tag("m1")
        public void method() {
        }

        public Carriers() {
        }

        public static void withParam(@Tag("p1") int p) {
        }
    }

    static void testAnnotatedElement() {
        AnnotatedElement e = Annotated.class;
        check(e.isAnnotationPresent(Deprecated.class), "isAnnotationPresent");
        check(e.getAnnotation(Deprecated.class) != null, "getAnnotation");
        check(e.getDeclaredAnnotation(Deprecated.class) != null, "getDeclaredAnnotation");
        check(e.getAnnotations().length == 1, "getAnnotations length");
        check(e.getDeclaredAnnotations().length == 1, "getDeclaredAnnotations length");
        check(e.getAnnotationsByType(Deprecated.class).length == 1, "getAnnotationsByType");
        check(e.getDeclaredAnnotationsByType(Deprecated.class).length == 1,
              "getDeclaredAnnotationsByType");

        AnnotatedElement p = Plain.class;
        check(!p.isAnnotationPresent(Deprecated.class), "plain not annotated");
        check(p.getAnnotation(Deprecated.class) == null, "plain getAnnotation null");
        check(p.getAnnotations().length == 0, "plain getAnnotations");
    }

    // repeatable annotations: flattened by-type view vs direct container view
    static void testRepeatableAnnotations() {
        AnnotatedElement rep = Rep.class;
        Tag[] byType = rep.getAnnotationsByType(Tag.class);
        check(byType.length == 3, "repeatable flattened to three");
        check("x".equals(byType[0].value()), "first in declaration order");
        check("y".equals(byType[1].value()), "second in declaration order");
        check("z".equals(byType[2].value()), "third in declaration order");
        check(rep.getDeclaredAnnotationsByType(Tag.class).length == 3, "declared byType three");
        check(rep.getAnnotation(Tag.class) == null, "direct single absent when repeated");
        check(rep.getAnnotation(Tags.class) != null, "container directly present");
        check(rep.getAnnotation(Tags.class).value().length == 3, "container holds three");
        check(rep.isAnnotationPresent(Tags.class), "container present");
        check(!rep.isAnnotationPresent(Tag.class),
              "presence check follows getAnnotation semantics (null when repeated)");
        Annotation[] direct = rep.getDeclaredAnnotations();
        check(direct.length == 2, "direct view is container plus other annotation");
        check(direct[0] instanceof Tags, "container keeps declaration position");
        check(direct[1] instanceof PlainMark, "other annotation follows");
        check(byType[0].annotationType() == Tag.class, "flattened element type is Tag");
        check(rep.getAnnotationsByType(Tags.class).length == 1, "container byType one");
        check(rep.getAnnotation(Tags.class).toString().contains("Tag"), "container toString mentions Tag");

        // single occurrence is not wrapped in the container
        AnnotatedElement single = Single.class;
        check(single.getAnnotation(Tag.class) != null, "single occurrence directly present");
        check("only".equals(single.getAnnotation(Tag.class).value()), "single value");
        check(single.getDeclaredAnnotation(Tag.class) != null, "single declared annotation");
        check(single.getAnnotationsByType(Tag.class).length == 1, "single byType one");
        check(single.getAnnotation(Tags.class) == null, "no container for single occurrence");
    }

    // inheritance: visible on subclass through get* but never through getDeclared*
    static void testInheritedSemantics() {
        AnnotatedElement child = ChildAnno.class;
        check(child.getAnnotation(InhMark.class) != null, "inherited visible on subclass");
        check(child.isAnnotationPresent(InhMark.class), "inherited isAnnotationPresent");
        check(child.getDeclaredAnnotation(InhMark.class) == null, "inherited not declared on subclass");
        check(child.getAnnotations().length == 1, "subclass getAnnotations includes inherited");
        check(child.getAnnotations()[0] instanceof InhMark, "inherited annotation instance");
        check(child.getDeclaredAnnotations().length == 0, "subclass declared excludes inherited");
        check(child.getAnnotationsByType(InhMark.class).length == 1, "inherited byType one");
        check(child.getDeclaredAnnotationsByType(InhMark.class).length == 0, "declared byType zero");

        AnnotatedElement grand = GrandChildAnno.class;
        check(grand.isAnnotationPresent(InhMark.class), "grandchild still sees inherited");
        check(grand.getDeclaredAnnotation(InhMark.class) == null, "grandchild not declared");

        AnnotatedElement base = BaseAnno.class;
        check(base.getDeclaredAnnotation(InhMark.class) != null, "superclass declares it");
        check(base.getAnnotations().length == 1, "superclass own annotations");

        // subclass with its own annotation keeps both in getAnnotations
        AnnotatedElement mixed = MixedChild.class;
        check(mixed.getAnnotations().length == 2, "own plus inherited in getAnnotations");
        check(mixed.getDeclaredAnnotations().length == 1, "only own in getDeclaredAnnotations");
        check(mixed.getDeclaredAnnotation(PlainMark.class) != null, "own annotation declared");
        check(mixed.getAnnotation(InhMark.class) != null, "inherited still reachable");

        check(Plain.class.getAnnotation(InhMark.class) == null, "unrelated class not affected");
        check(Carriers.class.getAnnotation(InhMark.class) == null, "carrier class not affected");
    }

    // Class.getDeclaredAnnotations keeps source declaration order (stable across JDKs)
    static void testDeclarationOrder() {
        Annotation[] ds = Ordered.class.getDeclaredAnnotations();
        check(ds.length == 3, "three declared annotations");
        check(ds[0].annotationType() == Ord1.class, "first is Ord1");
        check(ds[1].annotationType() == Ord2.class, "second is Ord2");
        check(ds[2].annotationType() == Ord3.class, "third is Ord3");
        Annotation[] all = Ordered.class.getAnnotations();
        check(all.length == 3, "three annotations including inherited set");
        check(all[0].annotationType() == Ord1.class, "getAnnotations preserves order too");
    }

    // the contract is uniform across member kinds
    static void testMemberCarriers() throws Exception {
        AnnotatedElement f = Carriers.class.getField("field");
        check(f.isAnnotationPresent(Tag.class), "field present");
        check("f1".equals(f.getAnnotation(Tag.class).value()), "field annotation value");
        check(f.getDeclaredAnnotation(Tag.class) != null, "field declared annotation");
        check(f.getAnnotationsByType(Tag.class).length == 1, "field byType one");
        check(f.getAnnotations().length == 1, "field one annotation total");

        AnnotatedElement m = Carriers.class.getMethod("method");
        check(m.isAnnotationPresent(Tag.class), "method present");
        check("m1".equals(m.getAnnotation(Tag.class).value()), "method annotation value");
        check(m.getDeclaredAnnotations().length == 1, "method declared one");

        AnnotatedElement ctor = Carriers.class.getDeclaredConstructor();
        check(ctor.getAnnotation(Tag.class) == null, "constructor unannotated null");
        check(ctor.getAnnotations().length == 0, "constructor no annotations");
        check(!ctor.isAnnotationPresent(Tag.class), "constructor not present");
        check(ctor.getDeclaredAnnotationsByType(Tag.class).length == 0, "constructor declared byType zero");

        AnnotatedElement param = Carriers.class.getMethod("withParam", int.class).getParameters()[0];
        check(param.isAnnotationPresent(Tag.class), "parameter present");
        check("p1".equals(param.getAnnotation(Tag.class).value()), "parameter annotation value");
        check(param.getAnnotations().length == 1, "parameter one annotation");
        check(param instanceof Parameter, "carrier is a Parameter");
    }

    // every null Class argument throws NPE on real elements
    static void testNullArguments() throws Exception {
        final AnnotatedElement e = Plain.class;
        checkThrows(NullPointerException.class, () -> e.getAnnotation(null), "getAnnotation(null)");
        checkThrows(NullPointerException.class, () -> e.isAnnotationPresent(null), "isAnnotationPresent(null)");
        checkThrows(NullPointerException.class, () -> e.getAnnotationsByType(null), "getAnnotationsByType(null)");
        checkThrows(NullPointerException.class, () -> e.getDeclaredAnnotation(null), "getDeclaredAnnotation(null)");
        checkThrows(NullPointerException.class, () -> e.getDeclaredAnnotationsByType(null), "getDeclaredAnnotationsByType(null)");
        Field fld = Carriers.class.getField("field");
        checkThrows(NullPointerException.class, () -> fld.getAnnotation(null), "field getAnnotation(null)");
        Method mth = Carriers.class.getMethod("method");
        checkThrows(NullPointerException.class, () -> mth.getAnnotationsByType(null), "method byType(null)");
        Constructor<?> ctor = Carriers.class.getDeclaredConstructor();
        checkThrows(NullPointerException.class, () -> ctor.getDeclaredAnnotation(null), "ctor declared(null)");
        Parameter prm = Carriers.class.getMethod("withParam", int.class).getParameters()[0];
        checkThrows(NullPointerException.class, () -> prm.getAnnotation(null), "parameter getAnnotation(null)");
        checkThrows(NullPointerException.class, () -> prm.isAnnotationPresent(null), "parameter present(null)");
    }

    // absent annotation type: every accessor reports absence, no exceptions
    static void testAbsentPaths() {
        AnnotatedElement p = Plain.class;
        check(p.getAnnotationsByType(Tag.class).length == 0, "plain byType zero");
        check(p.getDeclaredAnnotationsByType(Tag.class).length == 0, "plain declared byType zero");
        check(p.getDeclaredAnnotation(Tag.class) == null, "plain declared single null");
        check(p.getAnnotations().length == 0, "plain no annotations at all");
        check(p.getDeclaredAnnotations().length == 0, "plain no declared annotations");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedElement();
        testRepeatableAnnotations();
        testInheritedSemantics();
        testDeclarationOrder();
        testMemberCarriers();
        testNullArguments();
        testAbsentPaths();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

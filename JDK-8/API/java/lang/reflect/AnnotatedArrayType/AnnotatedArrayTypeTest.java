/*
 * @test
 * @summary java.lang.reflect.AnnotatedArrayType conformance: component/owner on an
 *          array return type, all eight primitive array components, annotations on
 *          the component type, per-dimension annotations on nested arrays (outer
 *          dimension annotation is the leftmost one), generic component carrying a
 *          parameterized annotated type with its own annotations, raw-type
 *          annotation reachable through the component, and method return/parameter
 *          paths.
 * @run main AnnotatedArrayTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class AnnotatedArrayTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures ----

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface A {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface B {
    }

    static class Types {
        public boolean[] bools;
        public byte[] bytes;
        public char[] chars;
        public short[] shorts;
        public int[] ints;
        public long[] longs;
        public float[] floats;
        public double[] doubles;
        public String[] strings;
        public @A String[] annotatedComponent;
        public String @A [] dim2outer;
        public String @A [] @B [] dim2;
        public String @A [] @B [] @A [] dim3;
        public List<@A String>[] genericArgs;
        public @B List<String>[] rawAnnotatedComponent;
        public String[][] nested;
    }

    public static int[] returnsArray() {
        return null;
    }

    public static int @A [] returnsAnnotatedArray() {
        return null;
    }

    static void take(String @B [] ss) {
    }

    static void testAnnotatedArrayType() throws Exception {
        AnnotatedType at = AnnotatedArrayTypeTest.class.getMethod("returnsArray")
                .getAnnotatedReturnType();
        check(at instanceof AnnotatedArrayType, "is AnnotatedArrayType");
        AnnotatedArrayType aat = (AnnotatedArrayType) at;
        check(aat.getType() instanceof Class, "getType is Class");
        check(((Class) aat.getType()).getComponentType() == int.class, "component int");
        check(aat.getAnnotatedGenericComponentType() != null, "getAnnotatedGenericComponentType");
        check(aat.getAnnotations().length == 0, "getAnnotations");
    }

    // every primitive array reports its exact component and no annotations
    static void testPrimitiveComponents() throws Exception {
        Object[][] cases = {
            {"bools", boolean[].class, boolean.class},
            {"bytes", byte[].class, byte.class},
            {"chars", char[].class, char.class},
            {"shorts", short[].class, short.class},
            {"ints", int[].class, int.class},
            {"longs", long[].class, long.class},
            {"floats", float[].class, float.class},
            {"doubles", double[].class, double.class},
        };
        for (Object[] c : cases) {
            Field f = Types.class.getField((String) c[0]);
            AnnotatedType at = f.getAnnotatedType();
            check(at instanceof AnnotatedArrayType, c[0] + " is AnnotatedArrayType");
            AnnotatedArrayType aat = (AnnotatedArrayType) at;
            check(aat.getType() == c[1], c[0] + " exact array class");
            check(aat.getAnnotatedGenericComponentType().getType() == c[2],
                  c[0] + " exact component class");
            check(aat.getAnnotatedGenericComponentType().getAnnotations().length == 0,
                  c[0] + " component unannotated");
            check(aat.getAnnotations().length == 0, c[0] + " array level unannotated");
        }
    }

    // annotation applies to the component type in "@A String[]"
    static void testAnnotatedComponent() throws Exception {
        AnnotatedArrayType aat = (AnnotatedArrayType) Types.class.getField("annotatedComponent")
                .getAnnotatedType();
        check(aat.getAnnotations().length == 0, "array level unannotated");
        AnnotatedType comp = aat.getAnnotatedGenericComponentType();
        check(comp.getType() == String.class, "component String");
        check(comp.getAnnotation(A.class) != null, "component carries annotation");
        check(comp.isAnnotationPresent(A.class), "component present");
        check(comp.getAnnotations().length == 1, "component one annotation");
        check(comp.getAnnotationsByType(A.class).length == 1, "component byType one");
        check(aat.getType() == String[].class, "exact String[] class");
    }

    // per-dimension annotations: leftmost annotation belongs to the outermost array type
    static void testDimensionAnnotations() throws Exception {
        AnnotatedArrayType outer = (AnnotatedArrayType) Types.class.getField("dim2")
                .getAnnotatedType();
        check(outer.getType() == String[][].class, "two-dim exact class");
        check(outer.getAnnotation(A.class) != null, "outer dimension carries A");
        check(outer.getAnnotation(B.class) == null, "outer dimension has no B");
        AnnotatedType inner = outer.getAnnotatedGenericComponentType();
        check(inner instanceof AnnotatedArrayType, "inner still an array annotated type");
        check(inner.getType() == String[].class, "inner exact class");
        check(inner.getAnnotation(B.class) != null, "inner dimension carries B");
        check(inner.getAnnotation(A.class) == null, "inner dimension has no A");
        check(((AnnotatedArrayType) inner).getAnnotatedGenericComponentType().getType()
                == String.class, "innermost component String");
        check(((AnnotatedArrayType) inner).getAnnotatedGenericComponentType().getAnnotations()
                .length == 0, "innermost component unannotated");

        AnnotatedArrayType oneDim = (AnnotatedArrayType) Types.class.getField("dim2outer")
                .getAnnotatedType();
        check(oneDim.getAnnotation(A.class) != null, "single-dim annotation on outer");
        check(oneDim.getAnnotatedGenericComponentType().getAnnotations().length == 0,
              "component without annotation");

        AnnotatedArrayType three = (AnnotatedArrayType) Types.class.getField("dim3")
                .getAnnotatedType();
        check(three.getAnnotation(A.class) != null, "three-dim outer A");
        AnnotatedType l2 = three.getAnnotatedGenericComponentType();
        check(l2.getAnnotation(B.class) != null, "three-dim middle B");
        AnnotatedType l3 = ((AnnotatedArrayType) l2).getAnnotatedGenericComponentType();
        check(l3.getAnnotation(A.class) != null, "three-dim inner A");
        check(((AnnotatedArrayType) l3).getAnnotatedGenericComponentType().getType()
                == String.class, "three-dim innermost String");
    }

    // generic component: List<@A String>[] peels into a parameterized annotated type
    static void testGenericComponent() throws Exception {
        AnnotatedArrayType aat = (AnnotatedArrayType) Types.class.getField("genericArgs")
                .getAnnotatedType();
        AnnotatedType comp = aat.getAnnotatedGenericComponentType();
        check(comp instanceof AnnotatedParameterizedType, "generic component is parameterized");
        check(comp.getType() instanceof ParameterizedType, "component underlying PT");
        check(((ParameterizedType) comp.getType()).getRawType() == List.class, "component raw List");
        AnnotatedType arg = ((AnnotatedParameterizedType) comp).getAnnotatedActualTypeArguments()[0];
        check(arg.getType() == String.class, "component argument String");
        check(arg.isAnnotationPresent(A.class), "component argument annotated");

        // annotation on the raw type of the component is visible on the component itself
        AnnotatedArrayType raw = (AnnotatedArrayType) Types.class.getField("rawAnnotatedComponent")
                .getAnnotatedType();
        check(raw.getAnnotations().length == 0, "array level unannotated for raw case");
        AnnotatedType rawComp = raw.getAnnotatedGenericComponentType();
        check(rawComp instanceof AnnotatedParameterizedType, "raw-annotated component is parameterized");
        check(rawComp.getAnnotation(B.class) != null, "raw-type annotation on component");
        check(rawComp.getAnnotation(A.class) == null, "no other annotation on component");
    }

    // method return and parameter paths behave like fields
    static void testMethodPaths() throws Exception {
        AnnotatedArrayType rt = (AnnotatedArrayType) AnnotatedArrayTypeTest.class
                .getMethod("returnsAnnotatedArray").getAnnotatedReturnType();
        check(rt instanceof AnnotatedArrayType, "return is AnnotatedArrayType");
        check(rt.getType() == int[].class, "return exact int[] class");
        check(rt.getAnnotation(A.class) != null, "return dimension annotated");
        check(rt.getAnnotatedGenericComponentType().getType() == int.class,
              "return component int");

        Method m = AnnotatedArrayTypeTest.class.getDeclaredMethod("take", String[].class);
        AnnotatedArrayType pt = (AnnotatedArrayType) m.getParameters()[0].getAnnotatedType();
        check(pt instanceof AnnotatedArrayType, "parameter is AnnotatedArrayType");
        check(pt.getAnnotation(B.class) != null, "parameter dimension annotated");
        check(pt.getAnnotatedGenericComponentType().getType() == String.class,
              "parameter component String");
        check(pt.getAnnotatedGenericComponentType().getAnnotations().length == 0,
              "parameter component unannotated");
    }

    // unannotated nested arrays peel one dimension per call
    static void testNested() throws Exception {
        AnnotatedArrayType nested = (AnnotatedArrayType) Types.class.getField("nested")
                .getAnnotatedType();
        AnnotatedType inner = nested.getAnnotatedGenericComponentType();
        check(inner instanceof AnnotatedArrayType, "inner is still AnnotatedArrayType");
        check(inner.getType() == String[].class, "inner exact class");
        check(((AnnotatedArrayType) inner).getAnnotatedGenericComponentType().getType()
                == String.class, "innermost component String");
        check(inner.getAnnotations().length == 0, "inner unannotated");
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedArrayType();
        testPrimitiveComponents();
        testAnnotatedComponent();
        testDimensionAnnotations();
        testGenericComponent();
        testMethodPaths();
        testNested();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

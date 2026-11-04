/*
 * @test
 * @summary java.lang.reflect.AnnotatedArrayType conformance: white-box structure + black-box behavior.
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

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sink {
    }

    static class Types {
        public int[] ints;
        public String[] strings;
        public @Sink String[] sunkStrings;
        public String[][] nested;
        public List<String>[] listOfArrays;
    }

    public static int[] returnsArray() {
        return null;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        AnnotatedType at = AnnotatedArrayTypeTest.class.getMethod("returnsArray")
                .getAnnotatedReturnType();
        check(at instanceof AnnotatedArrayType, "is AnnotatedArrayType");
        AnnotatedArrayType aat = (AnnotatedArrayType) at;
        check(aat.getType() instanceof Class, "getType is Class");
        check(((Class<?>) aat.getType()).getComponentType() == int.class, "component int");
        check(aat.getAnnotatedGenericComponentType() != null, "getAnnotatedGenericComponentType");
        check(aat.getAnnotations().length == 0, "getAnnotations");
        check(AnnotatedArrayType.class.isInterface(), "is an interface");
        check(AnnotatedType.class.isAssignableFrom(AnnotatedArrayType.class),
              "extends AnnotatedType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. primitive array component
        AnnotatedArrayType ints = (AnnotatedArrayType) Types.class.getField("ints").getAnnotatedType();
        AnnotatedType comp = ints.getAnnotatedGenericComponentType();
        check(comp.getType() == int.class, "int[] component int");
        check(comp.getAnnotations().length == 0, "unannotated component");

        // 2. TYPE_USE annotation on the component type
        AnnotatedArrayType sunk = (AnnotatedArrayType) Types.class.getField("sunkStrings").getAnnotatedType();
        AnnotatedType scomp = sunk.getAnnotatedGenericComponentType();
        check(scomp.getType() == String.class, "sunk component String");
        check(scomp.getAnnotation(Sink.class) != null, "component carries @Sink");
        check(scomp.isAnnotationPresent(Sink.class), "component present");
        check(sunk.getAnnotations().length == 0, "array type itself unannotated");

        // 3. unannotated reference array
        AnnotatedArrayType strings = (AnnotatedArrayType) Types.class.getField("strings").getAnnotatedType();
        check(strings.getAnnotatedGenericComponentType().getType() == String.class,
              "String[] component");
        check(strings.getAnnotatedGenericComponentType().getAnnotation(Sink.class) == null,
              "no annotation on plain component");
        check(strings.getType() == String[].class, "underlying type String[]");

        // 4. nested arrays peel one dimension per call
        AnnotatedArrayType nested = (AnnotatedArrayType) Types.class.getField("nested").getAnnotatedType();
        AnnotatedType inner = nested.getAnnotatedGenericComponentType();
        check(inner instanceof AnnotatedArrayType, "String[][] inner is still AnnotatedArrayType");
        check(inner.getType() == String[].class, "inner underlying String[]");
        check(((AnnotatedArrayType) inner).getAnnotatedGenericComponentType().getType() == String.class,
              "innermost component String");

        // 5. generic component: component annotated type is a parameterized one
        AnnotatedArrayType gat = (AnnotatedArrayType) Types.class.getField("listOfArrays").getAnnotatedType();
        AnnotatedType gcomp = gat.getAnnotatedGenericComponentType();
        check(gcomp instanceof AnnotatedParameterizedType, "List<String>[] component is APT");
        check(gcomp.getType() instanceof ParameterizedType, "component underlying is PT");
        check(gcomp.getType().getTypeName().equals("java.util.List<java.lang.String>"),
              "component typeName");

        // 6. method return and parameter paths
        AnnotatedArrayType rt = (AnnotatedArrayType) AnnotatedArrayTypeTest.class
                .getMethod("returnsArray").getAnnotatedReturnType();
        check(rt.getAnnotatedGenericComponentType().getType() == int.class, "return component int");
        Method m = AnnotatedArrayTypeTest.class.getDeclaredMethod("take", String[].class);
        AnnotatedArrayType pt = (AnnotatedArrayType) m.getParameters()[0].getAnnotatedType();
        check(pt.getAnnotatedGenericComponentType().getAnnotation(Sink.class) != null,
              "parameter component annotated");

        // 7. owner default returns null for arrays (arrays have no enclosing instance)
        check(ints.getAnnotatedOwnerType() == null, "array annotated owner null");
    }

    static void take(@Sink String[] ss) {
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

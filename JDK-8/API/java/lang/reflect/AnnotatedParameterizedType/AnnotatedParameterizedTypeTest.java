/*
 * @test
 * @summary java.lang.reflect.AnnotatedParameterizedType conformance: actual type
 *          arguments/owner, annotated arguments, annotation on the raw type itself,
 *          raw (non-parameterized) use is NOT an AnnotatedParameterizedType,
 *          independently annotated nested arguments, argument count matching the
 *          underlying ParameterizedType, and method return/parameter paths.
 * @run main AnnotatedParameterizedTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public class AnnotatedParameterizedTypeTest {
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
        public List<String> plain;
        public List<@A String> annotatedArg;
        public @B List<String> rawAnnotated;
        public List rawList;
        public Map<@A String, @B List<@A Integer>> nested;
        public Map<String, Map<String, List<@A String>>> deep;
        public Map.Entry<String, Integer> entry;
    }

    public static List<String> returnsList() {
        return null;
    }

    public static Map<@A String, @B List<Integer>> returnsNestedMap() {
        return null;
    }

    static void take(List<@A String> xs) {
    }

    static void testAnnotatedParameterizedType() throws Exception {
        AnnotatedType at = AnnotatedParameterizedTypeTest.class.getMethod("returnsList")
                .getAnnotatedReturnType();
        check(at instanceof AnnotatedParameterizedType, "is AnnotatedParameterizedType");
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType) at;
        check(apt.getAnnotatedActualTypeArguments().length == 1, "one actual type argument");
        check(apt.getAnnotatedActualTypeArguments()[0].getType() == String.class,
              "type argument String");
        check(apt.getType() instanceof java.lang.reflect.ParameterizedType,
              "getType is ParameterizedType");
    }

    // plain parameterized type: exact underlying ParameterizedType contract
    static void testPlainUnderlying() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                Types.class.getField("plain").getAnnotatedType();
        check(apt.getType() instanceof ParameterizedType, "underlying is ParameterizedType");
        ParameterizedType pt = (ParameterizedType) apt.getType();
        check(pt.getRawType() == List.class, "raw type List");
        check(pt.getActualTypeArguments()[0] == String.class, "underlying argument String");
        check(pt.getOwnerType() == null, "top-level type has no owner");
        AnnotatedType[] args = apt.getAnnotatedActualTypeArguments();
        check(args.length == 1 && args[0].getType() == String.class, "one plain argument");
        check(args[0].getAnnotations().length == 0, "plain argument unannotated");
        check(apt.getAnnotations().length == 0, "outer type unannotated");
        check(apt.getAnnotatedActualTypeArguments().length == pt.getActualTypeArguments().length,
              "argument count matches underlying");
    }

    // annotation on a type argument
    static void testAnnotatedArgument() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                Types.class.getField("annotatedArg").getAnnotatedType();
        AnnotatedType arg = apt.getAnnotatedActualTypeArguments()[0];
        check(arg.getType() == String.class, "annotated argument String");
        check(arg.getAnnotation(A.class) != null, "argument carries A");
        check(arg.isAnnotationPresent(A.class), "argument present");
        check(arg.getAnnotations().length == 1, "argument one annotation");
        check(arg.getAnnotationsByType(A.class).length == 1, "argument byType one");
        check(arg.getDeclaredAnnotation(A.class) != null, "argument declared single");
        check(apt.getAnnotations().length == 0, "outer unannotated when only argument annotated");
    }

    // annotation on the raw type is visible on the parameterized annotated type itself
    static void testRawTypeAnnotation() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                Types.class.getField("rawAnnotated").getAnnotatedType();
        check(apt.getAnnotation(B.class) != null, "raw-type annotation on parameterized type");
        check(apt.isAnnotationPresent(B.class), "raw-type annotation present");
        check(apt.getAnnotations().length == 1, "one annotation at raw level");
        check(apt.getAnnotatedActualTypeArguments()[0].getAnnotations().length == 0,
              "argument unannotated when only raw type annotated");
        check(apt.getType() instanceof ParameterizedType, "underlying still ParameterizedType");
        check(((ParameterizedType) apt.getType()).getRawType() == List.class, "raw List");
    }

    // raw use of a generic type is NOT parameterized at all
    static void testRawListNotParameterized() throws Exception {
        AnnotatedType at = Types.class.getField("rawList").getAnnotatedType();
        check(!(at instanceof AnnotatedParameterizedType), "raw List is not parameterized annotated");
        check(at.getType() == List.class, "raw List underlying is the plain interface");
        check(at.getAnnotations().length == 0, "raw List unannotated");
        check(Types.class.getField("rawList").getGenericType() == List.class,
              "getGenericType agrees raw");
    }

    // independently annotated arguments, including nested parameterized ones
    static void testNestedArguments() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                Types.class.getField("nested").getAnnotatedType();
        AnnotatedType k = apt.getAnnotatedActualTypeArguments()[0];
        AnnotatedType v = apt.getAnnotatedActualTypeArguments()[1];
        check(k.getType() == String.class, "key argument String");
        check(k.isAnnotationPresent(A.class), "key carries A");
        check(v instanceof AnnotatedParameterizedType, "value is nested parameterized");
        check(v.isAnnotationPresent(B.class), "nested value raw level carries B");
        check(v.getAnnotation(A.class) == null, "nested value raw level has no A");
        AnnotatedType inner = ((AnnotatedParameterizedType) v).getAnnotatedActualTypeArguments()[0];
        check(inner.getType() == Integer.class, "innermost argument Integer");
        check(inner.isAnnotationPresent(A.class), "innermost argument carries A");
        check(apt.getAnnotatedActualTypeArguments().length == 2, "two arguments");

        // three levels deep keeps annotations at the bottom
        AnnotatedParameterizedType deep = (AnnotatedParameterizedType)
                Types.class.getField("deep").getAnnotatedType();
        AnnotatedParameterizedType mid = (AnnotatedParameterizedType)
                deep.getAnnotatedActualTypeArguments()[1];
        AnnotatedParameterizedType bottom = (AnnotatedParameterizedType)
                mid.getAnnotatedActualTypeArguments()[1];
        AnnotatedType leaf = bottom.getAnnotatedActualTypeArguments()[0];
        check(leaf.getType() == String.class, "deep leaf String");
        check(leaf.getAnnotation(A.class) != null, "deep leaf annotated");
        check(mid.getAnnotations().length == 0, "middle levels unannotated");
    }

    // owner type through the underlying view (annotated owner accessor is 9+)
    static void testOwnerType() throws Exception {
        AnnotatedParameterizedType entry = (AnnotatedParameterizedType)
                Types.class.getField("entry").getAnnotatedType();
        check(entry.getAnnotatedActualTypeArguments().length == 2, "entry two arguments");
        ParameterizedType underlying = (ParameterizedType) entry.getType();
        check(underlying.getOwnerType() == Map.class, "underlying owner is Map");
        check(underlying.getRawType() == Map.Entry.class, "underlying raw Map.Entry");
        AnnotatedType[] entryArgs = entry.getAnnotatedActualTypeArguments();
        check(entryArgs[0].getType() == String.class && entryArgs[1].getType() == Integer.class,
              "entry argument types");
    }

    // method return and parameter paths
    static void testMethodPaths() throws Exception {
        AnnotatedParameterizedType rt = (AnnotatedParameterizedType)
                AnnotatedParameterizedTypeTest.class.getMethod("returnsNestedMap")
                        .getAnnotatedReturnType();
        check(rt.getAnnotatedActualTypeArguments()[0].isAnnotationPresent(A.class),
              "return key annotated");
        AnnotatedType v = rt.getAnnotatedActualTypeArguments()[1];
        check(v instanceof AnnotatedParameterizedType, "return value nested");
        check(v.isAnnotationPresent(B.class), "return value raw level B");

        Method m = AnnotatedParameterizedTypeTest.class.getDeclaredMethod("take", List.class);
        AnnotatedParameterizedType pt = (AnnotatedParameterizedType)
                m.getParameters()[0].getAnnotatedType();
        AnnotatedType arg = pt.getAnnotatedActualTypeArguments()[0];
        check(arg.getType() == String.class, "parameter argument String");
        check(arg.getAnnotation(A.class) != null, "parameter argument annotated");
    }

    // for every fixture the annotated argument count matches the underlying Type
    static void testArgCountMatchesUnderlying() throws Exception {
        String[] names = {"plain", "annotatedArg", "rawAnnotated", "nested", "deep", "entry"};
        for (String name : names) {
            Field f = Types.class.getField(name);
            AnnotatedParameterizedType a = (AnnotatedParameterizedType) f.getAnnotatedType();
            ParameterizedType underlying = (ParameterizedType) a.getType();
            check(a.getAnnotatedActualTypeArguments().length
                    == underlying.getActualTypeArguments().length,
                  "arg count matches underlying for " + name);
        }
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedParameterizedType();
        testPlainUnderlying();
        testAnnotatedArgument();
        testRawTypeAnnotation();
        testRawListNotParameterized();
        testNestedArguments();
        testOwnerType();
        testMethodPaths();
        testArgCountMatchesUnderlying();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

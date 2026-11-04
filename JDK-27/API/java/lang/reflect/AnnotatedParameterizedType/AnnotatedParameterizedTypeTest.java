/*
 * @test
 * @summary java.lang.reflect.AnnotatedParameterizedType conformance: white-box structure + black-box behavior.
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

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sink {
    }

    static class Types {
        public List<String> plain;
        public List<@Sink String> sunkArg;
        public Map<@Sink String, @Sink Integer> bothArgs;
        public Map<String, List<@Sink Integer>> nested;
        public Map.Entry<String, Integer> entry;
    }

    public static List<String> returnsList() {
        return null;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        AnnotatedType at = AnnotatedParameterizedTypeTest.class.getMethod("returnsList")
                .getAnnotatedReturnType();
        check(at instanceof AnnotatedParameterizedType, "is AnnotatedParameterizedType");
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType) at;
        check(apt.getAnnotatedActualTypeArguments().length == 1, "one actual type argument");
        check(apt.getAnnotatedActualTypeArguments()[0].getType() == String.class,
              "type argument String");
        check(apt.getType() instanceof java.lang.reflect.ParameterizedType,
              "getType is ParameterizedType");
        check(AnnotatedParameterizedType.class.isInterface(), "is an interface");
        check(AnnotatedType.class.isAssignableFrom(AnnotatedParameterizedType.class),
              "extends AnnotatedType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. plain parameterized type: unannotated arguments
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                Types.class.getField("plain").getAnnotatedType();
        AnnotatedType[] args = apt.getAnnotatedActualTypeArguments();
        check(args.length == 1 && args[0].getType() == String.class, "plain arg String");
        check(args[0].getAnnotations().length == 0, "plain arg unannotated");
        check(apt.getType() instanceof ParameterizedType, "underlying is PT");
        check(((ParameterizedType) apt.getType()).getRawType() == List.class, "raw List");

        // 2. annotated type argument
        AnnotatedParameterizedType sunk = (AnnotatedParameterizedType)
                Types.class.getField("sunkArg").getAnnotatedType();
        AnnotatedType sarg = sunk.getAnnotatedActualTypeArguments()[0];
        check(sarg.getType() == String.class, "sunk arg String");
        check(sarg.getAnnotation(Sink.class) != null, "arg carries @Sink");
        check(sarg.isAnnotationPresent(Sink.class), "arg present");
        check(sunk.getAnnotations().length == 0, "outer type unannotated");

        // 3. both arguments annotated independently
        AnnotatedParameterizedType both = (AnnotatedParameterizedType)
                Types.class.getField("bothArgs").getAnnotatedType();
        AnnotatedType k = both.getAnnotatedActualTypeArguments()[0];
        AnnotatedType v = both.getAnnotatedActualTypeArguments()[1];
        check(k.getAnnotation(Sink.class) != null && v.getAnnotation(Sink.class) != null,
              "both args annotated");
        check(k.getType() == String.class && v.getType() == Integer.class, "arg types");

        // 4. nested parameterized argument keeps annotations at depth
        AnnotatedParameterizedType nested = (AnnotatedParameterizedType)
                Types.class.getField("nested").getAnnotatedType();
        AnnotatedType innerList = nested.getAnnotatedActualTypeArguments()[1];
        check(innerList instanceof AnnotatedParameterizedType, "value is nested APT");
        AnnotatedType deepest = ((AnnotatedParameterizedType) innerList)
                .getAnnotatedActualTypeArguments()[0];
        check(deepest.getType() == Integer.class, "innermost arg Integer");
        check(deepest.getAnnotation(Sink.class) != null, "innermost carries @Sink");

        // 5. owner type through annotated view
        AnnotatedParameterizedType entry = (AnnotatedParameterizedType)
                Types.class.getField("entry").getAnnotatedType();
        AnnotatedType owner = entry.getAnnotatedOwnerType();
        check(owner != null && owner.getType() == Map.class, "Map.Entry owner via APT");
        check(entry.getAnnotatedActualTypeArguments().length == 2, "entry two args");

        // 6. method parameter path
        Method m = AnnotatedParameterizedTypeTest.class.getDeclaredMethod("take", List.class);
        AnnotatedParameterizedType papt = (AnnotatedParameterizedType)
                m.getParameters()[0].getAnnotatedType();
        AnnotatedType parg = papt.getAnnotatedActualTypeArguments()[0];
        check(parg.getType() == String.class, "param arg String");
        check(parg.getAnnotation(Sink.class) != null, "param arg annotated");

        // 7. argument count matches underlying PT for every fixture
        for (String name : new String[]{"plain", "sunkArg", "bothArgs", "nested", "entry"}) {
            Field f = Types.class.getField(name);
            AnnotatedParameterizedType a = (AnnotatedParameterizedType) f.getAnnotatedType();
            ParameterizedType underlying = (ParameterizedType) a.getType();
            check(a.getAnnotatedActualTypeArguments().length == underlying.getActualTypeArguments().length,
                  "arg count matches underlying for " + name);
        }
    }

    static void take(List<@Sink String> xs) {
    }

    // ---- Part 3: raw-type annotation, raw use, distinct nested annotations ----

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Mark {
    }

    static class MoreTypes {
        public @Mark List<String> rawAnnotated;
        public List rawList;
        public Map<@Sink String, @Mark List<@Sink Integer>> distinct;
    }

    public static Map<@Sink String, @Mark List<Integer>> returnsAnnotated() {
        return null;
    }

    // annotation on the raw type sits on the parameterized annotated type itself
    static void testRawTypeAnnotation() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                MoreTypes.class.getField("rawAnnotated").getAnnotatedType();
        check(apt.getAnnotation(Mark.class) != null, "raw-type annotation present");
        check(apt.isAnnotationPresent(Mark.class), "raw-type isAnnotationPresent");
        check(apt.getAnnotations().length == 1, "one annotation at raw level");
        check(apt.getAnnotatedActualTypeArguments()[0].getAnnotations().length == 0,
              "argument clean when only raw annotated");
        check(((ParameterizedType) apt.getType()).getRawType() == List.class, "raw List");
    }

    // raw use of a generic type is not parameterized at all
    static void testRawListNotParameterized() throws Exception {
        Field f = MoreTypes.class.getField("rawList");
        AnnotatedType at = f.getAnnotatedType();
        check(!(at instanceof AnnotatedParameterizedType), "raw List not parameterized");
        check(at.getType() == List.class, "raw List plain interface");
        check(f.getGenericType() == List.class, "getGenericType agrees raw");
    }

    // distinct annotation types at nested levels
    static void testDistinctNestedAnnotations() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                MoreTypes.class.getField("distinct").getAnnotatedType();
        AnnotatedType k = apt.getAnnotatedActualTypeArguments()[0];
        AnnotatedType v = apt.getAnnotatedActualTypeArguments()[1];
        check(k.isAnnotationPresent(Sink.class), "key carries Sink");
        check(!k.isAnnotationPresent(Mark.class), "key has no Mark");
        check(v instanceof AnnotatedParameterizedType, "value nested parameterized");
        check(v.isAnnotationPresent(Mark.class), "value raw level Mark");
        check(!v.isAnnotationPresent(Sink.class), "value raw level no Sink");
        AnnotatedType inner = ((AnnotatedParameterizedType) v).getAnnotatedActualTypeArguments()[0];
        check(inner.getType() == Integer.class, "innermost Integer");
        check(inner.isAnnotationPresent(Sink.class), "innermost Sink");
        check(!inner.isAnnotationPresent(Mark.class), "innermost no Mark");
    }

    // annotated method return path
    static void testAnnotatedReturnPath() throws Exception {
        AnnotatedParameterizedType rt = (AnnotatedParameterizedType)
                AnnotatedParameterizedTypeTest.class.getMethod("returnsAnnotated")
                        .getAnnotatedReturnType();
        check(rt.getAnnotatedActualTypeArguments()[0].isAnnotationPresent(Sink.class),
              "return key annotated");
        AnnotatedType v = rt.getAnnotatedActualTypeArguments()[1];
        check(v instanceof AnnotatedParameterizedType, "return value parameterized");
        check(v.isAnnotationPresent(Mark.class), "return value raw Mark");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRawTypeAnnotation();
        testRawListNotParameterized();
        testDistinctNestedAnnotations();
        testAnnotatedReturnPath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.reflect.AnnotatedWildcardType conformance: white-box structure + black-box behavior.
 * @run main AnnotatedWildcardTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Method;
import java.util.List;

public class AnnotatedWildcardTypeTest {
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
        public List<? extends @Sink Number> up;
        public List<? super @Sink Integer> down;
        public List<?> unbounded;
        public java.util.Map<? extends @Sink String, ? super @Sink Long> both;
    }

    public static void wildcard(List<? extends Number> l) {
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Method m = AnnotatedWildcardTypeTest.class.getMethod("wildcard", List.class);
        AnnotatedType at = m.getParameters()[0].getAnnotatedType();
        check(at instanceof AnnotatedParameterizedType, "parameter is parameterized");
        AnnotatedType arg =
                ((AnnotatedParameterizedType) at).getAnnotatedActualTypeArguments()[0];
        check(arg instanceof AnnotatedWildcardType, "argument is AnnotatedWildcardType");
        AnnotatedWildcardType awt = (AnnotatedWildcardType) arg;
        check(awt.getAnnotatedUpperBounds().length == 1, "one annotated upper bound");
        check(awt.getAnnotatedUpperBounds()[0].getType() == Number.class, "upper bound Number");
        check(awt.getAnnotatedLowerBounds().length == 0, "no lower bounds");
        check(AnnotatedWildcardType.class.isInterface(), "is an interface");
        check(AnnotatedType.class.isAssignableFrom(AnnotatedWildcardType.class),
              "extends AnnotatedType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. upper-bounded wildcard with annotation on the bound
        AnnotatedWildcardType up = wildcardArg("up");
        AnnotatedType[] ups = up.getAnnotatedUpperBounds();
        check(ups.length == 1 && ups[0].getType() == Number.class, "up bound Number");
        check(ups[0].getAnnotation(Sink.class) != null, "up bound carries @Sink");
        check(up.getAnnotatedLowerBounds().length == 0, "up no lower bounds");

        // 2. lower-bounded wildcard with annotation on the bound
        AnnotatedWildcardType down = wildcardArg("down");
        AnnotatedType[] downs = down.getAnnotatedLowerBounds();
        check(downs.length == 1 && downs[0].getType() == Integer.class, "down bound Integer");
        check(downs[0].getAnnotation(Sink.class) != null, "down bound carries @Sink");
        AnnotatedType[] downUps = down.getAnnotatedUpperBounds();
        check(downUps.length == 1 && downUps[0].getType() == Object.class,
              "down implicit Object upper");
        check(downUps[0].getAnnotations().length == 0, "implicit upper unannotated");

        // 3. unbounded wildcard
        AnnotatedWildcardType un = wildcardArg("unbounded");
        check(un.getAnnotatedUpperBounds().length == 1
              && un.getAnnotatedUpperBounds()[0].getType() == Object.class,
              "unbounded implicit Object");
        check(un.getAnnotatedLowerBounds().length == 0, "unbounded no lower");
        check(un.getAnnotatedUpperBounds()[0].getAnnotation(Sink.class) == null,
              "unbounded bound unannotated");

        // 4. both wildcards annotated in one parameterized type
        java.lang.reflect.Field f = Types.class.getField("both");
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType) f.getAnnotatedType();
        AnnotatedWildcardType k = (AnnotatedWildcardType) apt.getAnnotatedActualTypeArguments()[0];
        AnnotatedWildcardType v = (AnnotatedWildcardType) apt.getAnnotatedActualTypeArguments()[1];
        check(k.getAnnotatedUpperBounds()[0].getAnnotation(Sink.class) != null,
              "key upper annotated");
        check(v.getAnnotatedLowerBounds()[0].getAnnotation(Sink.class) != null,
              "value lower annotated");
        check(k.getType().getTypeName() != null && v.getType().getTypeName() != null, "underlying typeNames non-null");

        // 5. use-site from a method signature behaves like the field path
        Method m = AnnotatedWildcardTypeTest.class.getDeclaredMethod("take", List.class);
        AnnotatedWildcardType mp = (AnnotatedWildcardType)
                ((AnnotatedParameterizedType) m.getParameters()[0].getAnnotatedType())
                        .getAnnotatedActualTypeArguments()[0];
        check(mp.getAnnotatedLowerBounds().length == 1
              && mp.getAnnotatedLowerBounds()[0].getType() == Long.class,
              "method ? super Long bound");
        check(mp.getAnnotatedLowerBounds()[0].getAnnotation(Sink.class) != null,
              "method lower annotated");

        // 6. the wildcard annotated type itself is not annotated at its own level
        check(up.getAnnotations().length == 0, "wildcard itself unannotated");
        check(up.getAnnotatedOwnerType() == null, "wildcard owner null");
    }

    static AnnotatedWildcardType wildcardArg(String fieldName) throws Exception {
        java.lang.reflect.Field f = Types.class.getField(fieldName);
        return (AnnotatedWildcardType)
                ((AnnotatedParameterizedType) f.getAnnotatedType())
                        .getAnnotatedActualTypeArguments()[0];
    }

    static void take(List<? super @Sink Long> xs) {
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

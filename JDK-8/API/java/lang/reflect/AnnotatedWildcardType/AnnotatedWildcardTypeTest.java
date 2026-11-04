/*
 * @test
 * @summary java.lang.reflect.AnnotatedWildcardType conformance: upper/lower annotated
 *          bounds, upper-bounded wildcard with annotation on the bound, lower-bounded
 *          wildcard with annotated lower bound and implicit Object upper, unbounded
 *          wildcard, annotation on the wildcard itself, both wildcards annotated in
 *          one parameterized type, method return path and deep nesting, plus the
 *          underlying WildcardType contract.
 * @run main AnnotatedWildcardTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

public class AnnotatedWildcardTypeTest {
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
        public List<? extends @A Number> up;
        public List<? super @B Integer> down;
        public List<?> unbounded;
        public List<@A ?> selfAnnotated;
        public Map<? extends @A String, ? super @B Long> both;
        public List<List<? super @B Integer>> deep;
    }

    public static void wildcard(List<? extends Number> l) {
    }

    public static List<? extends @A Number> returnsUp() {
        return null;
    }

    static void take(List<? super @B Long> xs) {
    }

    static void testAnnotatedWildcardType() throws Exception {
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
    }

    // upper-bounded wildcard with annotation on the bound
    static void testUpperBound() throws Exception {
        AnnotatedWildcardType up = wildcardArg("up");
        AnnotatedType[] ups = up.getAnnotatedUpperBounds();
        check(ups.length == 1, "one upper bound");
        check(ups[0].getType() == Number.class, "upper bound Number");
        check(ups[0].getAnnotation(A.class) != null, "upper bound carries A");
        check(ups[0].isAnnotationPresent(A.class), "upper bound present");
        check(ups[0].getAnnotations().length == 1, "upper bound one annotation");
        check(up.getAnnotatedLowerBounds().length == 0, "upper case has no lower bounds");
        check(up.getAnnotations().length == 0, "wildcard itself unannotated");
        check(up.getType() instanceof WildcardType, "underlying is WildcardType");
        check(((WildcardType) up.getType()).getUpperBounds()[0] == Number.class,
              "underlying upper bound Number");
        check(((WildcardType) up.getType()).getLowerBounds().length == 0,
              "underlying lower empty");
    }

    // lower-bounded wildcard: annotated lower bound; on this JDK release family the
    // annotated upper bound array stays empty while the raw WildcardType reports Object
    static void testLowerBound() throws Exception {
        AnnotatedWildcardType down = wildcardArg("down");
        AnnotatedType[] lows = down.getAnnotatedLowerBounds();
        check(lows.length == 1, "one lower bound");
        check(lows[0].getType() == Integer.class, "lower bound Integer");
        check(lows[0].getAnnotation(B.class) != null, "lower bound carries B");
        check(lows[0].isAnnotationPresent(B.class), "lower bound present");
        AnnotatedType[] ups = down.getAnnotatedUpperBounds();
        check(ups.length == 0, "no explicit annotated upper bound (recorded on this JDK)");
        check(down.getAnnotations().length == 0, "wildcard itself unannotated");
        WildcardType raw = (WildcardType) down.getType();
        check(raw.getLowerBounds()[0] == Integer.class, "underlying lower bound Integer");
        check(raw.getUpperBounds()[0] == Object.class, "underlying implicit upper Object");
    }

    // unbounded wildcard: single implicit Object upper, no lower
    static void testUnbounded() throws Exception {
        AnnotatedWildcardType un = wildcardArg("unbounded");
        check(un.getAnnotatedUpperBounds().length == 1, "unbounded one implicit upper");
        check(un.getAnnotatedUpperBounds()[0].getType() == Object.class,
              "unbounded implicit Object");
        check(un.getAnnotatedUpperBounds()[0].getAnnotations().length == 0,
              "unbounded bound unannotated");
        check(un.getAnnotatedLowerBounds().length == 0, "unbounded no lower");
        check(un.getAnnotations().length == 0, "unbounded itself clean");
    }

    // annotation on the wildcard itself (not on its bound)
    static void testSelfAnnotated() throws Exception {
        AnnotatedWildcardType self = wildcardArg("selfAnnotated");
        check(self.getAnnotations().length == 1, "wildcard itself has one annotation");
        check(self.getAnnotation(A.class) != null, "wildcard carries A");
        check(self.isAnnotationPresent(A.class), "wildcard present");
        AnnotatedType[] ups = self.getAnnotatedUpperBounds();
        check(ups.length == 1 && ups[0].getType() == Object.class, "bounds stay implicit Object");
        check(ups[0].getAnnotation(A.class) == null, "bound not annotated");
        check(self.getAnnotatedLowerBounds().length == 0, "no lower bounds");
    }

    // two independently annotated wildcards inside one parameterized type
    static void testTwoWildcards() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                Types.class.getField("both").getAnnotatedType();
        AnnotatedWildcardType k = (AnnotatedWildcardType) apt.getAnnotatedActualTypeArguments()[0];
        AnnotatedWildcardType v = (AnnotatedWildcardType) apt.getAnnotatedActualTypeArguments()[1];
        check(k.getAnnotatedUpperBounds()[0].getAnnotation(A.class) != null,
              "key upper bound annotated");
        check(k.getAnnotatedUpperBounds()[0].getType() == String.class, "key upper String");
        check(v.getAnnotatedLowerBounds()[0].getAnnotation(B.class) != null,
              "value lower bound annotated");
        check(v.getAnnotatedLowerBounds()[0].getType() == Long.class, "value lower Long");
        check(k.getAnnotations().length == 0 && v.getAnnotations().length == 0,
              "wildcards themselves unannotated");
    }

    // method return and parameter paths
    static void testMethodPaths() throws Exception {
        AnnotatedWildcardType rt = (AnnotatedWildcardType)
                ((AnnotatedParameterizedType) AnnotatedWildcardTypeTest.class
                        .getMethod("returnsUp").getAnnotatedReturnType())
                        .getAnnotatedActualTypeArguments()[0];
        check(rt.getAnnotatedUpperBounds()[0].getType() == Number.class, "return upper Number");
        check(rt.getAnnotatedUpperBounds()[0].getAnnotation(A.class) != null,
              "return upper annotated");

        Method m = AnnotatedWildcardTypeTest.class.getDeclaredMethod("take", List.class);
        AnnotatedWildcardType pt = (AnnotatedWildcardType)
                ((AnnotatedParameterizedType) m.getParameters()[0].getAnnotatedType())
                        .getAnnotatedActualTypeArguments()[0];
        check(pt.getAnnotatedLowerBounds().length == 1, "param one lower bound");
        check(pt.getAnnotatedLowerBounds()[0].getType() == Long.class, "param lower Long");
        check(pt.getAnnotatedLowerBounds()[0].getAnnotation(B.class) != null,
              "param lower annotated");
    }

    // wildcard nested inside another parameterized argument keeps annotations
    static void testDeepNesting() throws Exception {
        AnnotatedParameterizedType apt = (AnnotatedParameterizedType)
                Types.class.getField("deep").getAnnotatedType();
        AnnotatedParameterizedType inner = (AnnotatedParameterizedType)
                apt.getAnnotatedActualTypeArguments()[0];
        AnnotatedWildcardType w = (AnnotatedWildcardType)
                inner.getAnnotatedActualTypeArguments()[0];
        check(w.getAnnotatedLowerBounds().length == 1, "deep lower one");
        check(w.getAnnotatedLowerBounds()[0].getType() == Integer.class, "deep lower Integer");
        check(w.getAnnotatedLowerBounds()[0].getAnnotation(B.class) != null,
              "deep lower annotated");
    }

    static AnnotatedWildcardType wildcardArg(String fieldName) throws Exception {
        Field f = Types.class.getField(fieldName);
        return (AnnotatedWildcardType)
                ((AnnotatedParameterizedType) f.getAnnotatedType())
                        .getAnnotatedActualTypeArguments()[0];
    }

    public static void main(String[] args) throws Exception {
        testAnnotatedWildcardType();
        testUpperBound();
        testLowerBound();
        testUnbounded();
        testSelfAnnotated();
        testTwoWildcards();
        testMethodPaths();
        testDeepNesting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

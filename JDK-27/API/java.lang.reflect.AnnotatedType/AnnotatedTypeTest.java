/*
 * @test
 * @summary java.lang.reflect.AnnotatedType conformance: white-box structure + black-box behavior.
 * @run main AnnotatedTypeTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

public class AnnotatedTypeTest {
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
        public String plain;
        public @Sink String sunk;
        public List<String> list;
        public Map.Entry<String, Integer> entry;
    }

    public static String returnsString() {
        return null;
    }

    public static @Sink String returnsSunk() {
        return null;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        AnnotatedType at = AnnotatedTypeTest.class.getMethod("returnsString")
                .getAnnotatedReturnType();
        check(at != null, "getAnnotatedReturnType non-null");
        check(at.getType() == String.class, "getType == String");
        check(at.getAnnotations().length == 0, "getAnnotations");
        check(at.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations");
        check(AnnotatedType.class.isInterface(), "is an interface");
        check(java.lang.reflect.AnnotatedElement.class.isAssignableFrom(AnnotatedType.class),
              "extends AnnotatedElement");
        check(!java.lang.reflect.Type.class.isAssignableFrom(AnnotatedType.class),
              "AnnotatedType wraps (not extends) Type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. plain types carry no annotations but expose their underlying Type
        AnnotatedType plain = Types.class.getField("plain").getAnnotatedType();
        check(plain.getType() == String.class, "plain field underlying type");
        check(plain.getAnnotations().length == 0 && plain.getDeclaredAnnotations().length == 0,
              "plain no annotations");
        check(plain.getAnnotation(Sink.class) == null, "plain getAnnotation null");
        check(!plain.isAnnotationPresent(Sink.class), "plain not present");

        // 2. TYPE_USE annotations are visible through the contract
        AnnotatedType sunk = Types.class.getField("sunk").getAnnotatedType();
        check(sunk.getType() == String.class, "sunk underlying type");
        check(sunk.getAnnotation(Sink.class) != null, "sunk getAnnotation");
        check(sunk.isAnnotationPresent(Sink.class), "sunk isAnnotationPresent");
        check(sunk.getAnnotations().length == 1, "sunk one annotation");
        check(sunk.getDeclaredAnnotation(Sink.class) != null, "sunk declared annotation");

        // 3. method return types behave identically
        AnnotatedType rt = AnnotatedTypeTest.class.getMethod("returnsSunk").getAnnotatedReturnType();
        check(rt.getAnnotation(Sink.class) != null, "return type annotated");
        check(AnnotatedTypeTest.class.getMethod("returnsString").getAnnotatedReturnType()
              .getAnnotations().length == 0, "plain return type empty");
        check(rt.getType() == String.class, "return type underlying");

        // 4. owner type of an inner class annotated type
        AnnotatedType entryAt = Types.class.getField("entry").getAnnotatedType();
        AnnotatedType owner = entryAt.getAnnotatedOwnerType();
        check(owner != null && owner.getType() == Map.class, "Map.Entry owner is Map");
        check(owner.getAnnotations().length == 0, "owner unannotated");
        AnnotatedType plainAt = Types.class.getField("plain").getAnnotatedType();
        check(plainAt.getAnnotatedOwnerType() == null, "top-level type has no owner");

        // 5. parameterized underlying type stays fully generic
        AnnotatedType listAt = Types.class.getField("list").getAnnotatedType();
        check(listAt.getType() instanceof ParameterizedType, "List field underlying is PT");
        check(listAt.getType() == Types.class.getField("list").getGenericType()
              || listAt.getType().equals(Types.class.getField("list").getGenericType()),
              "underlying equals getGenericType");

        // 6. parameter annotated types
        Method m = AnnotatedTypeTest.class.getDeclaredMethod("consume", String.class);
        AnnotatedType pt = m.getParameters()[0].getAnnotatedType();
        check(pt.getType() == String.class, "parameter underlying type");
        check(pt.getAnnotation(Sink.class) != null, "parameter annotation visible");

        // 7. inherited default methods on the interface itself
        check(listAt.getAnnotationsByType(Sink.class).length == 0, "byType empty for unannotated");
        check(listAt.getDeclaredAnnotationsByType(Sink.class).length == 0,
              "declared-byType empty");
        boolean npe = false;
        try { listAt.getAnnotation(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getAnnotation(null) NPE");
    }

    static void consume(@Sink String s) {
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

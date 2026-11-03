/*
 * @test
 * @summary java.lang.reflect.RecordComponent conformance: white-box structure + black-box behavior.
 * @run main RecordComponentTest
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;

public class RecordComponentTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    @Target(ElementType.RECORD_COMPONENT)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Comp {
        String v();
    }

    record Point(int x, int y) {
    }

    record Labeled(int id, @Comp(v = "tagged") @Deprecated String label) {
    }

    record Box<T>(T payload, java.util.List<String> items) {
    }

    record Empty() {
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        RecordComponent[] rcs = Point.class.getRecordComponents();
        check(rcs.length == 2, "two components");
        check("x".equals(rcs[0].getName()) && "y".equals(rcs[1].getName()), "names");
        check(rcs[0].getType() == int.class && rcs[1].getType() == int.class, "types");
        check("x".equals(rcs[0].getAccessor().getName()), "accessor");
        check(rcs[0].getDeclaringRecord() == Point.class, "declaringRecord");
        check(rcs[0].getGenericType() == int.class, "genericType");
        check(rcs[0].getAnnotation(Deprecated.class) == null, "no Deprecated annotation");
        check(RecordComponent.class.isAssignableFrom(RecordComponent.class)
              && Point.class.isRecord(), "Point is a record");
        check(!Empty.class.isRecord() || Empty.class.getRecordComponents().length == 0,
              "empty record has no components");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. declaration order, types, and live accessor invocation
        RecordComponent[] rcs = Point.class.getRecordComponents();
        Point p = new Point(3, 4);
        check(rcs[0].getAccessor().invoke(p).equals(3), "accessor x returns value");
        check(rcs[1].getAccessor().invoke(p).equals(4), "accessor y returns value");
        check(rcs[0].getAccessor().getReturnType() == int.class, "accessor return type");
        check(rcs[0].getAccessor().getDeclaringClass() == Point.class, "accessor declared by record");
        check(rcs[0].getAccessor().getParameterCount() == 0, "accessor no params");
        check(rcs[0].toString().contains("x"), "toString contains name");
        check(rcs[0].getGenericSignature() == null, "non-generic component null signature");

        // 2. component-retained vs propagated annotations
        RecordComponent[] lrcs = Labeled.class.getRecordComponents();
        Comp tag = lrcs[1].getAnnotation(Comp.class);
        check(tag != null && "tagged".equals(tag.v()), "RECORD_COMPONENT annotation retained");
        check(lrcs[1].getAnnotations().length == 1, "only component-targeted kept on component");
        Method acc = lrcs[1].getAccessor();
        check(acc.getAnnotation(Deprecated.class) != null,
              "@Deprecated propagated to accessor");
        check(acc.getAnnotation(Comp.class) == null, "component-only not propagated");
        check(lrcs[0].getAnnotation(Comp.class) == null, "unannotated component null");
        check(lrcs[0].isAnnotationPresent(Comp.class) == false, "isAnnotationPresent false");
        check(lrcs[1].getDeclaringRecord() == Labeled.class, "declaring record");

        // 3. generic record components
        RecordComponent[] brcs = Box.class.getRecordComponents();
        check(brcs[0].getGenericType().getTypeName().equals("T"), "type variable component");
        check("TT;".equals(brcs[0].getGenericSignature()), "generic signature TT;");
        check(brcs[0].getType() == Object.class, "erased component type Object");
        check(brcs[1].getGenericType() instanceof ParameterizedType, "List component is PT");
        check(brcs[1].getGenericType().getTypeName().equals("java.util.List<java.lang.String>"),
              "List component typeName");
        check(brcs[0].getAnnotatedType().getType() instanceof java.lang.reflect.TypeVariable,
              "annotated type keeps type variable");
        Box<String> box = new Box<>("pay", java.util.List.of("a", "b"));
        check(box.payload().equals("pay") && brcs[0].getAccessor().invoke(box).equals("pay"),
              "generic accessor round-trip");
        check(brcs[1].getAccessor().invoke(box).equals(java.util.List.of("a", "b")),
              "list accessor round-trip");

        // 4. empty record and non-record classes
        check(Empty.class.getRecordComponents().length == 0, "empty record zero components");
        check(!RecordComponentTest.class.isRecord(), "plain class not record");
        check(RecordComponentTest.class.getRecordComponents() == null,
              "non-record getRecordComponents null");

        // 5. component equality proxies: repeated lookups agree
        RecordComponent x1 = Point.class.getRecordComponents()[0];
        RecordComponent x2 = Point.class.getRecordComponents()[0];
        check(x1.getName().equals(x2.getName()) && x1.getType() == x2.getType()
              && x1.getAccessor().equals(x2.getAccessor()), "repeated lookups agree");
        check(!x1.getName().equals(Point.class.getRecordComponents()[1].getName()),
              "components ordered distinctly");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

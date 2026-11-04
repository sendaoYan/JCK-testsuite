/*
 * @test
 * @summary java.lang.reflect.RecordComponent conformance: white-box structure + black-box behavior.
 *          Deepened: explicit vs implicit accessor dispatch, long-form canonical ctor,
 *          toString shape, TYPE_USE annotated types, parameterized deep dive.
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

    // ---- Part 3: contract deepening (2026-11-05); behaviors probed on JDK 27.0.1 ----
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Use {
        int id();
    }

    enum EnumLike {
        X
    }

    interface PlainIfc {
    }

    abstract static class AbstractLike {
    }

    @interface Marker {
    }

    record Implicit(int v) {
    }

    record Explicit(int v) {
        public int v() { return v * 2; }
    }

    record Canonical(int v) {
        Canonical(int v) { this.v = v; }
    }

    record Mixed(int x, String y) {
    }

    record Typed(@Use(id = 7) int a) {
    }

    static void testExplicitAccessor() throws Exception {
        // record declaring its own accessor: getAccessor() must return that very method
        RecordComponent rc = Explicit.class.getRecordComponents()[0];
        Method explicit = Explicit.class.getMethod("v");
        check(rc.getAccessor().equals(explicit), "getAccessor returns the explicit accessor");
        check(rc.getAccessor().invoke(new Explicit(21)).equals(42),
              "accessor dispatches to the explicit body (21 -> 42)");
        check(rc.getAccessor().getDeclaringClass() == Explicit.class, "declared by the record");
        // record without an explicit accessor: getAccessor() hits the implicit one
        RecordComponent ric = Implicit.class.getRecordComponents()[0];
        check(ric.getAccessor().getName().equals("v"), "implicit accessor named v");
        check(ric.getAccessor().invoke(new Implicit(7)).equals(7), "implicit accessor round-trip");
        check(ric.getAccessor().equals(Implicit.class.getMethod("v")), "implicit accessor resolves to Method");
    }

    static void testAccessorFlagSurface() throws Exception {
        Method acc = Implicit.class.getRecordComponents()[0].getAccessor();
        check(acc.accessFlags().equals(java.util.EnumSet.of(java.lang.reflect.AccessFlag.PUBLIC)),
              "implicit accessor flags exactly PUBLIC");
        check(java.lang.reflect.Modifier.isPublic(acc.getModifiers())
              && !java.lang.reflect.Modifier.isFinal(acc.getModifiers()),
              "accessor is public and not final");
        check(acc.getParameterCount() == 0 && acc.getReturnType() == int.class, "accessor shape 0-arg int");
    }

    static void testCanonicalCtor() throws Exception {
        // long-form canonical constructor keeps the component list identical
        RecordComponent[] rcs = Canonical.class.getRecordComponents();
        check(rcs.length == 1 && "v".equals(rcs[0].getName()), "long-form canonical ctor keeps component");
        check(Canonical.class.getDeclaredConstructors().length == 1, "no extra ctor synthesized");
        java.lang.reflect.Constructor<?> ctor = Canonical.class.getDeclaredConstructor(int.class);
        check(ctor.getParameterCount() == 1, "canonical ctor signature exact");
        Object made = ctor.newInstance(5);
        check(made instanceof Canonical && ((Canonical) made).v() == 5,
              "canonical ctor instantiates working record");
        check(rcs[0].getAccessor().invoke(made).equals(5), "component reads through after long-form ctor");
    }

    static void testToStringShape() {
        check(Implicit.class.getRecordComponents()[0].toString().equals("int v"),
              "toString 'int v'");
        check(Mixed.class.getRecordComponents()[1].toString().equals("java.lang.String y"),
              "toString 'java.lang.String y'");
        check(Box.class.getRecordComponents()[1].toString().equals("java.util.List items"),
              "toString uses the erased declared type");
        check(Box.class.getRecordComponents()[0].toString().equals("java.lang.Object payload"),
              "toString uses erased type variable bound");
    }

    static void testTypeUseAnnotation() {
        // TYPE_USE annotations live on getAnnotatedType(), not on component declarations
        RecordComponent rc = Typed.class.getRecordComponents()[0];
        check(rc.getAnnotatedType().isAnnotationPresent(Use.class), "TYPE_USE visible on annotated type");
        check(rc.getAnnotatedType().getAnnotation(Use.class).id() == 7, "TYPE_USE value round-trip");
        check(rc.getAnnotatedType().getType() == int.class, "annotated type carries int");
        check(rc.getAnnotations().length == 0, "TYPE_USE-only not on component declarations");
        check(rc.isAnnotationPresent(Use.class) == false, "isAnnotationPresent false for TYPE_USE-only");
    }

    static void testMixedOrder() throws Exception {
        RecordComponent[] rcs = Mixed.class.getRecordComponents();
        check(rcs.length == 2, "two components");
        check(rcs[0].getName().equals("x") && rcs[1].getName().equals("y"), "declaration order x,y");
        check(rcs[0].getType() == int.class && rcs[1].getType() == String.class, "raw types");
        check(rcs[0].getGenericType() == int.class && rcs[1].getGenericType() == String.class,
              "non-generic component: genericType == type");
        check(rcs[0].getGenericSignature() == null && rcs[1].getGenericSignature() == null,
              "null signatures for non-generic components");
        Mixed m = new Mixed(1, "s");
        check(rcs[0].getAccessor().invoke(m).equals(1), "x accessor round-trip");
        check(rcs[1].getAccessor().invoke(m).equals("s"), "y accessor round-trip");
        check(rcs[0].getDeclaringRecord() == Mixed.class && rcs[1].getDeclaringRecord() == Mixed.class,
              "declaring record both");
    }

    static void testNonRecordVariety() {
        Class<?>[] notRecords = {EnumLike.class, PlainIfc.class, AbstractLike.class,
              Marker.class, int.class, int[].class, RecordComponentTest.class, String.class};
        for (Class<?> k : notRecords) {
            check(!k.isRecord(), "not a record: " + k);
            check(k.getRecordComponents() == null, "getRecordComponents null: " + k);
        }
        check(Mixed.class.isRecord() && Mixed.class.getSuperclass() == java.lang.Record.class,
              "record extends java.lang.Record");
        check(Empty.class.getSuperclass() == java.lang.Record.class, "empty record extends Record too");
        check(Point.class.isRecord() && Point.class.getRecordComponents() != null
              && Point.class.getRecordComponents().length == 2,
              "isRecord true implies non-null component list");
    }

    static void testParameterizedDeep() {
        // List<String> component: full ParameterizedType contract
        RecordComponent items = Box.class.getRecordComponents()[1];
        java.lang.reflect.Type gt = items.getGenericType();
        check(gt instanceof ParameterizedType, "items generic type is ParameterizedType");
        ParameterizedType pt = (ParameterizedType) gt;
        check(pt.getRawType() == java.util.List.class, "raw List");
        check(pt.getActualTypeArguments().length == 1
              && pt.getActualTypeArguments()[0] == String.class, "actual type argument String");
        check(pt.getOwnerType() == null, "no owner type");
        check(items.getType() == java.util.List.class, "erased type List");
        // T component: TypeVariable declared by the record itself
        java.lang.reflect.TypeVariable<?> tv =
              (java.lang.reflect.TypeVariable<?>) Box.class.getRecordComponents()[0].getGenericType();
        check(tv.getName().equals("T"), "payload type variable named T");
        check(tv.getGenericDeclaration() == Box.class, "T declared by the record itself");
        check(tv.getBounds().length == 1 && tv.getBounds()[0] == Object.class, "implicit bound Object");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExplicitAccessor();
        testAccessorFlagSurface();
        testCanonicalCtor();
        testToStringShape();
        testTypeUseAnnotation();
        testMixedOrder();
        testNonRecordVariety();
        testParameterizedDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

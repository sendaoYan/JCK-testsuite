/*
 * @test
 * @summary java.lang.constant.Constable conformance: white-box + black-box behavior.
 * @run main ConstableTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

public class ConstableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** user type carrying a nominal form: the Integer 42 */
    static final class Answer implements Constable {
        @Override
        public Optional<? extends ConstantDesc> describeConstable() {
            return Optional.of(Integer.valueOf(42));
        }
    }

    /** a Constable that has no nominal form at all */
    static final class Opaque implements Constable {
        @Override
        public Optional<? extends ConstantDesc> describeConstable() {
            return Optional.empty();
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.Constable");
        check(c.isInterface(), "is an interface");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1 && ms[0].getName().equals("describeConstable"),
              "single abstract method describeConstable");
        check(ms[0].getReturnType() == Optional.class, "returns Optional");
        check(!ms[0].isDefault(), "describeConstable is abstract");
        // the JDK's principal Constable implementors
        check(Constable.class.isAssignableFrom(Integer.class), "Integer is Constable");
        check(Constable.class.isAssignableFrom(String.class), "String is Constable");
        check(Constable.class.isAssignableFrom(Class.class), "Class is Constable");
        check(Constable.class.isAssignableFrom(MethodType.class), "MethodType is Constable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // every primitive-boxed value round-trips through its constant description
        Object[] values = {Integer.valueOf(7), "hello", Long.valueOf(9L), Boolean.TRUE,
                           Character.valueOf('x'), Byte.valueOf((byte) 1),
                           Short.valueOf((short) 2), Float.valueOf(1.5f), Double.valueOf(2.5d)};
        for (Object v : values) {
            Optional<? extends ConstantDesc> d = ((Constable) v).describeConstable();
            check(d.isPresent(), v.getClass().getSimpleName() + " describes a constant");
            check(d.get().resolveConstantDesc(lookup).equals(v),
                  v.getClass().getSimpleName() + " round trips");
        }

        // Class and MethodType nominal forms resolve back to the same objects
        Optional<? extends ConstantDesc> cd = String.class.describeConstable();
        check(cd.isPresent() && cd.get() instanceof ClassDesc, "Class -> ClassDesc");
        check(cd.get().resolveConstantDesc(lookup) == String.class, "ClassDesc -> String.class");
        MethodType mt = MethodType.methodType(int.class, String.class);
        Optional<? extends ConstantDesc> md = mt.describeConstable();
        check(md.isPresent() && md.get() instanceof MethodTypeDesc, "MethodType -> MethodTypeDesc");
        check(md.get().resolveConstantDesc(lookup) == mt, "MethodTypeDesc -> MethodType");

        // enum constant describes as an EnumDesc that resolves to the same constant
        Optional<? extends ConstantDesc> ed =
                java.util.concurrent.TimeUnit.SECONDS.describeConstable();
        check(ed.isPresent(), "enum describes a constant");
        check(ed.get().resolveConstantDesc(lookup) == java.util.concurrent.TimeUnit.SECONDS,
              "enum constant round trips");

        // user-implemented Constable supplying a ready-made constant
        ConstantDesc answer = new Answer().describeConstable().orElseThrow();
        check(answer.resolveConstantDesc(lookup).equals(42), "custom Constable resolves");

        // a Constable without a nominal form yields an empty Optional (not null)
        check(new Opaque().describeConstable().equals(Optional.empty()),
              "empty Optional allowed");

        // describeConstable of a non-constant view: new String still describes
        check(((Constable) new String("built")).describeConstable().isPresent(),
              "fresh String is still constant-describable");
    }

    // ---- Part 3: deep contract (constant-precision edges) ----
    static void testDeepContract() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // not everything is Constable: plain Object and user classes opt out entirely
        check(!Constable.class.isAssignableFrom(Object.class),
              "Object is not Constable (nominal form is opt-in)");
        check(!Constable.class.isAssignableFrom(StringBuilder.class),
              "StringBuilder is not Constable");

        // floating-point edge constants keep full precision through the nominal round trip
        Float[] floats = {Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                          -0.0f, Float.MIN_VALUE, Float.MAX_VALUE};
        for (Float f : floats) {
            ConstantDesc d = f.describeConstable().orElseThrow();
            Object back = d.resolveConstantDesc(lookup);
            check(back instanceof Float && ((Float) back).equals(f),
                  "Float " + f + " round-trips bit-precisely");
        }
        Double[] doubles = {Double.NaN, -0.0d, Double.MIN_VALUE, Double.MAX_VALUE,
                            Double.NEGATIVE_INFINITY};
        for (Double dv : doubles) {
            ConstantDesc d = dv.describeConstable().orElseThrow();
            Object back = d.resolveConstantDesc(lookup);
            check(back instanceof Double && ((Double) back).equals(dv),
                  "Double " + dv + " round-trips bit-precisely");
        }
        // Long extremes
        for (long lv : new long[]{Long.MIN_VALUE, -1L, 0L, Long.MAX_VALUE}) {
            ConstantDesc d = Long.valueOf(lv).describeConstable().orElseThrow();
            check(d.resolveConstantDesc(lookup).equals(lv), "Long " + lv + " round-trips");
        }
        // Character extremes (including surrogate halves)
        for (char cv : new char[]{Character.MIN_VALUE, 'A', Character.MAX_VALUE,
                                  Character.MIN_SURROGATE, Character.MAX_SURROGATE}) {
            ConstantDesc d = Character.valueOf(cv).describeConstable().orElseThrow();
            check(d.resolveConstantDesc(lookup).equals(cv), "char round-trip");
        }
        // the nominal descriptor itself is a usable ConstantDesc (nominal -> nominal)
        ConstantDesc nominal = Integer.valueOf(7).describeConstable().orElseThrow();
        check(nominal instanceof Integer && ((Integer) nominal).intValue() == 7,
              "Integer's nominal form is the Integer constant itself");

        // an empty Optional is the documented "no nominal form" answer, never null
        check(!((Optional) new Opaque().describeConstable()).isPresent()
                        && new Opaque().describeConstable() != null,
              "describeConstable returns empty Optional, not null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

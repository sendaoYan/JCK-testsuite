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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

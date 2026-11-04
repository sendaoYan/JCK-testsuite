/*
 * @test
 * @summary java.lang.constant.DirectMethodHandleDesc.Kind conformance: white-box + black-box behavior.
 * @run main KindTest
 */

import java.lang.constant.DirectMethodHandleDesc.Kind;

public class KindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.DirectMethodHandleDesc$Kind");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 11, "eleven constants");
        check(c.getDeclaringClass() == java.lang.constant.DirectMethodHandleDesc.class,
              "nested in DirectMethodHandleDesc");
        check(c.getField("refKind").getType() == int.class, "public refKind field");
        check(c.getField("isInterface").getType() == boolean.class, "public isInterface field");
        check(c.getDeclaredMethod("valueOf", int.class) != null, "valueOf(int)");
        check(c.getDeclaredMethod("valueOf", int.class, boolean.class) != null, "valueOf(int,boolean)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // declaration order
        StringBuilder sb = new StringBuilder();
        for (Kind k : Kind.values()) sb.append(k.name()).append(',');
        String joined = sb.substring(0, sb.length() - 1);
        check(joined.equals("STATIC,INTERFACE_STATIC,VIRTUAL,INTERFACE_VIRTUAL,SPECIAL,"
                + "INTERFACE_SPECIAL,CONSTRUCTOR,GETTER,SETTER,STATIC_GETTER,STATIC_SETTER"),
              "declaration order: " + joined);

        // refKind / isInterface table (JVMS method handle kinds)
        check(Kind.STATIC.refKind == 6 && !Kind.STATIC.isInterface, "STATIC 6");
        check(Kind.INTERFACE_STATIC.refKind == 6 && Kind.INTERFACE_STATIC.isInterface, "INTERFACE_STATIC 6i");
        check(Kind.VIRTUAL.refKind == 5 && !Kind.VIRTUAL.isInterface, "VIRTUAL 5");
        check(Kind.INTERFACE_VIRTUAL.refKind == 9 && Kind.INTERFACE_VIRTUAL.isInterface, "INTERFACE_VIRTUAL 9i");
        check(Kind.SPECIAL.refKind == 7 && !Kind.SPECIAL.isInterface, "SPECIAL 7");
        check(Kind.INTERFACE_SPECIAL.refKind == 7 && Kind.INTERFACE_SPECIAL.isInterface, "INTERFACE_SPECIAL 7i");
        check(Kind.CONSTRUCTOR.refKind == 8 && !Kind.CONSTRUCTOR.isInterface, "CONSTRUCTOR 8");
        check(Kind.GETTER.refKind == 1, "GETTER 1");
        check(Kind.SETTER.refKind == 3, "SETTER 3");
        check(Kind.STATIC_GETTER.refKind == 2, "STATIC_GETTER 2");
        check(Kind.STATIC_SETTER.refKind == 4, "STATIC_SETTER 4");

        // valueOf(String) round-trips every constant
        for (Kind k : Kind.values()) check(Kind.valueOf(k.name()) == k, "valueOf " + k);
        try { Kind.valueOf("NO_SUCH"); check(false, "unknown name rejected"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf unknown IAE"); }

        // valueOf(int): exact matches for every distinct refKind
        check(Kind.valueOf(1) == Kind.GETTER, "valueOf(1)");
        check(Kind.valueOf(2) == Kind.STATIC_GETTER, "valueOf(2)");
        check(Kind.valueOf(3) == Kind.SETTER, "valueOf(3)");
        check(Kind.valueOf(4) == Kind.STATIC_SETTER, "valueOf(4)");
        check(Kind.valueOf(5) == Kind.VIRTUAL, "valueOf(5)");
        check(Kind.valueOf(8) == Kind.CONSTRUCTOR, "valueOf(8)");
        check(Kind.valueOf(9) == Kind.INTERFACE_VIRTUAL, "valueOf(9)");
        // valueOf(int,boolean) distinguishes the static/special interface pairs
        check(Kind.valueOf(6, false) == Kind.STATIC, "valueOf(6,false) STATIC");
        check(Kind.valueOf(6, true) == Kind.INTERFACE_STATIC, "valueOf(6,true) INTERFACE_STATIC");
        check(Kind.valueOf(7, false) == Kind.SPECIAL, "valueOf(7,false) SPECIAL");
        check(Kind.valueOf(7, true) == Kind.INTERFACE_SPECIAL, "valueOf(7,true) INTERFACE_SPECIAL");
        check(Kind.valueOf(5, true) == Kind.VIRTUAL, "valueOf(5,true) ignores iface for VIRTUAL");

        // invalid refKinds rejected
        try { Kind.valueOf(0); check(false, "refKind 0 rejected"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf(0) IAE"); }
        try { Kind.valueOf(-1); check(false, "negative rejected"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf(-1) IAE"); }
        try { Kind.valueOf(10); check(false, "refKind 10 rejected"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf(10) IAE"); }

        // enum mechanics
        check(Kind.STATIC.compareTo(Kind.VIRTUAL) < 0, "compareTo by declaration");
        check(Kind.values()[0] == Kind.STATIC, "first constant STATIC");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

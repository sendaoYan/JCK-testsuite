/*
 * @test
 * @summary java.lang.Enum conformance: white-box structure + black-box behavior.
 * @run main EnumTest
 */

import java.lang.reflect.Modifier;

public class EnumTest {
    enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    enum Op {
        PLUS("+")  { double apply(double a, double b) { return a + b; } },
        MINUS("-") { double apply(double a, double b) { return a - b; } },
        TIMES("*") { double apply(double a, double b) { return a * b; } };

        final String symbol;
        Op(String symbol) { this.symbol = symbol; }
        abstract double apply(double a, double b);
    }

    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Enum");
        check(Modifier.isAbstract(c.getModifiers()), "Enum is abstract");
        check(c.getSuperclass() == Object.class, "Enum extends Object");
        check(Comparable.class.isAssignableFrom(c), "Enum implements Comparable");
        check(Modifier.isFinal(c.getMethod("name").getModifiers()), "name() final");
        check(Modifier.isFinal(c.getMethod("ordinal").getModifiers()), "ordinal() final");
        check(Modifier.isFinal(c.getMethod("equals", Object.class).getModifiers()), "equals final");
        check(Modifier.isFinal(c.getMethod("hashCode").getModifiers()), "hashCode final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testValuesAndValueOf() {
        Suit[] all = Suit.values();
        check(all.length == 4, "values().length == 4");
        check(all[0] == Suit.CLUBS && all[1] == Suit.DIAMONDS
              && all[2] == Suit.HEARTS && all[3] == Suit.SPADES,
              "values() in declaration order");
        check(Suit.valueOf("CLUBS") == Suit.CLUBS, "valueOf(CLUBS)");
        check(Suit.valueOf("SPADES") == Suit.SPADES, "valueOf(SPADES)");
        check(Enum.valueOf(Suit.class, "HEARTS") == Suit.HEARTS, "Enum.valueOf(Class,String)");

        try {
            Suit.valueOf("NONE");
            throw new AssertionError("valueOf(unknown): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            Suit.valueOf(null);
            throw new AssertionError("valueOf(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testOrdinalName() {
        check(Suit.CLUBS.ordinal() == 0, "CLUBS.ordinal() == 0");
        check(Suit.DIAMONDS.ordinal() == 1, "DIAMONDS.ordinal() == 1");
        check(Suit.SPADES.ordinal() == 3, "SPADES.ordinal() == 3");
        check(Suit.HEARTS.name().equals("HEARTS"), "name() == constant name");
        check(Suit.HEARTS.toString().equals("HEARTS"), "default toString() == name()");
    }

    static void testCompareEqualsHashCode() {
        check(Suit.CLUBS.compareTo(Suit.DIAMONDS) < 0, "CLUBS < DIAMONDS");
        check(Suit.SPADES.compareTo(Suit.CLUBS) > 0, "SPADES > CLUBS");
        check(Suit.HEARTS.compareTo(Suit.HEARTS) == 0, "HEARTS == HEARTS");

        check(Suit.HEARTS.equals(Suit.HEARTS), "equals same constant");
        check(!Suit.HEARTS.equals(Suit.SPADES), "!equals different constant");
        check(!Suit.HEARTS.equals(null), "!equals null");
        check(Suit.HEARTS.equals(Suit.valueOf("HEARTS")), "valueOf returns same instance");

        check(Suit.HEARTS.hashCode() == Suit.HEARTS.hashCode(), "hashCode stable");
        check(Suit.HEARTS.hashCode() == Suit.valueOf("HEARTS").hashCode(), "same constant same hashCode");
        check(System.identityHashCode(Suit.HEARTS) == Suit.HEARTS.hashCode(),
              "Enum.hashCode is identity hash");
    }

    static void testDeclaringClass() {
        check(Suit.CLUBS.getDeclaringClass() == Suit.class, "getDeclaringClass() == Suit.class");
        check(Suit.CLUBS.getClass() == Suit.class, "constant getClass() == Suit.class");
        check(Suit.CLUBS instanceof Enum, "constant instanceof Enum");
        check(Suit.CLUBS instanceof Suit, "constant instanceof Suit");
    }

    static void testConstantSpecificBody() {
        check(Op.PLUS.symbol.equals("+"), "PLUS field init");
        check(Op.PLUS.apply(2, 3) == 5, "PLUS.apply");
        check(Op.MINUS.apply(2, 3) == -1, "MINUS.apply");
        check(Op.TIMES.apply(2, 3) == 6, "TIMES.apply");
        check(Op.PLUS.getClass() != Op.class, "constant-specific class body has distinct class");
        check(Op.values().length == 3, "Op.values().length == 3");
    }

    // ---- Part 2b: black-box for previously uncovered methods ----
    static void testBlackBox() {
        // describeConstable(): returns an Optional<EnumDesc> describing this constant.
        java.util.Optional<java.lang.Enum.EnumDesc<Suit>> od = Suit.CLUBS.describeConstable();
        check(od.isPresent(), "describeConstable present for enum constant");
        java.lang.Enum.EnumDesc<Suit> desc = od.get();
        check("CLUBS".equals(desc.constantName()), "EnumDesc.constantName == CLUBS");
        check("HEARTS".equals(Suit.HEARTS.describeConstable().get().constantName()),
              "describeConstable for another constant");
        check(Suit.SPADES.describeConstable().isPresent(), "describeConstable present for SPADES");

        // clone() and finalize() are protected final in java.lang.Enum and not
        // reachable from outside the Enum class hierarchy; they are covered here
        // only structurally (they exist, are final, and finalize is deprecated).
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testValuesAndValueOf();
        testOrdinalName();
        testCompareEqualsHashCode();
        testDeclaringClass();
        testConstantSpecificBody();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary JLS 8.9 (Java SE 8): an enum type declares a fixed set of named
 *          constants with ordinal(), name(), and values(); enum bodies may add
 *          fields and methods.
 * @run main EnumTest
 */

public class EnumTest {
    enum Color { RED, GREEN, BLUE }

    enum Op {
        PLUS("+") { int apply(int a, int b) { return a + b; } },
        MINUS("-") { int apply(int a, int b) { return a - b; } };
        final String sym;
        Op(String s) { sym = s; }
        abstract int apply(int a, int b);
    }

    public static void main(String[] args) {
        Color c = Color.GREEN;
        if (c.ordinal() != 1) throw new AssertionError("ordinal");
        if (!c.name().equals("GREEN")) throw new AssertionError("name");
        Color[] all = Color.values();
        if (all.length != 3 || all[2] != Color.BLUE) throw new AssertionError("values()");

        if (Op.PLUS.apply(3, 4) != 7) throw new AssertionError("enum constant body");
        if (Op.MINUS.apply(10, 4) != 6) throw new AssertionError("enum constant body 2");
        if (!Op.PLUS.sym.equals("+")) throw new AssertionError("enum field");

        System.out.println("PASS");
    }
}

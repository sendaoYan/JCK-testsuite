/*
 * @test
 * @summary JLS 8.9.2: enum constants may pass arguments to a constructor; the
 *          constructor is chosen by the normal rules of overload resolution.
 * @run main EnumConstructor
 */

public class EnumConstructor {
    enum Coin {
        PENNY(1), NICKEL(5), DIME(10), QUARTER(25);
        Coin(int value) { this.value = value; }
        private final int value;
        public int value() { return value; }
    }

    public static void main(String[] args) {
        if (Coin.PENNY.value() != 1) throw new AssertionError("PENNY");
        if (Coin.NICKEL.value() != 5) throw new AssertionError("NICKEL");
        if (Coin.DIME.value() != 10) throw new AssertionError("DIME");
        if (Coin.QUARTER.value() != 25) throw new AssertionError("QUARTER");
        System.out.println("PASS");
    }
}

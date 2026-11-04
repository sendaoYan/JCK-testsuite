/*
 * @test
 * @summary JLS 8.9.3: enum constants provide ordinal(), name(), compareTo() and a
 *          toString() that defaults to the constant name.
 * @run main EnumOrdinalNameCompareTo
 */

public class EnumOrdinalNameCompareTo {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum Rank { ACE, TWO, THREE }

    public static void main(String[] args) {
        check(Rank.ACE.ordinal() == 0, "ordinal of first constant is 0");
        check(Rank.THREE.ordinal() == 2, "ordinal tracks declaration order");
        check(Rank.TWO.name().equals("TWO"), "name is the identifier spelling");
        check(Rank.ACE.compareTo(Rank.THREE) < 0, "compareTo follows declaration order");
        check(Rank.THREE.compareTo(Rank.ACE) > 0, "compareTo reverse");
        check(Rank.ACE.compareTo(Rank.ACE) == 0, "compareTo reflexive");
        check(Rank.TWO.toString().equals("TWO"), "toString defaults to name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

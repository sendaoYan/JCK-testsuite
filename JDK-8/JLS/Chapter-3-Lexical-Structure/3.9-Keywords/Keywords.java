/*
 * @test
 * @summary JLS 3.9 (Java SE 8): the 50 keywords and the reserved words const and
 *          goto may not be used as identifiers.  Words that became keywords in
 *          later releases (var, yield, record, sealed, module, etc.) are still
 *          legal identifiers in Java 8.
 * @run main Keywords
 */

public class Keywords {
    public static void main(String[] args) {
        // These became keywords in later releases but are plain identifiers in Java 8.
        int var = 1;
        int yield = 2;
        int record = 3;
        int sealed = 4;
        int module = 5;
        int open = 6;
        int requires = 7;
        int exports = 8;
        int permits = 9;
        int when = 10;

        if (var + yield + record + sealed + module + open + requires + exports + permits + when != 55)
            throw new AssertionError("later-release keywords are identifiers in Java 8");

        System.out.println("PASS");
    }
}

/*
 * @test
 * @summary JLS 3.8 (Java SE 8): a "Java letter" includes currency symbols, so
 *          characters such as the pound, yen, and euro signs may begin an
 *          identifier.
 * @run main CurrencySymbolIdentifier
 */

public class CurrencySymbolIdentifier {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        int £ = 1;   // POUND SIGN
        int ¥ = 2;   // YEN SIGN
        int € = 3;   // EURO SIGN
        check(£ == 1 && ¥ == 2 && € == 3,
                "currency symbols must begin identifiers");

        check(Character.isJavaIdentifierStart('£'), "pound sign is a Java letter");
        check(Character.isJavaIdentifierStart('¥'), "yen sign is a Java letter");
        check(Character.isJavaIdentifierStart('€'), "euro sign is a Java letter");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

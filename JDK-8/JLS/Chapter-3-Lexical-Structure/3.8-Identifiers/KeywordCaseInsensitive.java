/*
 * @test
 * @summary JLS 3.8 (Java SE 8): keywords are case-sensitive, so spellings that
 *          differ from a keyword only by letter case (Class, INT, For, PUBLIC)
 *          are valid identifiers.
 * @run main KeywordCaseInsensitive
 */

public class KeywordCaseInsensitive {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        int Class = 1;
        int INT = 2;
        int For = 3;
        int PUBLIC = 4;
        check(Class == 1 && INT == 2 && For == 3 && PUBLIC == 4,
                "case-variant spellings of keywords are identifiers");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

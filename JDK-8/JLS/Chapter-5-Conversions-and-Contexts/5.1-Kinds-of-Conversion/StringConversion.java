/*
 * @test
 * @summary JLS 5.1.11/5.4 (Java SE 8): the + operator performs string conversion;
 *          a null reference converts to the string "null".
 * @run main StringConversion
 */

public class StringConversion {
    public static void main(String[] args) {
        if (!("x" + 1 + 2).equals("x12")) throw new AssertionError("left-to-right string concat");
        if (!(1 + 2 + "x").equals("3x")) throw new AssertionError("numeric addition before concat");
        if (!("n=" + null).equals("n=null")) throw new AssertionError("null -> \"null\"");
        if (!("" + 3.5).equals("3.5")) throw new AssertionError("double concat");
        if (!("" + 'a').equals("a")) throw new AssertionError("char concat");

        System.out.println("PASS");
    }
}

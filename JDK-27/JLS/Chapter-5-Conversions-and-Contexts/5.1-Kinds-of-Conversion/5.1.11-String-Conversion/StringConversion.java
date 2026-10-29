/*
 * @test
 * @summary JLS 5.1.11: any type may be converted to String by string
 *          conversion. A primitive value is first boxed (Boolean.valueOf etc.)
 *          and then converted; null becomes "null"; otherwise toString() is
 *          invoked (and "null" used if that returns null).
 * @run main StringConversion
 */

public class StringConversion {
    static class NullToString {
        public String toString() { return null; }
    }

    public static void main(String[] args) {
        // Primitive string conversion: int -> Integer.valueOf -> toString.
        if (!("" + 5).equals("5")) throw new AssertionError("int -> String");
        if (!("" + true).equals("true")) throw new AssertionError("boolean -> String");
        if (!("" + 'A').equals("A")) throw new AssertionError("char -> String");
        if (!("" + 1.5).equals("1.5")) throw new AssertionError("double -> String");
        if (!("" + 1.5f).equals("1.5")) throw new AssertionError("float -> String");

        // null reference converts to "null".
        String s = null;
        if (!("" + s).equals("null")) throw new AssertionError("null -> \"null\"");
        if (!("" + (Object) null).equals("null")) throw new AssertionError("null Object -> \"null\"");

        // Otherwise toString() is invoked.
        if (!("" + new StringBuilder("abc")).equals("abc")) throw new AssertionError("toString");

        // If toString returns null, "null" is used instead.
        if (!("" + new NullToString()).equals("null")) throw new AssertionError("null toString");

        System.out.println("PASS");
    }
}

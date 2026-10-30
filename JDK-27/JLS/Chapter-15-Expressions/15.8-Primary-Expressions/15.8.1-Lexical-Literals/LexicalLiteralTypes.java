/*
 * @test
 * @summary JLS 15.8.1: the type of a literal is int, long, float, double, char,
 *          boolean, String or the null type, per the literal form.
 * @run main LexicalLiteralTypes
 */

public class LexicalLiteralTypes {
    public static void main(String[] args) {
        long l = 1L;          // long
        int i = 1;            // int
        float f = 1.0f;       // float
        double d = 1.0;       // double
        char c = 'a';         // char
        boolean b = true;     // boolean
        String s = "abc";     // String
        String n = null;      // null type, assignable to any reference type

        if (l != 1L || i != 1 || f != 1.0f || d != 1.0)
            throw new AssertionError("numeric literals");
        if (c != 'a' || !b || !"abc".equals(s) || n != null)
            throw new AssertionError("other literals");

        System.out.println("PASS");
    }
}

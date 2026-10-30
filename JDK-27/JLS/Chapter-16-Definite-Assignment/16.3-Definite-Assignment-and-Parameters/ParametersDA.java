/*
 * @test
 * @summary JLS 16.3: formal parameters are definitely assigned before the body
 *          of a method, so they may be read without prior assignment.
 * @run main ParametersDA
 */

public class ParametersDA {
    static int f(int p, String s) {
        int r = p + s.length();   // reads p and s without assigning them
        return r;
    }

    public static void main(String[] args) {
        if (f(3, "ab") != 5) throw new AssertionError("parameters");

        System.out.println("PASS");
    }
}

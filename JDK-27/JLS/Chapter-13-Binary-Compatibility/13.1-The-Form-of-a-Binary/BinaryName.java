/*
 * @test
 * @summary JLS 13.1: the binary name of a member class is the enclosing binary
 *          name followed by '$' and the simple name; an anonymous class uses a
 *          '$' followed by a non-empty sequence of digits.
 * @run main BinaryName
 */

public class BinaryName {
    class Member {}

    public static void main(String[] args) {
        if (!"BinaryName$Member".equals(Member.class.getName()))
            throw new AssertionError("member binary name: " + Member.class.getName());

        Runnable r = new Runnable() { public void run() {} };
        String anon = r.getClass().getName();
        if (!anon.matches("BinaryName\\$\\d+"))
            throw new AssertionError("anonymous binary name: " + anon);
        System.out.println("PASS");
    }
}

/*
 * @test
 * @summary java.lang.Appendable API conformance: append(CharSequence), append(CharSequence,
 *          int,int), and append(char) return the receiver and append in order; index bounds
 *          on the (csq,start,end) overload.
 * @run main AppendableTest
 */

public class AppendableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAppend() throws Exception {
        Appendable sb = new StringBuilder();
        check(sb.append("ab") == sb, "append(csq) returns this");
        check(sb.append("cd", 0, 2).toString().equals("abcd"), "append(csq,start,end)");
        check(sb.append('e').toString().equals("abcde"), "append(char)");
        check(sb.append(null).toString().equals("abcdenull"), "append(null csq) appends \"null\"");
    }

    static void testBounds() throws Exception {
        Appendable sb = new StringBuilder();
        try {
            sb.append("ab", -1, 1);
            throw new AssertionError("append(csq,-1,1): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            sb.append("ab", 1, 3);
            throw new AssertionError("append(csq,1,3): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            sb.append("ab", 2, 1);
            throw new AssertionError("append(csq,2,1): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testAppend();
        testBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

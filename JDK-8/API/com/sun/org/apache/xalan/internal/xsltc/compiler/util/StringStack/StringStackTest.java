/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack conformance: behavioral construction.
 * @run main StringStackTest
 */

public class StringStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack s = new com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack();
        check(s.empty(), "initially empty");
        s.pushString("a"); s.pushString("b");
        check(!s.empty(), "not empty after push");
        check("b".equals(s.popString()), "popString LIFO");
        check("a".equals(s.peekString()), "peekString");
        check("a".equals(s.popString()), "popString again");
        check(s.empty(), "empty after pops");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

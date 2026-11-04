/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray conformance: behavioral construction.
 * @run main IntegerArrayTest
 */

public class IntegerArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray a = new com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray();
        a.add(10); a.add(20); a.add(30);
        check(a.at(0) == 10 && a.at(1) == 20 && a.at(2) == 30, "add/at");
        check(a.indexOf(20) == 1, "indexOf");
        int[] arr = a.toIntArray();
        check(arr.length == 3 && arr[0] == 10 && arr[2] == 30, "toIntArray");
        a.reverse();
        check(a.at(0) == 30, "reverse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

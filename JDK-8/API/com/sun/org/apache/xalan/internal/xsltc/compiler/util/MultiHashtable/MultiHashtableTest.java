/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.MultiHashtable conformance: behavioral construction.
 * @run main MultiHashtableTest
 */

public class MultiHashtableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xalan.internal.xsltc.compiler.util.MultiHashtable<String,String> m = new com.sun.org.apache.xalan.internal.xsltc.compiler.util.MultiHashtable<String,String>();
        check(m.put("k", "v1") != null, "put returns set");
        java.util.Set<String> s = m.put("k", "v2");
        check(s != null && s.size() >= 2, "put accumulates values");
        m.maps("k", "v3");
        m.makeUnmodifiable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

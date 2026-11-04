/*
 * @test
 * @summary javax.tools.SimpleJavaFileObject conformance: simple java file object.
 * @run main SimpleJavaFileObjectTest
 */

public class SimpleJavaFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.tools.SimpleJavaFileObject f = new javax.tools.SimpleJavaFileObject(
            java.net.URI.create("file:///tmp/X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            public CharSequence getCharContent(boolean ignore) { return "class X {}"; }
        };
        check(f.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getKind");
        check(f.getName() != null, "getName");
        check("class X {}".equals(f.getCharContent(false).toString()), "getCharContent");
        check(f.toUri() != null, "toUri");
        check(f.isNameCompatible("X", javax.tools.JavaFileObject.Kind.SOURCE), "isNameCompatible");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

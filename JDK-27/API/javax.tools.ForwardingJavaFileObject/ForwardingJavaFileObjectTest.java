/*
 * @test
 * @summary javax.tools.ForwardingJavaFileObject conformance: forwarding java file object.
 * @run main ForwardingJavaFileObjectTest
 */

public class ForwardingJavaFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        final javax.tools.SimpleJavaFileObject sjfo = new javax.tools.SimpleJavaFileObject(java.net.URI.create("file:///tmp/X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {};
        javax.tools.ForwardingJavaFileObject<javax.tools.JavaFileObject> f = new javax.tools.ForwardingJavaFileObject<javax.tools.JavaFileObject>(sjfo) {};
        check(f.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getKind");
        check(f.isNameCompatible("X", javax.tools.JavaFileObject.Kind.SOURCE), "isNameCompatible");
        check(f.getNestingKind() == null, "getNestingKind");
        check(f.getAccessLevel() == null, "getAccessLevel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

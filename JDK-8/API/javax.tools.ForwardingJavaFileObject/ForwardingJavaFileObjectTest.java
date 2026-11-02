/*
 * @test
 * @summary javax.tools.ForwardingJavaFileObject conformance: forwarding java file object.
 * @run main ForwardingJavaFileObjectTest
 */

public class ForwardingJavaFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.tools.SimpleJavaFileObject delegate = new javax.tools.SimpleJavaFileObject(
            java.net.URI.create("file:///tmp/X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            public CharSequence getCharContent(boolean ignore) { return "class X {}"; }
        };
        javax.tools.ForwardingJavaFileObject<javax.tools.JavaFileObject> f = new javax.tools.ForwardingJavaFileObject<javax.tools.JavaFileObject>(delegate) {};
        check(f.getKind() == javax.tools.JavaFileObject.Kind.SOURCE, "getKind forwards");
        check(f.getName().equals(delegate.getName()), "getName forwards");
        check(f.getNestingKind() == delegate.getNestingKind(), "getNestingKind forwards");
        check(f.getAccessLevel() == delegate.getAccessLevel(), "getAccessLevel forwards");
        check(f.isNameCompatible("X", javax.tools.JavaFileObject.Kind.SOURCE) == delegate.isNameCompatible("X", javax.tools.JavaFileObject.Kind.SOURCE), "isNameCompatible forwards");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

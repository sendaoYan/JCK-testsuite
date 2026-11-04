/*
 * @test
 * @summary javax.tools.ForwardingFileObject conformance: forwarding file object.
 * @run main ForwardingFileObjectTest
 */

public class ForwardingFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.tools.SimpleJavaFileObject delegate = new javax.tools.SimpleJavaFileObject(
            java.net.URI.create("file:///tmp/X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            public CharSequence getCharContent(boolean ignore) { return "class X {}"; }
        };
        javax.tools.ForwardingFileObject<javax.tools.FileObject> f = new javax.tools.ForwardingFileObject<javax.tools.FileObject>(delegate) {};
        check(f.getName().equals(delegate.getName()), "getName forwards");
        check(f.toUri().equals(delegate.toUri()), "toUri forwards");
        check(f.getCharContent(false).toString().equals(delegate.getCharContent(false).toString()), "getCharContent forwards");
        check(f.getLastModified() == delegate.getLastModified(), "getLastModified forwards");
        check(f.delete() == delegate.delete(), "delete forwards");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

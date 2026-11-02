/*
 * @test
 * @summary javax.tools.ForwardingFileObject conformance: forwarding file object.
 * @run main ForwardingFileObjectTest
 */

public class ForwardingFileObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        final javax.tools.SimpleJavaFileObject sjfo = new javax.tools.SimpleJavaFileObject(java.net.URI.create("file:///tmp/X.java"), javax.tools.JavaFileObject.Kind.SOURCE) {};
        javax.tools.ForwardingFileObject<javax.tools.FileObject> f = new javax.tools.ForwardingFileObject<javax.tools.FileObject>(sjfo) {};
        check(f.toUri().equals(sjfo.toUri()), "toUri");
        check(f.getName().equals(sjfo.getName()), "getName");
        check(f.getLastModified() == sjfo.getLastModified(), "getLastModified");
        f.delete();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

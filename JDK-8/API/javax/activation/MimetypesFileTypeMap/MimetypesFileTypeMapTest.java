/*
 * @test
 * @summary javax.activation.MimetypesFileTypeMap conformance: mimetypes file type map.
 * @run main MimetypesFileTypeMapTest
 */


public class MimetypesFileTypeMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.activation.MimetypesFileTypeMap m = new javax.activation.MimetypesFileTypeMap();
        check("text/plain".equals(m.getContentType("x.txt")), "txt -> text/plain");
        check(m instanceof javax.activation.FileTypeMap, "extends FileTypeMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

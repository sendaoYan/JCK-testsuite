/*
 * @test
 * @summary javax.activation.MimeTypeParameterList conformance: mime type parameter list.
 * @run main MimeTypeParameterListTest
 */


public class MimeTypeParameterListTest {
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
        javax.activation.MimeTypeParameterList pl = new javax.activation.MimeTypeParameterList();
        check(pl.isEmpty(), "empty");
        pl.set("a", "b");
        check(pl.size() == 1, "size");
        check("b".equals(pl.get("a")), "get");
        pl.remove("a");
        check(pl.isEmpty(), "after remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary JLS 6.5.5.2 Example 6.5.5.2-1: a qualified type name Q.Id denotes an
 *          accessible member class/interface of the package or type Q (e.g.
 *          java.util.Date).
 * @run main QualifiedTypeName
 */

public class QualifiedTypeName {
    public static void main(String[] args) {
        java.util.Date d = new java.util.Date(0);
        if (d.getTime() != 0) throw new AssertionError("qualified type name");

        System.out.println("PASS");
    }
}

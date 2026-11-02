/*
 * @test
 * @summary com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription conformance: behavioral construction.
 * @run main FullValueDescriptionTest
 */

public class FullValueDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription x = new com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription();
        x.name = "nm"; x.is_abstract = true; x.is_custom = false;
        check("nm".equals(x.name), "name");
        check(x.is_abstract && !x.is_custom, "boolean fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

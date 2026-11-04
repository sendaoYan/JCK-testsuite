/*
 * @test
 * @summary com.sun.org.omg.CORBA.ParameterDescription conformance: behavioral construction.
 * @run main ParameterDescriptionTest
 */

public class ParameterDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.omg.CORBA.ParameterDescription x = new com.sun.org.omg.CORBA.ParameterDescription();
        x.name = "nm";
        check("nm".equals(x.name), "field roundtrip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

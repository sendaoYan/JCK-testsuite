/*
 * @test
 * @summary com.sun.org.omg.CORBA.AttributeDescription conformance: behavioral construction.
 * @run main AttributeDescriptionTest
 */

public class AttributeDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.omg.CORBA.AttributeDescription x = new com.sun.org.omg.CORBA.AttributeDescription();
        x.name = "nm"; x.id = "id"; x.defined_in = "def"; x.version = "1.0";
        check("nm".equals(x.name) && "id".equals(x.id) && "def".equals(x.defined_in) && "1.0".equals(x.version), "field roundtrip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

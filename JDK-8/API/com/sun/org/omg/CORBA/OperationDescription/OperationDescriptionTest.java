/*
 * @test
 * @summary com.sun.org.omg.CORBA.OperationDescription conformance: behavioral construction.
 * @run main OperationDescriptionTest
 */

public class OperationDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.omg.CORBA.OperationDescription x = new com.sun.org.omg.CORBA.OperationDescription();
        x.name = "nm"; x.id = "id"; x.defined_in = "def"; x.version = "1.0";
        check("nm".equals(x.name) && "id".equals(x.id) && "def".equals(x.defined_in) && "1.0".equals(x.version), "field roundtrip");
        x.contexts = new String[]{"c1"}; x.parameters = new com.sun.org.omg.CORBA.ParameterDescription[0]; x.exceptions = new com.sun.org.omg.CORBA.ExceptionDescription[0];
        check(x.contexts.length == 1 && "c1".equals(x.contexts[0]), "contexts");
        check(x.parameters != null && x.exceptions != null, "arrays set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

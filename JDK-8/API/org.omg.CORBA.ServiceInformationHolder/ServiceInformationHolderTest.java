/*
 * @test
 * @summary org.omg.CORBA.ServiceInformationHolder conformance: holder value field.
 * @run main ServiceInformationHolderTest
 */

public class ServiceInformationHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ServiceInformationHolder h0 = new org.omg.CORBA.ServiceInformationHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.ServiceInformation v = null;
        org.omg.CORBA.ServiceInformationHolder h1 = new org.omg.CORBA.ServiceInformationHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

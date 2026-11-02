/*
 * @test
 * @summary javax.xml.soap.MessageFactory conformance: SOAP message factory.
 * @run main MessageFactoryTest
 */


public class MessageFactoryTest {
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
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        check(mf != null, "factory non-null");
        javax.xml.soap.SOAPMessage msg = mf.createMessage();
        check(msg != null, "message non-null");
        check(msg.getSOAPPart() != null, "soap part non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

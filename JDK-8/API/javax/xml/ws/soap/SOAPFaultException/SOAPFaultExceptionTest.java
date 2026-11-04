/*
 * @test
 * @summary javax.xml.ws.soap.SOAPFaultException conformance: exception hierarchy and constructors.
 * @run main SOAPFaultExceptionTest
 */


public class SOAPFaultExceptionTest {
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
        check(javax.xml.ws.soap.SOAPFaultException.class.getSuperclass().getName().equals("javax.xml.ws.ProtocolException"), "extends javax.xml.ws.ProtocolException");
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPFault fault = mf.createMessage().getSOAPBody().addFault();
        javax.xml.ws.soap.SOAPFaultException fe = new javax.xml.ws.soap.SOAPFaultException(fault);
        check(fe.getFault() == fault, "fault");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

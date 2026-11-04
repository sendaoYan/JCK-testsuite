/*
 * @test
 * @summary com.sun.security.ntlm.NTLMException conformance: behavior of constructors, accessors and value semantics.
 * @run main NTLMExceptionTest
 */

public class NTLMExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.security.ntlm.NTLMException e = new com.sun.security.ntlm.NTLMException(com.sun.security.ntlm.NTLMException.PROTOCOL, "boom");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e.errorCode() == com.sun.security.ntlm.NTLMException.PROTOCOL, "errorCode round trip");
        check("boom".equals(e.getMessage()), "message");
        int[] codes = { com.sun.security.ntlm.NTLMException.PACKET_READ_ERROR, com.sun.security.ntlm.NTLMException.NO_DOMAIN_INFO, com.sun.security.ntlm.NTLMException.USER_UNKNOWN, com.sun.security.ntlm.NTLMException.AUTH_FAILED, com.sun.security.ntlm.NTLMException.BAD_VERSION, com.sun.security.ntlm.NTLMException.PROTOCOL, com.sun.security.ntlm.NTLMException.INVALID_INPUT };
        java.util.Set<Integer> s = new java.util.HashSet<Integer>();
        for (int c : codes) s.add(c);
        check(s.size() == 7, "7 distinct error codes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

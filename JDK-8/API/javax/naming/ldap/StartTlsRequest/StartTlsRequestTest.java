/*
 * @test
 * @summary javax.naming.ldap.StartTlsRequest conformance: oid/id.
 * @run main StartTlsRequestTest
 */



import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.StartTlsResponse;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ConfigurationException;
public class StartTlsRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.StartTlsRequest r = new javax.naming.ldap.StartTlsRequest();
        check(r.getID().equals(javax.naming.ldap.StartTlsRequest.OID), "getID OID");
        check("1.3.6.1.4.1.1466.20037".equals(javax.naming.ldap.StartTlsRequest.OID), "OID value");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dStReqBasics();
        dStReqCreateResponse();
        System.out.println("deep: StartTlsRequest contracts OK");
    }

    static void dStReqBasics() {
        check("1.3.6.1.4.1.1466.20037".equals(StartTlsRequest.OID), "OID constant");
        StartTlsRequest r = new StartTlsRequest();
        check(StartTlsRequest.OID.equals(r.getID()), "getID is the OID");
        check(r.getEncodedValue() == null, "valueless extended request");
        check(r instanceof ExtendedRequest, "implements ExtendedRequest");
    }

    static void dStReqCreateResponse() throws Exception {
        StartTlsRequest r = new StartTlsRequest();
        // matching id -> a usable StartTlsResponse (impl provided by the platform)
        ExtendedResponse ok = r.createExtendedResponse(StartTlsRequest.OID, null, 0, 0);
        check(ok != null, "response created for matching id");
        check(ok instanceof StartTlsResponse, "response is a StartTlsResponse");
        check(StartTlsResponse.OID.equals(ok.getID()), "response id matches");
        check(ok.getEncodedValue() == null, "response carries no value");
        // response arguments are ignored for this operation
        ExtendedResponse withBytes = r.createExtendedResponse(StartTlsRequest.OID,
                new byte[]{1}, 0, 1);
        check(withBytes instanceof StartTlsResponse, "value bytes ignored");
        // a different id is rejected
        boolean ce = false;
        try { r.createExtendedResponse("1.2.3.4", null, 0, 0); }
        catch (ConfigurationException e) {
            ce = true;
            check(e.getMessage() != null && e.getMessage().contains("1.2.3.4"),
                    "message mentions the mismatched id");
        }
        check(ce, "mismatched id -> ConfigurationException");
    }

}

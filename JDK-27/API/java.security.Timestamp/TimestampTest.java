/*
 * @test
 * @summary java.security.Timestamp conformance: constructor and accessors.
 * @run main TimestampTest
 */

import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.Date;

public class TimestampTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertPath cp = CertificateFactory.getInstance("X.509").generateCertPath(Collections.emptyList());
        Date d = new Date();
        Timestamp ts = new Timestamp(d, cp);
        check(d.equals(ts.getTimestamp()), "getTimestamp");
        check(cp.equals(ts.getSignerCertPath()), "getSignerCertPath");
        Timestamp ts2 = new Timestamp(d, cp);
        check(ts.equals(ts2), "equals");
        check(ts.hashCode() == ts2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

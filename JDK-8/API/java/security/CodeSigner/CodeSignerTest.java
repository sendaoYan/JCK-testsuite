/*
 * @test
 * @summary java.security.CodeSigner conformance: full contract coverage.
 *          constructor (null signerCertPath -> NPE, null timestamp allowed),
 *          getSignerCertPath/getTimestamp, equals/hashCode value semantics,
 *          toString non-empty, and serialization round-trip.
 * @run main CodeSignerTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.CodeSigner;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

public class CodeSignerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static final String CERT_B64 =
        "MIIDhzCCAm+gAwIBAgIEDxeuOjANBgkqhkiG9w0BAQsFADB0MQswCQYDVQQGEwJV" +
        "UzELMAkGA1UECBMCSUwxFDASBgNVBAcTC1NwcmluZ2ZpZWxkMRQwEgYDVQQKEwtF" +
        "eGFtcGxlIE9yZzEUMBIGA1UECxMLQ29uZm9ybWFuY2UxFjAUBgNVBAMTDUpDSyBU" +
        "ZXN0IENlcnQwHhcNMjYxMTA0MDkzNzI4WhcNMzYxMTAxMDkzNzI4WjB0MQswCQYD" +
        "VQQGEwJVUzELMAkGA1UECBMCSUwxFDASBgNVBAcTC1NwcmluZ2ZpZWxkMRQwEgYD" +
        "VQQKEwtFeGFtcGxlIE9yZzEUMBIGA1UECxMLQ29uZm9ybWFuY2UxFjAUBgNVBAMT" +
        "DUpDSyBUZXN0IENlcnQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDA" +
        "ImuPu7xdlUkyW0Q8Y75ek2oybshN0xF3MxmxqiUWJruiM+U2+Ema1DA7lSquiCNG" +
        "bzmHhWpk8mK2rjEFu++avlIG5mnr4XQdiQe5o4q2IIuN3LKdvfauwmT5WoxUTdCa" +
        "FxtBDdAPsfckCr8C/ypWwJUdK4uY4MFgrVWg5NAIIG8xVmGONYIpE25fxQ7O55CM" +
        "23CXsJ3iU5+GpTBFaDr8/LJ2eKQEx35voV3iyWa9bKdsys4kg1sQgTCLv2kvRFU9" +
        "QXmcLwuhP0tEUGf7fann26h5FrL5fsj4UwT+djO42tNHTDN5KGgeoinVKYlUcfEY" +
        "WEyek9a95hFdn8I/30fLAgMBAAGjITAfMB0GA1UdDgQWBBTo72jITqQdGMA1Fxl/" +
        "lTqG24M1VTANBgkqhkiG9w0BAQsFAAOCAQEAnofWJt83h4XkRfD6ejfnYJeMXZ+O" +
        "QY4J4/cHa6jSTgXPrKI8GQtZD8QF0jlb/2rjEKQ33YW8r9lci6p1po5bIMqbToGb" +
        "zoKFK2x16ggsx37pxwIWqdp9aFIWJvQFXrK8vwzEGFc518wxjhOLKE7BbYzeCSiL" +
        "jP92JbEWUw6Ae4eCEpxMyZKWCw/iizv2zRv3iH5uobZwZ+aUiQUgjTXRM0exJXFv" +
        "VfXV9EY8MG9PMbWNjkusz66NIyUKoupRvl9rTnjNv1LlggVSAwW5uXzNCh/sUSMm" +
        "jVmmYMwYpDTG1Uz54HYKL7ZNhkDUj0IzSM5PI0ut8jdnMJOOWXr6tg0GPg==";

    static CertPath certPath() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(Base64.getDecoder().decode(CERT_B64)));
        return cf.generateCertPath(Collections.<java.security.cert.Certificate>singletonList(cert));
    }

    public static void main(String[] args) throws Exception {
        CertPath cp = certPath();
        Date now = new Date(1234567890000L);
        Timestamp ts = new Timestamp(now, cp);

        // ---- construct + getters ----
        CodeSigner cs = new CodeSigner(cp, ts);
        check(cp.equals(cs.getSignerCertPath()), "getSignerCertPath");
        check(ts.equals(cs.getTimestamp()), "getTimestamp");

        // ---- null signerCertPath -> NPE ----
        checkThrows(NullPointerException.class, () -> new CodeSigner(null, ts),
                "constructor(null signerCertPath, timestamp)");

        // ---- null timestamp allowed ----
        CodeSigner csNullTs = new CodeSigner(cp, null);
        check(csNullTs.getTimestamp() == null, "null timestamp allowed -> getTimestamp null");

        // ---- equals/hashCode value semantics ----
        CodeSigner same = new CodeSigner(cp, ts);
        check(cs.equals(same), "equals identical");
        check(cs.hashCode() == same.hashCode(), "hashCode identical");
        check(!cs.equals(new CodeSigner(cp, new Timestamp(new Date(999L), cp))),
                "equals different timestamp false");
        check(!cs.equals(csNullTs), "equals null timestamp false");
        check(!cs.equals(null), "equals null false");
        check(!cs.equals("not a codesigner"), "equals unrelated type false");

        // ---- toString non-empty ----
        check(cs.toString() != null && cs.toString().length() > 0, "toString non-empty");

        // ---- serialization round-trip ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(cs); }
        CodeSigner back;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            back = (CodeSigner) ois.readObject();
        }
        check(cs.equals(back), "serialization equals");
        check(cp.equals(back.getSignerCertPath()), "serialization preserves certPath");
        check(ts.equals(back.getTimestamp()), "serialization preserves timestamp");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

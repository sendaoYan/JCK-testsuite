/*
 * @test
 * @summary javax.xml.crypto.OctetStreamData conformance.
 * @run main OctetStreamDataTest
 */

import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class OctetStreamDataTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.OctetStreamData");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(javax.xml.crypto.Data.class.isAssignableFrom(c), "implements Data");
        // both constructors reject a null stream with the precise message
        try { OctetStreamData t = new OctetStreamData(null); check(false, "1-arg null: " + t); }
        catch (NullPointerException e) { check("octetStream is null".equals(e.getMessage()), "1-arg null message"); }
        try { OctetStreamData t = new OctetStreamData(null, "u", "m"); check(false, "3-arg null: " + t); }
        catch (NullPointerException e) { check("octetStream is null".equals(e.getMessage()), "3-arg null message"); }
        // one-arg form: stream identity, uri/mime default to null
        InputStream in = new ByteArrayInputStream(new byte[] {1, 2, 3});
        OctetStreamData d = new OctetStreamData(in);
        check(d.getOctetStream() == in, "stream identity round-trip");
        check(d.getURI() == null, "uri defaults to null");
        check(d.getMimeType() == null, "mime defaults to null");
        check(d instanceof Data, "is Data");
        // three-arg form carries uri and mime verbatim, including empty strings
        OctetStreamData d2 = new OctetStreamData(in, "http://example/payload", "application/octet-stream");
        check("http://example/payload".equals(d2.getURI()), "uri round-trip");
        check("application/octet-stream".equals(d2.getMimeType()), "mime round-trip");
        check(d2.getOctetStream() == in, "3-arg stream identity");
        OctetStreamData d3 = new OctetStreamData(in, "", "");
        check("".equals(d3.getURI()) && "".equals(d3.getMimeType()), "empty uri/mime preserved");
        // stream remains readable through the wrapper
        check(d.getOctetStream().read() == 1 && d.getOctetStream().read() == 2, "wrapped stream readable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

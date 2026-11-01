/*
 * @test
 * @summary java.util.jar.Manifest conformance: main attributes/entries/write-read.
 * @run main ManifestTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testManifest() throws Exception {
        Manifest m = new Manifest();
        check(m.getMainAttributes().isEmpty(), "empty main attributes");
        check(m.getEntries().isEmpty(), "no entries");
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        check("1.0".equals(m.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "set/get main attribute");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        m.write(bout);
        Manifest m2 = new Manifest(new ByteArrayInputStream(bout.toByteArray()));
        check("1.0".equals(m2.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION)),
              "read back");
        check(m.equals(m2), "equals after round-trip");
        check(m.hashCode() == m2.hashCode(), "hashCode after round-trip");
        check(m.clone() instanceof Manifest, "clone");
        m2.clear();
        check(m2.getMainAttributes().isEmpty(), "clear");
        check(m.toString() != null, "toString");
    }

    public static void main(String[] args) throws Exception {
        testManifest();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.util.jar.JarEntry conformance: name/certificates/attributes.
 * @run main JarEntryTest
 */

import java.util.jar.JarEntry;

public class JarEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJarEntry() throws Exception {
        JarEntry e = new JarEntry("file.txt");
        check("file.txt".equals(e.getName()), "getName");
        check(e.getCertificates() == null, "getCertificates null");
        check(e.getCodeSigners() == null, "getCodeSigners null");
        check(e.getAttributes() == null, "getAttributes null");
        e.setSize(100);
        check(e.getSize() == 100, "setSize/getSize");
    }

    public static void main(String[] args) throws Exception {
        testJarEntry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

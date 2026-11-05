/*
 * @test
 * @summary javax.xml.crypto.Data conformance.
 * @run main DataTest
 */

public class DataTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.Data");
        check(c.isInterface(), "is interface");
        check(c.getInterfaces().length == 0, "marker interface, extends nothing");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
        // the two shipped subtypes
        check(javax.xml.crypto.OctetStreamData.class.getInterfaces()[0].getName().equals("javax.xml.crypto.Data"),
            "OctetStreamData implements Data");
        check(javax.xml.crypto.Data.class.isAssignableFrom(javax.xml.crypto.NodeSetData.class),
            "NodeSetData extends Data");
        // a bare anonymous Data is constructible and usable as a return value
        javax.xml.crypto.Data bare = new javax.xml.crypto.Data() { };
        check(bare != null, "anonymous Data constructible");
        // both subtypes flow through a dereferencer contract
        javax.xml.crypto.OctetStreamData osd = new javax.xml.crypto.OctetStreamData(
            new java.io.ByteArrayInputStream(new byte[] {9}), "u", "m");
        check(osd instanceof javax.xml.crypto.Data, "octet stream is Data");
        javax.xml.crypto.NodeSetData nsd = new javax.xml.crypto.NodeSetData() {
            public java.util.Iterator iterator() { return java.util.Collections.emptyIterator(); }
        };
        check(nsd instanceof javax.xml.crypto.Data, "node set is Data");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

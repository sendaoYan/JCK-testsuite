/*
 * @test
 * @summary javax.sql.rowset.serial.SerialDatalink conformance: white-box structure + black-box behavior.
 * @run main SerialDatalinkTest
 */

public class SerialDatalinkTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialDatalink");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct interface(s)");
        check(ifs[0].getName().equals("java.io.Serializable"), "interface 0");
        check(ifs[1].getName().equals("java.lang.Cloneable"), "interface 1");
        check(c.getDeclaredMethods().length == 4, "declared method count 4");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.net.URL url = new java.net.URL("https://example.com/path");
        javax.sql.rowset.serial.SerialDatalink sd = new javax.sql.rowset.serial.SerialDatalink(url);

        java.net.URL got = sd.getDatalink();
        check(got != null, "getDatalink non-null");
        check(url.equals(got), "getDatalink equals original URL");
        check(got != url, "getDatalink returns a fresh copy, not the same instance");

        // equals/hashCode based on the stored URL
        javax.sql.rowset.serial.SerialDatalink same = new javax.sql.rowset.serial.SerialDatalink(new java.net.URL("https://example.com/path"));
        javax.sql.rowset.serial.SerialDatalink other = new javax.sql.rowset.serial.SerialDatalink(new java.net.URL("https://example.com/other"));
        check(sd.equals(same), "equals same URL");
        check(sd.hashCode() == same.hashCode(), "hashCode same URL");
        check(!sd.equals(other), "not equals different URL");
        check(!sd.equals(null), "not equals null");
        check(!sd.equals("https://example.com/path"), "not equals non-SerialDatalink");

        javax.sql.rowset.serial.SerialDatalink clone = (javax.sql.rowset.serial.SerialDatalink) sd.clone();
        check(clone != sd, "clone is a distinct instance");
        check(sd.equals(clone), "clone equals original");

        // null URL is rejected at construction time.
        try {
            new javax.sql.rowset.serial.SerialDatalink(null);
            check(false, "null URL should throw");
        } catch (javax.sql.rowset.serial.SerialException expected) {
            check(true, "null URL throws SerialException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

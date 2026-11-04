/*
 * @test
 * @summary java.util.zip.ZipEntry contract: name validation (null->NPE, too-long->IAE),
 *          metadata get/set round-trip (size/compressedSize/crc/comment/extra/time),
 *          clone, equals/hashCode, directory detection, setMethod/setTime.
 * @run main ZipEntryTest
 */

import java.util.zip.ZipEntry;

public class ZipEntryTest {
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

    static void testNameValidation() {
        ZipEntry e = new ZipEntry("hello.txt");
        check("hello.txt".equals(e.getName()), "getName");
        checkThrows(NullPointerException.class, () -> new ZipEntry((String) null), "null name NPE");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70000; i++) sb.append('a');
        checkThrows(IllegalArgumentException.class, () -> new ZipEntry(sb.toString()), "name too long IAE");
    }

    static void testMetadata() {
        ZipEntry e = new ZipEntry("data");
        check(e.getSize() == -1 && e.getCompressedSize() == -1, "default sizes -1");
        check(e.getCrc() == -1, "default crc -1");
        check(e.getTime() == -1, "default time -1");
        e.setSize(100);
        check(e.getSize() == 100, "setSize/getSize");
        e.setCompressedSize(50);
        check(e.getCompressedSize() == 50, "setCompressedSize");
        e.setCrc(0xCBF43926L);
        check(e.getCrc() == 0xCBF43926L, "setCrc");
        e.setComment("a comment");
        check("a comment".equals(e.getComment()), "setComment/getComment");
        byte[] extra = new byte[]{1, 2, 3};
        e.setExtra(extra);
        check(e.getExtra() == extra && e.getExtra().length == 3, "setExtra keeps reference");
        e.setExtra(null);
        check(e.getExtra() == null, "setExtra(null) clears");
        checkThrows(IllegalArgumentException.class, () -> e.setExtra(new byte[70000]), "setExtra too long IAE");
        e.setComment(null);
        check(e.getComment() == null, "setComment(null) clears");
    }

    static void testMethods() {
        ZipEntry e = new ZipEntry("m.txt");
        e.setMethod(ZipEntry.STORED);
        check(e.getMethod() == ZipEntry.STORED, "setMethod STORED");
        e.setMethod(ZipEntry.DEFLATED);
        check(e.getMethod() == ZipEntry.DEFLATED, "setMethod DEFLATED");
        e.setTime(1600000000000L);
        check(e.getTime() == 1600000000000L, "setTime/getTime");
    }

    static void testDirectory() {
        ZipEntry dir = new ZipEntry("dir/");
        check(dir.isDirectory(), "trailing slash is directory");
        ZipEntry file = new ZipEntry("dir/file");
        check(!file.isDirectory(), "no slash not directory");
    }

    static void testClone() {
        ZipEntry e = new ZipEntry("clone.txt");
        e.setSize(10);
        e.setComment("c");
        ZipEntry c = (ZipEntry) e.clone();
        check(c != e, "clone distinct instance");
        check("clone.txt".equals(c.getName()) && c.getSize() == 10 && "c".equals(c.getComment()), "clone copies fields");
        check(!e.equals(c), "clone not equal (identity equals)");
    }

    static void testEqualsHashCode() {
        ZipEntry a = new ZipEntry("x");
        ZipEntry b = new ZipEntry("x");
        check(!a.equals(b), "no name-based equals (identity semantics)");
        check(a.equals(a), "equals self");
        check(a.hashCode() == a.hashCode(), "hashCode self-consistent");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("x"), "not equal to string");
    }

    public static void main(String[] args) {
        testNameValidation();
        testMetadata();
        testMethods();
        testDirectory();
        testClone();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

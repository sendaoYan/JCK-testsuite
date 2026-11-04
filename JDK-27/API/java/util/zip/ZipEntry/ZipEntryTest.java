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


    // ===================== JDK-9+ 增量：setTimeLocal/getTimeLocal（JDK-27 侧锁定） =====================
    static void testJdk27Additions() {
        ZipEntry e = new ZipEntry("t");
        check(e.getTime() == -1L, "default getTime -1");
        // 实现怪癖：未设置时间时 getTimeLocal() 抛 DateTimeException（javadoc 未声明 null 路径）
        try {
            e.getTimeLocal();
            check(false, "unset getTimeLocal behavior");
        } catch (java.time.DateTimeException ex) {
            check(true, "unset getTimeLocal throws DateTimeException (impl quirk)");
        }
        // setTimeLocal 精确往返（毫秒字段保留，奇数秒不丢失）
        java.time.LocalDateTime t = java.time.LocalDateTime.of(2021, 3, 14, 12, 30, 17);
        e.setTimeLocal(t);
        check(t.equals(e.getTimeLocal()), "setTimeLocal exact round-trip incl. odd second");
        check(e.getTime() == 1615696217000L, "setTimeLocal epoch millis in Asia/Shanghai-local semantics");
        // setTime 也反映到 getTimeLocal（epoch 0 -> 1970-01-01，时区相关的具体时不断言）
        e.setTime(0L);
        check(e.getTime() == 0L, "setTime(0) passthrough");
        java.time.LocalDateTime zero = e.getTimeLocal();
        check(zero.getYear() == 1970 && zero.getMonthValue() == 1 && zero.getDayOfMonth() == 1,
              "epoch 0 local date is 1970-01-01");
        // setTimeLocal 返回 void（无链式）
        try {
            java.lang.reflect.Method m = ZipEntry.class.getMethod("setTimeLocal", java.time.LocalDateTime.class);
            check(m.getReturnType() == void.class, "setTimeLocal returns void");
        } catch (NoSuchMethodException nsme) { check(false, "setTimeLocal present"); }
    }

    public static void main(String[] args) {
        testNameValidation();
        testMetadata();
        testMethods();
        testDirectory();
        testClone();
        testEqualsHashCode();        testJdk27Additions();

        System.out.println("PASS (" + passed + " assertions)");
    }
}

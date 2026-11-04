/*
 * @test
 * @summary java.util.zip.ZipException conformance: constructors/inheritance.
 * @run main ZipExceptionTest
 */

import java.io.IOException;
import java.io.*;
import java.util.zip.*;
import java.util.zip.ZipException;

public class ZipExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZipException() {
        ZipException e = new ZipException();
        check(e.getMessage() == null, "no-arg message null");
        ZipException e2 = new ZipException("bad zip");
        check("bad zip".equals(e2.getMessage()), "message ctor");
        check(e2 instanceof IOException, "is an IOException");
    }


    // ===================== 契约深化：构造矩阵/序列化/垃圾 zip 触发/重复 entry 消息 =====================
    static void testContracts() throws Exception {
        ZipException noMsg = new ZipException();
        check(noMsg.getMessage() == null && noMsg.getCause() == null, "no-arg state");
        ZipException z = new ZipException("boom");
        check(z instanceof java.io.IOException, "is IOException");
        check("java.util.zip.ZipException: boom".equals(z.toString()), "toString");
        RuntimeException c = new RuntimeException("r");
        check(z.initCause(c).getCause() == c, "initCause chaining");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(z); }
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ZipException && "boom".equals(((ZipException) back).getMessage()),
              "serialization round-trip");
        // 垃圾文件打开 -> ZipException（8 与 27 消息不同：error in opening zip file / zip END header not found）
        File garbage = File.createTempFile("jck-zip-garbage", ".zip");
        garbage.deleteOnExit();
        try (FileOutputStream fo = new FileOutputStream(garbage)) {
            for (int i = 0; i < 64; i++) fo.write(i * 37 + 5);
        }
        try {
            new ZipFile(garbage).close();
            check(false, "garbage zip throws ZipException");
        } catch (ZipException e) {
            check(e.getMessage() != null, "ZipException message present: " + e.getMessage());
        }
        // 空文件 -> ZipException("zip file is empty") 双侧一致
        File emptyF = File.createTempFile("jck-zip-empty", ".zip");
        emptyF.deleteOnExit();
        try {
            new ZipFile(emptyF).close();
            check(false, "empty zip throws ZipException");
        } catch (ZipException e) {
            check("zip file is empty".equals(e.getMessage()), "empty zip exact message: " + e.getMessage());
        }
        // 重复 entry -> ZipException("duplicate entry: <name>")
        java.io.ByteArrayOutputStream zb = new java.io.ByteArrayOutputStream();
        ZipOutputStream zo = new ZipOutputStream(zb);
        zo.putNextEntry(new ZipEntry("dup"));
        zo.closeEntry();
        try {
            zo.putNextEntry(new ZipEntry("dup"));
            check(false, "duplicate entry throws ZipException");
        } catch (ZipException e) {
            check("duplicate entry: dup".equals(e.getMessage()), "duplicate entry exact message");
        }
        zo.close();
    }

    public static void main(String[] args) {
        testZipException();
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

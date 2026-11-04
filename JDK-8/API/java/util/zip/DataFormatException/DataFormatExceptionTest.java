/*
 * @test
 * @summary java.util.zip.DataFormatException conformance: constructors/inheritance.
 * @run main DataFormatExceptionTest
 */

import java.util.zip.DataFormatException;
import java.io.*;
import java.util.zip.*;

public class DataFormatExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDataFormatException() {
        DataFormatException e = new DataFormatException();
        check(e.getMessage() == null, "no-arg message null");
        DataFormatException e2 = new DataFormatException("bad format");
        check("bad format".equals(e2.getMessage()), "message ctor");
        check(e instanceof Exception, "is an Exception");
    }


    // ===================== 契约深化：构造矩阵/cause/序列化/损坏数据触发 =====================
    static void testContracts() throws Exception {
        DataFormatException noMsg = new DataFormatException();
        check(noMsg.getMessage() == null, "no-arg message null");
        check(noMsg.getCause() == null, "initial cause null");
        RuntimeException cause = new RuntimeException("root");
        check(noMsg.initCause(cause).getCause() == cause, "initCause returns this and stores cause");
        check(noMsg.toString().startsWith("java.util.zip.DataFormatException"), "toString class name");
        DataFormatException withMsg = new DataFormatException("msg-x");
        check("java.util.zip.DataFormatException: msg-x".equals(withMsg.toString()), "toString includes message");
        // 序列化 round-trip（继承 IOException → Serializable）
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) {
            oo.writeObject(new DataFormatException("ser"));
        }
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof DataFormatException && "ser".equals(((DataFormatException) back).getMessage()),
              "serialization round-trip keeps message");
        // 压缩损坏数据由 Inflater 抛出（消息非空，JDK 一致为 zlib 文本）
        byte[] garbage = new byte[16];
        for (int i = 0; i < garbage.length; i++) garbage[i] = (byte) (i * 7 + 1);
        Inflater inf = new Inflater();
        inf.setInput(garbage);
        try {
            inf.inflate(new byte[64]);
            check(false, "corrupt input throws DataFormatException");
        } catch (DataFormatException e) {
            check(e.getMessage() != null, "DataFormatException message present: " + e.getMessage());
        }
        inf.end();
        // 无输入 inflate -> 返回 0 且 needsInput（不抛）
        Inflater empty = new Inflater();
        check(empty.inflate(new byte[8]) == 0, "no-input inflate returns 0");
        check(empty.needsInput(), "no-input inflate leaves needsInput true");
        empty.end();
    }

    public static void main(String[] args) {
        testDataFormatException();
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

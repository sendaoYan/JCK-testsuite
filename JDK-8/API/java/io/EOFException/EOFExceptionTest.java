/*
 * @test
 * @summary java.io.EOFException conformance (pure black-box): constructors; real
 *          triggers from DataInputStream primitive reads, RandomAccessFile reads
 *          past EOF, ObjectInputStream.readUTF past the end.
 * @run main EOFExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

public class EOFExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Throwing { void run() throws java.io.IOException; }

    static void testBlackBox() throws Exception {
        // 1. constructors
        EOFException e0 = new EOFException();
        check(e0.getMessage() == null && e0.getCause() == null, "no-arg: nulls");
        check("unexpected end".equals(new EOFException("unexpected end").getMessage()),
              "(String) message");

        // 2. trigger: multi-byte primitive read hits EOF mid-value
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(new byte[]{1, 2}));
        try {
            d.readInt();
            check(false, "readInt with 2 bytes must throw EOFException");
        } catch (EOFException expected) {
            passed++;
        }

        // 3. trigger: every wide primitive type on a fully empty stream
        byte[] empty = new byte[0];
        for (Throwing r : new Throwing[]{
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readLong(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readDouble(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readChar(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readUTF(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readFully(new byte[1])}) {
            try {
                r.run();
                check(false, "expected EOFException");
            } catch (java.io.IOException ex) {
                check(ex instanceof EOFException, "EOFException from " + ex.getClass().getSimpleName());
            }
        }

        // 3b. contrast: readLine and skipBytes signal EOF without an exception
        check(new DataInputStream(new ByteArrayInputStream(empty)).readLine() == null,
              "readLine returns null at EOF");
        check(new DataInputStream(new ByteArrayInputStream(empty)).skipBytes(4) == 0,
              "skipBytes returns 0 at EOF");

        // 4. trigger: readFully consumes what it can, then reports EOF
        byte[] buf = new byte[4];
        java.util.Arrays.fill(buf, (byte) 7);
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[]{9})).readFully(buf);
            check(false, "readFully short stream must throw");
        } catch (EOFException expected) {
            check(buf[0] == 9 && buf[3] == 7,
                  "readFully filled available bytes, left the rest untouched");
        }

        // 5. trigger: RandomAccessFile.readByte/readInt past end of file
        File tmp = File.createTempFile("eofx", ".dat");
        try {
            try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
                raf.writeShort(0x1234);
                raf.seek(0);                           // pointer sits at EOF after the write
                check(raf.readShort() == 0x1234, "short reads back");
                try {
                    raf.readInt();                     // needs 4 bytes, only 0 left
                    check(false, "readInt at EOF must throw");
                } catch (EOFException expected) {
                    passed++;
                }
                try {
                    raf.readByte();
                    check(false, "readByte at EOF must throw");
                } catch (EOFException expected) {
                    passed++;
                }
                check(raf.readLine() == null, "RandomAccessFile.readLine null at EOF");
                raf.seek(0);
                try {
                    raf.readInt();                     // only 2 bytes available
                    check(false, "partially available readInt must throw");
                } catch (EOFException expected) {
                    passed++;
                }
            }
        } finally {
            tmp.delete();
        }

        // 6. trigger: ObjectInputStream.readUTF after the payload object is exhausted
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeUTF("only");
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("only".equals(ois.readUTF()), "payload first");
            try {
                ois.readInt();
                check(false, "readInt past stream end must throw");
            } catch (EOFException expected) {
                passed++;
            }
        }

        // 7. negative contrast: -1 is returned by InputStream.read at EOF, not an exception
        java.io.File tmp2 = File.createTempFile("eofx2", ".dat");
        try (FileInputStream fin = new FileInputStream(tmp2)) {
            check(fin.read() == -1, "raw InputStream.read returns -1 at EOF");
            check(fin.read(new byte[1]) == -1, "raw bulk read returns -1 at EOF");
        } finally {
            tmp2.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

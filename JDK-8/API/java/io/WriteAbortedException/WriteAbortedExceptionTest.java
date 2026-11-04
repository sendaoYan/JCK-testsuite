/*
 * @test
 * @summary java.io.WriteAbortedException conformance (pure black-box): the public
 *          detail field, composed getMessage, getCause == detail; real triggers:
 *          a custom writeObject throwing mid-write, and a non-Serializable field
 *          poisoning the stream — both surface on the read side.
 * @run main WriteAbortedExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.WriteAbortedException;

public class WriteAbortedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class ExplodingWrite implements Serializable {
        private static final long serialVersionUID = 750L;
        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new IOException("disk died mid-write");
        }
    }

    static class Wrapper implements Serializable {
        private static final long serialVersionUID = 751L;
        Object payload;                     // non-Serializable value poisons the stream
        Wrapper(Object payload) { this.payload = payload; }
    }

    static void testBlackBox() throws Exception {
        // 1. constructor, detail field, composed message, cause
        Exception root = new IOException("root cause");
        WriteAbortedException wae = new WriteAbortedException("write aborted", root);
        check("write aborted; java.io.IOException: root cause".equals(wae.getMessage()),
              "getMessage = detail message + \"; \" + detail: " + wae.getMessage());
        check(wae.detail == root, "detail field holds the write-side exception");
        check(wae.getCause() == root, "getCause returns detail");
        check(wae.detail instanceof Exception, "detail is an Exception");

        // 2. real trigger: custom writeObject throwing aborts the write; the partially
        //    written stream read back surfaces WriteAbortedException wrapping the cause
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(new ExplodingWrite());
            check(false, "throwing writeObject must abort the write");
        } catch (IOException expected) {
            check("disk died mid-write".equals(expected.getMessage()),
                  "write side propagates the original IOException");
        }
        oos.flush();
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            check(false, "aborted stream must fail on read");
        } catch (WriteAbortedException readSide) {
            check(readSide.detail instanceof IOException
                      && "disk died mid-write".equals(readSide.detail.getMessage()),
                  "read side wraps the original IOException: " + readSide.detail);
        }

        // 3. real trigger: non-Serializable member poisons the stream mid-write
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(bos2);
        try {
            oos2.writeObject(new Wrapper(new Object()));
            check(false, "non-Serializable member must abort the write");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
        oos2.flush();
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()))) {
            ois.readObject();
            check(false, "poisoned stream must fail on read");
        } catch (WriteAbortedException readSide) {
            check(readSide.detail instanceof java.io.NotSerializableException,
                  "NotSerializableException wrapped as detail: " + readSide.detail);
        }

        // 4. caught as ObjectStreamException
        try {
            throw new WriteAbortedException("m", root);
        } catch (ObjectStreamException ose) {
            check(ose instanceof WriteAbortedException, "catchable as ObjectStreamException");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

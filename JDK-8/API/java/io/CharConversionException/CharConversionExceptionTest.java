/*
 * @test
 * @summary java.io.CharConversionException conformance (pure black-box):
 *          constructors, message retention, checked hierarchy, propagation through
 *          Reader signatures, serialization of the exception.
 * @run main CharConversionExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;

public class CharConversionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructors and message retention
        check(new CharConversionException().getMessage() == null, "no-arg message null");
        CharConversionException cce = new CharConversionException("bad byte at 5");
        check("bad byte at 5".equals(cce.getMessage()), "(String) message kept");
        check(cce.getCause() == null, "no cause by default");

        // 2. caught as IOException / Exception
        try {
            throw cce;
        } catch (IOException io) {
            check(io == cce, "CharConversionException catchable as IOException");
        }

        // 3. propagates out of Reader signatures that declare IOException
        class HalfDecoded extends Reader {
            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                throw new CharConversionException("decoder choked");
            }
            @Override public void close() {}
        }
        try {
            new HalfDecoded().read(new char[1]);
            check(false, "decoder failure must propagate");
        } catch (IOException io) {
            check(io instanceof CharConversionException
                  && "decoder choked".equals(io.getMessage()),
                  "conversion failure surfaces with its message");
        }

        // 4. the exception itself serializes keeping its message
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new CharConversionException("kept"));
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("kept".equals(((CharConversionException) ois.readObject()).getMessage()),
                  "message survives serialization");
        }

        // 5. contrast: a valid decode path completes without the exception
        java.io.BufferedReader br = new java.io.BufferedReader(
            new InputStreamReader(new ByteArrayInputStream("ok".getBytes("UTF-8")), "UTF-8"));
        check("ok".equals(br.readLine()), "valid decoding completes normally");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

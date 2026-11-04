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
import java.io.ObjectStreamClass;
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

    // ================= 深化追加（契约级） =================

    static byte[] serOf(Object o) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    static Object back(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    // ---- Throwable protocol matrix on the two-ctor surface ----
    static void testThrowableProtocol() {
        CharConversionException e = new CharConversionException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.CharConversionException: m"),
              "toString = FQCN + \": \" + message");
        check(new CharConversionException().toString().endsWith("CharConversionException"),
              "toString of no-arg instance has no trailing colon");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("C", "read", "C.java", 11)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1 && "C".equals(e.getStackTrace()[0].getClassName())
                  && "read".equals(e.getStackTrace()[0].getMethodName())
                  && e.getStackTrace()[0].getLineNumber() == 11,
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        // cause surface: the class has no cause ctors on either JDK, initCause is the only path
        Exception cause = new IllegalStateException("root");
        CharConversionException c = new CharConversionException("wrap");
        check(c.initCause(cause) == c && c.getCause() == cause,
              "initCause injects the cause and returns this");
        try {
            c.initCause(new Exception());
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        try {
            CharConversionException self = new CharConversionException("self");
            self.initCause(self);
            check(false, "self-causation must throw");
        } catch (IllegalArgumentException expected) {
            check("Self-causation not permitted".equals(expected.getMessage()),
                  "self-causation refused with the specified message");
        }
        // 实测：initCause(null) 在 cause 未设置时合法
        check(new CharConversionException("n").initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        check(new CharConversionException().getSuppressed().length == 0,
              "suppressed list empty by default");
        CharConversionException sup = new CharConversionException("s");
        Exception s1 = new Exception("sup1");
        sup.addSuppressed(s1);
        sup.addSuppressed(new Exception("sup2"));
        check(sup.getSuppressed().length == 2 && sup.getSuppressed()[0] == s1,
              "suppressed kept in insertion order");
        try {
            sup.addSuppressed(sup);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            sup.addSuppressed(null);
            check(false, "addSuppressed(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(CharConversionException.class).getSerialVersionUID()
                  == -8680016352018427031L, "serialVersionUID == -8680016352018427031");
        CharConversionException r = (CharConversionException) back(serOf(new CharConversionException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.CharConversionException: kept"),
              "toString survives round-trip");
        check(r.getCause() == null, "no cause after round-trip when never set");
        CharConversionException chained = new CharConversionException("wrap");
        chained.initCause(new IllegalStateException("root"));
        CharConversionException rc = (CharConversionException) back(serOf(chained));
        check(rc.getCause() instanceof IllegalStateException
                  && "root".equals(rc.getCause().getMessage()),
              "initCause'd cause survives round-trip");
        CharConversionException sup = new CharConversionException("s");
        sup.addSuppressed(new Exception("a"));
        CharConversionException rs = (CharConversionException) back(serOf(sup));
        check(rs.getSuppressed().length == 1 && "a".equals(rs.getSuppressed()[0].getMessage()),
              "suppressed survive round-trip");
        // a set stack trace also survives
        CharConversionException withSt = new CharConversionException("st");
        withSt.setStackTrace(new StackTraceElement[]{new StackTraceElement("S", "m", "S.java", 1)});
        CharConversionException rst = (CharConversionException) back(serOf(withSt));
        check(rst.getStackTrace().length == 1 && "S".equals(rst.getStackTrace()[0].getClassName()),
              "stack trace survives round-trip");
        // no-arg instance round-trips with a still-null message
        check(back(serOf(new CharConversionException())) instanceof CharConversionException
                  && ((CharConversionException) back(serOf(new CharConversionException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
    }

    // ---- writer-side propagation + malformed-input contrast (no CCE from lenient decoders) ----
    static void testPropagationAndContrast() throws Exception {
        java.io.Writer w = new java.io.Writer() {
            @Override public void write(char[] cbuf, int off, int len) throws IOException {
                throw new CharConversionException("encoder choked");
            }
            @Override public void flush() {}
            @Override public void close() {}
        };
        try {
            w.write(new char[1]);
            check(false, "encoder failure must propagate out of Writer");
        } catch (CharConversionException expected) {
            check("encoder choked".equals(expected.getMessage()), "message propagated verbatim");
        }
        // malformed input under a named charset decodes with U+FFFD replacement, not CCE
        InputStreamReader isr = new InputStreamReader(
            new ByteArrayInputStream(new byte[]{(byte) 0x80, 'x'}), "UTF-8");
        check(isr.read() == 0xFFFD && isr.read() == 'x',
              "lenient decode replaces malformed input instead of throwing");
        isr.close();
        // hierarchy: direct child of IOException, checked
        check(CharConversionException.class.getSuperclass() == IOException.class,
              "direct child of IOException");
        try {
            throw new CharConversionException("x");
        } catch (Exception ex) {
            check(ex instanceof CharConversionException && !(ex instanceof RuntimeException),
                  "checked: catchable as Exception, not a RuntimeException");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testPropagationAndContrast();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

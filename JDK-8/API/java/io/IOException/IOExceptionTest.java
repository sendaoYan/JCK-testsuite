/*
 * @test
 * @summary java.io.IOException conformance (pure black-box): checked root of the
 *          java.io exception hierarchy, four constructors; message/cause
 *          composition, initCause, addSuppressed, serialization of the exception
 *          itself, catch semantics against real throwing APIs.
 * @run main IOExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.io.SyncFailedException;
import java.io.UTFDataFormatException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.io.WriteAbortedException;

public class IOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructor/message/cause matrix
        IOException e0 = new IOException();
        check(e0.getMessage() == null && e0.getCause() == null, "no-arg: null message and cause");
        IOException e1 = new IOException("disk on fire");
        check("disk on fire".equals(e1.getMessage()) && e1.getCause() == null,
              "(String): message only");
        Exception cause = new IllegalStateException("root");
        IOException e2 = new IOException("wrapped", cause);
        check("wrapped".equals(e2.getMessage()) && e2.getCause() == cause,
              "(String, Throwable): message + cause");
        IOException e3 = new IOException(cause);
        check(cause.toString().equals(e3.getMessage()) && e3.getCause() == cause,
              "(Throwable): message defaults to cause.toString()");

        // 2. initCause: once only, self-cause refused
        IOException once = new IOException("m");
        check(once.initCause(cause) == once, "initCause returns this");
        try {
            once.initCause(new Exception());
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        IOException self = new IOException("self");
        try {
            self.initCause(self);
            check(false, "initCause(this) must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        check(new IOException().initCause(cause) != null,
              "initCause accepts any non-self Throwable when unset");

        // 3. suppressed exceptions attached to an IOException
        IOException sup = new IOException("during-close");
        Exception s1 = new Exception("supp1"), s2 = new Exception("supp2");
        sup.addSuppressed(s1);
        sup.addSuppressed(s2);
        check(sup.getSuppressed().length == 2 && sup.getSuppressed()[0] == s1
              && sup.getSuppressed()[1] == s2, "suppressed preserved in order");

        // 4. the exception itself serializes (Throwable is Serializable) keeping its message
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new IOException("kept"));
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            IOException back = (IOException) ois.readObject();
            check("kept".equals(back.getMessage()), "message survives serialization");
        }

        // 5. real API throws an IOException subtype catchable as IOException
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[0])).readInt();
            check(false, "readInt on empty stream must throw");
        } catch (IOException io) {                       // EOFException caught as IOException
            check(io instanceof EOFException, "EOFException is an IOException");
        }

        // 6. multi-catch and try-with-resources compatibility
        try (java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(new byte[1])) {
            check(in.read() == 0, "resource usable");
        }

        // 7. toString/stack trace capture the class name and message
        IOException t = new IOException("xyz");
        check(t.toString().equals("java.io.IOException: xyz"), "toString format");
        check(t.getStackTrace().length > 0, "stack trace captured at construction");
        passed++;
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

    // ---- constructor edge cases: null message / null cause combinations ----
    static void testConstructorEdges() {
        IOException e = new IOException("m", null);
        check("m".equals(e.getMessage()) && e.getCause() == null,
              "(String, null): message kept verbatim, cause null");
        IOException e2 = new IOException((Throwable) null);
        check(e2.getMessage() == null && e2.getCause() == null,
              "(Throwable) null: message and cause both null");
        RuntimeException rt = new RuntimeException("rt");
        IOException e3 = new IOException(rt);
        check(e3.getCause() == rt && rt.toString().equals(e3.getMessage()),
              "(Throwable): message == cause.toString()");
        IOException e4 = new IOException("outer", e3);
        check(e4.getCause() == e3 && e4.getCause().getCause() == rt,
              "chained causes keep identity two levels deep");
        check(new IOException("").getMessage().isEmpty(), "empty-string message is legal and distinct from null");
    }

    // ---- Throwable protocol matrix applied to IOException ----
    static void testThrowableProtocol() {
        IOException e = new IOException("m");
        check(e.getMessage().equals(e.getLocalizedMessage()),
              "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.IOException: m"), "toString = FQCN + \": \" + message");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured at construction");
        StackTraceElement[] st = {new StackTraceElement("A", "op", "A.java", 7)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "A".equals(e.getStackTrace()[0].getClassName())
                  && "op".equals(e.getStackTrace()[0].getMethodName())
                  && "A.java".equals(e.getStackTrace()[0].getFileName())
                  && e.getStackTrace()[0].getLineNumber() == 7,
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        // 实测：initCause(null) 在 cause 未设置时合法（置 null 不抛）；
        // 一旦设置过 cause，任何二次 initCause（含 null）都是 IllegalStateException
        check(new IOException("n").initCause(null) != null
                  && new IOException("n2").initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        IOException onceSet = new IOException("c");
        onceSet.initCause(rt0);
        try {
            onceSet.initCause(null);
            check(false, "second initCause(null) after a set cause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new IOException().getSuppressed().length == 0, "suppressed list empty by default");
    }

    static final RuntimeException rt0 = new RuntimeException("r0");

    // ---- serialization contract: message/cause/suppressed/stackTrace + serialVersionUID ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(IOException.class).getSerialVersionUID()
                  == 7818375828146090155L, "serialVersionUID == 7818375828146090155");
        IOException base = (IOException) back(serOf(new IOException("kept")));
        check("kept".equals(base.getMessage()) && base.getCause() == null,
              "no-cause instance round-trips with message");
        check(base.toString().equals("java.io.IOException: kept"), "toString stable across round-trip");
        IOException chained = new IOException("outer", new IllegalStateException("inner"));
        IOException r = (IOException) back(serOf(chained));
        check(r.getCause() instanceof IllegalStateException
                  && "inner".equals(r.getCause().getMessage()),
              "cause survives round-trip with type and message");
        check(r.getCause() != chained.getCause(), "cause is a deserialized copy, identity not kept");
        IOException sup = new IOException("s");
        sup.addSuppressed(new IllegalStateException("s1"));
        sup.addSuppressed(new UnsupportedOperationException("s2"));
        IOException rs = (IOException) back(serOf(sup));
        check(rs.getSuppressed().length == 2, "both suppressed exceptions survive round-trip");
        check("s1".equals(rs.getSuppressed()[0].getMessage())
                  && "s2".equals(rs.getSuppressed()[1].getMessage()),
              "suppressed order preserved after round-trip");
        IOException st = new IOException("st");
        st.setStackTrace(new StackTraceElement[]{new StackTraceElement("K", "m", "K.java", 3)});
        IOException rst = (IOException) back(serOf(st));
        check(rst.getStackTrace().length == 1 && "K".equals(rst.getStackTrace()[0].getClassName()),
              "stack trace survives round-trip");
    }

    // ---- the family tree: every java.io serialization/io exception is an IOException ----
    static void testFamilyHierarchy() {
        Class<?>[] family = {
            CharConversionException.class, EOFException.class, FileNotFoundException.class,
            InterruptedIOException.class, ObjectStreamException.class, SyncFailedException.class,
            UTFDataFormatException.class, UnsupportedEncodingException.class,
            InvalidClassException.class, InvalidObjectException.class, NotActiveException.class,
            NotSerializableException.class, OptionalDataException.class,
            StreamCorruptedException.class, WriteAbortedException.class};
        for (Class<?> c : family) {
            check(IOException.class.isAssignableFrom(c), c.getSimpleName() + " is an IOException");
            check(Exception.class.isAssignableFrom(c), c.getSimpleName() + " is checked (an Exception)");
        }
        check(!IOException.class.isAssignableFrom(UncheckedIOException.class),
              "UncheckedIOException is a RuntimeException, not an IOException");
        check(!IOException.class.isAssignableFrom(IOError.class),
              "IOError is an Error, not an IOException");
        check(FileNotFoundException.class.getSuperclass() == IOException.class,
              "FileNotFoundException is a direct child");
        check(InterruptedIOException.class.getSuperclass() == IOException.class,
              "InterruptedIOException is a direct child");
        check(ObjectStreamException.class.getSuperclass() == IOException.class,
              "ObjectStreamException is a direct child");
        check(SyncFailedException.class.getSuperclass() == IOException.class,
              "SyncFailedException is a direct child");
        check(UTFDataFormatException.class.getSuperclass() == IOException.class,
              "UTFDataFormatException is a direct child");
        check(UnsupportedEncodingException.class.getSuperclass() == IOException.class,
              "UnsupportedEncodingException is a direct child");
        check(InvalidClassException.class.getSuperclass() == ObjectStreamException.class,
              "InvalidClassException sits under ObjectStreamException");
        check(WriteAbortedException.class.getSuperclass() == ObjectStreamException.class,
              "WriteAbortedException sits under ObjectStreamException");
        check(OptionalDataException.class.getSuperclass() == ObjectStreamException.class,
              "OptionalDataException sits under ObjectStreamException");
        check(InvalidObjectException.class.getSuperclass() == ObjectStreamException.class,
              "InvalidObjectException sits under ObjectStreamException");
        try {
            throw new FileNotFoundException("miss");
        } catch (FileNotFoundException e) {
            check(e instanceof FileNotFoundException, "precise subtype catch arm wins");
        } catch (IOException e) {
            check(false, "the specific catch arm must win");
        }
        try {
            throw new EOFException("eof");
        } catch (IOException e) {
            check(e instanceof EOFException && !(e instanceof FileNotFoundException),
                  "root catch sees the exact runtime subtype");
        }
    }

    // ---- additional real-API surfaces catchable through the family root ----
    static void testRealThrowSurfaces() {
        try {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4}));
            check(false, "garbage header must throw");
        } catch (IOException io) {
            check(io instanceof java.io.StreamCorruptedException,
                  "OIS header failure surfaces as an IOException subtype");
        }
        try {
            new String(new byte[1], "jck-NoSuchCharset-1");
            check(false, "unknown charset must throw");
        } catch (IOException io) {
            check(io instanceof UnsupportedEncodingException,
                  "charset failure surfaces as an IOException subtype");
        }
        IOException nested = new IOException("level1", new IOException("level2", new IOException("level3")));
        check("level2".equals(nested.getCause().getMessage())
                  && "level3".equals(nested.getCause().getCause().getMessage()),
              "deep cause chains keep every level");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testConstructorEdges();
        testThrowableProtocol();
        testSerializationContract();
        testFamilyHierarchy();
        testRealThrowSurfaces();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

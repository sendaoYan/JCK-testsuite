/*
 * @test
 * @summary java.lang.module.InvalidModuleDescriptorException conformance: white-box + black-box behavior.
 * @run main InvalidModuleDescriptorExceptionTest
 */

import java.lang.module.InvalidModuleDescriptorException;
import java.lang.module.ModuleDescriptor;

public class InvalidModuleDescriptorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.InvalidModuleDescriptorException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 2, "exactly two public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredField("serialVersionUID").getType() == long.class,
              "serialVersionUID declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(new InvalidModuleDescriptorException() instanceof RuntimeException,
              "is a RuntimeException");

        InvalidModuleDescriptorException e0 = new InvalidModuleDescriptorException();
        check(e0.getMessage() == null, "no-arg message null");
        check(e0.getCause() == null, "no-arg cause null");

        InvalidModuleDescriptorException e1 =
                new InvalidModuleDescriptorException("bad descriptor");
        check("bad descriptor".equals(e1.getMessage()), "String ctor message");
        check(e1.getCause() == null, "String ctor no cause");

        check(e1.toString().startsWith(
                      "java.lang.module.InvalidModuleDescriptorException: bad descriptor"),
              "toString format");
        e1.addSuppressed(new IllegalStateException("s"));
        check(e1.getSuppressed().length == 1, "suppressed supported");
        // message-only constructor never carries a cause; initCause is available
        RuntimeException c = new RuntimeException("c");
        e1.initCause(c);
        check(e1.getCause() == c, "initCause sets cause");
    }

    // ---- Part 3: contract deepening (2026-11) ----

    static InvalidModuleDescriptorException roundTrip(InvalidModuleDescriptorException e)
            throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(e);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return (InvalidModuleDescriptorException) ois.readObject();
        }
    }

    static void testThrowableMechanics() throws Exception {
        InvalidModuleDescriptorException e = new InvalidModuleDescriptorException("bad");
        check("java.lang.module.InvalidModuleDescriptorException: bad".equals(e.toString()),
                "exact toString");
        InvalidModuleDescriptorException back = roundTrip(e);
        check(back.getMessage().equals("bad"), "serialization keeps message");
        check(back instanceof InvalidModuleDescriptorException, "serialization keeps class");
        check(e.getSuppressed().length == 0, "default suppressed empty");
        IllegalStateException s1 = new IllegalStateException("s1");
        e.addSuppressed(s1);
        check(e.getSuppressed().length == 1 && e.getSuppressed()[0] == s1, "suppressed kept");
        Throwable[] copy = e.getSuppressed();
        copy[0] = null;
        check(e.getSuppressed()[0] == s1, "getSuppressed defensive copy");
        StackTraceElement[] st = {new StackTraceElement("C", "m", "C.java", 4)};
        e.setStackTrace(st);
        check(e.getStackTrace()[0].getClassName().equals("C"), "setStackTrace round trip");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintStream(bos));
        check(bos.toString("UTF-8")
                      .contains("java.lang.module.InvalidModuleDescriptorException: bad"),
                "printStackTrace prefix");
        check(new InvalidModuleDescriptorException().toString()
                      .equals("java.lang.module.InvalidModuleDescriptorException"),
                "no-arg toString");
        check(InvalidModuleDescriptorException.class.getPackageName()
                      .equals("java.lang.module"), "package");
        check(java.io.Serializable.class
                      .isAssignableFrom(InvalidModuleDescriptorException.class), "Serializable");
        check(e.getLocalizedMessage().equals("bad"), "getLocalizedMessage default");
        // exactly two constructors: no Throwable-taking forms exist
        boolean hasCauseCtor = false;
        for (java.lang.reflect.Constructor<?> ct
                : InvalidModuleDescriptorException.class.getDeclaredConstructors()) {
            for (Class<?> p : ct.getParameterTypes()) {
                if (p == Throwable.class) hasCauseCtor = true;
            }
        }
        check(!hasCauseCtor, "no Throwable constructor");
        check(e.getCause() == null, "String ctor leaves cause null");
        check(e.getStackTrace().length >= 1, "stack trace captured");
        RuntimeException c = new RuntimeException("c");
        check(e.initCause(c) == e && e.getCause() == c, "initCause after construction");
        try {
            e.initCause(new RuntimeException("x"));
            check(false, "re-initCause ISE");
        } catch (IllegalStateException ex) {
            check(true, "re-initCause ISE");
        }
    }

    // real triggers: ModuleDescriptor.read on malformed inputs
    static void testRealTriggers() throws Exception {
        byte[] badVersion = new byte[] {
            (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
            0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x42, 0x01, 0x02, 0x03 };
        try {
            ModuleDescriptor.read(new java.io.ByteArrayInputStream(badVersion));
            check(false, "bad class version must throw");
        } catch (InvalidModuleDescriptorException e) {
            check("Unsupported major.minor version 0.3".equals(e.getMessage()),
                    "bad version exact message");
        }
        try {
            ModuleDescriptor.read(new java.io.ByteArrayInputStream(new byte[0]));
            check(false, "empty input must throw");
        } catch (InvalidModuleDescriptorException e) {
            check("Truncated module-info.class".equals(e.getMessage()),
                    "truncated exact message");
        }
        try (java.io.InputStream is = String.class.getResourceAsStream("/java/lang/String.class")) {
            try {
                ModuleDescriptor.read(is);
                check(false, "non-module classfile must throw");
            } catch (InvalidModuleDescriptorException e) {
                check("access_flags should be ACC_MODULE".equals(e.getMessage()),
                        "ACC_MODULE exact message");
            }
        }
        try {
            ModuleDescriptor.read((java.io.InputStream) null);
            check(false, "read(null) NPE");
        } catch (NullPointerException e) {
            check(true, "read(null) NPE");
        }
        try {
            ModuleDescriptor.read(java.nio.ByteBuffer.allocate(0));
            check(false, "empty ByteBuffer must throw");
        } catch (InvalidModuleDescriptorException e) {
            check("Truncated module-info.class".equals(e.getMessage()),
                    "empty ByteBuffer exact message");
        }
        // a valid read still works right after the failures (no global state)
        try (java.io.InputStream in = String.class.getModule()
                .getResourceAsStream("module-info.class")) {
            check("java.base".equals(ModuleDescriptor.read(in).name()),
                    "healthy read unaffected by earlier failures");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableMechanics();
        testRealTriggers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

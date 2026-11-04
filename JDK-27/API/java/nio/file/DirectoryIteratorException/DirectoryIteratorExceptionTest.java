/*
 * @test
 * @summary java.nio.file.DirectoryIteratorException conformance: white-box structure + black-box behavior.
 * @run main DirectoryIteratorExceptionTest
 */

import java.nio.file.DirectoryIteratorException;

public class DirectoryIteratorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(DirectoryIteratorException.class), "hierarchy: extends RuntimeException");

        java.io.IOException cause = new java.io.IOException("boom");
        DirectoryIteratorException e = new DirectoryIteratorException(cause);
        check(e.getCause() == cause, "cause stored");
        check(e instanceof java.util.ConcurrentModificationException,
              "hierarchy: extends ConcurrentModificationException");
        check(e.getCause() instanceof java.io.IOException, "cause is an IOException");
        boolean npe = false;
        try { new DirectoryIteratorException(null); }
        catch (NullPointerException x) { npe = true; }
        check(npe, "null cause -> NullPointerException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Files.writeString(dir.resolve("a.txt"), "a");
            // a filter failing with IOException surfaces wrapped at iteration time
            java.io.IOException boom = new java.io.IOException("boom");
            java.nio.file.DirectoryStream<java.nio.file.Path> stream =
                    java.nio.file.Files.newDirectoryStream(dir, entry -> { throw boom; });
            boolean wrapped = false;
            try {
                stream.iterator().hasNext();
            } catch (DirectoryIteratorException e) {
                wrapped = e.getCause() == boom;
            } finally {
                stream.close();
            }
            check(wrapped, "filter IOException -> DirectoryIteratorException wrapping it");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("a.txt"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }


    // ---- batch A11 shared helpers ----
    static void checkThrowsExact(Class<?> expected, Runnable action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (t.getClass() != expected)
                throw new AssertionError(msg + ": expected " + expected.getName()
                        + " but got " + t.getClass().getName(), t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    interface IOAct { void run() throws java.io.IOException; }

    static void checkThrowsExactIO(Class<?> expected, IOAct action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (t.getClass() != expected)
                throw new AssertionError(msg + ": expected " + expected.getName()
                        + " but got " + t.getClass().getName(), t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static Object serialRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            return ois.readObject();
        }
    }

    static void deleteTree(java.nio.file.Path p) {
        try {
            if (java.nio.file.Files.isDirectory(p)) {
                try (java.nio.file.DirectoryStream<java.nio.file.Path> ds =
                        java.nio.file.Files.newDirectoryStream(p)) {
                    for (java.nio.file.Path c : ds) deleteTree(c);
                }
            }
            java.nio.file.Files.deleteIfExists(p);
        } catch (Exception ignore) { }
    }

    // ==== batch A11: constructor / cause chain / serialization / filter triggers ====
    static void testContractHierarchyAndCtors() throws Exception {
        check((DirectoryIteratorException.class.getModifiers()
                & java.lang.reflect.Modifier.FINAL) != 0, "class is final");
        check(DirectoryIteratorException.class.getSuperclass()
                == java.util.ConcurrentModificationException.class,
                "direct superclass is ConcurrentModificationException");
        check(RuntimeException.class.isAssignableFrom(DirectoryIteratorException.class),
                "caught as RuntimeException");
        check(!java.io.IOException.class.isAssignableFrom(DirectoryIteratorException.class),
                "NOT an IOException (unchecked by design)");
        check(java.io.Serializable.class.isAssignableFrom(DirectoryIteratorException.class),
                "Serializable");
        check(DirectoryIteratorException.class.getMethod("getCause").getReturnType()
                == java.io.IOException.class, "getCause covariantly returns IOException");
        check(DirectoryIteratorException.class.getConstructors().length == 1,
                "single public (IOException) constructor");
        checkThrowsExact(NullPointerException.class,
                () -> new DirectoryIteratorException(null), "null cause -> NullPointerException");
        java.io.IOException boom = new java.io.IOException("boom");
        DirectoryIteratorException e = new DirectoryIteratorException(boom);
        check(e.getCause() == boom, "cause identity preserved");
        check(e.getCause().getClass() == java.io.IOException.class,
                "getCause statically typed IOException");
        check(e.getMessage().equals(boom.toString()), "message == cause.toString()");
        check(e.toString().equals(
                "java.nio.file.DirectoryIteratorException: java.io.IOException: boom"),
                "toString embeds cause");
        checkThrowsExact(IllegalStateException.class,
                () -> e.initCause(new java.io.IOException("x")),
                "cause fixed at construction; initCause rejected");
        check(java.io.ObjectStreamClass.lookup(DirectoryIteratorException.class)
                .getSerialVersionUID() == -6012699886086212874L,
                "serialVersionUID == -6012699886086212874");
        Object back = serialRoundTrip(e);
        check(back instanceof DirectoryIteratorException, "round-trip preserves class");
        DirectoryIteratorException r = (DirectoryIteratorException) back;
        check(r.getCause().getClass() == java.io.IOException.class
                && "boom".equals(r.getCause().getMessage()),
                "round-trip keeps the IOException cause");
    }

    static void testContractRealTrigger() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("a11");
        try {
            java.nio.file.Files.createFile(dir.resolve("a.txt"));
            final java.io.IOException ioe = new java.io.IOException("io-boom");
            java.nio.file.DirectoryStream<java.nio.file.Path> s1 =
                    java.nio.file.Files.newDirectoryStream(dir,
                            new java.nio.file.DirectoryStream.Filter<java.nio.file.Path>() {
                                public boolean accept(java.nio.file.Path p)
                                        throws java.io.IOException { throw ioe; }
                            });
            try {
                s1.iterator().hasNext();
                check(false, "IOException from filter must surface wrapped");
            } catch (DirectoryIteratorException d) {
                check(d.getCause() == ioe, "wrapped cause is the filter IOException");
            } finally {
                s1.close();
            }
            java.nio.file.DirectoryStream<java.nio.file.Path> s2 =
                    java.nio.file.Files.newDirectoryStream(dir,
                            new java.nio.file.DirectoryStream.Filter<java.nio.file.Path>() {
                                public boolean accept(java.nio.file.Path p)
                                        throws java.io.IOException { throw ioe; }
                            });
            try {
                s2.iterator().next();
                check(false, "next() must surface the wrapped IOException too");
            } catch (DirectoryIteratorException d) {
                check(d.getCause() == ioe, "next() wrapping identical");
            } finally {
                s2.close();
            }
            // a RuntimeException from the filter propagates AS-IS (no wrapping)
            final java.lang.IllegalStateException rte = new IllegalStateException("rte");
            java.nio.file.DirectoryStream<java.nio.file.Path> s3 =
                    java.nio.file.Files.newDirectoryStream(dir,
                            new java.nio.file.DirectoryStream.Filter<java.nio.file.Path>() {
                                public boolean accept(java.nio.file.Path p) { throw rte; }
                            });
            try {
                try {
                    s3.iterator().next();
                    check(false, "RuntimeException from filter must propagate");
                } catch (DirectoryIteratorException d) {
                    check(false, "RuntimeException must NOT be wrapped in DIE");
                } catch (IllegalStateException t) {
                    check(t == rte, "same RuntimeException object propagated unwrapped");
                }
            } finally {
                s3.close();
            }
            // control: accepting filter iterates fine
            java.nio.file.DirectoryStream<java.nio.file.Path> s4 =
                    java.nio.file.Files.newDirectoryStream(dir,
                            new java.nio.file.DirectoryStream.Filter<java.nio.file.Path>() {
                                public boolean accept(java.nio.file.Path p) { return true; }
                            });
            try {
                check(s4.iterator().hasNext(), "healthy filter iterates");
            } finally {
                s4.close();
            }
        } finally {
            deleteTree(dir);
        }
    }

    // ==== batch A11: second extra coverage ====
    static void testContractExtra2() throws Exception {
        java.io.IOException nsn = new java.nio.file.NoSuchFileException("f");
        DirectoryIteratorException sub = new DirectoryIteratorException(nsn);
        check(sub.getCause() == nsn && sub.getCause() instanceof java.io.IOException,
                "IOException subclass accepted as cause");
        check((sub.getClass().getModifiers() & java.lang.reflect.Modifier.FINAL) != 0,
                "still final after serialization round-trips in the wild");
        Object back = serialRoundTrip(new DirectoryIteratorException(
                new java.io.IOException("x")));
        check(back.getClass().getMethod("getCause").getReturnType() == java.io.IOException.class,
                "covariant getCause survives round-trip");
        java.io.IOException c1 = new java.io.IOException("a");
        java.io.IOException c2 = new java.io.IOException("a");
        check(new DirectoryIteratorException(c1).toString()
                .equals(new DirectoryIteratorException(c2).toString()),
                "toString equal for equal causes");
        check(new DirectoryIteratorException(c1).getCause() != c2,
                "cause identity, not equality");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractHierarchyAndCtors();
        testContractRealTrigger();
        testContractExtra2();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

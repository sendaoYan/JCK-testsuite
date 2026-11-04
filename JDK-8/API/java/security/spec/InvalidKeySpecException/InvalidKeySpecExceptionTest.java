/*
 * @test
 * @summary java.security.spec.InvalidKeySpecException conformance: constructors, message, cause and superclass.
 * @run main InvalidKeySpecExceptionTest
 */

public class InvalidKeySpecExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.InvalidKeySpecException e = new java.security.spec.InvalidKeySpecException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.spec.InvalidKeySpecException e2 = new java.security.spec.InvalidKeySpecException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.spec.InvalidKeySpecException e3 = new java.security.spec.InvalidKeySpecException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.spec.InvalidKeySpecException e4 = new java.security.spec.InvalidKeySpecException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        // ---- 契约深化：异常构造矩阵/cause 链/suppressed/序列化 ----
        java.security.spec.InvalidKeySpecException excNoArg = new java.security.spec.InvalidKeySpecException();
        check(excNoArg.getMessage() == null && excNoArg.getCause() == null, "no-arg ctor: null message and cause");
        check(new java.security.spec.InvalidKeySpecException((String) null).getMessage() == null, "(null String) ctor keeps message null");
        Throwable root = new IllegalStateException("root");
        java.security.spec.InvalidKeySpecException fromCause = new java.security.spec.InvalidKeySpecException(root);
        check(fromCause.getCause() == root, "(Throwable) ctor preserves cause identity");
        check(root.toString().equals(fromCause.getMessage()), "(Throwable) ctor message defaults to cause.toString()");
        excNoArg.initCause(root);
        check(excNoArg.getCause() == root, "initCause sets cause when constructed without one");
        checkThrows(IllegalStateException.class, () -> excNoArg.initCause(root), "second initCause -> IllegalStateException");
        checkThrows(IllegalStateException.class, () -> fromCause.initCause(root), "initCause on (Throwable)-constructed -> IllegalStateException");
        java.security.spec.InvalidKeySpecException withBoth = new java.security.spec.InvalidKeySpecException("msg2", root);
        check("msg2".equals(withBoth.getMessage()) && withBoth.getCause() == root, "(String,Throwable) preserves both");
        withBoth.addSuppressed(root);
        check(withBoth.getSuppressed().length == 1 && withBoth.getSuppressed()[0] == root, "suppressed exception retained");
        java.lang.StackTraceElement[] st = {new java.lang.StackTraceElement("Foo", "bar", "Foo.java", 1)};
        withBoth.setStackTrace(st);
        check(withBoth.getStackTrace().length > 0 && withBoth.getStackTrace()[0].getMethodName().equals("bar"), "setStackTrace honored");
        check(java.security.spec.InvalidKeySpecException.class.getSuperclass() == java.security.GeneralSecurityException.class, "direct superclass GeneralSecurityException");
        check(withBoth instanceof java.io.Serializable, "Serializable");
        byte[] excSer = serializeToBytes(withBoth);
        java.security.spec.InvalidKeySpecException back = (java.security.spec.InvalidKeySpecException) deserializeFromBytes(excSer);
        check("msg2".equals(back.getMessage()), "serialization round-trip preserves message");
        check(back.getCause() instanceof IllegalStateException && "root".equals(back.getCause().getMessage()), "round-trip preserves cause type and message");
        check(withBoth.toString().contains("msg2") && withBoth.toString().contains("InvalidKeySpecException"), "toString contains class and message");
        check("boom9".equals(new java.security.spec.InvalidKeySpecException("boom9").getLocalizedMessage()), "getLocalizedMessage == message");
        // ---- 契约深化补充：默认状态 ----
        check(new java.security.spec.InvalidKeySpecException("only-msg").getCause() == null, "String ctor leaves cause null");
        check(new java.security.spec.InvalidKeySpecException("only-msg").getSuppressed().length == 0, "fresh exception has no suppressed");
        check(new java.security.spec.InvalidKeySpecException("only-msg").getStackTrace().length >= 0, "stack trace accessible");
        java.security.spec.InvalidKeySpecException nullCause = new java.security.spec.InvalidKeySpecException("m", null);
        check("m".equals(nullCause.getMessage()) && nullCause.getCause() == null, "(String,null) permitted: cause stays null");
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- helpers for contract deepening (appended) ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r, String sub, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(sub),
                        msg + ": message contains \"" + sub + "\" (actual: " + t.getMessage() + ")");
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static byte[] serializeToBytes(Object o) {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(o);
            return bos.toByteArray();
        } catch (java.io.IOException e) {
            throw new AssertionError("serialize failed: " + e, e);
        }
    }

    static Object deserializeFromBytes(byte[] b) {
        try {
            return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b)).readObject();
        } catch (Exception e) {
            throw new AssertionError("deserialize failed: " + e, e);
        }
    }
}

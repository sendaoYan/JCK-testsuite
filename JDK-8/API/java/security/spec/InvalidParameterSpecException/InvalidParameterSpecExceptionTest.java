/*
 * @test
 * @summary java.security.spec.InvalidParameterSpecException conformance: constructors, message, cause and superclass.
 * @run main InvalidParameterSpecExceptionTest
 */

public class InvalidParameterSpecExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.InvalidParameterSpecException e = new java.security.spec.InvalidParameterSpecException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        java.security.spec.InvalidParameterSpecException e4 = new java.security.spec.InvalidParameterSpecException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        // ---- 契约深化：异常构造矩阵/cause 链/suppressed/序列化 ----
        java.security.spec.InvalidParameterSpecException excNoArg = new java.security.spec.InvalidParameterSpecException();
        check(excNoArg.getMessage() == null && excNoArg.getCause() == null, "no-arg ctor: null message and cause");
        check(new java.security.spec.InvalidParameterSpecException((String) null).getMessage() == null, "(null String) ctor keeps message null");
        // 契约事实：InvalidParameterSpecException 只有两个构造器 () 和 (String)——无 cause 构造器（区别于 InvalidKeySpecException）
        check(java.security.spec.InvalidParameterSpecException.class.getConstructors().length == 2, "exactly two public constructors: () and (String)");
        check(new java.security.spec.InvalidParameterSpecException("x").getCause() == null, "no cause constructor: getCause null after String ctor");
        Throwable root = new IllegalStateException("root");
        excNoArg.initCause(root);
        check(excNoArg.getCause() == root, "initCause attaches cause when constructed without one");
        checkThrows(IllegalStateException.class, () -> excNoArg.initCause(root), "second initCause -> IllegalStateException");
        java.security.spec.InvalidParameterSpecException withMsg = new java.security.spec.InvalidParameterSpecException("msg2");
        withMsg.addSuppressed(root);
        check(withMsg.getSuppressed().length == 1 && withMsg.getSuppressed()[0] == root, "suppressed exception retained");
        java.lang.StackTraceElement[] st = {new java.lang.StackTraceElement("Foo", "bar", "Foo.java", 1)};
        withMsg.setStackTrace(st);
        check(withMsg.getStackTrace().length > 0 && withMsg.getStackTrace()[0].getMethodName().equals("bar"), "setStackTrace honored");
        check(java.security.spec.InvalidParameterSpecException.class.getSuperclass() == java.security.GeneralSecurityException.class, "direct superclass GeneralSecurityException");
        check(withMsg instanceof java.io.Serializable, "Serializable");
        byte[] excSer = serializeToBytes(withMsg);
        java.security.spec.InvalidParameterSpecException back = (java.security.spec.InvalidParameterSpecException) deserializeFromBytes(excSer);
        check("msg2".equals(back.getMessage()), "serialization round-trip preserves message");
        check(withMsg.toString().contains("msg2") && withMsg.toString().contains("InvalidParameterSpecException"), "toString contains class and message");
        check("boom9".equals(new java.security.spec.InvalidParameterSpecException("boom9").getLocalizedMessage()), "getLocalizedMessage == message");
        // ---- 契约深化补充：默认状态 ----
        check(new java.security.spec.InvalidParameterSpecException("only-msg").getCause() == null, "String ctor leaves cause null");
        check(new java.security.spec.InvalidParameterSpecException("only-msg").getSuppressed().length == 0, "fresh exception has no suppressed");
        check(new java.security.spec.InvalidParameterSpecException("only-msg").getStackTrace().length >= 0, "stack trace accessible");
        check(new java.security.spec.InvalidParameterSpecException((String) null).getMessage() == null, "(null String) message stays null");
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

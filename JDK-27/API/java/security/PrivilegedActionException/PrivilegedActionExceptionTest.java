/*
 * @test
 * @summary java.security.PrivilegedActionException conformance: white-box structure + black-box behavior.
 * @run main PrivilegedActionExceptionTest
 */

import java.security.PrivilegedActionException;
import java.security.*;
import java.util.*;

public class PrivilegedActionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Exception cause = new Exception("root");
        PrivilegedActionException e = new PrivilegedActionException(cause);
        check(cause == e.getException(), "getException");
        check(e.getCause() == cause, "getCause");
        check(e instanceof Exception, "extends Exception");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Exception cause = new java.io.IOException("root-cause");
        PrivilegedActionException pae = new PrivilegedActionException(cause);
        check(pae.getException() == cause, "getException");
        check(pae.getCause() == cause, "getCause");
        check(pae.toString().contains("java.io.IOException"), "toString");
        // real wrapping by doPrivileged produces the same shape
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> { throw new CloneNotSupportedException("cn"); });
            check(false, "expected PAE");
        } catch (PrivilegedActionException real) {
            check(real.getException() instanceof CloneNotSupportedException, "real cause type");
            check("cn".equals(real.getException().getMessage()), "real cause message");
            check(real instanceof Exception, "is Exception");
        }
        // JDK-27 accepts a null exception
        PrivilegedActionException nullCause = new PrivilegedActionException(null);
        check(nullCause.getException() == null, "null exception retained");
        // JDK-27 constructs with a null message (it does not mirror the cause message)
        check(pae.getMessage() == null, "message null by design");
        check(pae.toString().contains("root-cause"), "toString includes cause");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- 契约深化：单构造器 / getException-getCause 同一性 / doPrivileged 包装语义 / 非受检直通 / 序列化 ----
    static void testContractDeep() throws Exception {
        // 构造器唯一：仅 (Exception)；null 合法（无校验）
        check(java.security.PrivilegedActionException.class.getConstructors().length == 1, "exactly one public constructor");
        java.io.IOException ioe = new java.io.IOException("iofail");
        java.security.PrivilegedActionException pae = new java.security.PrivilegedActionException(ioe);
        check(pae.getException() == ioe, "getException identity with ctor argument");
        check(pae.getCause() == ioe, "getCause identity with ctor argument");
        check(pae.getException() == pae.getCause(), "getException and getCause return same object");
        check(pae.getMessage() == null, "message is null (delegated to wrapped exception)");
        check(pae.toString().equals("java.security.PrivilegedActionException: java.io.IOException: iofail"),
                "toString is <PAE>: <wrapped toString>");
        // null 包装异常：合法构造，各 getter 为 null，toString 无尾随冒号
        java.security.PrivilegedActionException nul = new java.security.PrivilegedActionException(null);
        check(nul.getException() == null && nul.getCause() == null, "ctor(null): exception/cause null");
        check(nul.getMessage() == null, "ctor(null): message null");
        check(nul.toString().equals("java.security.PrivilegedActionException"),
                "ctor(null): toString bare class name");
        // initCause 规则（Throwable 契约）
        ct(java.lang.IllegalStateException.class, () -> pae.initCause(new java.lang.Exception("x")),
                "second initCause throws ISE (cause already set by ctor)");
        // PAE 特有：ctor 始终经 super(exception) 初始化 cause 槽（即使参数为 null），故 initCause 一律 ISE
        java.security.PrivilegedActionException nulIc = new java.security.PrivilegedActionException(null);
        ct(java.lang.IllegalStateException.class, () -> nulIc.initCause(new java.lang.Exception("x")),
                "initCause after ctor(null) also throws ISE (cause slot always initialized by super)");
        // Throwable 通用契约
        check(pae.fillInStackTrace() == pae, "fillInStackTrace returns this");
        check(pae.getSuppressed().length == 0, "suppressed empty by default");
        pae.addSuppressed(new java.lang.Exception("s1"));
        check(pae.getSuppressed().length == 1 && "s1".equals(pae.getSuppressed()[0].getMessage()),
                "addSuppressed/getSuppressed round-trip");
        ct(java.lang.NullPointerException.class, () -> pae.addSuppressed(null), "addSuppressed(null)");
        check(!pae.equals(new java.security.PrivilegedActionException(ioe)), "identity equals semantics");
        // 真实抛出路径：doPrivileged 包装受检异常
        boolean wrapped = false;
        try {
            java.security.AccessController.doPrivileged(
                    (java.security.PrivilegedExceptionAction<java.lang.String>) () -> {
                        throw new java.io.IOException("iofail");
                    });
        } catch (java.security.PrivilegedActionException p) {
            wrapped = p.getException() instanceof java.io.IOException
                    && p.getCause() == p.getException()
                    && "iofail".equals(p.getException().getMessage())
                    && p.getMessage() == null;
        }
        check(wrapped, "doPrivileged(PEA throwing checked IOException) wraps into PAE");
        // 非受检异常与 Error 不包装、原样直通
        boolean directRE = false;
        try {
            java.security.AccessController.doPrivileged(
                    (java.security.PrivilegedExceptionAction<java.lang.String>) () -> {
                        throw new java.lang.IllegalStateException("direct");
                    });
        } catch (java.security.PrivilegedActionException p) { directRE = false; }
        catch (java.lang.IllegalStateException expected) { directRE = true; }
        check(directRE, "RuntimeException from action propagates unwrapped");
        boolean directErr = false;
        try {
            java.security.AccessController.doPrivileged(
                    (java.security.PrivilegedExceptionAction<java.lang.String>) () -> {
                        throw new java.lang.AssertionError("err");
                    });
        } catch (java.security.PrivilegedActionException p) { directErr = false; }
        catch (java.lang.AssertionError expected) { directErr = true; }
        check(directErr, "Error from action propagates unwrapped");
        // getException 未废弃（两侧一致，实测记录）
        check(!java.security.PrivilegedActionException.class.getMethod("getException").isAnnotationPresent(java.lang.Deprecated.class),
                "getException not @Deprecated");
        // Serializable 契约
        check(java.io.ObjectStreamClass.lookup(java.security.PrivilegedActionException.class).getSerialVersionUID()
                == 4724086851538908602L, "serialVersionUID stable == 4724086851538908602");
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(ob)) { oos.writeObject(pae); }
        java.security.PrivilegedActionException rt = (java.security.PrivilegedActionException) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(ob.toByteArray())).readObject();
        check(rt.getException() instanceof java.io.IOException
                && "iofail".equals(rt.getException().getMessage()),
                "serialization preserves wrapped exception");
        check(rt.getSuppressed().length == 1, "serialization preserves suppressed");
        // 层级：Exception 直接子类，非 GeneralSecurityException
        check(java.lang.Exception.class.isAssignableFrom(java.security.PrivilegedActionException.class), "assignable to Exception");
        check(!java.security.GeneralSecurityException.class.isAssignableFrom(java.security.PrivilegedActionException.class),
                "NOT a GeneralSecurityException");
        java.io.ByteArrayOutputStream pba = new java.io.ByteArrayOutputStream();
        pae.printStackTrace(new java.io.PrintStream(pba));
        check(pba.toString().contains("java.security.PrivilegedActionException"),
                "printStackTrace contains class name");
    }

    // ---- 契约深化公共 helper ----
    interface TR { void run() throws Throwable; }
    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

}

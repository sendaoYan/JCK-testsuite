/*
 * @test
 * @summary javax.xml.ws.http.HTTPException conformance: ProtocolException hierarchy, single int constructor matrix, status code carry and Throwable mechanics.
 * @run main HTTPExceptionTest
 */

import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPException;


public class HTTPExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        check(javax.xml.ws.http.HTTPException.class.getSuperclass().getName().equals("javax.xml.ws.ProtocolException"), "extends javax.xml.ws.ProtocolException");
        javax.xml.ws.http.HTTPException he = new javax.xml.ws.http.HTTPException(404);
        check(he.getStatusCode() == 404, "status code");
        testHierarchy();
        testConstructorMatrix();
        testStatusCarry();
        testThrowableMechanics();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 层级：ProtocolException -> WebServiceException -> RuntimeException
    static void testHierarchy() {
        check(HTTPException.class.getSuperclass() == javax.xml.ws.ProtocolException.class,
              "direct superclass ProtocolException");
        check(javax.xml.ws.ProtocolException.class.isAssignableFrom(HTTPException.class),
              "assignable to ProtocolException");
        check(WebServiceException.class.isAssignableFrom(HTTPException.class),
              "assignable to WebServiceException");
        check(RuntimeException.class.isAssignableFrom(HTTPException.class),
              "assignable to RuntimeException");
        check(Exception.class.isAssignableFrom(HTTPException.class),
              "assignable to Exception");
        check(java.io.Serializable.class.isAssignableFrom(HTTPException.class),
              "Throwable is Serializable");
    }

    // 构造矩阵：仅 (int) 一个公共构造器
    static void testConstructorMatrix() throws Exception {
        java.lang.reflect.Constructor<?>[] ctors = HTTPException.class.getConstructors();
        check(ctors.length == 1, "exactly 1 public constructor: " + ctors.length);
        check(HTTPException.class.getConstructor(int.class) != null, "(int) ctor exists");
        try {
            HTTPException.class.getConstructor(String.class);
            check(false, "no (String) ctor must hold");
        } catch (NoSuchMethodException e) {
            check(true, "no (String) ctor (NoSuchMethodException)");
        }
        try {
            HTTPException.class.getConstructor();
            check(false, "no no-arg ctor must hold");
        } catch (NoSuchMethodException e) {
            check(true, "no no-arg ctor (NoSuchMethodException)");
        }
        try {
            HTTPException.class.getConstructor(Throwable.class);
            check(false, "no (Throwable) ctor must hold");
        } catch (NoSuchMethodException e) {
            check(true, "no (Throwable) ctor (NoSuchMethodException)");
        }
    }

    // 状态码携带矩阵
    static void testStatusCarry() {
        int[] codes = {0, 100, 200, 301, 404, 500, 503, 599, 999, Integer.MAX_VALUE};
        for (int code : codes) {
            HTTPException e = new HTTPException(code);
            check(e.getStatusCode() == code, "status code " + code + " round-trips");
        }
        // 负值同样按值携带（构造器不做范围校验——实测记录）
        HTTPException neg = new HTTPException(-1);
        check(neg.getStatusCode() == -1, "negative status carried as-is (no validation)");
        // 多次读取一致
        HTTPException once = new HTTPException(418);
        check(once.getStatusCode() == once.getStatusCode(), "getStatusCode stable");
        // 抛出/捕获不改变状态
        int caught = -1;
        try {
            throw new HTTPException(502);
        } catch (HTTPException e) {
            caught = e.getStatusCode();
        }
        check(caught == 502, "status preserved across throw/catch");
        // 子类型多态捕获
        try {
            throw new HTTPException(403);
        } catch (WebServiceException e) {
            check(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 403,
                  "catchable as WebServiceException with status intact");
        }
    }

    // Throwable 机制：message/cause/toString
    static void testThrowableMechanics() {
        HTTPException e = new HTTPException(404);
        check(e.getMessage() == null, "no message by default (status not in message)");
        check(e.getCause() == null, "no cause by default");
        check(e.toString().contains("HTTPException"), "toString contains class name");
        check(!e.toString().contains("404"), "toString has no status (message null)");
        // initCause 可用一次
        RuntimeException cause = new RuntimeException("c");
        e.initCause(cause);
        check(e.getCause() == cause, "initCause round-trip");
        try {
            e.initCause(new RuntimeException("c2"));
            check(false, "second initCause must throw");
        } catch (IllegalStateException ise) {
            check(true, "second initCause -> IllegalStateException");
        }
        // fillInStackTrace 返回自身
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace available");
        // 同状态两实例 equals 语义为对象同一性（未覆写）
        HTTPException a = new HTTPException(500);
        HTTPException b = new HTTPException(500);
        check(!a.equals(b), "equals is identity (not value-based)");
        check(a.equals(a), "reflexive equals");
    }
}

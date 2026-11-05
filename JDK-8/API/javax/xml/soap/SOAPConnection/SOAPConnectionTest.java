/*
 * @test
 * @summary javax.xml.soap.SOAPConnection conformance: abstract class hierarchy and methods.
 * @run main SOAPConnectionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPConnectionTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPConnection");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("call"), "has method call");
        check(names.contains("get"), "has method get");
        check(names.contains("close"), "has method close");
        testLifecycleAndNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 生命周期与负例（不真联网：端点 null / 非法对象 / 关闭后调用）
    static void testLifecycleAndNegatives() throws Exception {
        javax.xml.soap.SOAPConnectionFactory cf = javax.xml.soap.SOAPConnectionFactory.newInstance();
        javax.xml.soap.SOAPConnection sc = cf.createConnection();
        check(sc != null, "createConnection non-null");
        check(sc instanceof javax.xml.soap.SOAPConnection, "connection is SOAPConnection");
        javax.xml.soap.SOAPMessage req = javax.xml.soap.MessageFactory.newInstance().createMessage();
        // 端点 null → SOAPException "Bad endPoint type null"
        try {
            sc.call(req, null);
            check(false, "call(msg, null) should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bad endPoint type"),
                    "call null endpoint message: " + e.getMessage());
        }
        // 端点非法对象 → SOAPException
        try {
            sc.call(req, new Object());
            check(false, "call(msg, new Object()) should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bad endPoint type"),
                    "call bad object endpoint message");
        }
        // get(Object) 同样拒绝
        try {
            sc.get(null);
            check(false, "get(null) should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bad endPoint type"),
                    "get null endpoint message");
        }
        try {
            sc.get(new Object());
            check(false, "get(new Object()) should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bad endPoint type"),
                    "get bad object endpoint message");
        }
        // 非法 URL 字符串端点（无协议）→ SOAPException "Bad URL: no protocol"（不触发网络）
        try {
            sc.call(req, "not a url");
            check(false, "call with protocol-less string should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bad URL") && e.getMessage().contains("no protocol"),
                    "call bad URL string message: " + e.getMessage());
        }
        try {
            sc.get("not a url");
            check(false, "get with protocol-less string should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bad URL"),
                    "get bad URL string message");
        }
        // close 后 call → SOAPException "Connection is closed"
        sc.close();
        try {
            sc.call(req, null);
            check(false, "call after close should throw SOAPException");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("closed"),
                    "call after close message: " + e.getMessage());
        }
        // RI 实测锁定：close() 不幂等——重复 close 抛 "Connection already closed"
        try {
            sc.close();
            check(false, "second close should throw in RI");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("already closed"),
                    "second close message: " + e.getMessage());
        }
        // 一个连接关闭不影响新建连接
        javax.xml.soap.SOAPConnection sc2 = cf.createConnection();
        check(sc2 != null && sc2 != sc, "new connection distinct after closing previous");
        // 对新连接的负例仍成立（未关闭状态）
        try {
            sc2.call(req, new Object());
            check(false, "fresh connection call with bad endpoint should throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "fresh connection rejects bad endpoint");
        }
        sc2.close();
    }
}

/*
 * @test
 * @summary javax.xml.soap.SOAPConnectionFactory conformance: SOAP connection factory.
 * @run main SOAPConnectionFactoryTest
 */


public class SOAPConnectionFactoryTest {
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
        javax.xml.soap.SOAPConnectionFactory cf = javax.xml.soap.SOAPConnectionFactory.newInstance();
        check(cf != null, "factory non-null");
        testConnections();
        testFactoryIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 两次 createConnection 返回不同实例；连接可用
    static void testConnections() throws Exception {
        javax.xml.soap.SOAPConnectionFactory cf = javax.xml.soap.SOAPConnectionFactory.newInstance();
        check(cf instanceof javax.xml.soap.SOAPConnectionFactory, "factory type");
        javax.xml.soap.SOAPConnection c1 = cf.createConnection();
        javax.xml.soap.SOAPConnection c2 = cf.createConnection();
        check(c1 != null, "first connection non-null");
        check(c2 != null, "second connection non-null");
        check(c1 != c2, "two connections are distinct instances");
        check(c1 instanceof javax.xml.soap.SOAPConnection, "connection type");
        // 连接对非法端点立即拒绝（无网络副作用）
        try {
            c1.call(javax.xml.soap.MessageFactory.newInstance().createMessage(), new Object());
            check(false, "expected SOAPException for bad endpoint");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "connection from factory rejects bad endpoint");
        }
        c1.close();
        c2.close();
        // 工厂可继续产新连接
        javax.xml.soap.SOAPConnection c3 = cf.createConnection();
        check(c3 != null, "factory keeps producing connections after closes");
        check(c3 != c1 && c3 != c2, "third connection distinct");
        c3.close();
    }

    // newInstance 重复调用与类形态
    static void testFactoryIdentity() throws Exception {
        javax.xml.soap.SOAPConnectionFactory f1 = javax.xml.soap.SOAPConnectionFactory.newInstance();
        javax.xml.soap.SOAPConnectionFactory f2 = javax.xml.soap.SOAPConnectionFactory.newInstance();
        check(f1 != null && f2 != null, "both newInstance calls non-null");
        // RI 实测：newInstance 每次返回新工厂实例（记录，不锁死实现类名）
        check(f1.getClass().getName().equals(f2.getClass().getName()),
                "repeated newInstance yields same implementation class");
        check(!javax.xml.soap.SOAPConnectionFactory.class.isInterface(),
                "SOAPConnectionFactory is a concrete-instantiable abstract class family");
        check(java.lang.reflect.Modifier.isAbstract(javax.xml.soap.SOAPConnectionFactory.class.getModifiers()),
                "SOAPConnectionFactory itself is abstract");
        javax.xml.soap.SOAPConnection c = f1.createConnection();
        check(c != null, "connection from second factory instance");
        c.close();
        // 跨工厂实例的连接同样互为独立对象
        javax.xml.soap.SOAPConnection ca = f1.createConnection();
        javax.xml.soap.SOAPConnection cb = f2.createConnection();
        check(ca != cb, "connections from different factory instances are distinct");
        // 第二个工厂的连接同样实现负例契约
        try {
            cb.call(javax.xml.soap.MessageFactory.newInstance().createMessage(), new Object());
            check(false, "f2 connection should reject bad endpoint");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Bad endPoint type"),
                    "f2 connection rejects bad endpoint with standard message");
        }
        ca.close();
        cb.close();
        // 方法面完整（call/get/close 都在 SOAPConnection 上由工厂产品提供）
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : javax.xml.soap.SOAPConnection.class.getMethods()) names.add(m.getName());
        check(names.contains("call") && names.contains("get") && names.contains("close"),
                "SOAPConnection exposes call/get/close");
        check(java.lang.reflect.Modifier.isAbstract(
                javax.xml.soap.SOAPConnection.class.getMethod("call",
                        javax.xml.soap.SOAPMessage.class, Object.class).getModifiers()),
                "call is abstract on SOAPConnection");
    }
}

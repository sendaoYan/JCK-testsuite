/*
 * @test
 * @summary javax.xml.soap.SAAJMetaFactory conformance: 抽象 SPI 骨架(protected 构造/两个 protected abstract 工厂方法)/匿名与命名子类实例化/桥接 MessageFactory-SOAPFactory 协议链产物 envelope ns 精确/直接实例化被拒.
 * @run main SAAJMetaFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SAAJMetaFactoryTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SAAJMetaFactory");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getName().equals("javax.xml.soap.SAAJMetaFactory"), "FQCN exact");
        testProtectedSkeleton();
        testAnonymousSubclass();
        testNamedSubclassSpiBridge();
        testDirectInstantiationRejected();
        testSpiEndProductEnvelope();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // SPI 骨架：无公开构造；唯一构造 protected；两个 protected abstract 工厂方法
    static void testProtectedSkeleton() throws Exception {
        Class<?> c = Class.forName("javax.xml.soap.SAAJMetaFactory");
        check(c.getConstructors().length == 0, "no public constructors");
        check(c.getDeclaredConstructors().length == 1, "exactly one declared constructor");
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()),
                "declared constructor is protected");
        java.lang.reflect.Method mf = c.getDeclaredMethod("newMessageFactory", String.class);
        check(java.lang.reflect.Modifier.isProtected(mf.getModifiers()), "newMessageFactory protected");
        check(java.lang.reflect.Modifier.isAbstract(mf.getModifiers()), "newMessageFactory abstract");
        check(mf.getReturnType().getName().equals("javax.xml.soap.MessageFactory"),
                "newMessageFactory returns MessageFactory");
        Class<?>[] exs = mf.getExceptionTypes();
        boolean throwsSoap = false;
        for (Class<?> e : exs) if (e.getName().equals("javax.xml.soap.SOAPException")) throwsSoap = true;
        check(throwsSoap, "newMessageFactory declares SOAPException");
        java.lang.reflect.Method sf = c.getDeclaredMethod("newSOAPFactory", String.class);
        check(java.lang.reflect.Modifier.isProtected(sf.getModifiers()), "newSOAPFactory protected");
        check(java.lang.reflect.Modifier.isAbstract(sf.getModifiers()), "newSOAPFactory abstract");
        check(sf.getReturnType().getName().equals("javax.xml.soap.SOAPFactory"),
                "newSOAPFactory returns SOAPFactory");
        check(c.getDeclaredMethods().length == 3, "3 declared methods (2 SPI hooks + package-private static getInstance)");
        // 包私有静态查找入口：getInstance()（跨包不可直接调用，反射锁定签名）
        java.lang.reflect.Method gi = c.getDeclaredMethod("getInstance");
        check(java.lang.reflect.Modifier.isStatic(gi.getModifiers()), "getInstance is static");
        check(!java.lang.reflect.Modifier.isPublic(gi.getModifiers())
                        && !java.lang.reflect.Modifier.isProtected(gi.getModifiers()),
                "getInstance is package-private");
        check(gi.getReturnType().getName().equals("javax.xml.soap.SAAJMetaFactory"),
                "getInstance returns SAAJMetaFactory");
        check(gi.getParameterTypes().length == 0, "getInstance takes no args");
        check(!java.lang.reflect.Modifier.isInterface(c.getModifiers()), "is a class not interface");
    }

    // 匿名子类实例化：实现两个钩子即得具体实例
    static void testAnonymousSubclass() throws Exception {
        javax.xml.soap.SAAJMetaFactory anon = new javax.xml.soap.SAAJMetaFactory() {
            protected javax.xml.soap.MessageFactory newMessageFactory(String protocol)
                    throws javax.xml.soap.SOAPException {
                return javax.xml.soap.MessageFactory.newInstance(protocol);
            }
            protected javax.xml.soap.SOAPFactory newSOAPFactory(String protocol)
                    throws javax.xml.soap.SOAPException {
                return javax.xml.soap.SOAPFactory.newInstance(protocol);
            }
        };
        check(anon != null, "anonymous subclass instantiates");
        check(anon instanceof javax.xml.soap.SAAJMetaFactory, "anonymous is SAAJMetaFactory");
        check(!java.lang.reflect.Modifier.isAbstract(anon.getClass().getModifiers()),
                "anonymous subclass is concrete");
        check(anon.getClass().getSuperclass() == javax.xml.soap.SAAJMetaFactory.class,
                "anonymous superclass is SAAJMetaFactory");
    }

    // 命名子类：桥接到平台查找链，产物可用且协议语义精确
    static void testNamedSubclassSpiBridge() throws Exception {
        Bridge bridge = new Bridge();
        check(bridge instanceof javax.xml.soap.SAAJMetaFactory, "named subclass is SAAJMetaFactory");
        javax.xml.soap.MessageFactory mf11 = bridge.newMessageFactoryPublic(
                javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL);
        check(mf11 != null, "bridged MessageFactory (1.1) non-null");
        javax.xml.soap.MessageFactory mf12 = bridge.newMessageFactoryPublic(
                javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        check(mf12 != null, "bridged MessageFactory (1.2) non-null");
        javax.xml.soap.SOAPFactory sf = bridge.newSOAPFactoryPublic(
                javax.xml.soap.SOAPConstants.DEFAULT_SOAP_PROTOCOL);
        check(sf != null, "bridged SOAPFactory non-null");
        check(sf.createElement("spi-el") != null, "bridged SOAPFactory produces elements");
        javax.xml.soap.Name nm = sf.createName("spi", "p", "urn:spi");
        check("urn:spi".equals(nm.getURI()), "bridged SOAPFactory createName works");
        // 协议负例经由桥同样传导
        try {
            bridge.newMessageFactoryPublic("bogus");
            check(false, "bogus protocol through bridge must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Unknown Protocol"),
                    "bogus protocol message through bridge: " + e.getMessage());
        }
    }

    // 直接 Class.newInstance 被拒（protected 构造跨包不可达）
    static void testDirectInstantiationRejected() throws Exception {
        try {
            Class.forName("javax.xml.soap.SAAJMetaFactory").newInstance();
            check(false, "direct instantiation must be rejected");
        } catch (IllegalAccessException e) {
            check(true, "direct newInstance -> IllegalAccessException (protected ctor)");
        }
    }

    // SPI 链最终产物弱断言：平台默认查找产物 envelope ns 精确
    static void testSpiEndProductEnvelope() throws Exception {
        javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory.newInstance();
        javax.xml.soap.SOAPEnvelope env = mf.createMessage().getSOAPPart().getEnvelope();
        check(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(env.getNamespaceURI()),
                "default SPI product is SOAP 1.1 envelope");
        javax.xml.soap.MessageFactory mf12 = javax.xml.soap.MessageFactory.newInstance(
                javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL);
        javax.xml.soap.SOAPEnvelope env12 = mf12.createMessage().getSOAPPart().getEnvelope();
        check(javax.xml.soap.SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(env12.getNamespaceURI()),
                "1.2 SPI product is SOAP 1.2 envelope");
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        check(sf.createName("end", "e", "urn:end").getURI().equals("urn:end"),
                "default SOAPFactory lookup functional");
    }

    // 桥接实现：把 protected 钩子暴露为可调用方法
    static class Bridge extends javax.xml.soap.SAAJMetaFactory {
        protected javax.xml.soap.MessageFactory newMessageFactory(String protocol)
                throws javax.xml.soap.SOAPException {
            return javax.xml.soap.MessageFactory.newInstance(protocol);
        }
        protected javax.xml.soap.SOAPFactory newSOAPFactory(String protocol)
                throws javax.xml.soap.SOAPException {
            return javax.xml.soap.SOAPFactory.newInstance(protocol);
        }
        javax.xml.soap.MessageFactory newMessageFactoryPublic(String protocol)
                throws javax.xml.soap.SOAPException {
            return newMessageFactory(protocol);
        }
        javax.xml.soap.SOAPFactory newSOAPFactoryPublic(String protocol)
                throws javax.xml.soap.SOAPException {
            return newSOAPFactory(protocol);
        }
    }
}

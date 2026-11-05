/*
 * @test
 * @summary javax.xml.soap.SOAPElementFactory conformance: 三 create 重载回读/参数序(local,prefix,uri)/私有构造与纯 javadoc 弃用(实测无 Deprecated 运行期注解)/委托 SOAPFactory 语义/可移植入消息.
 * @run main SOAPElementFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPElementFactoryTest {
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
        javax.xml.soap.SOAPElementFactory f = javax.xml.soap.SOAPElementFactory.newInstance();
        check(f != null, "factory non-null");
        javax.xml.soap.SOAPElement e = f.create("foo");
        check(e != null, "element non-null");
        testReflectionFace();
        testCreateNameForm();
        testCreateLocalForm();
        testCreateThreeArgForm();
        testStandaloneAndPortable();
        testNullNegatives();
        testDelegationParity();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static int count(java.util.Iterator<?> it) {
        int c = 0;
        while (it.hasNext()) { it.next(); c++; }
        return c;
    }

    // 反射：具体类/私有构造（无公开构造）/JDK-8 实测弃用仅 javadoc 级（无 @Deprecated 注解）
    static void testReflectionFace() throws Exception {
        Class<?> c = Class.forName("javax.xml.soap.SOAPElementFactory");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class (not abstract)");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getConstructors().length == 0, "no public constructors (private ctor + static newInstance)");
        check(c.getDeclaredConstructors().length == 1, "exactly one declared constructor");
        check(Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers()),
                "declared constructor is private");
        // JDK-8 实测：类与 create 方法均无运行期 @Deprecated 注解（弃用仅 @deprecated javadoc 标注）
        check(c.getAnnotation(Deprecated.class) == null,
                "JDK-8: no runtime @Deprecated on class (javadoc-level only)");
        check(c.getDeclaredAnnotations().length == 0, "JDK-8: no runtime annotations on class");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) if (m.getDeclaringClass() == c) names.add(m.getName());
        check(names.contains("create") && names.contains("newInstance"), "own methods create/newInstance");
        int createOverloads = 0;
        for (Method m : c.getMethods()) if (m.getDeclaringClass() == c && m.getName().equals("create")) createOverloads++;
        check(createOverloads == 3, "exactly 3 create overloads (Name/String/3-String)");
        check(Modifier.isStatic(c.getMethod("newInstance").getModifiers()), "newInstance static");
    }

    // create(Name) 回读
    static void testCreateNameForm() throws Exception {
        javax.xml.soap.SOAPElementFactory f = javax.xml.soap.SOAPElementFactory.newInstance();
        javax.xml.soap.Name n = javax.xml.soap.SOAPFactory.newInstance().createName("nlocal", "npfx", "urn:nef");
        javax.xml.soap.SOAPElement e = f.create(n);
        check(e != null, "create(Name) non-null");
        check("nlocal".equals(e.getElementName().getLocalName()), "create(Name) local");
        check("npfx".equals(e.getElementName().getPrefix()), "create(Name) prefix");
        check("urn:nef".equals(e.getElementName().getURI()), "create(Name) uri");
        check(e.getElementQName().equals(new javax.xml.namespace.QName("urn:nef", "nlocal", "npfx")),
                "create(Name) QName view");
        check("npfx".equals(e.getPrefix()), "create(Name) DOM prefix");
        check("urn:nef".equals(e.getNamespaceURI()), "create(Name) DOM namespace");
    }

    // create(String) 回读：无命名空间独立片段
    static void testCreateLocalForm() throws Exception {
        javax.xml.soap.SOAPElementFactory f = javax.xml.soap.SOAPElementFactory.newInstance();
        javax.xml.soap.SOAPElement e = f.create("frag");
        check("frag".equals(e.getElementName().getLocalName()), "create(local) local name");
        check("".equals(e.getElementName().getURI()), "create(local) no uri");
        check("".equals(e.getElementName().getPrefix()), "create(local) no prefix");
        check(e.getValue() == null, "fresh fragment has null value");
        e.addTextNode("fdata");
        check("fdata".equals(e.getValue()), "fragment text round-trip after addTextNode");
        check("fdata".equals(e.getTextContent()), "fragment DOM textContent");
    }

    // create(String,String,String)：实测参数序为 (localName, prefix, uri) —— 第 2 参是前缀（易错）
    static void testCreateThreeArgForm() throws Exception {
        javax.xml.soap.SOAPElementFactory f = javax.xml.soap.SOAPElementFactory.newInstance();
        javax.xml.soap.SOAPElement e = f.create("tlocal", "tpfx", "urn:tef");
        check("tlocal".equals(e.getElementName().getLocalName()), "3-arg local");
        check("tpfx".equals(e.getElementName().getPrefix()), "3-arg: 2nd parameter IS the prefix");
        check("urn:tef".equals(e.getElementName().getURI()), "3-arg: 3rd parameter IS the uri");
        check("tpfx".equals(e.getPrefix()), "3-arg DOM prefix");
        check("urn:tef".equals(e.getNamespaceURI()), "3-arg DOM namespace");
        // 反向对照：换合法 token 顺序，结果跟随位置而非语义名
        javax.xml.soap.SOAPElement e2 = f.create("tlocal", "second", "third");
        check("second".equals(e2.getElementName().getPrefix()), "3-arg positional: 2nd arg lands in prefix");
        check("third".equals(e2.getElementName().getURI()), "3-arg positional: 3rd arg lands in uri");
        // RI 实测负例：第 2 参非合法 NCName 前缀 → 未检查 DOMException(NAMESPACE_ERR)（不包装为 SOAPException）
        try {
            f.create("tlocal", "urn:other", "opfx");
            check(false, "invalid NCName prefix must throw");
        } catch (org.w3c.dom.DOMException de) {
            check(de.code == org.w3c.dom.DOMException.NAMESPACE_ERR,
                    "invalid prefix -> unchecked DOMException NAMESPACE_ERR (RI passthrough)");
        }
    }

    // 独立片段可移植：挂入真实消息 body
    static void testStandaloneAndPortable() throws Exception {
        javax.xml.soap.SOAPElementFactory f = javax.xml.soap.SOAPElementFactory.newInstance();
        javax.xml.soap.SOAPElement frag = f.create("portable", "pp", "urn:pef");
        check(frag.getParentElement() == null, "factory fragment standalone (no SAAJ parent)");
        javax.xml.soap.SOAPMessage msg = javax.xml.soap.MessageFactory.newInstance().createMessage();
        javax.xml.soap.SOAPBody body = msg.getSOAPBody();
        int before = count(body.getChildElements());
        javax.xml.soap.SOAPElement added = body.addChildElement(frag);
        check(count(body.getChildElements()) == before + 1, "body count +1 after adopting fragment");
        check(added != frag, "adoption copies the fragment (document import)");
        check(added.getElementName().getURI().equals("urn:pef"), "adopted fragment keeps name");
        check(body.getElementsByTagNameNS("urn:pef", "portable").getLength() == 1,
                "adopted fragment visible in DOM lookup");
    }

    // null 负例（委托 SOAPFactory.createElement 的实测异常）
    static void testNullNegatives() throws Exception {
        javax.xml.soap.SOAPElementFactory f = javax.xml.soap.SOAPElementFactory.newInstance();
        try {
            f.create((String) null);
            check(false, "create((String)null) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "create((String)null) throws SOAPException");
        }
        try {
            f.create((javax.xml.soap.Name) null);
            check(false, "create((Name)null) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(true, "create((Name)null) throws SOAPException");
        }
        try {
            f.create(null, "p", "u");
            check(false, "create(null,p,u) must throw");
        } catch (javax.xml.soap.SOAPException e) {
            check(e.getMessage() != null && e.getMessage().contains("Null localName"),
                    "create(null,p,u) message: " + e.getMessage());
        }
    }

    // 委托一致性：与 SOAPFactory.createElement 同参同产物名
    static void testDelegationParity() throws Exception {
        javax.xml.soap.SOAPElementFactory ef = javax.xml.soap.SOAPElementFactory.newInstance();
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        check(ef.create("dp", "dpx", "urn:dp").getElementName()
                        .equals(sf.createElement("dp", "dpx", "urn:dp").getElementName()),
                "3-arg create delegates to SOAPFactory.createElement(local,prefix,uri)");
        check(ef.create("dl").getElementName().equals(sf.createElement("dl").getElementName()),
                "1-arg create parity");
        javax.xml.soap.Name shared = sf.createName("dn", "dnp", "urn:dn");
        check(ef.create(shared).getElementName().equals(sf.createElement(shared).getElementName()),
                "Name-form create parity");
    }
}

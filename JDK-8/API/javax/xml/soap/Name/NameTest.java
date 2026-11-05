/*
 * @test
 * @summary javax.xml.soap.Name conformance: interface hierarchy and methods.
 * @run main NameTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NameTest {
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
        Class<?> c = Class.forName("javax.xml.soap.Name");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLocalName"), "has method getLocalName");
        check(names.contains("getQualifiedName"), "has method getQualifiedName");
        check(names.contains("getPrefix"), "has method getPrefix");
        check(names.contains("getURI"), "has method getURI");
        testLocalNameForm();
        testTripleForm();
        testEqualitySemantics();
        testCrossFactoryEquality();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // SOAPFactory.createName(local)：prefix/uri 均空，qualified == local
    static void testLocalNameForm() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.Name n = sf.createName("localPart");
        check("localPart".equals(n.getLocalName()), "createName(local) local name");
        check("".equals(n.getPrefix()), "createName(local) prefix is empty string");
        check("".equals(n.getURI()), "createName(local) uri is empty string");
        check("localPart".equals(n.getQualifiedName()), "createName(local) qualified name equals local");
    }

    // createName(local,prefix,uri)：三元精确回读，qualified == "prefix:local"
    static void testTripleForm() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.Name n = sf.createName("ln", "px", "http://u/");
        check("ln".equals(n.getLocalName()), "triple form local");
        check("px".equals(n.getPrefix()), "triple form prefix");
        check("http://u/".equals(n.getURI()), "triple form uri");
        check("px:ln".equals(n.getQualifiedName()), "triple form qualified name is 'prefix:local'");
        // 空前缀但有 uri
        javax.xml.soap.Name d = sf.createName("dn", "", "http://du/");
        check("".equals(d.getPrefix()), "empty prefix kept");
        check("dn".equals(d.getQualifiedName()), "empty prefix qualified name is bare local");
        check("http://du/".equals(d.getURI()), "empty prefix uri kept");
        // envelope.createName 同语义
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.Name en = env.createName("el");
        check("el".equals(en.getLocalName()) && "".equals(en.getPrefix()) && "".equals(en.getURI()),
                "envelope.createName(local) same semantics");
        javax.xml.soap.Name en2 = env.createName("el2", "ep", "http://eu/");
        check("ep:el2".equals(en2.getQualifiedName()), "envelope.createName triple form");
    }

    // 同参两实例 equals/hashCode；不同参不等；equals 各分量
    static void testEqualitySemantics() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.Name a = sf.createName("n", "p", "http://u/");
        javax.xml.soap.Name b = sf.createName("n", "p", "http://u/");
        check(a.equals(b), "same triple equals");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "same triple hashCode equal");
        check(!a.equals(sf.createName("n", "p", "http://other/")), "different uri not equal");
        // RI 实测锁定：Name 的 equals 只看 (localName, uri)，前缀不同仍相等（前缀视为可替换的语法糖）
        check(a.equals(sf.createName("n", "q", "http://u/")), "different prefix still equal (prefix-insensitive)");
        check(a.hashCode() == sf.createName("n", "q", "http://u/").hashCode(), "prefix-insensitive hashCode agrees");
        check(!a.equals(sf.createName("m", "p", "http://u/")), "different local not equal");
        // 无参与空三元等价
        check(sf.createName("n").equals(sf.createName("n", "", "")), "createName(local) equals (local,'','')");
        check(!a.equals("n"), "Name not equal to String");
        check(!a.equals(null), "Name not equal to null");
        check(a.equals(a), "reflexive equals");
    }

    // envelope.createName 与 SOAPFactory.createName 跨工厂等值（同一 NameImpl）
    static void testCrossFactoryEquality() throws Exception {
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        javax.xml.soap.SOAPEnvelope env = javax.xml.soap.MessageFactory.newInstance()
                .createMessage().getSOAPPart().getEnvelope();
        javax.xml.soap.Name fromFactory = sf.createName("x", "p", "http://x/");
        javax.xml.soap.Name fromEnvelope = env.createName("x", "p", "http://x/");
        check(fromFactory.equals(fromEnvelope), "cross-factory same-param Names equal");
        check(fromFactory.hashCode() == fromEnvelope.hashCode(), "cross-factory hashCode equal");
        // Name 可直接作 addChildElement/addAttribute 入参（互操作通道）
        javax.xml.soap.SOAPElement e = env.getBody().addChildElement(fromEnvelope);
        check("x".equals(e.getElementName().getLocalName()), "Name from envelope usable via factory-created element");
    }
}

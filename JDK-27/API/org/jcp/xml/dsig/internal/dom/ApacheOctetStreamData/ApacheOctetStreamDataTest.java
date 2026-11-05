/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open
 * @run main ApacheOctetStreamDataTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ApacheOctetStreamDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData", false, ApacheOctetStreamDataTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.OctetStreamData"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("org.jcp.xml.dsig.internal.dom.ApacheData"), "iface:org.jcp.xml.dsig.internal.dom.ApacheData");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getXMLSignatureInput"), "method:getXMLSignatureInput");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData");
        check("javax.xml.crypto.OctetStreamData".equals(c.getSuperclass().getName()), "superclass javax.xml.crypto.OctetStreamData");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("org.jcp.xml.dsig.internal.dom.ApacheData"))), "exact interface set");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "modifier set [public]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getXMLSignatureInput"))),
                "exact public/protected method face (1 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput,"), "constructor(XMLSignatureInput) present");
        // black-box behavior
        Class<?> xsi = Class.forName("com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput");
        Object input = xsi.getConstructor(byte[].class).newInstance((Object) new byte[]{9, 8, 7});
        Object aosd = c.getConstructor(xsi).newInstance(input);
        check(aosd instanceof javax.xml.crypto.OctetStreamData, "ApacheOctetStreamData is an OctetStreamData");
        check(aosd instanceof org.jcp.xml.dsig.internal.dom.ApacheData, "ApacheOctetStreamData implements ApacheData");
        check(c.getMethod("getXMLSignatureInput").invoke(aosd) == input,
                "getXMLSignatureInput returns the wrapped input");
        check(((javax.xml.crypto.OctetStreamData) aosd).getOctetStream().read() == 9,
                "octets stream reads the first supplied byte");
        Object input2 = xsi.getConstructor(byte[].class).newInstance((Object) new byte[]{1});
        Object aosd2 = c.getConstructor(xsi).newInstance(input2);
        check(aosd != aosd2, "distinct inputs yield distinct data objects");
    }
}

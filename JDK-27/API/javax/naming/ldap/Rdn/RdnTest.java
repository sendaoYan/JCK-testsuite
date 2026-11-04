/*
 * @test
 * @summary javax.naming.ldap.Rdn conformance: white-box structure + black-box behavior.
 * @run main RdnTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

public class RdnTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.Rdn");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(Comparable.class.isAssignableFrom(c), "Comparable");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == Attributes.class), "ctor(Attributes)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == Rdn.class), "ctor(Rdn)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2), "ctor(String,Object)");
        for (String m : new String[]{"getValue", "getType", "toString", "compareTo", "equals", "hashCode", "toAttributes", "size", "escapeValue", "unescapeValue"}) {
            check(hasMethod(c, m), m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        Rdn r = new Rdn("cn=John");
        check("cn".equals(r.getType()), "type");
        check("John".equals(r.getValue()), "value");
        check("cn=John".equals(r.toString()), "toString");
        check(r.size() == 1, "size 1");

        // multi-valued RDN
        Rdn multi = new Rdn("cn=John+sn=Doe");
        check(multi.size() == 2, "multi size 2");
        check("cn".equals(multi.getType()), "multi type first");
        check("John".equals(multi.getValue()), "multi value first");
        check("cn=John+sn=Doe".equals(multi.toString()), "multi toString");

        // Rdn(String,Object) escapes value
        Rdn esc = new Rdn("cn", "a,b+c");
        check("a,b+c".equals(esc.getValue()), "object value unescaped");
        check("cn=a\\,b\\+c".equals(esc.toString()), "toString escapes");

        // copy constructor
        Rdn copy = new Rdn(esc);
        check(copy.equals(esc), "copy equals");
        check(copy.toString().equals(esc.toString()), "copy toString");

        // toAttributes
        Attributes attrs = multi.toAttributes();
        check(attrs.size() == 2, "toAttributes size");
        check("John".equals(attrs.get("cn").get()), "toAttributes cn");
        check("Doe".equals(attrs.get("sn").get()), "toAttributes sn");

        // escape/unescape round trip
        check("a\\,b\\+c".equals(Rdn.escapeValue("a,b+c")), "escapeValue");
        check("a,b+c".equals(Rdn.unescapeValue("a\\,b\\+c")), "unescapeValue");

        // equals / hashCode / compareTo
        check(r.equals(new Rdn("cn=John")), "equals");
        check(r.hashCode() == new Rdn("cn=John").hashCode(), "hashCode");
        check(r.compareTo(new Rdn("cn=John")) == 0, "compareTo equal");
        check(new Rdn("cn=a").compareTo(new Rdn("cn=b")) < 0, "compareTo ordering");

        // invalid
        try {
            new Rdn("=x");
            check(false, "invalid should throw");
        } catch (InvalidNameException expected) {
            check(true, "invalid throws InvalidNameException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

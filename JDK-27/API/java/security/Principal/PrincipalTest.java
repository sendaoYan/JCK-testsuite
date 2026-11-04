/*
 * @test
 * @summary java.security.Principal conformance: white-box structure + black-box behavior.
 * @run main PrincipalTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500Principal;

public class PrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Principal");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"equals", "getName", "hashCode", "implies", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Functional-style implementation of the single-method interface
        Principal named = () -> "jck-principal";
        check(named.getName().equals("jck-principal"), "lambda getName");
        // classic anonymous implementation
        Principal anon = new Principal() {
            public String getName() { return "anon-principal"; }
        };
        check(anon.getName().equals("anon-principal"), "anon getName");
        // X500Principal is the flagship built-in implementation
        X500Principal x500 = new X500Principal("CN=jck-root, OU=JCK, O=Test, C=US");
        check(x500.getName().equals("CN=jck-root,OU=JCK,O=Test,C=US"), "X500 RFC2253");
        check(x500.getName(X500Principal.CANONICAL).equals("cn=jck-root,ou=jck,o=test,c=us"), "X500 canonical");
        check(x500.toString().contains("CN=jck-root"), "X500 toString");
        // equals/hashCode between equal X500 principals
        X500Principal same = new X500Principal("CN=jck-root, OU=JCK, O=Test, C=US");
        check(x500.equals(same) && x500.hashCode() == same.hashCode(), "X500 equals/hashCode");
        check(!x500.equals(new X500Principal("CN=other")), "X500 not equals");
        // X500Principal from name bytes
        byte[] nameBytes = x500.getEncoded();
        check(new X500Principal(nameBytes).equals(x500), "X500 from encoded bytes");
        // Principal used in ProtectionDomain principal list
        ProtectionDomain pd = new ProtectionDomain(null, null, null, new Principal[]{x500});
        check(pd.getPrincipals()[0] == x500, "principal in ProtectionDomain");
        // Identity (deprecated) also implements Principal
        Identity id = new Identity("jckid") {};
        check(id instanceof Principal && id.getName().equals("jckid"), "Identity is Principal");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}

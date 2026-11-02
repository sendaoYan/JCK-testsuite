/*
 * @test
 * @summary sun.security.krb5.PrincipalName conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main PrincipalNameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PrincipalNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.PrincipalName", false, PrincipalNameTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.PrincipalName"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.Cloneable"), "iface:java.lang.Cloneable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asn1Encode"), "method:asn1Encode");
        check(methods.contains("clone"), "method:clone");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getInstanceComponent"), "method:getInstanceComponent");
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("getNameString"), "method:getNameString");
        check(methods.contains("getNameStrings"), "method:getNameStrings");
        check(methods.contains("getNameType"), "method:getNameType");
        check(methods.contains("getPrincipalNameAsString"), "method:getPrincipalNameAsString");
        check(methods.contains("getRealm"), "method:getRealm");
        check(methods.contains("getRealmAsString"), "method:getRealmAsString");
        check(methods.contains("getRealmString"), "method:getRealmString");
        check(methods.contains("getSalt"), "method:getSalt");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("match"), "method:match");
        check(methods.contains("parse"), "method:parse");
        check(methods.contains("tgsService"), "method:tgsService");
        check(methods.contains("toByteArray"), "method:toByteArray");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("writePrincipal"), "method:writePrincipal");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("KRB_NT_ENTERPRISE"), "field:KRB_NT_ENTERPRISE");
        check(fields.contains("KRB_NT_PRINCIPAL"), "field:KRB_NT_PRINCIPAL");
        check(fields.contains("KRB_NT_SRV_HST"), "field:KRB_NT_SRV_HST");
        check(fields.contains("KRB_NT_SRV_INST"), "field:KRB_NT_SRV_INST");
        check(fields.contains("KRB_NT_SRV_XHST"), "field:KRB_NT_SRV_XHST");
        check(fields.contains("KRB_NT_UID"), "field:KRB_NT_UID");
        check(fields.contains("KRB_NT_UNKNOWN"), "field:KRB_NT_UNKNOWN");
        check(fields.contains("NAME_COMPONENT_SEPARATOR"), "field:NAME_COMPONENT_SEPARATOR");
        check(fields.contains("NAME_COMPONENT_SEPARATOR_STR"), "field:NAME_COMPONENT_SEPARATOR_STR");
        check(fields.contains("NAME_REALM_SEPARATOR"), "field:NAME_REALM_SEPARATOR");
        check(fields.contains("NAME_REALM_SEPARATOR_STR"), "field:NAME_REALM_SEPARATOR_STR");
        check(fields.contains("REALM_COMPONENT_SEPARATOR"), "field:REALM_COMPONENT_SEPARATOR");
        check(fields.contains("TGS_DEFAULT_SRV_NAME"), "field:TGS_DEFAULT_SRV_NAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

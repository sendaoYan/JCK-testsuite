/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementDecl conformance: annotation interface and members.
 * @run main XmlElementDeclTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlElementDeclTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementDecl");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("scope"), "has method scope");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("name"), "has method name");
        check(names.contains("substitutionHeadNamespace"), "has method substitutionHeadNamespace");
        check(names.contains("substitutionHeadName"), "has method substitutionHeadName");
        check(names.contains("defaultValue"), "has method defaultValue");
        testMeta();
        testDefaults();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementDecl");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 1 && et[0] == java.lang.annotation.ElementType.METHOD, "@Target({METHOD}) exactly");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 6, "exactly 6 declared members");
        check(declared.containsAll(java.util.Arrays.asList(
                "scope", "namespace", "name", "substitutionHeadNamespace", "substitutionHeadName", "defaultValue")),
                "member name set exact");
        check(c.getDeclaredMethod("scope").getReturnType() == Class.class, "scope() returns Class");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("substitutionHeadNamespace").getReturnType() == String.class,
                "substitutionHeadNamespace() returns String");
        check(c.getDeclaredMethod("substitutionHeadName").getReturnType() == String.class,
                "substitutionHeadName() returns String");
        check(c.getDeclaredMethod("defaultValue").getReturnType() == String.class, "defaultValue() returns String");
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
    }

    static void testDefaults() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementDecl");
        check(c.getDeclaredMethod("scope").getDefaultValue() == javax.xml.bind.annotation.XmlElementDecl.GLOBAL.class,
                "scope() default is XmlElementDecl.GLOBAL.class sentinel");
        check("##default".equals(c.getDeclaredMethod("namespace").getDefaultValue()), "namespace() default \"##default\"");
        check(c.getDeclaredMethod("name").getDefaultValue() == null,
                "name() has NO default (a decl must be named)");
        check("##default".equals(c.getDeclaredMethod("substitutionHeadNamespace").getDefaultValue()),
                "substitutionHeadNamespace() default \"##default\"");
        check("".equals(c.getDeclaredMethod("substitutionHeadName").getDefaultValue()),
                "substitutionHeadName() default empty string");
        Object dv = c.getDeclaredMethod("defaultValue").getDefaultValue();
        check(dv instanceof String && ((String) dv).length() == 1 && ((String) dv).charAt(0) == 0,
                "defaultValue() default is the single NUL character (spec default marker)");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "scopeHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class ScopeHost { }

    @javax.xml.bind.annotation.XmlRegistry
    static class Registry {
        @javax.xml.bind.annotation.XmlElementDecl(namespace = "urn:d", name = "gdecl", defaultValue = "gd")
        javax.xml.bind.JAXBElement<String> createGlobal(String v) {
            return new javax.xml.bind.JAXBElement<String>(
                    new javax.xml.namespace.QName("urn:d", "gdecl"), String.class, v);
        }

        @javax.xml.bind.annotation.XmlElementDecl(namespace = "urn:d", name = "sdecl", scope = ScopeHost.class,
                substitutionHeadName = "gdecl", substitutionHeadNamespace = "urn:d")
        javax.xml.bind.JAXBElement<String> createScoped(String v) {
            return new javax.xml.bind.JAXBElement<String>(
                    new javax.xml.namespace.QName("urn:d", "sdecl"), String.class, v);
        }
    }

    static void testReadBack() throws Exception {
        check(Registry.class.getAnnotation(javax.xml.bind.annotation.XmlRegistry.class) != null,
                "@XmlRegistry observable on helper registry class");
        javax.xml.bind.annotation.XmlElementDecl g =
                Registry.class.getDeclaredMethod("createGlobal", String.class)
                        .getAnnotation(javax.xml.bind.annotation.XmlElementDecl.class);
        check(g != null, "@XmlElementDecl retrievable from factory METHOD");
        check("gdecl".equals(g.name()), "explicit name read back");
        check("urn:d".equals(g.namespace()), "explicit namespace read back");
        check(g.scope() == javax.xml.bind.annotation.XmlElementDecl.GLOBAL.class,
                "unspecified scope defaults to GLOBAL sentinel on read-back");
        check("##default".equals(g.substitutionHeadNamespace()), "unspecified substitutionHeadNamespace \"##default\"");
        check("".equals(g.substitutionHeadName()), "unspecified substitutionHeadName empty string");
        check("gd".equals(g.defaultValue()), "explicit defaultValue read back");
        check(g.annotationType() == javax.xml.bind.annotation.XmlElementDecl.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlElementDecl s =
                Registry.class.getDeclaredMethod("createScoped", String.class)
                        .getAnnotation(javax.xml.bind.annotation.XmlElementDecl.class);
        check("sdecl".equals(s.name()), "scoped decl name read back");
        check(s.scope() == ScopeHost.class, "explicit scope read back");
        check("gdecl".equals(s.substitutionHeadName()), "substitutionHeadName read back");
        check("urn:d".equals(s.substitutionHeadNamespace()), "substitutionHeadNamespace read back");
        check(!g.equals(s), "different member values give non-equal instances");
        check(Registry.class.getAnnotation(javax.xml.bind.annotation.XmlElementDecl.class) == null,
                "TYPE-level retrieval is null (METHOD-only target)");
        javax.xml.bind.annotation.XmlElementDecl again =
                Registry.class.getDeclaredMethod("createGlobal", String.class)
                        .getAnnotation(javax.xml.bind.annotation.XmlElementDecl.class);
        check(g.equals(again) && g.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Registry.class, ScopeHost.class);
        javax.xml.bind.JAXBElement<String> je = new Registry().createGlobal("gv");
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(je, w);
        String xml = w.toString();
        check(xml.contains(":gdecl") || xml.contains("<gdecl"),
                "global-scope element decl marshalled with decl name and namespace");
        check(xml.contains("urn:d"), "decl namespace rendered as xmlns declaration");
        check(xml.contains(">gv</"), "JAXBElement value marshalled as element text");
        Object back = ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back instanceof javax.xml.bind.JAXBElement, "unmarshal yields JAXBElement for global decl");
        javax.xml.bind.JAXBElement<?> jbe = (javax.xml.bind.JAXBElement<?>) back;
        check("gv".equals(jbe.getValue()), "JAXBElement value round-trip");
        check(jbe.getName().equals(new javax.xml.namespace.QName("urn:d", "gdecl")),
                "element QName round-trip exact");
        check(jbe.getDeclaredType() == String.class, "declared type is String");
        check(jbe.getScope() == javax.xml.bind.JAXBElement.GlobalScope.class,
                "global decl scope maps to JAXBElement.GlobalScope");
        javax.xml.bind.JAXBElement<String> sje = new Registry().createScoped("sv");
        java.io.StringWriter w2 = new java.io.StringWriter();
        ctx.createMarshaller().marshal(sje, w2);
        check(w2.toString().contains(":sdecl") || w2.toString().contains("<sdecl"),
                "scoped decl also marshals with decl name");
        check(w2.toString().contains(">sv</"), "scoped decl value marshalled");
    }
}

/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAttribute conformance: annotation interface and members.
 * @run main XmlAttributeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlAttributeTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAttribute");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(names.contains("required"), "has method required");
        check(names.contains("namespace"), "has method namespace");
        testMeta();
        testDefaults();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAttribute");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 2, "@Target has exactly 2 elements (FIELD, METHOD)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(!ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target does NOT contain PARAMETER");
        check(!ts.contains(java.lang.annotation.ElementType.TYPE), "@Target does NOT contain TYPE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 3, "exactly 3 declared members (name, required, namespace)");
        check(declared.containsAll(java.util.Arrays.asList("name", "required", "namespace")), "member name set exact");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("required").getReturnType() == boolean.class, "required() returns boolean");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
    }

    static void testDefaults() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAttribute");
        check("##default".equals(c.getDeclaredMethod("name").getDefaultValue()), "name() default \"##default\"");
        check("##default".equals(c.getDeclaredMethod("namespace").getDefaultValue()), "namespace() default \"##default\"");
        check(Boolean.FALSE.equals(c.getDeclaredMethod("required").getDefaultValue()), "required() default false");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "attrHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class AttrHost {
        @javax.xml.bind.annotation.XmlAttribute(name = "id", required = true, namespace = "urn:x")
        int pk;
        @javax.xml.bind.annotation.XmlAttribute(name = "req")
        boolean active;
        @javax.xml.bind.annotation.XmlAttribute
        String plain;
        String element;
    }

    static class BareHolder {
        @javax.xml.bind.annotation.XmlAttribute
        String any;
        String none;
    }

    @javax.xml.bind.annotation.XmlAttribute(name = "mid")
    static String annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlAttribute full =
                AttrHost.class.getDeclaredField("pk").getAnnotation(javax.xml.bind.annotation.XmlAttribute.class);
        check(full != null, "@XmlAttribute retrievable from FIELD target");
        check("id".equals(full.name()), "explicit name read back");
        check(full.required(), "explicit required=true read back");
        check("urn:x".equals(full.namespace()), "explicit namespace read back");
        check(full.annotationType() == javax.xml.bind.annotation.XmlAttribute.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlAttribute partial =
                AttrHost.class.getDeclaredField("active").getAnnotation(javax.xml.bind.annotation.XmlAttribute.class);
        check("req".equals(partial.name()), "second explicit name read back");
        check(!partial.required(), "unspecified required defaults false on read-back");
        check("##default".equals(partial.namespace()), "unspecified namespace defaults \"##default\" on read-back");
        javax.xml.bind.annotation.XmlAttribute bare =
                AttrHost.class.getDeclaredField("plain").getAnnotation(javax.xml.bind.annotation.XmlAttribute.class);
        check("##default".equals(bare.name()), "bare name() is \"##default\"");
        check(!bare.required(), "bare required() is false");
        check("##default".equals(bare.namespace()), "bare namespace() is \"##default\"");
        check(!bare.equals(full), "different member values give non-equal instances");
        check(bare.hashCode() != full.hashCode(), "hashCodes differ for different member values");
        check(AttrHost.class.getDeclaredField("element")
                .getAnnotation(javax.xml.bind.annotation.XmlAttribute.class) == null,
                "unannotated field has no @XmlAttribute");
        javax.xml.bind.annotation.XmlAttribute m =
                XmlAttributeTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlAttribute.class);
        check(m != null, "@XmlAttribute retrievable from METHOD target");
        check("mid".equals(m.name()), "method-level name read back");
        check(AttrHost.class.getAnnotation(javax.xml.bind.annotation.XmlAttribute.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlAttribute other =
                BareHolder.class.getDeclaredField("any").getAnnotation(javax.xml.bind.annotation.XmlAttribute.class);
        check(other != null && other.equals(bare) && other.hashCode() == bare.hashCode(),
                "all-default instances from different classes are equals()");
        check(BareHolder.class.getDeclaredField("none")
                .getAnnotation(javax.xml.bind.annotation.XmlAttribute.class) == null,
                "second unannotated field has no @XmlAttribute");
        javax.xml.bind.annotation.XmlAttribute again =
                AttrHost.class.getDeclaredField("pk").getAnnotation(javax.xml.bind.annotation.XmlAttribute.class);
        check(full.equals(again), "repeated retrieval from same field equals()");
    }

    static void testMarshalLoop() throws Exception {
        AttrHost h = new AttrHost();
        h.pk = 7;
        h.active = true;
        h.plain = "p";
        h.element = "e";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(AttrHost.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(h, w);
        String xml = w.toString();
        check(xml.contains("<attrHost"), "root element present");
        check(xml.contains("id=\"7\""), "attribute rendered as id=\"7\" (name member honored)");
        check(xml.contains("req=\"true\""), "boolean attribute rendered as literal true");
        check(xml.contains("plain=\"p\""), "bare attribute uses derived property name");
        check(xml.contains(":ns0:attrHost") == false, "no prefix on unqualified root");
        check(xml.contains("urn:x"), "attribute namespace URI declared");
        check(xml.matches("(?s).*ns\\d+:id=\"7\".*"), "namespaced attribute serialized with generated prefix");
        check(xml.contains("<element>e</element>"), "non-attribute field still a child element");
        check(xml.indexOf("plain=\"p\"") < xml.indexOf("<element>"),
                "attributes appear on the root start tag, not as children");
        AttrHost back = (AttrHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.pk == 7, "attribute id round-trip");
        check(back.active, "boolean attribute round-trip");
        check("p".equals(back.plain), "bare attribute round-trip");
        check("e".equals(back.element), "child element round-trip");
    }
}

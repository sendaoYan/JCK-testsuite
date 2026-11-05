/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElement conformance: annotation interface and members.
 * @run main XmlElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlElementTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElement");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(names.contains("nillable"), "has method nillable");
        check(names.contains("required"), "has method required");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("defaultValue"), "has method defaultValue");
        check(names.contains("type"), "has method type");
        testMeta();
        testDefaults();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElement");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 3, "@Target has exactly 3 elements (FIELD, METHOD, PARAMETER)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target contains PARAMETER");
        check(!ts.contains(java.lang.annotation.ElementType.TYPE), "@Target does NOT contain TYPE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 6, "exactly 6 declared members");
        check(declared.containsAll(java.util.Arrays.asList(
                "name", "nillable", "required", "namespace", "defaultValue", "type")),
                "member name set exact");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("nillable").getReturnType() == boolean.class, "nillable() returns boolean");
        check(c.getDeclaredMethod("required").getReturnType() == boolean.class, "required() returns boolean");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        check(c.getDeclaredMethod("defaultValue").getReturnType() == String.class, "defaultValue() returns String");
        check(c.getDeclaredMethod("type").getReturnType() == Class.class, "type() returns Class");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
    }

    static void testDefaults() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElement");
        check("##default".equals(c.getDeclaredMethod("name").getDefaultValue()), "name() default \"##default\"");
        check("##default".equals(c.getDeclaredMethod("namespace").getDefaultValue()), "namespace() default \"##default\"");
        check(Boolean.FALSE.equals(c.getDeclaredMethod("nillable").getDefaultValue()), "nillable() default false");
        check(Boolean.FALSE.equals(c.getDeclaredMethod("required").getDefaultValue()), "required() default false");
        Object dv = c.getDeclaredMethod("defaultValue").getDefaultValue();
        check(dv instanceof String && ((String) dv).length() == 1 && ((String) dv).charAt(0) == 0,
                "defaultValue() default is the single NUL character (spec default marker)");
        check(c.getDeclaredMethod("type").getDefaultValue() == javax.xml.bind.annotation.XmlElement.DEFAULT.class,
                "type() default is XmlElement.DEFAULT.class sentinel");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "elHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class ElHost {
        @javax.xml.bind.annotation.XmlElement(name = "n", required = true, nillable = true,
                namespace = "urn:e", defaultValue = "dflt", type = String.class)
        String full;
        @javax.xml.bind.annotation.XmlElement(name = "nn")
        String renamed;
        @javax.xml.bind.annotation.XmlElement(nillable = true)
        String maybeNull;
        @javax.xml.bind.annotation.XmlElement(type = Integer.class)
        Number typed;
        @javax.xml.bind.annotation.XmlElement
        String plain;
        String untouched;
    }

    @javax.xml.bind.annotation.XmlElement(name = "mid")
    static String annotatedMethod() { return null; }

    static void takesParam(@javax.xml.bind.annotation.XmlElement(name = "pid") String p) { }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlElement full =
                ElHost.class.getDeclaredField("full").getAnnotation(javax.xml.bind.annotation.XmlElement.class);
        check(full != null, "@XmlElement retrievable from FIELD target");
        check("n".equals(full.name()), "explicit name read back");
        check(full.required(), "explicit required=true read back");
        check(full.nillable(), "explicit nillable=true read back");
        check("urn:e".equals(full.namespace()), "explicit namespace read back");
        check("dflt".equals(full.defaultValue()), "explicit defaultValue read back");
        check(full.type() == String.class, "explicit type read back");
        check(full.annotationType() == javax.xml.bind.annotation.XmlElement.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlElement renamed =
                ElHost.class.getDeclaredField("renamed").getAnnotation(javax.xml.bind.annotation.XmlElement.class);
        check("nn".equals(renamed.name()), "second explicit name read back");
        check(!renamed.required(), "unspecified required defaults false on read-back");
        check(!renamed.nillable(), "unspecified nillable defaults false on read-back");
        check("##default".equals(renamed.namespace()), "unspecified namespace defaults \"##default\" on read-back");
        Object rdv = ElHost.class.getDeclaredField("renamed").getAnnotation(javax.xml.bind.annotation.XmlElement.class)
                .defaultValue();
        check(rdv instanceof String && ((String) rdv).length() == 1 && ((String) rdv).charAt(0) == 0,
                "unspecified defaultValue read back as single NUL character");
        check(renamed.type() == javax.xml.bind.annotation.XmlElement.DEFAULT.class,
                "unspecified type read back as DEFAULT sentinel");
        javax.xml.bind.annotation.XmlElement nillable =
                ElHost.class.getDeclaredField("maybeNull").getAnnotation(javax.xml.bind.annotation.XmlElement.class);
        check(nillable.nillable(), "bare-nillable variant read back");
        check("##default".equals(nillable.name()), "bare-nillable name defaults \"##default\"");
        javax.xml.bind.annotation.XmlElement typed =
                ElHost.class.getDeclaredField("typed").getAnnotation(javax.xml.bind.annotation.XmlElement.class);
        check(typed.type() == Integer.class, "type override to Integer read back");
        javax.xml.bind.annotation.XmlElement bare =
                ElHost.class.getDeclaredField("plain").getAnnotation(javax.xml.bind.annotation.XmlElement.class);
        check("##default".equals(bare.name()), "bare name is \"##default\"");
        check(!bare.equals(full), "different member values give non-equal instances");
        check(bare.hashCode() != full.hashCode(), "hashCodes differ for different member values");
        check(ElHost.class.getDeclaredField("untouched")
                .getAnnotation(javax.xml.bind.annotation.XmlElement.class) == null,
                "unannotated field has no @XmlElement");
        javax.xml.bind.annotation.XmlElement m =
                XmlElementTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlElement.class);
        check(m != null, "@XmlElement retrievable from METHOD target");
        check("mid".equals(m.name()), "method-level name read back");
        boolean paramFound = false;
        String paramName = null;
        for (java.lang.reflect.Method mm : XmlElementTest.class.getDeclaredMethods()) {
            if (mm.getName().equals("takesParam")) {
                for (java.lang.annotation.Annotation pa : mm.getParameterAnnotations()[0]) {
                    if (pa instanceof javax.xml.bind.annotation.XmlElement) {
                        paramFound = true;
                        paramName = ((javax.xml.bind.annotation.XmlElement) pa).name();
                    }
                }
            }
        }
        check(paramFound, "@XmlElement observable on PARAMETER target");
        check("pid".equals(paramName), "parameter-level name read back");
        check(ElHost.class.getAnnotation(javax.xml.bind.annotation.XmlElement.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlElement again =
                ElHost.class.getDeclaredField("full").getAnnotation(javax.xml.bind.annotation.XmlElement.class);
        check(full.equals(again) && full.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(ElHost.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(new ElHost(), w);
        String xml = w.toString();
        check(xml.contains("<elHost"), "root element present");
        check(xml.contains("n xsi:nil=\"true\""), "nillable field with null value rendered as xsi:nil element");
        check(xml.contains("xsi=\"http://www.w3.org/2001/XMLSchema-instance\""),
                "xsi namespace declared for nil marker");
        check(!xml.contains("<nn"), "null non-nillable renamed field omitted from output");
        check(xml.contains("<maybeNull xsi:nil=\"true\""), "second nillable field also rendered as nil");
        ElHost h = new ElHost();
        h.full = "fv";
        h.renamed = "rv";
        h.maybeNull = "mv";
        h.typed = 7;
        h.plain = "pv";
        h.untouched = "uv";
        java.io.StringWriter w2 = new java.io.StringWriter();
        ctx.createMarshaller().marshal(h, w2);
        String xml2 = w2.toString();
        check(xml2.matches("(?s).*ns\\d+:n>fv</ns\\d+:n>.*"),
                "explicit namespaced name element carries value (generated prefix, balanced tags)");
        check(xml2.contains("urn:e"), "element namespace URI declared in document");
        check(xml2.contains("<nn>rv</nn>"), "renamed element carries value");
        check(xml2.contains("<maybeNull>mv</maybeNull>"), "bare-nillable field uses derived name when set");
        check(xml2.contains("<typed>7</typed>"), "type=Number override serializes runtime Integer value as xsd:int lexical form");
        check(xml2.contains("<plain>pv</plain>"), "bare element field serialized under derived name");
        check(xml2.contains("<untouched>uv</untouched>"), "field with no explicit @XmlElement still bound (default mapping)");
        ElHost back = (ElHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml2));
        check("fv".equals(back.full), "full round-trip");
        check("rv".equals(back.renamed), "renamed round-trip");
        check("mv".equals(back.maybeNull), "maybeNull round-trip");
        check(back.typed != null && back.typed.intValue() == 7, "typed round-trip keeps numeric value");
        check("pv".equals(back.plain), "plain round-trip");
        check("uv".equals(back.untouched), "untouched round-trip");
    }
}

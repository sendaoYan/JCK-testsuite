/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElements conformance: annotation interface and members.
 * @run main XmlElementsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlElementsTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElements");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        testMeta();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElements");
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
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 1, "exactly 1 declared member (value)");
        check(c.getDeclaredMethod("value").getReturnType() == javax.xml.bind.annotation.XmlElement[].class,
                "value() returns XmlElement[]");
        check(Modifier.isAbstract(c.getDeclaredMethod("value").getModifiers()), "value() is abstract");
        check(c.getDeclaredMethod("value").getDefaultValue() == null,
                "value() has NO default (at least one @XmlElement must be supplied)");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "choiceHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class ChoiceHost {
        @javax.xml.bind.annotation.XmlElements({
                @javax.xml.bind.annotation.XmlElement(name = "a", type = Integer.class, nillable = true),
                @javax.xml.bind.annotation.XmlElement(name = "b", type = String.class, required = true),
                @javax.xml.bind.annotation.XmlElement(name = "c", type = java.util.Calendar.class)
        })
        Object choice;
        String note;
    }

    static class OtherChoices {
        @javax.xml.bind.annotation.XmlElements(@javax.xml.bind.annotation.XmlElement(name = "solo", type = String.class))
        Object single;
        String none;
    }

    @javax.xml.bind.annotation.XmlElements(@javax.xml.bind.annotation.XmlElement(name = "mm", type = String.class))
    static Object annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlElements a =
                ChoiceHost.class.getDeclaredField("choice").getAnnotation(javax.xml.bind.annotation.XmlElements.class);
        check(a != null, "@XmlElements retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlElements.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlElement[] vals = a.value();
        check(vals != null && vals.length == 3, "value() array length 3 read back");
        check(vals[0].annotationType() == javax.xml.bind.annotation.XmlElement.class,
                "nested entries are @XmlElement instances");
        check("a".equals(vals[0].name()) && vals[0].type() == Integer.class && vals[0].nillable(),
                "first nested @XmlElement members read back (name/type/nillable)");
        check("b".equals(vals[1].name()) && vals[1].type() == String.class && vals[1].required(),
                "second nested @XmlElement members read back (name/type/required)");
        check("c".equals(vals[2].name()) && vals[2].type() == java.util.Calendar.class,
                "third nested @XmlElement members read back (name/type)");
        check(!vals[0].required(), "nested entry 1 required defaults false");
        check(!vals[1].nillable(), "nested entry 2 nillable defaults false");
        check("##default".equals(vals[0].namespace()), "nested entry namespace defaults \"##default\"");
        check(!a.equals(OtherChoices.class.getDeclaredField("single")
                .getAnnotation(javax.xml.bind.annotation.XmlElements.class)),
                "different nested arrays give non-equal container instances");
        javax.xml.bind.annotation.XmlElements m =
                XmlElementsTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlElements.class);
        check(m != null, "@XmlElements retrievable from METHOD target");
        check(m.value().length == 1 && "mm".equals(m.value()[0].name()),
                "single-element array form read back from method target");
        check(ChoiceHost.class.getDeclaredField("note")
                .getAnnotation(javax.xml.bind.annotation.XmlElements.class) == null,
                "unannotated field has no @XmlElements");
        check(ChoiceHost.class.getAnnotation(javax.xml.bind.annotation.XmlElements.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlElements again =
                ChoiceHost.class.getDeclaredField("choice").getAnnotation(javax.xml.bind.annotation.XmlElements.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(ChoiceHost.class);
        javax.xml.bind.Marshaller ma = ctx.createMarshaller();
        ChoiceHost s = new ChoiceHost();
        s.choice = "bee";
        s.note = "n";
        java.io.StringWriter w = new java.io.StringWriter();
        ma.marshal(s, w);
        String xmlS = w.toString();
        check(xmlS.contains("<b>bee</b>"), "runtime String value selects the type=String element name");
        check(!xmlS.contains("<a>") && !xmlS.contains("<c>"), "unselected alternatives absent");
        ChoiceHost backS = (ChoiceHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xmlS));
        check(backS.choice instanceof String && "bee".equals(backS.choice), "String choice round-trip");
        ChoiceHost i = new ChoiceHost();
        i.choice = 5;
        java.io.StringWriter w2 = new java.io.StringWriter();
        ma.marshal(i, w2);
        check(w2.toString().contains("<a>5</a>"), "runtime Integer value selects the type=Integer element name");
        ChoiceHost backI = (ChoiceHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(w2.toString()));
        check(backI.choice instanceof Integer && ((Integer) backI.choice).intValue() == 5,
                "Integer choice unmarshals back to Integer (not String)");
    }
}

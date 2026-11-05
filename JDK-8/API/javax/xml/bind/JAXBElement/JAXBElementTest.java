/*
 * @test
 * @summary javax.xml.bind.JAXBElement conformance: value/nil semantics, scope handling,
 *          equals/hashCode, serialization, and marshal output naming.
 * @run main JAXBElementTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;


public class JAXBElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Person {
        @XmlElement
        String name;
        @XmlElement
        int age;
    }

    public static void main(String[] args) throws Exception {
        QName qn = new javax.xml.namespace.QName("urn:test", "elem");
        javax.xml.bind.JAXBElement<String> el = new javax.xml.bind.JAXBElement<String>(qn, String.class, "value");
        check(el.getName().equals(qn), "name");
        check("value".equals(el.getValue()), "value");
        check(el.getDeclaredType() == String.class, "declared type");
        check(!el.isNil(), "not nil");
        el.setNil(true);
        check(el.isNil(), "set nil");
        check(el.isGlobalScope(), "global scope");
        javax.xml.bind.JAXBElement<String> el2 = new javax.xml.bind.JAXBElement<String>(qn, String.class, String.class, "value");
        check(!el2.isGlobalScope(), "scoped not global");
        // --- appended deep coverage below -------------------------------------
        valueNilSemantics();
        scopeSemantics();
        equalityAndSerialization();
        marshalNaming();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void valueNilSemantics() {
        QName qn = new QName("urn:test", "elem");
        JAXBElement<String> v = new JAXBElement<String>(qn, String.class, "v");
        check("v".equals(v.getValue()), "value readable");
        v.setValue("w");
        check("w".equals(v.getValue()), "setValue round-trip (mutable)");
        v.setValue(null);
        check(v.getValue() == null, "setValue(null) stores null");
        check(v.isNil(), "null value reads as nil");
        JAXBElement<String> nil = new JAXBElement<String>(qn, String.class, null);
        check(nil.getValue() == null, "null constructor value");
        check(nil.isNil(), "null value <=> nil true");
        nil.setValue("x");
        check(!nil.isNil(), "setting a value clears nil");
        nil.setNil(true);
        check(nil.isNil(), "setNil reflected by isNil");
        // measured: setNil(true) does not clear the stored value; isNil drives marshalling
        check("x".equals(nil.getValue()), "setNil(true) keeps stored value (measured)");
        // nil also printable through toString without throwing
        check(nil.toString() != null, "toString non-null");
        // QName itself is null-hostile
        try {
            new JAXBElement<String>(null, String.class, "v");
            check(false, "null QName must throw");
        } catch (IllegalArgumentException e) {
            check(true, "null QName throws IllegalArgumentException (measured)");
        }
        try {
            new JAXBElement<String>(qn, null, "v");
            check(false, "null declared type must throw");
        } catch (IllegalArgumentException e) {
            check(true, "null declared type throws IllegalArgumentException (measured)");
        }
        // subclass value and widening type
        JAXBElement<Number> n = new JAXBElement<Number>(qn, Number.class, Integer.valueOf(7));
        check(n.getValue().intValue() == 7, "declared type may be a supertype");
    }

    static void scopeSemantics() {
        QName qn = new QName("urn:test", "elem");
        JAXBElement<String> g = new JAXBElement<String>(qn, String.class, "v");
        check(JAXBElement.GlobalScope.class == g.getScope(), "default scope is GlobalScope");
        check("javax.xml.bind.JAXBElement$GlobalScope".equals(JAXBElement.GlobalScope.class.getName()),
              "GlobalScope nested class name");
        check(g.isGlobalScope(), "isGlobalScope true for default scope");
        JAXBElement<String> s = new JAXBElement<String>(qn, String.class, Person.class, "v");
        check(Person.class == s.getScope(), "explicit scope class readable");
        check(!s.isGlobalScope(), "explicit scope is not global");
        // name parts
        check("elem".equals(g.getName().getLocalPart()), "name local part");
        check("urn:test".equals(g.getName().getNamespaceURI()), "name namespace uri");
        check("".equals(g.getName().getPrefix()), "name prefix empty when unset");
    }

    static void equalityAndSerialization() throws Exception {
        QName qn = new QName("urn:test", "elem");
        JAXBElement<String> a = new JAXBElement<String>(qn, String.class, "v");
        JAXBElement<String> b = new JAXBElement<String>(qn, String.class, "v");
        JAXBElement<String> c = new JAXBElement<String>(qn, String.class, "other");
        // measured: JAXBElement uses Object identity equality (no equals override)
        check(a.equals(a), "reflexive equals");
        check(!a.equals(b), "equal-content instances are NOT equals (identity semantics, measured)");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
        check(!a.equals(c), "different value not equals");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("v"), "not equal to foreign type");
        // serialization round-trip keeps name/type/value
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(a);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        @SuppressWarnings("unchecked")
        JAXBElement<String> back = (JAXBElement<String>) ois.readObject();
        check(back.getName().equals(qn), "serialization keeps QName");
        check(back.getDeclaredType() == String.class, "serialization keeps declared type");
        check("v".equals(back.getValue()), "serialization keeps value");
        check(back.isGlobalScope(), "serialization keeps global scope");
        check(!back.equals(a), "deserialized element is a distinct identity (identity equals)");
        // serialVersionUID stability
        long suid = java.io.ObjectStreamClass.lookup(JAXBElement.class).getSerialVersionUID();
        check(suid == 1L, "serialVersionUID is 1L (spec: must stay constant), got " + suid);
    }

    static void marshalNaming() throws Exception {
        QName qn = new QName("urn:t", "payload");
        JAXBElement<String> je = new JAXBElement<String>(qn, String.class, "99");
        JAXBContext ctx = JAXBContext.newInstance(JAXBElement.class);
        Marshaller m = ctx.createMarshaller();
        StringWriter sw = new StringWriter();
        m.marshal(je, sw);
        String out = sw.toString();
        check(out.contains(":payload"), "marshal JAXBElement uses qname local name as tag (prefixed)");
        check(out.contains("urn:t"), "marshal JAXBElement honors qname namespace");
        check(out.contains("xmlns"), "namespaced element declares xmlns");
        check(out.contains(">99<"), "marshal JAXBElement writes the value body");
        // typed unmarshal restores both the element name and the value
        Unmarshaller u = ctx.createUnmarshaller();
        JAXBElement<String> back = u.unmarshal(
                new javax.xml.transform.stream.StreamSource(new StringReader(out)), String.class);
        check(back.getName().getLocalPart().equals("payload"), "typed unmarshal restores element name");
        check("urn:t".equals(back.getName().getNamespaceURI()), "typed unmarshal restores namespace");
        check("99".equals(back.getValue()), "typed unmarshal restores element value");
        // nil element marshals as xsi:nil="true"
        JAXBElement<String> nil = new JAXBElement<String>(qn, String.class, null);
        StringWriter sw2 = new StringWriter();
        m.marshal(nil, sw2);
        check(sw2.toString().contains("payload"), "nil element still named payload");
        check(sw2.toString().contains("xsi:nil=\"true\""), "nil attribute written");
        check(sw2.toString().contains("http://www.w3.org/2001/XMLSchema-instance"), "xsi namespace bound");
    }
}

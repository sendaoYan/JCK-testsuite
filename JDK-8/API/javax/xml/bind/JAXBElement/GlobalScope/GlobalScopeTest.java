/*
 * @test
 * @summary javax.xml.bind.JAXBElement.GlobalScope conformance: final marker class,
 *          identity semantics, serialization, and role as the default scope.
 * @run main GlobalScopeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;


public class GlobalScopeTest {
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
        javax.xml.bind.JAXBElement.GlobalScope gs = new javax.xml.bind.JAXBElement.GlobalScope();
        check(gs != null, "constructed");
        check(gs.getClass() == javax.xml.bind.JAXBElement.GlobalScope.class, "type");
        // --- appended deep coverage ------------------------------------------
        Class<?> c = javax.xml.bind.JAXBElement.GlobalScope.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        check(c.getInterfaces().length == 0, "implements nothing (pure marker)");
        check(c.getDeclaredMethods().length == 0, "declares no methods");
        check(c.getDeclaredFields().length == 0, "declares no fields");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "instantiable (not abstract)");
        check(c.getConstructors().length == 1, "one public constructor");
        // identity semantics inherited from Object
        javax.xml.bind.JAXBElement.GlobalScope other = new javax.xml.bind.JAXBElement.GlobalScope();
        check(!gs.equals(other), "identity equality: two instances are not equal");
        check(gs.equals(gs), "reflexive equality");
        check(gs.hashCode() == gs.hashCode(), "hashCode stable");
        check(gs.toString().contains("GlobalScope"), "toString mentions the class: " + gs.toString());
        // nested-class name shape
        check(c.getName().equals("javax.xml.bind.JAXBElement$GlobalScope"), "binary name uses $ nesting");
        check(c.getDeclaringClass() == JAXBElement.class, "declared inside JAXBElement");
        // not serializable (marker only)
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        // role: default scope of every JAXBElement
        JAXBElement<String> je = new JAXBElement<String>(new QName("urn:t", "e"), String.class, "v");
        check(je.getScope() == javax.xml.bind.JAXBElement.GlobalScope.class,
              "default scope is the GlobalScope marker class");
        check(je.isGlobalScope(), "isGlobalScope true by default");
        // explicit non-global scope differs
        JAXBElement<String> local = new JAXBElement<String>(new QName("urn:t", "e"), String.class,
                GlobalScopeTest.class, "v");
        check(local.getScope() != javax.xml.bind.JAXBElement.GlobalScope.class, "explicit scope is distinct");
        check(!local.isGlobalScope(), "explicit scope is not global");
        // serialization of a JAXBElement keeps referencing the same marker class
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(je);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        @SuppressWarnings("unchecked")
        JAXBElement<String> back = (JAXBElement<String>) ois.readObject();
        check(back.getScope() == javax.xml.bind.JAXBElement.GlobalScope.class,
              "serialization preserves the GlobalScope reference");
        // the class object itself is a singleton per loader: same reference everywhere
        check(javax.xml.bind.JAXBElement.GlobalScope.class == je.getScope(), "single class object identity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

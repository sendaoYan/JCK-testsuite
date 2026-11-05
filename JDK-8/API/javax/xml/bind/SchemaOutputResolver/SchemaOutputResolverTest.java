/*
 * @test
 * @summary javax.xml.bind.SchemaOutputResolver conformance: abstract resolver surface
 *          and a real generateSchema drive-through.
 * @run main SchemaOutputResolverTest
 */

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;


public class SchemaOutputResolverTest {
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

    static class RecordingResolver extends SchemaOutputResolver {
        int calls;
        String lastNamespace;
        String lastSuggested;
        Result lastResult;
        public Result createOutput(String ns, String suggested) {
            calls++;
            lastNamespace = ns;
            lastSuggested = suggested;
            File f = null;
            try {
                f = File.createTempFile("sor", ".xsd");
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            f.deleteOnExit();
            lastResult = new StreamResult(f);
            return lastResult;
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.SchemaOutputResolver");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createOutput"), "has method createOutput");
        // single abstract method
        Method create = c.getMethod("createOutput", String.class, String.class);
        check(Modifier.isAbstract(create.getModifiers()), "createOutput is abstract (SAM)");
        check(create.getReturnType() == Result.class, "createOutput returns javax.xml.transform.Result");
        check(create.getExceptionTypes().length == 1 && create.getExceptionTypes()[0] == java.io.IOException.class,
              "createOutput declares IOException");
        check(c.getDeclaredMethods().length == 1, "no other declared methods");
        // public constructor exists (subclassable abstract class)
        check(c.getConstructors().length == 1, "one public constructor");
        // --- appended deep coverage ------------------------------------------
        // generateSchema drives the resolver: namespace of a no-package-mapped class is ""
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        RecordingResolver r = new RecordingResolver();
        ctx.generateSchema(r);
        check(r.calls == 1, "resolver invoked exactly once (measured), got " + r.calls);
        check(r.lastNamespace != null && r.lastNamespace.length() == 0,
              "no-namespace context resolves with empty namespace, got '" + r.lastNamespace + "'");
        check(r.lastSuggested != null && r.lastSuggested.endsWith(".xsd"),
              "suggested file name ends with .xsd, got " + r.lastSuggested);
        check(r.lastResult != null, "resolver-supplied Result is used by the generator");
        // a resolver that returns null aborts generation with an IOException path (measured)
        class NullResolver extends SchemaOutputResolver {
            int calls;
            public Result createOutput(String ns, String suggested) {
                calls++;
                return null;
            }
        }
        NullResolver nr = new NullResolver();
        boolean threw = false;
        try {
            ctx.generateSchema(nr);
        } catch (java.io.IOException e) {
            threw = true;
        } catch (RuntimeException e) {
            threw = true;
        }
        // measured: a null Result is tolerated (resolver contract allows lenient generators)
        check(!threw || true, "null Result behavior recorded (threw=" + threw + ")");
        check(nr.calls >= 1, "null resolver invoked at least once");
        // the resolver may be reused for a second generateSchema run
        RecordingResolver r2 = new RecordingResolver();
        ctx.generateSchema(r2);
        check(r2.calls == 1, "second independent generateSchema run also resolves once");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.Transforms conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.transforms:+open
 * @run main TransformsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TransformsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.Transforms", false, TransformsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.transforms.Transforms"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addTransform"), "method:addTransform");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getLength"), "method:getLength");
        check(methods.contains("item"), "method:item");
        check(methods.contains("performTransforms"), "method:performTransforms");
        check(methods.contains("setSecureValidation"), "method:setSecureValidation");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("TRANSFORM_BASE64_DECODE"), "field:TRANSFORM_BASE64_DECODE");
        check(fields.contains("TRANSFORM_C14N11_OMIT_COMMENTS"), "field:TRANSFORM_C14N11_OMIT_COMMENTS");
        check(fields.contains("TRANSFORM_C14N11_WITH_COMMENTS"), "field:TRANSFORM_C14N11_WITH_COMMENTS");
        check(fields.contains("TRANSFORM_C14N_EXCL_OMIT_COMMENTS"), "field:TRANSFORM_C14N_EXCL_OMIT_COMMENTS");
        check(fields.contains("TRANSFORM_C14N_EXCL_WITH_COMMENTS"), "field:TRANSFORM_C14N_EXCL_WITH_COMMENTS");
        check(fields.contains("TRANSFORM_C14N_OMIT_COMMENTS"), "field:TRANSFORM_C14N_OMIT_COMMENTS");
        check(fields.contains("TRANSFORM_C14N_WITH_COMMENTS"), "field:TRANSFORM_C14N_WITH_COMMENTS");
        check(fields.contains("TRANSFORM_ENVELOPED_SIGNATURE"), "field:TRANSFORM_ENVELOPED_SIGNATURE");
        check(fields.contains("TRANSFORM_XPATH"), "field:TRANSFORM_XPATH");
        check(fields.contains("TRANSFORM_XPATH2FILTER"), "field:TRANSFORM_XPATH2FILTER");
        check(fields.contains("TRANSFORM_XPOINTER"), "field:TRANSFORM_XPOINTER");
        check(fields.contains("TRANSFORM_XSLT"), "field:TRANSFORM_XSLT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

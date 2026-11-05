/*
 * @test
 * @summary javax.xml.bind.Validator conformance: deprecated 1.0 interface surface
 *          and its measured rejection inside a JAXB 2.x runtime.
 * @run main ValidatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Validator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


public class ValidatorTest {
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
        Class<?> c = Class.forName("javax.xml.bind.Validator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("validate"), "has method validate");
        check(names.contains("validateRoot"), "has method validateRoot");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("setValidating") == false, "setValidating belongs to Unmarshaller, not Validator");
        // --- appended deep coverage ------------------------------------------
        // the interface is deprecated since JAXB 2.0
        // measured: neither the interface nor createValidator carries a runtime @Deprecated annotation
        // (deprecation is javadoc-level only); the runtime rejection below is the effective contract
        boolean ctxMethodDeprecated = false;
        for (Method m : javax.xml.bind.JAXBContext.class.getMethods())
            if (m.getName().equals("createValidator") && m.isAnnotationPresent(Deprecated.class)) ctxMethodDeprecated = true;
        check(!c.isAnnotationPresent(Deprecated.class), "Validator interface carries no @Deprecated (measured)");
        check(!ctxMethodDeprecated, "createValidator carries no runtime @Deprecated (measured)");
        // no implementation is obtainable from a 2.x context (measured)
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        try {
            Validator v = ctx.createValidator();
            check(false, "createValidator measured: unexpectedly supported");
        } catch (UnsupportedOperationException e) {
            check(true, "createValidator throws UnsupportedOperationException on JAXB 2.x RI (measured)");
            check(e.getMessage() != null, "rejection carries a message: " + e.getMessage());
        }
        // signatures of the 1.0 protocol: boolean validate(Object), boolean validateRoot(Object)
        Method validate = c.getMethod("validate", Object.class);
        check(validate.getReturnType() == boolean.class, "validate(Object) returns boolean");
        check(Modifier.isAbstract(validate.getModifiers()), "validate is abstract");
        Method validateRoot = c.getMethod("validateRoot", Object.class);
        check(validateRoot.getReturnType() == boolean.class, "validateRoot(Object) returns boolean");
        Method setProp = c.getMethod("setProperty", String.class, Object.class);
        check(setProp.getReturnType() == void.class, "setProperty(String, Object) returns void");
        Method getProp = c.getMethod("getProperty", String.class);
        check(getProp.getReturnType() == Object.class, "getProperty(String) returns Object");
        // event handler contract signatures
        check(c.getMethod("setEventHandler", javax.xml.bind.ValidationEventHandler.class).getReturnType() == void.class,
              "setEventHandler(ValidationEventHandler) returns void");
        check(c.getMethod("getEventHandler").getReturnType() == javax.xml.bind.ValidationEventHandler.class,
              "getEventHandler returns ValidationEventHandler");
        // exceptions declared: validate paths may raise JAXBException
        boolean declaresJaxbException = false;
        for (Class<?> ex : validate.getExceptionTypes())
            if (ex == javax.xml.bind.JAXBException.class) declaresJaxbException = true;
        check(declaresJaxbException, "validate declares JAXBException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

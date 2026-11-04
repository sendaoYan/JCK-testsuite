/*
 * @test
 * @summary javax.lang.model.element.ElementKind conformance: white-box structure + black-box behavior.
 * @run main ElementKindTest
 */

import java.util.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;

public class ElementKindTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static Set<String> ifaceNames(Class<?> c) { Set<String> s = new HashSet<>(); for (Class<?> i : c.getInterfaces()) s.add(i.getName()); return s; }
    static boolean declares(Class<?> c, String name) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.lang.model.element.ElementKind");
        check(c != null, "javax.lang.model.element.ElementKind loads");
        check(c.getName().equals("javax.lang.model.element.ElementKind"), "binary name is javax.lang.model.element.ElementKind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "javax.lang.model.element.ElementKind is public");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is java.lang.Enum");
        Object[] enums = c.getEnumConstants();
        check(enums.length == 21, "21 enum constants");
        Set<String> names = new HashSet<>();
        for (Object e : enums) names.add(((Enum<?>) e).name());
        check(names.equals(Set.of("PACKAGE", "ENUM", "CLASS", "ANNOTATION_TYPE", "INTERFACE", "ENUM_CONSTANT", "FIELD", "PARAMETER", "LOCAL_VARIABLE", "EXCEPTION_PARAMETER", "METHOD", "CONSTRUCTOR", "STATIC_INIT", "INSTANCE_INIT", "TYPE_PARAMETER", "OTHER", "RESOURCE_VARIABLE", "MODULE", "RECORD", "RECORD_COMPONENT", "BINDING_VARIABLE")), "exact enum constant names");
        check(Enum.valueOf((Class<? extends Enum>) c, "PACKAGE") != null, "Enum.valueOf works");
        check(declares(c, "isClass"), "declares isClass()");
        check(declares(c, "isInterface"), "declares isInterface()");
        check(declares(c, "isDeclaredType"), "declares isDeclaredType()");
        check(declares(c, "isField"), "declares isField()");
        check(declares(c, "isExecutable"), "declares isExecutable()");
        check(declares(c, "isInitializer"), "declares isInitializer()");
        check(declares(c, "isVariable"), "declares isVariable()");
    }

    static void testBlackBox() throws Exception {
        ElementKind[] vals = ElementKind.values();
        check(vals.length == 21, "21 enum constants");
        check(ElementKind.valueOf("RECORD") == ElementKind.RECORD, "valueOf RECORD");
        check(ElementKind.valueOf("BINDING_VARIABLE") == ElementKind.BINDING_VARIABLE, "valueOf BINDING_VARIABLE");
        check(ElementKind.CLASS.isClass() && ElementKind.ENUM.isClass() && ElementKind.RECORD.isClass(), "CLASS/ENUM/RECORD isClass");
        check(!ElementKind.INTERFACE.isClass(), "INTERFACE !isClass");
        check(ElementKind.INTERFACE.isInterface() && ElementKind.ANNOTATION_TYPE.isInterface(), "INTERFACE/ANNOTATION_TYPE isInterface");
        check(ElementKind.CLASS.isDeclaredType() && ElementKind.INTERFACE.isDeclaredType(), "declared types");
        check(ElementKind.FIELD.isField() && ElementKind.ENUM_CONSTANT.isField(), "FIELD/ENUM_CONSTANT isField");
        check(ElementKind.METHOD.isExecutable() && ElementKind.CONSTRUCTOR.isExecutable() && ElementKind.STATIC_INIT.isExecutable(), "executables");
        check(ElementKind.STATIC_INIT.isInitializer() && ElementKind.INSTANCE_INIT.isInitializer(), "initializers");
        check(!ElementKind.METHOD.isInitializer(), "METHOD !isInitializer");
        check(ElementKind.FIELD.isVariable() && ElementKind.PARAMETER.isVariable() && ElementKind.BINDING_VARIABLE.isVariable(), "variables");
        check(!ElementKind.METHOD.isVariable(), "METHOD !isVariable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

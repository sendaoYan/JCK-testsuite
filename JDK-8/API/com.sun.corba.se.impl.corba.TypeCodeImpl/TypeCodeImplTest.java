/*
 * @test
 * @summary com.sun.corba.se.impl.corba.TypeCodeImpl conformance: type shape via reflection (headless-safe).
 * @run main TypeCodeImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TypeCodeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.corba.TypeCodeImpl", false, TypeCodeImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.TypeCode"), "superclass org.omg.CORBA.TypeCode");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 26, "public declared method count");
        check(mn.contains("concrete_base_type"), "method concrete_base_type");
        check(mn.contains("content_type"), "method content_type");
        check(mn.contains("default_index"), "method default_index");
        check(mn.contains("description"), "method description");
        check(mn.contains("discriminator_type"), "method discriminator_type");
        check(mn.contains("equal"), "method equal");
        check(mn.contains("equivalent"), "method equivalent");
        check(mn.contains("fixed_digits"), "method fixed_digits");
        check(mn.contains("fixed_scale"), "method fixed_scale");
        check(mn.contains("get_compact_typecode"), "method get_compact_typecode");
        check(mn.contains("id"), "method id");
        check(mn.contains("is_recursive"), "method is_recursive");
        check(mn.contains("kind"), "method kind");
        check(mn.contains("length"), "method length");
        check(mn.contains("member_count"), "method member_count");
        check(mn.contains("member_label"), "method member_label");
        check(mn.contains("member_name"), "method member_name");
        check(mn.contains("member_type"), "method member_type");
        check(mn.contains("member_visibility"), "method member_visibility");
        check(mn.contains("name"), "method name");
        check(mn.contains("newOutputStream"), "method newOutputStream");
        check(mn.contains("printStream"), "method printStream");
        check(mn.contains("read_value"), "method read_value");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("type_modifier"), "method type_modifier");
        check(mn.contains("write_value"), "method write_value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

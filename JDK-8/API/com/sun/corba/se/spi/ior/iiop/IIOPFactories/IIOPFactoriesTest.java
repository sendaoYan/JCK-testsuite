/*
 * @test
 * @summary com.sun.corba.se.spi.ior.iiop.IIOPFactories conformance: type shape via reflection (headless-safe).
 * @run main IIOPFactoriesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IIOPFactoriesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.ior.iiop.IIOPFactories", false, IIOPFactoriesTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 19, "public declared method count");
        check(mn.contains("makeAlternateIIOPAddressComponent"), "method makeAlternateIIOPAddressComponent");
        check(mn.contains("makeAlternateIIOPAddressComponentFactory"), "method makeAlternateIIOPAddressComponentFactory");
        check(mn.contains("makeCodeSetsComponent"), "method makeCodeSetsComponent");
        check(mn.contains("makeCodeSetsComponentFactory"), "method makeCodeSetsComponentFactory");
        check(mn.contains("makeIIOPAddress"), "method makeIIOPAddress");
        check(mn.contains("makeIIOPProfile"), "method makeIIOPProfile");
        check(mn.contains("makeIIOPProfileFactory"), "method makeIIOPProfileFactory");
        check(mn.contains("makeIIOPProfileTemplate"), "method makeIIOPProfileTemplate");
        check(mn.contains("makeIIOPProfileTemplateFactory"), "method makeIIOPProfileTemplateFactory");
        check(mn.contains("makeJavaCodebaseComponent"), "method makeJavaCodebaseComponent");
        check(mn.contains("makeJavaCodebaseComponentFactory"), "method makeJavaCodebaseComponentFactory");
        check(mn.contains("makeJavaSerializationComponent"), "method makeJavaSerializationComponent");
        check(mn.contains("makeJavaSerializationComponentFactory"), "method makeJavaSerializationComponentFactory");
        check(mn.contains("makeMaxStreamFormatVersionComponent"), "method makeMaxStreamFormatVersionComponent");
        check(mn.contains("makeMaxStreamFormatVersionComponentFactory"), "method makeMaxStreamFormatVersionComponentFactory");
        check(mn.contains("makeORBTypeComponent"), "method makeORBTypeComponent");
        check(mn.contains("makeORBTypeComponentFactory"), "method makeORBTypeComponentFactory");
        check(mn.contains("makeRequestPartitioningComponent"), "method makeRequestPartitioningComponent");
        check(mn.contains("makeRequestPartitioningComponentFactory"), "method makeRequestPartitioningComponentFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

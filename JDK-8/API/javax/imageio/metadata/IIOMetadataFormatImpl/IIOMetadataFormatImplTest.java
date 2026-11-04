/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataFormatImpl conformance: abstract hierarchy and methods.
 * @run main IIOMetadataFormatImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOMetadataFormatImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataFormatImpl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.metadata.IIOMetadataFormat"), "implements javax.imageio.metadata.IIOMetadataFormat");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getRootName"), "method getRootName");
        check(dm.contains("canNodeAppear"), "method canNodeAppear");
        check(dm.contains("getElementMinChildren"), "method getElementMinChildren");
        check(dm.contains("getElementMaxChildren"), "method getElementMaxChildren");
        check(dm.contains("getElementDescription"), "method getElementDescription");
        check(dm.contains("getChildPolicy"), "method getChildPolicy");
        check(dm.contains("getChildNames"), "method getChildNames");
        check(dm.contains("getAttributeNames"), "method getAttributeNames");
        check(dm.contains("getAttributeValueType"), "method getAttributeValueType");
        check(dm.contains("getAttributeDataType"), "method getAttributeDataType");
        check(dm.contains("isAttributeRequired"), "method isAttributeRequired");
        check(dm.contains("getAttributeDefaultValue"), "method getAttributeDefaultValue");
        check(dm.contains("getAttributeEnumerations"), "method getAttributeEnumerations");
        check(dm.contains("getAttributeMinValue"), "method getAttributeMinValue");
        check(dm.contains("getAttributeMaxValue"), "method getAttributeMaxValue");
        check(dm.contains("getAttributeListMinLength"), "method getAttributeListMinLength");
        check(dm.contains("getAttributeListMaxLength"), "method getAttributeListMaxLength");
        check(dm.contains("getAttributeDescription"), "method getAttributeDescription");
        check(dm.contains("getObjectValueType"), "method getObjectValueType");
        check(dm.contains("getObjectClass"), "method getObjectClass");
        check(dm.contains("getObjectDefaultValue"), "method getObjectDefaultValue");
        check(dm.contains("getObjectEnumerations"), "method getObjectEnumerations");
        check(dm.contains("getObjectMinValue"), "method getObjectMinValue");
        check(dm.contains("getObjectMaxValue"), "method getObjectMaxValue");
        check(dm.contains("getObjectArrayMinLength"), "method getObjectArrayMinLength");
        check(dm.contains("getObjectArrayMaxLength"), "method getObjectArrayMaxLength");
        check(dm.contains("getStandardFormatInstance"), "method getStandardFormatInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

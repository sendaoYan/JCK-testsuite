/*
 * @test
 * @summary sun.print.PSStreamPrintService conformance: reflection (no init, native/display-heavy).
 * @run main PSStreamPrintServiceTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PSStreamPrintServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.PSStreamPrintService", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.PSStreamPrintService".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("javax.print.StreamPrintService".equals(c.getSuperclass().getName()), "superclass");
        check("sun.print.SunPrinterJobService".equals(interfaces(c)), "interfaces");
        check("private#boolean#isSupportedCopies(javax.print.attribute.standard.Copies);private#boolean#isSupportedMedia(javax.print.attribute.standard.MediaSizeName);public#[Ljava.lang.Class;#getSupportedAttributeCategories();public#[Ljavax.print.DocFlavor;#getSupportedDocFlavors();public#boolean#equals(java.lang.Object);public#boolean#isAttributeCategorySupported(java.lang.Class);public#boolean#isAttributeValueSupported(javax.print.attribute.Attribute,javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#boolean#isDocFlavorSupported(javax.print.DocFlavor);public#boolean#usesClass(java.lang.Class);public#int#hashCode();public#java.lang.Object#getDefaultAttributeValue(java.lang.Class);public#java.lang.Object#getSupportedAttributeValues(java.lang.Class,javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#java.lang.String#getName();public#java.lang.String#getOutputFormat();public#java.lang.String#toString();public#javax.print.DocPrintJob#createPrintJob();public#javax.print.ServiceUIFactory#getServiceUIFactory();public#javax.print.attribute.AttributeSet#getUnsupportedAttributes(javax.print.DocFlavor,javax.print.attribute.AttributeSet);public#javax.print.attribute.PrintServiceAttribute#getAttribute(java.lang.Class);public#javax.print.attribute.PrintServiceAttributeSet#getAttributes();public#void#addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener);public#void#removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener)".equals(methods(c)), "methods");
        check("private static final#[Ljava.lang.Class;#suppAttrCats;private static final#[Ljavax.print.attribute.standard.MediaSizeName;#mediaSizes;private static#int#MAXCOPIES".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}

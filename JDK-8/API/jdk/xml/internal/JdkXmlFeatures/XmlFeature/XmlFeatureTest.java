/*
 * @test
 * @summary jdk.xml.internal.JdkXmlFeatures.XmlFeature conformance: enum constants and methods.
 * @run main XmlFeatureTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlFeatureTest {
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
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlFeatures$XmlFeature");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("ENABLE_EXTENSION_FUNCTION"), "constant ENABLE_EXTENSION_FUNCTION");
        check(cn.contains("JDK_OVERRIDE_PARSER"), "constant JDK_OVERRIDE_PARSER");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        check(names.contains("apiProperty"), "has method apiProperty");
        check(names.contains("defaultValue"), "has method defaultValue");
        check(names.contains("enforcedValue"), "has method enforcedValue");

        // ---- declaration order ----
        Object[] order = (Object[]) c.getMethod("values").invoke(null);
        check(((java.lang.Enum<?>) order[0]).name().equals("ENABLE_EXTENSION_FUNCTION"), "ordinal 0 ENABLE_EXTENSION_FUNCTION");
        check(((java.lang.Enum<?>) order[1]).name().equals("JDK_OVERRIDE_PARSER"), "ordinal 1 JDK_OVERRIDE_PARSER");
        Object extF = order[0];
        Object ovrF = order[1];

        // ---- public accessors, exact values (verified against JDK-8u372 rt.jar) ----
        java.lang.reflect.Method apiProperty = c.getMethod("apiProperty");
        check("http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions".equals(apiProperty.invoke(extF)), "ext apiProperty exact");
        check("jdk.xml.overrideDefaultParser".equals(apiProperty.invoke(ovrF)), "override-parser apiProperty exact");
        check(((Boolean) c.getMethod("defaultValue").invoke(extF)).booleanValue(), "ext defaultValue true");
        check(!((Boolean) c.getMethod("defaultValue").invoke(ovrF)).booleanValue(), "override-parser defaultValue false");
        check(!((Boolean) c.getMethod("enforcedValue").invoke(extF)).booleanValue(), "ext enforcedValue false (disabled under FSP)");
        check(!((Boolean) c.getMethod("enforcedValue").invoke(ovrF)).booleanValue(), "override-parser enforcedValue false");

        // ---- package-private accessors, exact values ----
        java.lang.reflect.Method systemProperty = pkgMethod(c, "systemProperty");
        check("jdk.xml.enableExtensionFunctions".equals(systemProperty.invoke(extF)), "ext systemProperty (spec name) exact");
        check("jdk.xml.overrideDefaultParser".equals(systemProperty.invoke(ovrF)), "override-parser systemProperty exact");
        java.lang.reflect.Method systemPropertyOld = pkgMethod(c, "systemPropertyOld");
        check("javax.xml.enableExtensionFunctions".equals(systemPropertyOld.invoke(extF)), "ext legacy systemProperty exact");
        check("http://www.oracle.com/feature/use-service-mechanism".equals(systemPropertyOld.invoke(ovrF)), "override-parser legacy systemProperty exact");
        java.lang.reflect.Method hasSystemProperty = pkgMethod(c, "hasSystemProperty");
        check(((Boolean) hasSystemProperty.invoke(extF)).booleanValue(), "ext hasSystemProperty true");
        check(((Boolean) hasSystemProperty.invoke(ovrF)).booleanValue(), "override-parser hasSystemProperty true");
        java.lang.reflect.Method enforced = pkgMethod(c, "enforced");
        check(((Boolean) enforced.invoke(extF)).booleanValue(), "ext is FSP-enforced");
        check(!((Boolean) enforced.invoke(ovrF)).booleanValue(), "override-parser not FSP-enforced");

        // ---- equalsPropertyName: new api name, legacy api name, null, unknown ----
        java.lang.reflect.Method equalsPN = c.getDeclaredMethod("equalsPropertyName", String.class);
        equalsPN.setAccessible(true);
        check(((Boolean) equalsPN.invoke(extF, "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions")).booleanValue(), "ext matches its api name");
        check(!((Boolean) equalsPN.invoke(extF, "javax.xml.enableExtensionFunctions")).booleanValue(), "ext does not match legacy system-property name");
        check(!((Boolean) equalsPN.invoke(extF, (Object) null)).booleanValue(), "ext null name false");
        check(!((Boolean) equalsPN.invoke(extF, "bogus")).booleanValue(), "ext unknown name false");
        check(((Boolean) equalsPN.invoke(ovrF, "jdk.xml.overrideDefaultParser")).booleanValue(), "override-parser matches current name");
        check(((Boolean) equalsPN.invoke(ovrF, "http://www.oracle.com/feature/use-service-mechanism")).booleanValue(), "override-parser matches legacy name");
        check(!((Boolean) equalsPN.invoke(ovrF, (Object) null)).booleanValue(), "override-parser null name false");

        // ---- valueOf round-trip; unknown rejected ----
        java.lang.reflect.Method valueOf = c.getMethod("valueOf", String.class);
        check(valueOf.invoke(null, "ENABLE_EXTENSION_FUNCTION") == extF, "valueOf round-trip ext");
        check(valueOf.invoke(null, "JDK_OVERRIDE_PARSER") == ovrF, "valueOf round-trip override-parser");
        try {
            valueOf.invoke(null, "NO_SUCH_FEATURE");
            check(false, "valueOf unknown must throw");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException, "valueOf unknown -> IllegalArgumentException");
        }
        for (Object o : order) check(((java.lang.Enum<?>) o).name().equals(((java.lang.Enum<?>) o).toString()), "toString " + ((java.lang.Enum<?>) o).name());

        // ---- linkage: JdkXmlFeatures honors these feature descriptors ----
        Class<?> jfC = Class.forName("jdk.xml.internal.JdkXmlFeatures");
        java.lang.reflect.Method getFeature = jfC.getMethod("getFeature", c);
        Object jfF = jfC.getConstructor(boolean.class).newInstance(false);
        check(((Boolean) getFeature.invoke(jfF, extF)).booleanValue() == ((Boolean) c.getMethod("defaultValue").invoke(extF)).booleanValue(), "manager default == feature defaultValue (ext)");
        check(((Boolean) getFeature.invoke(jfF, ovrF)).booleanValue() == ((Boolean) c.getMethod("defaultValue").invoke(ovrF)).booleanValue(), "manager default == feature defaultValue (override-parser)");
        Object jfT = jfC.getConstructor(boolean.class).newInstance(true);
        check(((Boolean) getFeature.invoke(jfT, extF)).booleanValue() == ((Boolean) c.getMethod("enforcedValue").invoke(extF)).booleanValue(), "manager fsp == feature enforcedValue (ext)");
        check(((Boolean) getFeature.invoke(jfT, ovrF)).booleanValue() == ((Boolean) c.getMethod("defaultValue").invoke(ovrF)).booleanValue(), "non-enforced feature keeps default under fsp");
        java.lang.reflect.Method getIndex = jfC.getMethod("getIndex", String.class);
        check(((Integer) getIndex.invoke(jfF, (String) apiProperty.invoke(extF))).intValue() == 0, "manager index of ext api name");
        check(((Integer) getIndex.invoke(jfF, (String) apiProperty.invoke(ovrF))).intValue() == 1, "manager index of override-parser api name");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.lang.reflect.Method pkgMethod(Class<?> c, String name) throws Exception {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterTypes().length == 0) {
                m.setAccessible(true);
                return m;
            }
        }
        throw new NoSuchMethodException(name);
    }
}

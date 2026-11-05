/*
 * @test
 * @summary jdk.xml.internal.JdkXmlFeatures conformance: JDK XML features (internal).
 * @run main JdkXmlFeaturesTest
 */


public class JdkXmlFeaturesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    static Object en(Class<?> c, String name) {
        return java.lang.Enum.valueOf((Class) c, name);
    }

    static void restore(String name, String prev) {
        if (prev == null) System.clearProperty(name);
        else System.setProperty(name, prev);
    }

    public static void main(String[] args) throws Exception {
        jdk.xml.internal.JdkXmlFeatures f = new jdk.xml.internal.JdkXmlFeatures(false);
        check(f != null, "constructed");
        f.update();
        check(jdk.xml.internal.JdkXmlFeatures.ORACLE_JAXP_PROPERTY_PREFIX != null, "prefix non-null");
        check(jdk.xml.internal.JdkXmlFeatures.XML_FEATURE_MANAGER != null, "feature manager non-null");

        // ---- constants: exact values (reflection; class is internal) ----
        Class<?> jfC = Class.forName("jdk.xml.internal.JdkXmlFeatures");
        Class<?> xfC = Class.forName("jdk.xml.internal.JdkXmlFeatures$XmlFeature");
        Class<?> stC = Class.forName("jdk.xml.internal.JdkXmlFeatures$State");
        check("http://www.oracle.com/xml/jaxp/properties/".equals(jfC.getField("ORACLE_JAXP_PROPERTY_PREFIX").get(null)), "ORACLE_JAXP_PROPERTY_PREFIX exact");
        check("http://www.oracle.com/xml/jaxp/properties/XmlFeatureManager".equals(jfC.getField("XML_FEATURE_MANAGER").get(null)), "XML_FEATURE_MANAGER exact");
        check("http://www.oracle.com/feature/use-service-mechanism".equals(jfC.getField("ORACLE_FEATURE_SERVICE_MECHANISM").get(null)), "ORACLE_FEATURE_SERVICE_MECHANISM exact");
        check("http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions".equals(jfC.getField("ORACLE_ENABLE_EXTENSION_FUNCTION").get(null)), "ORACLE_ENABLE_EXTENSION_FUNCTION exact");
        check("javax.xml.enableExtensionFunctions".equals(jfC.getField("SP_ENABLE_EXTENSION_FUNCTION").get(null)), "SP_ENABLE_EXTENSION_FUNCTION (legacy) exact");
        check("jdk.xml.enableExtensionFunctions".equals(jfC.getField("SP_ENABLE_EXTENSION_FUNCTION_SPEC").get(null)), "SP_ENABLE_EXTENSION_FUNCTION_SPEC exact");

        // ---- FEATURE_SECURE_PROCESSING semantics per constructor ----
        Object extF = en(xfC, "ENABLE_EXTENSION_FUNCTION");
        Object ovrF = en(xfC, "JDK_OVERRIDE_PARSER");
        java.lang.reflect.Method getFeature = jfC.getMethod("getFeature", xfC);
        Object fFalse = jfC.getConstructor(boolean.class).newInstance(false);
        Object fTrue = jfC.getConstructor(boolean.class).newInstance(true);
        check(((Boolean) getFeature.invoke(fFalse, extF)).booleanValue(), "ext func default true when fsp=false");
        check(!((Boolean) getFeature.invoke(fTrue, extF)).booleanValue(), "ext func enforced false when fsp=true");
        check(!((Boolean) getFeature.invoke(fFalse, ovrF)).booleanValue(), "override parser default false");
        check(!((Boolean) getFeature.invoke(fTrue, ovrF)).booleanValue(), "override parser not fsp-enforced, stays default false");
        check(((Boolean) jfC.getMethod("getFeature", int.class).invoke(fFalse, 0)).booleanValue(), "getFeature(0) true");
        check(!((Boolean) jfC.getMethod("getFeature", int.class).invoke(fFalse, 1)).booleanValue(), "getFeature(1) false");
        check(!((Boolean) jfC.getMethod("getFeature", int.class).invoke(fTrue, 0)).booleanValue(), "getFeature(0) false when fsp=true");

        // ---- secureProcessing package-private field reflects constructor arg ----
        java.lang.reflect.Field spf = jfC.getDeclaredField("secureProcessing");
        spf.setAccessible(true);
        check(!((Boolean) spf.get(fFalse)).booleanValue(), "secureProcessing field false");
        check(((Boolean) spf.get(fTrue)).booleanValue(), "secureProcessing field true");

        // ---- setFeature(XmlFeature, State, boolean) readback and precedence ----
        Object apiSt = en(stC, "APIPROPERTY");
        Object fspSt = en(stC, "FSP");
        java.lang.reflect.Method setFeatureB = jfC.getMethod("setFeature", xfC, stC, boolean.class);
        setFeatureB.invoke(fTrue, extF, apiSt, true);
        check(((Boolean) getFeature.invoke(fTrue, extF)).booleanValue(), "APIPROPERTY overrides fsp-enforced value");
        setFeatureB.invoke(fTrue, extF, fspSt, false);
        check(((Boolean) getFeature.invoke(fTrue, extF)).booleanValue(), "lower-precedence FSP write ignored");
        setFeatureB.invoke(fFalse, extF, fspSt, false);
        check(!((Boolean) getFeature.invoke(fFalse, extF)).booleanValue(), "FSP overrides DEFAULT");

        // ---- setFeature(String, State, Object): known/unknown property names ----
        java.lang.reflect.Method setFeatureS = jfC.getMethod("setFeature", String.class, stC, Object.class);
        check(((Boolean) setFeatureS.invoke(fFalse, "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions", apiSt, Boolean.FALSE)).booleanValue(), "setFeature by api name returns true");
        check(!((Boolean) getFeature.invoke(fFalse, extF)).booleanValue(), "readback after Boolean.FALSE via api name");
        check(!((Boolean) setFeatureS.invoke(fFalse, "bogus.jck.property", apiSt, Boolean.TRUE)).booleanValue(), "setFeature unknown name returns false");

        // ---- setFeature(int, State, Object): String values parsed with Boolean.parseBoolean ----
        java.lang.reflect.Method setFeatureI = jfC.getMethod("setFeature", int.class, stC, Object.class);
        setFeatureI.invoke(fFalse, 0, apiSt, "true");
        check(((Boolean) jfC.getMethod("getFeature", int.class).invoke(fFalse, 0)).booleanValue(), "string 'true' parsed");
        setFeatureI.invoke(fFalse, 0, apiSt, "false");
        check(!((Boolean) jfC.getMethod("getFeature", int.class).invoke(fFalse, 0)).booleanValue(), "string 'false' parsed");
        setFeatureI.invoke(fFalse, 0, apiSt, Boolean.TRUE);
        check(((Boolean) jfC.getMethod("getFeature", int.class).invoke(fFalse, 0)).booleanValue(), "Boolean.TRUE accepted");

        // ---- getIndex ----
        java.lang.reflect.Method getIndex = jfC.getMethod("getIndex", String.class);
        check(((Integer) getIndex.invoke(fFalse, "http://www.oracle.com/xml/jaxp/properties/enableExtensionFunctions")).intValue() == 0, "getIndex enableExtensionFunctions");
        check(((Integer) getIndex.invoke(fFalse, "jdk.xml.overrideDefaultParser")).intValue() == 1, "getIndex overrideDefaultParser");
        check(((Integer) getIndex.invoke(fFalse, "http://www.oracle.com/feature/use-service-mechanism")).intValue() == 1, "getIndex legacy service-mechanism name");
        check(((Integer) getIndex.invoke(fFalse, "jdk.xml.enableExtensionFunctions")).intValue() == -1, "system property name not an api property");
        check(((Integer) getIndex.invoke(fFalse, "bogus")).intValue() == -1, "getIndex unknown -1");
        check(((Integer) getIndex.invoke(fFalse, (Object) null)).intValue() == -1, "getIndex null -1");

        // ---- update() re-reads system properties without throwing ----
        jfC.getMethod("update").invoke(fFalse);
        check(true, "update() no throw");

        // ---- system property linkage: new name, legacy name, FSP precedence ----
        String prevNew = System.setProperty("jdk.xml.enableExtensionFunctions", "false");
        try {
            Object fSys = jfC.getConstructor(boolean.class).newInstance(false);
            check(!((Boolean) getFeature.invoke(fSys, extF)).booleanValue(), "sysprop jdk.xml.enableExtensionFunctions=false observed");
        } finally {
            restore("jdk.xml.enableExtensionFunctions", prevNew);
        }
        String prevOld = System.setProperty("javax.xml.enableExtensionFunctions", "false");
        try {
            Object fSys = jfC.getConstructor(boolean.class).newInstance(false);
            check(!((Boolean) getFeature.invoke(fSys, extF)).booleanValue(), "legacy sysprop javax.xml.enableExtensionFunctions=false observed");
        } finally {
            restore("javax.xml.enableExtensionFunctions", prevOld);
        }
        String prevOn = System.setProperty("jdk.xml.enableExtensionFunctions", "true");
        try {
            Object fT = jfC.getConstructor(boolean.class).newInstance(true);
            check(((Boolean) getFeature.invoke(fT, extF)).booleanValue(), "SYSTEMPROPERTY=true overrides fsp-enforced false");
        } finally {
            restore("jdk.xml.enableExtensionFunctions", prevOn);
        }
        String prevYes = System.setProperty("jdk.xml.enableExtensionFunctions", "yes");
        try {
            Object fY = jfC.getConstructor(boolean.class).newInstance(false);
            check(!((Boolean) getFeature.invoke(fY, extF)).booleanValue(), "non-boolean value parsed via Boolean.parseBoolean -> false");
        } finally {
            restore("jdk.xml.enableExtensionFunctions", prevYes);
        }
        String prevOvr = System.setProperty("jdk.xml.overrideDefaultParser", "true");
        try {
            Object fO = jfC.getConstructor(boolean.class).newInstance(false);
            check(((Boolean) getFeature.invoke(fO, ovrF)).booleanValue(), "sysprop jdk.xml.overrideDefaultParser=true observed");
        } finally {
            restore("jdk.xml.overrideDefaultParser", prevOvr);
        }
        Object fR = jfC.getConstructor(boolean.class).newInstance(false);
        check(!((Boolean) getFeature.invoke(fR, ovrF)).booleanValue(), "override parser default restored after clearProperty");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

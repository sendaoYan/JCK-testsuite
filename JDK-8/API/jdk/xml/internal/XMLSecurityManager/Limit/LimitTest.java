/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.Limit conformance: enum constants and methods.
 * @run main LimitTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class LimitTest {
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 12, "12 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("ENTITY_EXPANSION_LIMIT"), "constant ENTITY_EXPANSION_LIMIT");
        check(cn.contains("MAX_OCCUR_NODE_LIMIT"), "constant MAX_OCCUR_NODE_LIMIT");
        check(cn.contains("ELEMENT_ATTRIBUTE_LIMIT"), "constant ELEMENT_ATTRIBUTE_LIMIT");
        check(cn.contains("TOTAL_ENTITY_SIZE_LIMIT"), "constant TOTAL_ENTITY_SIZE_LIMIT");
        check(cn.contains("GENERAL_ENTITY_SIZE_LIMIT"), "constant GENERAL_ENTITY_SIZE_LIMIT");
        check(cn.contains("PARAMETER_ENTITY_SIZE_LIMIT"), "constant PARAMETER_ENTITY_SIZE_LIMIT");
        check(cn.contains("MAX_ELEMENT_DEPTH_LIMIT"), "constant MAX_ELEMENT_DEPTH_LIMIT");
        check(cn.contains("MAX_NAME_LIMIT"), "constant MAX_NAME_LIMIT");
        check(cn.contains("ENTITY_REPLACEMENT_LIMIT"), "constant ENTITY_REPLACEMENT_LIMIT");
        check(cn.contains("XPATH_GROUP_LIMIT"), "constant XPATH_GROUP_LIMIT");
        check(cn.contains("XPATH_OP_LIMIT"), "constant XPATH_OP_LIMIT");
        check(cn.contains("XPATH_TOTALOP_LIMIT"), "constant XPATH_TOTALOP_LIMIT");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        check(names.contains("equalsAPIPropertyName"), "has method equalsAPIPropertyName");
        check(names.contains("equalsSystemPropertyName"), "has method equalsSystemPropertyName");
        check(names.contains("key"), "has method key");
        check(names.contains("apiProperty"), "has method apiProperty");
        check(names.contains("defaultValue"), "has method defaultValue");
        check(names.contains("isSupported"), "has method isSupported");

        // ---- declaration order: all 12 constants with their ordinals ----
        Object[] order = (Object[]) c.getMethod("values").invoke(null);
        String[] expected = {"ENTITY_EXPANSION_LIMIT", "MAX_OCCUR_NODE_LIMIT", "ELEMENT_ATTRIBUTE_LIMIT",
                "TOTAL_ENTITY_SIZE_LIMIT", "GENERAL_ENTITY_SIZE_LIMIT", "PARAMETER_ENTITY_SIZE_LIMIT",
                "MAX_ELEMENT_DEPTH_LIMIT", "MAX_NAME_LIMIT", "ENTITY_REPLACEMENT_LIMIT",
                "XPATH_GROUP_LIMIT", "XPATH_OP_LIMIT", "XPATH_TOTALOP_LIMIT"};
        for (int i = 0; i < expected.length; i++) {
            check(((java.lang.Enum<?>) order[i]).name().equals(expected[i]), "ordinal " + i + " " + expected[i]);
            check(((java.lang.Enum<?>) order[i]).ordinal() == i, expected[i] + " ordinal " + i);
        }

        // ---- key(): display name used in limit-violation reports ----
        java.lang.reflect.Method key = c.getMethod("key");
        String[] keys = {"EntityExpansionLimit", "MaxOccurLimit", "ElementAttributeLimit", "TotalEntitySizeLimit",
                "MaxEntitySizeLimit", "MaxEntitySizeLimit", "MaxElementDepthLimit", "MaxXMLNameLimit",
                "EntityReplacementLimit", "XPathGroupLimit", "XPathExprOpLimit", "XPathTotalOpLimit"};
        for (int i = 0; i < keys.length; i++) {
            check(keys[i].equals(key.invoke(order[i])), expected[i] + " key " + keys[i]);
        }

        // ---- apiProperty(): Oracle-prefixed JAXP property names ----
        java.lang.reflect.Method apiProperty = c.getMethod("apiProperty");
        String prefix = "http://www.oracle.com/xml/jaxp/properties/";
        String[] apis = {prefix + "entityExpansionLimit", prefix + "maxOccurLimit", prefix + "elementAttributeLimit",
                prefix + "totalEntitySizeLimit", prefix + "maxGeneralEntitySizeLimit", prefix + "maxParameterEntitySizeLimit",
                prefix + "maxElementDepth", prefix + "maxXMLNameLimit", prefix + "entityReplacementLimit",
                "jdk.xml.xpathExprGrpLimit", "jdk.xml.xpathExprOpLimit", "jdk.xml.xpathTotalOpLimit"};
        for (int i = 0; i < apis.length; i++) {
            check(apis[i].equals(apiProperty.invoke(order[i])), expected[i] + " apiProperty exact");
        }

        // ---- systemProperty(): package-private, jdk.xml.* names (first 9), unprefixed (last 3) ----
        java.lang.reflect.Method systemProperty = c.getDeclaredMethod("systemProperty");
        systemProperty.setAccessible(true);
        String[] sps = {"jdk.xml.entityExpansionLimit", "jdk.xml.maxOccurLimit", "jdk.xml.elementAttributeLimit",
                "jdk.xml.totalEntitySizeLimit", "jdk.xml.maxGeneralEntitySizeLimit", "jdk.xml.maxParameterEntitySizeLimit",
                "jdk.xml.maxElementDepth", "jdk.xml.maxXMLNameLimit", "jdk.xml.entityReplacementLimit",
                "jdk.xml.xpathExprGrpLimit", "jdk.xml.xpathExprOpLimit", "jdk.xml.xpathTotalOpLimit"};
        for (int i = 0; i < sps.length; i++) {
            check(sps[i].equals(systemProperty.invoke(order[i])), expected[i] + " systemProperty " + sps[i]);
        }

        // ---- defaultValue() and secureValue(): full matrices ----
        java.lang.reflect.Method defaultValue = c.getMethod("defaultValue");
        int[] defs = {0, 0, 0, 0, 0, 0, 0, 1000, 0, 10, 100, 10000};
        for (int i = 0; i < defs.length; i++) {
            check(((Integer) defaultValue.invoke(order[i])).intValue() == defs[i], expected[i] + " defaultValue " + defs[i]);
        }
        java.lang.reflect.Method secureValue = c.getDeclaredMethod("secureValue");
        secureValue.setAccessible(true);
        int[] secs = {64000, 5000, 10000, 50000000, 0, 1000000, 0, 1000, 3000000, 10, 100, 10000};
        for (int i = 0; i < secs.length; i++) {
            check(((Integer) secureValue.invoke(order[i])).intValue() == secs[i], expected[i] + " secureValue " + secs[i]);
        }
        // limits whose secure value tightens the default
        check(secs[0] > defs[0] && secs[1] > defs[1] && secs[2] > defs[2] && secs[3] > defs[3]
                && secs[5] > defs[5] && secs[8] > defs[8], "FSP tightens parser limits");
        check(secs[4] == 0 && secs[6] == 0, "general entity size and element depth have no secure override");

        // ---- isSupported(Processor): PARSER limits vs XPATH limits ----
        Class<?> procC = Class.forName("jdk.xml.internal.XMLSecurityManager$Processor");
        Object parser = procC.getMethod("valueOf", String.class).invoke(null, "PARSER");
        Object xpath = procC.getMethod("valueOf", String.class).invoke(null, "XPATH");
        java.lang.reflect.Method isSupported = c.getMethod("isSupported", procC);
        for (int i = 0; i < 9; i++) {
            check(((Boolean) isSupported.invoke(order[i], parser)).booleanValue(), expected[i] + " supported by PARSER");
            check(!((Boolean) isSupported.invoke(order[i], xpath)).booleanValue(), expected[i] + " not supported by XPATH");
        }
        for (int i = 9; i < 12; i++) {
            check(((Boolean) isSupported.invoke(order[i], xpath)).booleanValue(), expected[i] + " supported by XPATH");
            check(!((Boolean) isSupported.invoke(order[i], parser)).booleanValue(), expected[i] + " not supported by PARSER");
        }

        // ---- equalsAPIPropertyName / equalsSystemPropertyName ----
        java.lang.reflect.Method eqApi = c.getMethod("equalsAPIPropertyName", String.class);
        java.lang.reflect.Method eqSys = c.getMethod("equalsSystemPropertyName", String.class);
        for (int i = 0; i < 12; i++) {
            check(((Boolean) eqApi.invoke(order[i], apis[i])).booleanValue(), expected[i] + " matches own api name");
            check(((Boolean) eqSys.invoke(order[i], sps[i])).booleanValue(), expected[i] + " matches own system name");
            check(!((Boolean) eqApi.invoke(order[i], (Object) null)).booleanValue(), expected[i] + " null api name false");
            check(!((Boolean) eqSys.invoke(order[i], (Object) null)).booleanValue(), expected[i] + " null system name false");
            check(!((Boolean) eqApi.invoke(order[i], "jck-bogus")).booleanValue(), expected[i] + " bogus api name false");
            check(!((Boolean) eqSys.invoke(order[i], sps[(i + 1) % 12])).booleanValue(), expected[i] + " sibling system name false");
        }

        // ---- valueOf round-trip; unknown rejected ----
        java.lang.reflect.Method valueOf = c.getMethod("valueOf", String.class);
        for (Object o : order) {
            String n = ((java.lang.Enum<?>) o).name();
            check(valueOf.invoke(null, n) == o, "valueOf round-trip " + n);
            check(n.equals(((java.lang.Enum<?>) o).toString()), "toString " + n);
        }
        try {
            valueOf.invoke(null, "NO_SUCH_LIMIT");
            check(false, "valueOf unknown must throw");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException, "valueOf unknown -> IllegalArgumentException");
        }

        // ---- linkage: a live XMLSecurityManager serves defaultValue()/secureValue() of every limit ----
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager");
        java.lang.reflect.Method getLimit = smC.getMethod("getLimit", c);
        Object smF = smC.getConstructor(boolean.class).newInstance(false);
        Object smT = smC.getConstructor(boolean.class).newInstance(true);
        for (int i = 0; i < 12; i++) {
            check(((Integer) getLimit.invoke(smF, order[i])).intValue() == defs[i], "manager(fsp=false) serves " + expected[i]);
            check(((Integer) getLimit.invoke(smT, order[i])).intValue() == secs[i], "manager(fsp=true) serves " + expected[i]);
        }

        // ==== REAL PARSE LINKAGE: XPATH_OP_LIMIT default 100 rejects long expressions; sysprop relaxes it ====
        StringBuilder big = new StringBuilder("1");
        for (int i = 0; i < 300; i++) big.append("+1");
        try {
            javax.xml.xpath.XPathFactory.newInstance().newXPath().compile(big.toString());
            check(false, "301-operator expression must exceed the default op limit");
        } catch (javax.xml.xpath.XPathExpressionException xe) {
            check(xe.getMessage() != null && xe.getMessage().contains("operators"), "op-limit failure message");
            check(xe.getMessage().contains("'100'"), "message cites the 100-op limit");
        }
        String prevOp = System.setProperty("jdk.xml.xpathExprOpLimit", "500");
        try {
            javax.xml.xpath.XPathFactory.newInstance().newXPath().compile(big.toString());
            check(true, "sysprop jdk.xml.xpathExprOpLimit=500 admits 301 operators");
        } finally {
            restore("jdk.xml.xpathExprOpLimit", prevOp);
        }
        try {
            javax.xml.xpath.XPathFactory.newInstance().newXPath().compile(big.toString());
            check(false, "limit re-applied after property restore");
        } catch (javax.xml.xpath.XPathExpressionException xe) {
            check(true, "rejected again after restore");
        }
        // small expression always fine
        javax.xml.xpath.XPathExpression ok = javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("1+2+3");
        check(ok != null, "small expression compiles under default limits");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void restore(String name, String prev) {
        if (prev == null) System.clearProperty(name);
        else System.setProperty(name, prev);
    }
}

/*
 * @test
 * @summary com.sun.xml.internal.ws.api.addressing.AddressingVersion conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AddressingVersionTest
 */

import java.lang.reflect.Modifier;


public class AddressingVersionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.addressing.AddressingVersion", false, AddressingVersionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.addressing.AddressingVersion"), "name");
        check(c.isEnum(), "enum");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("fromBinding"), "method fromBinding");
        check(mn.contains("fromFeature"), "method fromFeature");
        check(mn.contains("fromNsUri"), "method fromNsUri");
        check(mn.contains("fromPort"), "method fromPort");
        check(mn.contains("fromSpecClass"), "method fromSpecClass");
        check(mn.contains("getAnonymousUri"), "method getAnonymousUri");
        check(mn.contains("getDefaultFaultAction"), "method getDefaultFaultAction");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getFeatureClass"), "method getFeatureClass");
        check(mn.contains("getInvalidMapText"), "method getInvalidMapText");
        check(mn.contains("getMapRequiredText"), "method getMapRequiredText");
        check(mn.contains("getNoneUri"), "method getNoneUri");
        check(mn.contains("getNsUri"), "method getNsUri");
        check(mn.contains("getPrefix"), "method getPrefix");
        check(mn.contains("getWsaHelper"), "method getWsaHelper");
        check(mn.contains("getWsdlPrefix"), "method getWsdlPrefix");
        check(mn.contains("isEnabled"), "method isEnabled");
        check(mn.contains("isReferenceParameter"), "method isReferenceParameter");
        check(mn.contains("isRequired"), "method isRequired");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("values"), "method values");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("MEMBER"), "field MEMBER");
        check(fn.contains("UNSET_INPUT_ACTION"), "field UNSET_INPUT_ACTION");
        check(fn.contains("UNSET_OUTPUT_ACTION"), "field UNSET_OUTPUT_ACTION");
        check(fn.contains("W3C"), "field W3C");
        check(fn.contains("actionMismatchTag"), "field actionMismatchTag");
        check(fn.contains("actionNotSupportedTag"), "field actionNotSupportedTag");
        check(fn.contains("actionNotSupportedText"), "field actionNotSupportedText");
        check(fn.contains("actionTag"), "field actionTag");
        check(fn.contains("anonymousEpr"), "field anonymousEpr");
        check(fn.contains("anonymousUri"), "field anonymousUri");
        check(fn.contains("eprType"), "field eprType");
        check(fn.contains("faultDetailTag"), "field faultDetailTag");
        check(fn.contains("faultToTag"), "field faultToTag");
        check(fn.contains("fault_duplicateAddressInEpr"), "field fault_duplicateAddressInEpr");
        check(fn.contains("fault_missingAddressInEpr"), "field fault_missingAddressInEpr");
        check(fn.contains("fromTag"), "field fromTag");
        check(fn.contains("invalidAddressTag"), "field invalidAddressTag");
        check(fn.contains("invalidCardinalityTag"), "field invalidCardinalityTag");
        check(fn.contains("invalidMapTag"), "field invalidMapTag");
        check(fn.contains("isReferenceParameterTag"), "field isReferenceParameterTag");
        check(fn.contains("mapRequiredTag"), "field mapRequiredTag");
        check(fn.contains("messageIDTag"), "field messageIDTag");
        check(fn.contains("noneUri"), "field noneUri");
        check(fn.contains("nsUri"), "field nsUri");
        check(fn.contains("policyNsUri"), "field policyNsUri");
        check(fn.contains("problemActionTag"), "field problemActionTag");
        check(fn.contains("problemHeaderQNameTag"), "field problemHeaderQNameTag");
        check(fn.contains("relatesToTag"), "field relatesToTag");
        check(fn.contains("replyToTag"), "field replyToTag");
        check(fn.contains("toTag"), "field toTag");
        check(fn.contains("wsdlActionTag"), "field wsdlActionTag");
        check(fn.contains("wsdlAnonymousTag"), "field wsdlAnonymousTag");
        check(fn.contains("wsdlExtensionTag"), "field wsdlExtensionTag");
        check(fn.contains("wsdlNsUri"), "field wsdlNsUri");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary org.w3c.dom.TypeInfo conformance: interface and methods.
 * @run main TypeInfoTest
 */

import org.w3c.dom.TypeInfo;

public class TypeInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(TypeInfo.class.isInterface(), "TypeInfo is an interface");
        check(TypeInfo.class.getMethod("getTypeName") != null, "getTypeName()");
        check(TypeInfo.class.getMethod("getTypeNamespace") != null, "getTypeNamespace()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.TypeInfo.DERIVATION_RESTRICTION == 1, "DERIVATION_RESTRICTION == 1");
        check(org.w3c.dom.TypeInfo.DERIVATION_EXTENSION == 2, "DERIVATION_EXTENSION == 2");
        check(org.w3c.dom.TypeInfo.DERIVATION_UNION == 4, "DERIVATION_UNION == 4");
        check(org.w3c.dom.TypeInfo.DERIVATION_LIST == 8, "DERIVATION_LIST == 8");
        org.w3c.dom.TypeInfo a = new org.w3c.dom.TypeInfo(){
            public java.lang.String getTypeName() { return "s"; }
            public java.lang.String getTypeNamespace() { return "s"; }
            public boolean isDerivedFrom(java.lang.String p0, java.lang.String p1, int p2) { return true; }
        };
        check(a != null, "anon impl of TypeInfo");
        check("s".equals(a.getTypeName()), "getTypeName() returns fixed default");
        check("s".equals(a.getTypeNamespace()), "getTypeNamespace() returns fixed default");
        check(a.isDerivedFrom("s", "s", 3) == true, "isDerivedFrom() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

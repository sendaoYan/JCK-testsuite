/*
 * @test
 * @summary javax.xml.ws.soap.AddressingFeature.Responses conformance: exactly three enum constants (ANONYMOUS/NON_ANONYMOUS/ALL), order, valueOf precision and AddressingFeature integration.
 * @run main ResponsesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.AddressingFeature.Responses;

public class ResponsesTest {
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
        Class<?> c = Class.forName("javax.xml.ws.soap.AddressingFeature$Responses");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("ANONYMOUS"), "constant ANONYMOUS");
        check(cn.contains("NON_ANONYMOUS"), "constant NON_ANONYMOUS");
        check(cn.contains("ALL"), "constant ALL");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        testConstantsAndOrder();
        testEnumMechanics(c);
        testFeatureIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 常量与顺序：ANONYMOUS, NON_ANONYMOUS, ALL（恰 3 个，无第四个）
    static void testConstantsAndOrder() {
        Responses[] vs = Responses.values();
        check(vs.length == 3, "values() length exactly 3: " + vs.length);
        check(vs[0] == Responses.ANONYMOUS, "values()[0] ANONYMOUS");
        check(vs[1] == Responses.NON_ANONYMOUS, "values()[1] NON_ANONYMOUS");
        check(vs[2] == Responses.ALL, "values()[2] ALL");
        check(Responses.valueOf("ANONYMOUS") == Responses.ANONYMOUS, "valueOf(ANONYMOUS)");
        check(Responses.valueOf("NON_ANONYMOUS") == Responses.NON_ANONYMOUS, "valueOf(NON_ANONYMOUS)");
        check(Responses.valueOf("ALL") == Responses.ALL, "valueOf(ALL)");
        check("ANONYMOUS".equals(Responses.ANONYMOUS.name()), "ANONYMOUS name");
        check("NON_ANONYMOUS".equals(Responses.NON_ANONYMOUS.name()), "NON_ANONYMOUS name");
        check("ALL".equals(Responses.ALL.name()), "ALL name");
        check(Responses.ANONYMOUS.toString().equals("ANONYMOUS"), "ANONYMOUS toString");
        check(Responses.ANONYMOUS.ordinal() == 0, "ANONYMOUS ordinal 0");
        check(Responses.NON_ANONYMOUS.ordinal() == 1, "NON_ANONYMOUS ordinal 1");
        check(Responses.ALL.ordinal() == 2, "ALL ordinal 2");
        // 常见误名不存在
        try {
            Responses.valueOf("NEVER");
            check(false, "NEVER must not exist (pre-2.2 name)");
        } catch (IllegalArgumentException e) {
            check(true, "NEVER not a constant (IllegalArgumentException)");
        }
        try {
            Responses.valueOf("");
            check(false, "empty name must throw");
        } catch (IllegalArgumentException e) {
            check(true, "empty name -> IllegalArgumentException");
        }
        try {
            Responses.valueOf(null);
            check(false, "null name must throw");
        } catch (NullPointerException e) {
            check(true, "null name -> NullPointerException");
        }
        // 大小写敏感
        try {
            Responses.valueOf("anonymous");
            check(false, "lowercase must throw");
        } catch (IllegalArgumentException e) {
            check(true, "valueOf case-sensitive");
        }
    }

    // 枚举机制
    static void testEnumMechanics(Class<?> c) throws Exception {
        check(Modifier.isFinal(c.getModifiers()), "final enum (no constant bodies)");
        check(c == Responses.ALL.getDeclaringClass(), "getDeclaringClass");
        check(Responses.ANONYMOUS.compareTo(Responses.NON_ANONYMOUS) < 0, "ANONYMOUS < NON_ANONYMOUS");
        check(Responses.NON_ANONYMOUS.compareTo(Responses.ALL) < 0, "NON_ANONYMOUS < ALL");
        check(Responses.ALL.compareTo(Responses.ANONYMOUS) > 0, "ALL > ANONYMOUS");
        check(Responses.NON_ANONYMOUS.compareTo(Responses.NON_ANONYMOUS) == 0, "compareTo self 0");
        check(Responses.values() != Responses.values(), "values() fresh array each call");
        check(Enum.valueOf(Responses.class, "ALL") == Responses.ALL, "Enum.valueOf route");
        // 声明成员恰 3 常量（$VALUES 为合成排除）
        int realFields = 0;
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (!f.isSynthetic()) realFields++;
        }
        check(realFields == 3, "exactly 3 declared fields: " + realFields);
        // 嵌套于 AddressingFeature
        check(c.getDeclaringClass() == AddressingFeature.class, "nested in AddressingFeature");
        check(c.getName().equals("javax.xml.ws.soap.AddressingFeature$Responses"),
              "binary name: " + c.getName());
    }

    // 与 AddressingFeature 的配合：默认 ALL、三值可往返
    static void testFeatureIntegration() {
        check(new AddressingFeature().getResponses() == Responses.ALL,
              "feature default responses is ALL");
        for (Responses r : Responses.values()) {
            AddressingFeature f = new AddressingFeature(true, true, r);
            check(f.getResponses() == r, "feature ctor round-trip " + r);
        }
        // @Addressing 注解默认与枚举默认一致
        javax.xml.ws.soap.Addressing a = Sample.class.getAnnotation(javax.xml.ws.soap.Addressing.class);
        check(a.responses() == Responses.ALL, "annotation default responses ALL");
        check(a.responses() == new AddressingFeature().getResponses(),
              "annotation and feature defaults agree");
    }

    @javax.xml.ws.soap.Addressing
    static class Sample { }
}

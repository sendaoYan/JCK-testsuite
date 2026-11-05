/*
 * @test
 * @summary javax.xml.datatype.DatatypeConstants.Field conformance: datatype field.
 * @run main FieldTest
 */


public class FieldTest {
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
        check(javax.xml.datatype.DatatypeConstants.YEARS.getId() == 0, "YEARS id");
        check("YEARS".equals(javax.xml.datatype.DatatypeConstants.YEARS.toString()), "YEARS toString");
        check(javax.xml.datatype.DatatypeConstants.SECONDS.getId() == 5, "SECONDS id");
        testWhiteBox();
        testFieldFamily();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.xml.datatype.DatatypeConstants$Field");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Field is public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Field is final");
        check(c.getSuperclass() == java.lang.Object.class, "Field superclass Object");
        // public surface: getId + toString only
        java.lang.reflect.Method[] ms = c.getMethods();
        int idCount = 0, toStringCount = 0;
        for (java.lang.reflect.Method m : ms) {
            if (m.getDeclaringClass() != c) continue;
            if (m.getName().equals("getId")) idCount++;
            if (m.getName().equals("toString")) toStringCount++;
        }
        check(idCount == 1, "exactly one getId declared");
        check(toStringCount == 1, "exactly one toString declared");
        boolean ownOthers = true;
        for (java.lang.reflect.Method m : ms) {
            if (m.getDeclaringClass() == c && !m.getName().equals("getId") && !m.getName().equals("toString"))
                ownOthers = false;
        }
        check(ownOthers, "no other own public methods");
    }

    // ---- Part 2: black-box (constant family) ----
    static void testFieldFamily() {
        javax.xml.datatype.DatatypeConstants.Field[] fields = {
            javax.xml.datatype.DatatypeConstants.YEARS,
            javax.xml.datatype.DatatypeConstants.MONTHS,
            javax.xml.datatype.DatatypeConstants.DAYS,
            javax.xml.datatype.DatatypeConstants.HOURS,
            javax.xml.datatype.DatatypeConstants.MINUTES,
            javax.xml.datatype.DatatypeConstants.SECONDS,
        };
        String[] names = {"YEARS", "MONTHS", "DAYS", "HOURS", "MINUTES", "SECONDS"};
        for (int i = 0; i < fields.length; i++) {
            check(fields[i] instanceof javax.xml.datatype.DatatypeConstants.Field,
                  names[i] + " instanceof Field");
            check(fields[i].getId() == i, names[i] + " getId " + i);
            check(names[i].equals(fields[i].toString()), names[i] + " toString exact");
        }
        for (int i = 0; i < fields.length; i++)
            for (int j = i + 1; j < fields.length; j++)
                check(fields[i] != fields[j], names[i] + " identity distinct from " + names[j]);
    }
}

/*
 * @test
 * @summary JLS 7.5.2: a single-type-import declaration shadows a
 *          type-import-on-demand declaration of a type with the same simple
 *          name.
 * @run main TypeImportOnDemandShadowedBySingleType
 */

import java.util.*;
import java.sql.Date;

public class TypeImportOnDemandShadowedBySingleType {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // java.util.Date is available on demand, but java.sql.Date is imported
        // by single-type-import, so the simple name Date refers to java.sql.Date.
        Date d = new Date(0L);
        check("java.sql.Date".equals(d.getClass().getName()),
              "single-type-import shadows on-demand import");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

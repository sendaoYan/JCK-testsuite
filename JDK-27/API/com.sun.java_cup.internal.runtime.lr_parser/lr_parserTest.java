/*
 * @test
 * @summary com.sun.java_cup.internal.runtime.lr_parser conformance: internal package reflection.
 * @modules java.xml/com.sun.java_cup.internal.runtime:+open
 * @run main lr_parserTest
 */
public class lr_parserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java_cup.internal.runtime.lr_parser", false, lr_parserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.java_cup.internal.runtime.lr_parser"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 16, "field-count");
        check(fd.contains("ID_GROUP"), "field-ID_GROUP");
        check(fd.contains("ID_OPERATOR"), "field-ID_OPERATOR");
        check(fd.contains("ID_TOTAL_OPERATOR"), "field-ID_TOTAL_OPERATOR");
        check(fd.contains("grpLimit"), "field-grpLimit");
        check(fd.contains("opLimit"), "field-opLimit");
        check(fd.contains("totalOpLimit"), "field-totalOpLimit");
        check(fd.contains("_error_sync_size"), "field-_error_sync_size");
        check(fd.contains("_done_parsing"), "field-_done_parsing");
        check(fd.contains("tos"), "field-tos");
        check(fd.contains("cur_token"), "field-cur_token");
        check(fd.contains("stack"), "field-stack");
        check(fd.contains("production_tab"), "field-production_tab");
        check(fd.contains("action_tab"), "field-action_tab");
        check(fd.contains("reduce_tab"), "field-reduce_tab");
        check(fd.contains("lookahead"), "field-lookahead");
        check(fd.contains("lookahead_pos"), "field-lookahead_pos");
        check(md.size() == 40, "method-count");
        check(md.contains("EOF_sym"), "method-EOF_sym");
        check(md.contains("action_table"), "method-action_table");
        check(md.contains("advance_lookahead"), "method-advance_lookahead");
        check(md.contains("cur_err_token"), "method-cur_err_token");
        check(md.contains("debug_message"), "method-debug_message");
        check(md.contains("debug_parse"), "method-debug_parse");
        check(md.contains("debug_reduce"), "method-debug_reduce");
        check(md.contains("debug_shift"), "method-debug_shift");
        check(md.contains("debug_stack"), "method-debug_stack");
        check(md.contains("do_action"), "method-do_action");
        check(md.contains("done_parsing"), "method-done_parsing");
        check(md.contains("dump_stack"), "method-dump_stack");
        check(md.contains("error_recovery"), "method-error_recovery");
        check(md.contains("error_sym"), "method-error_sym");
        check(md.contains("error_sync_size"), "method-error_sync_size");
        check(md.contains("find_recovery_config"), "method-find_recovery_config");
        check(md.contains("getCount"), "method-getCount");
        check(md.contains("getScanner"), "method-getScanner");
        check(md.contains("get_action"), "method-get_action");
        check(md.contains("get_reduce"), "method-get_reduce");
        check(md.contains("init_actions"), "method-init_actions");
        check(md.contains("isOverLimit"), "method-isOverLimit");
        check(md.contains("parse"), "method-parse");
        check(md.contains("parse_lookahead"), "method-parse_lookahead");
        check(md.contains("production_table"), "method-production_table");
        check(md.contains("read_lookahead"), "method-read_lookahead");
        check(md.contains("reduce_table"), "method-reduce_table");
        check(md.contains("report_error"), "method-report_error");
        check(md.contains("report_fatal_error"), "method-report_fatal_error");
        check(md.contains("restart_lookahead"), "method-restart_lookahead");
        check(md.contains("scan"), "method-scan");
        check(md.contains("setScanner"), "method-setScanner");
        check(md.contains("shift_under_error"), "method-shift_under_error");
        check(md.contains("start_production"), "method-start_production");
        check(md.contains("start_state"), "method-start_state");
        check(md.contains("syntax_error"), "method-syntax_error");
        check(md.contains("try_parse_ahead"), "method-try_parse_ahead");
        check(md.contains("unpackFromStrings"), "method-unpackFromStrings");
        check(md.contains("unrecovered_syntax_error"), "method-unrecovered_syntax_error");
        check(md.contains("user_init"), "method-user_init");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.awt.print.Book conformance: page collection (headless-safe):
 *          append with and without page count, setPage replacement, per-page
 *          Printable/PageFormat storage, out-of-range and null exception matrix.
 * @run main BookTest
 */

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

public class BookTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    static final Printable NOP = new Printable() {
        public int print(java.awt.Graphics g, PageFormat pf, int pageIndex) {
            return Printable.NO_SUCH_PAGE;
        }
    };
    static final Printable ALT = new Printable() {
        public int print(java.awt.Graphics g, PageFormat pf, int pageIndex) {
            return Printable.PAGE_EXISTS;
        }
    };

    public static void main(String[] args) {
        check(Book.class.getInterfaces().length == 1
                && Book.class.getInterfaces()[0] == Pageable.class, "Book implements Pageable");

        Book b = new Book();
        check(b.getNumberOfPages() == 0, "empty book has no pages");
        check(b instanceof Pageable, "usable as Pageable");
        Pageable pb = b;

        // append without count adds exactly one page.
        b.append(NOP, new PageFormat());
        check(b.getNumberOfPages() == 1, "single append");
        check(b.getPrintable(0) == NOP, "printable stored");
        check(b.getPageFormat(0).getClass() == PageFormat.class, "page format stored");

        // append with a count spans that many page indices.
        PageFormat landscape = new PageFormat();
        landscape.setOrientation(PageFormat.LANDSCAPE);
        b.append(ALT, landscape, 3);
        check(b.getNumberOfPages() == 4, "multi-page append");
        for (int i = 1; i <= 3; i++) {
            check(b.getPrintable(i) == ALT, "printable repeated at " + i);
            check(b.getPageFormat(i).getOrientation() == PageFormat.LANDSCAPE, "format repeated at " + i);
        }
        // getPageFormat returns the stored PageFormat by reference.
        PageFormat got1 = b.getPageFormat(1);
        check(b.getPageFormat(1) == got1, "getPageFormat returns the stored instance");

        // setPage replaces the entry in place without changing the count.
        b.setPage(0, ALT, landscape);
        check(b.getNumberOfPages() == 4, "setPage keeps count");
        check(b.getPrintable(0) == ALT, "setPage printable");
        check(b.getPageFormat(0).getOrientation() == PageFormat.LANDSCAPE, "setPage format");

        // Interface-level queries.
        check(pb.getNumberOfPages() == 4, "getNumberOfPages through Pageable");
        check(pb.getPrintable(2) == ALT, "getPrintable through Pageable");
        check(pb.getPageFormat(2).getOrientation() == PageFormat.LANDSCAPE, "getPageFormat through Pageable");

        // Exception matrix.
        final Book fb = b;
        checkThrows(ArrayIndexOutOfBoundsException.class, "getPageFormat(9)",
                new ThrowingRunnable() { public void run() { fb.getPageFormat(9); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getPrintable(9)",
                new ThrowingRunnable() { public void run() { fb.getPrintable(9); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getPageFormat(-1)",
                new ThrowingRunnable() { public void run() { fb.getPageFormat(-1); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "getPrintable(-1)",
                new ThrowingRunnable() { public void run() { fb.getPrintable(-1); } });
        checkThrows(ArrayIndexOutOfBoundsException.class, "setPage(9, ...)",
                new ThrowingRunnable() { public void run() { fb.setPage(9, NOP, new PageFormat()); } });
        checkThrows(NullPointerException.class, "setPage null printable",
                new ThrowingRunnable() { public void run() { fb.setPage(0, null, new PageFormat()); } });
        checkThrows(NullPointerException.class, "setPage null format",
                new ThrowingRunnable() { public void run() { fb.setPage(0, NOP, null); } });
        checkThrows(NullPointerException.class, "append null printable",
                new ThrowingRunnable() { public void run() { fb.append(null, new PageFormat()); } });
        checkThrows(NullPointerException.class, "append null format",
                new ThrowingRunnable() { public void run() { fb.append(NOP, null); } });
        // Zero-page append is accepted and changes nothing.
        int before = b.getNumberOfPages();
        b.append(NOP, new PageFormat(), 0);
        check(b.getNumberOfPages() == before, "append with 0 pages leaves count unchanged");
        // Negative counts must not throw (treated as page-count arithmetic by the
        // implementation; callers must pass sane values).
        boolean threw = false;
        try { b.append(NOP, new PageFormat(), -1); } catch (Throwable t) { threw = true; }
        check(!threw, "append with negative count does not throw");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

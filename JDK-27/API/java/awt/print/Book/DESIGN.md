# java.awt.print.Book 测试设计

## 1. API 功能
Pageable 默认实现：append(Printable, PageFormat) / append(Printable, PageFormat, int pages)、
setPage/getNumberOfPages/getPrintable/getPageFormat（按索引路由到页）。

## 2. Java 用户如何使用
`Book book = new Book(); book.append(p1, pf, 3); book.append(p2, pfLandscape);
PrinterJob.setPageable(book);` 多节文档（前言竖版+正文横版）一次打印。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/Book.java`；
PrinterJob.setPageable 调用点。

## 4. 测试用例设计 + 易错细节
- append 单页/多页重载：getNumberOfPages 累计、getPageFormat/getPrintable 按页索引路由正确。
- setPage(index, printable, format) 替换指定页；索引越界（ getPageFormat/getPrintable/setPage 正负越界）抛
  ArrayIndexOutOfBoundsException；append/setPage 的 null printable/format 抛 NullPointerException（立即抛）。
- getPageFormat 返回**存储的同一实例**（按引用，非副本）——与 PageFormat.setPaper 的克隆语义对照。
- 空书 getNumberOfPages()==0、getPrintable(0) 抛 IndexOutOfBoundsException。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。

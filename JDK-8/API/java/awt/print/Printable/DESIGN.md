# java.awt.print.Printable 测试设计

## 1. API 功能
单页绘制契约接口：`int print(Graphics graphics, PageFormat pageFormat, int pageIndex)`
+ 三个返回常量 PAGE_EXISTS=0 / NO_SUCH_PAGE=1。

## 2. Java 用户如何使用
实现 print：目标页绘制返回 PAGE_EXISTS，越界返回 NO_SUCH_PAGE 终止；
`job.setPrintable(myPrintable)`（对比 setPageable 的整本文档路由）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/Printable.java`；
`test/jdk/java/awt/print/` 各打印测试。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、恰好 1 个方法签名 (Graphics, PageFormat, int)、PAGE_EXISTS==0/NO_SUCH_PAGE==1。
- 多态：双页 Recording 实现经接口引用断言第 0/1 页返回值与传入 PageFormat 实例身份、
  第 2 页 NO_SUCH_PAGE（headless 下 Graphics 用缓存 Image 的 Graphics2D）。
- 易错点：pageIndex 从 0 计；print 契约允许同一页多次调用（分带打印），实现需幂等。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。

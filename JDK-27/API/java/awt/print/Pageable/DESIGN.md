# java/awt/print.Pageable 测试设计

## 1. API 功能
分页文档契约接口：UNKNOWN_NUMBER_OF_PAGES=-1 常量 + getNumberOfPages/getPageFormat(int)/
getPrintable(int) 三方法。

## 2. Java 用户如何使用
实现 Pageable（或用 Book）交给 `PrinterJob.setPageable(...)`，打印循环按页取 Printable 与格式。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/Pageable.java`；
Book 实现；PrinterJob 调用点。

## 4. 测试用例设计 + 易错细节
- 反射形态：接口、恰好 3 个方法、UNKNOWN_NUMBER_OF_PAGES==-1 精确值。
- 已知实现：Book；多态：数组路由的 Recording 实现经接口引用断言页数/格式/绘制器三元组一致。
- 越界行为由实现定义（Book 抛 IndexOutOfBoundsException），接口不约束。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。

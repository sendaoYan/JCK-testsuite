# java.awt.JobAttributes 测试设计

## 1. API 功能
作业属性束（Cloneable）：copies/defaultSelection/destination/dialog/fileName/fromPage/toPage/
maxPage/minPage/MDH/pageRanges/printer/sides 十三项，全参构造器、派生 getPageRanges、校验矩阵。

## 2. Java 用户如何使用
`JobAttributes ja = new JobAttributes(); ja.setCopies(3); ja.setDestination(
JobAttributes.DestinationType.FILE); ja.setFileName("out.ps");`
配合 PrintJob 设置作业参数（headless 安全，不实际打印）。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/JobAttributes.java`；
Toolkit/PrintJob 调用点。

## 4. 测试用例设计 + 易错细节
- 默认值矩阵：copies=1、ALL/PRINTER/NATIVE、fileName/printer null、from/to 1、
  maxPage=Integer.MAX_VALUE、minPage 1、UNCOLLATED、ONE_SIDED、派生 ranges "1-1;"。
- **全参构造器参数序：..., fileName, maxPage, minPage, MDH, ...**（maxPage 在 minPage 前——易错点）。
- setCopies(0/-1) 抛 IAE；各 setXxx(null) 抛 IAE。
- pageRanges 校验矩阵：null/空数组/一元 pair/null pair/低于 minPage/与 minPage 冲突/区间重叠/
  超过 maxPage/倒置 pair 全部 IAE；返回二维数组为防御性副本。
- 派生规则：显式 ranges 覆盖 from/to；无显式 ranges 时由 from/to 派生单区间。
- 嵌套常量类非枚举、toString 小写连字符（"two-sided-long-edge"、"separate-documents-collated-copies" 等）。
- equals/hashCode/clone/拷贝构造独立；**不可序列化**。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。

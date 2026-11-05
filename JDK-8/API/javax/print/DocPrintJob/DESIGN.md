# javax.print.DocPrintJob 黑盒行为测试设计

## 1. API 功能
DocPrintJob：getPrintService/getAttributes/print(Doc,attrs)/listener 4 方法。

## 2. Java 用户如何使用
单文档打印作业。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
真实 PS job：getPrintService 同引用；getAttributes() 空 PrintJobAttributeSet；print() 完成 PS 输出；listener add/remove 接受 null。
- 记录型 RecJob：listener 增删计数；getAttributes 透出 Copies(2)。

## 5. 负例与精确异常
无。

## 6. 闭环与版本差异
与 StreamPrintServiceFactory 的 PS 输出闭环联动。无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

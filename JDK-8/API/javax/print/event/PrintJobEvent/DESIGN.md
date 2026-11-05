# javax.print.event.PrintJobEvent 黑盒行为测试设计

## 1. API 功能
PrintJobEvent(DocPrintJob, int eventType)：常量 101..106。

## 2. Java 用户如何使用
作业生命周期事件。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
六常量精确值：JOB_CANCELED=101/JOB_COMPLETE=102/JOB_FAILED=103/REQUIRES_ATTENTION=104/NO_MORE_EVENTS=105/DATA_TRANSFER_COMPLETE=106；101..106 循环构造：getPrintEventType 回读 + getPrintJob 同引用 + toString 继承；任意 int（999）被接受回读。

## 5. 负例与精确异常
null source → IAE "null source"。

## 6. 闭环与版本差异
无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

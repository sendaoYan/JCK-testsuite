# javax.print.CancelablePrintJob 黑盒行为测试设计

## 1. API 功能
CancelablePrintJob extends DocPrintJob + cancel()。

## 2. Java 用户如何使用
可取消作业。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
extends 关系反射断言；RecCancel：cancel() 置位；print(Doc) 委托同引用；经父类型视图 addPrintJobListener 生效；cancel 方法存在性反射。

## 5. 负例与精确异常
cancel() 声明 PrintException（本记录实现不抛）。

## 6. 闭环与版本差异
无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

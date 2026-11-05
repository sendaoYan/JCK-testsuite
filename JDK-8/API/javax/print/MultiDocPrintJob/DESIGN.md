# javax.print.MultiDocPrintJob 黑盒行为测试设计

## 1. API 功能
MultiDocPrintJob extends DocPrintJob + print(MultiDoc, attrs)。

## 2. Java 用户如何使用
多文档作业。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
print(MultiDoc,..) 与 print(Doc,..) 双重载反射共存；RecMultiJob 走两节点链：count==2、first 身份。

## 5. 负例与精确异常
print(MultiDoc) 声明 PrintException；MultiDoc.next() 声明 IOException（记录实现包装为 PrintException）。

## 6. 闭环与版本差异
链遍历闭环。无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

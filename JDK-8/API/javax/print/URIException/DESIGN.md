# javax.print.URIException 黑盒行为测试设计

## 1. API 功能
PrintException 子接口：int 常量 URIInaccessible=1、URISchemeNotSupported=2、URIOtherProblem=-1；getReason()/getUnsupportedURI()。

## 2. Java 用户如何使用
报告打印机 URI 问题。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
常量全等（1/2/-1，实测非 20001 系）；记录型实现：URI 同引用 + reason 同值 + extends PrintException。
- 断言：三个常量精确值；getUnsupportedURI 身份；getReason 身份。

## 5. 负例与精确异常
无。

## 6. 闭环与版本差异
常量值为实测（1/2/-1），双侧一致。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

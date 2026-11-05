# javax.print.Doc 黑盒行为测试设计

## 1. API 功能
Doc 接口：getDocFlavor/getPrintData/getAttributes/getReaderForText/getStreamForBytes。

## 2. Java 用户如何使用
打印数据抽象；SimpleDoc 为标准实现。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
SimpleDoc 走全契约 + 自定义 RecDoc 记录型实现（null attrs 合法）。
- 断言：flavor 同引用；print data 同引用；null attrs 直接透传 getAttributes()==null；RecDoc 自定义值身份。

## 5. 负例与精确异常
见 SimpleDoc/（SimpleDoc 的负例矩阵）。

## 6. 闭环与版本差异
无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

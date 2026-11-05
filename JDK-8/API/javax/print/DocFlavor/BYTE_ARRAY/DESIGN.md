# javax.print.DocFlavor.BYTE_ARRAY 黑盒行为测试设计

## 1. API 功能
19 个 [B rep class 常量（文本 12 + 二进制 7）。

## 2. Java 用户如何使用
字节打印数据的预定义 flavor。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
全 19 常量 rep class 全等 [B；UTF_8/UTF_16BE/JPEG 等 mime 精断；new DocFlavor("image/png","[B").equals(BYTE_ARRAY.PNG)；去重 mime 数 >= 14（host 变体可重合）。

## 5. 负例与精确异常
见 DocFlavor/。

## 6. 闭环与版本差异
无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

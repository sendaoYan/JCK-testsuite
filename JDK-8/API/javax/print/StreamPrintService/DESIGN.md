# javax.print.StreamPrintService 黑盒行为测试设计

## 1. API 功能
抽象类：protected StreamPrintService(OutputStream)；getOutputStream/dispose/isDisposed + getOutputFormat 抽象。

## 2. Java 用户如何使用
流式服务基类。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
子类 RecStream：getOutputStream 同引用；isDisposed false→dispose()→true；getOutputFormat 子类值；implements PrintService。
- 反射：声明的 2 个构造中有且仅有一个 protected (OutputStream) 形态。
- 注意：StreamPrintService 不实现 PrintService 绝大多数抽象方法，子类须自行实现全套。

## 5. 负例与精确异常
无（构造/方法无检查型负例）。

## 6. 闭环与版本差异
dispose 状态闭环。无版本差异。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

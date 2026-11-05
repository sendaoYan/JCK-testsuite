# javax.print.PrintException 黑盒行为测试设计

## 1. API 功能
PrintException 4 个构造：()/（String）/(Throwable)/(String,Throwable)，extends Exception。

## 2. Java 用户如何使用
捕获打印失败并读取 message/cause；可序列化跨进程传递。

## 3. 开源代码实际用法调研
实现权威：JDK 源码（JDK-8: jdk8u jdk/src/share/classes/javax/print；JDK-27: jdk/src/java.desktop/share/classes/javax/print）。行为断言均以双 JDK 实测探针为基准（javap + 运行时探针输出 diff）。

## 4. 测试用例设计 + 自动化实现
构造矩阵 4 形态全断言；serRoundTrip 保留 message；getCause 同一引用。
- 断言：no-arg message==null && cause==null；message 形态；cause 形态 cause 同引用；wrapped 形态双字段；序列化 round-trip 后 PrintException 类型 + message 保留。

## 5. 负例与精确异常
无（4 个构造均接受 null）。

## 6. 闭环与版本差异
序列化闭环 serRoundTrip。无版本差异（双侧一致）。

## 7. 运行纪律
纯 headless（env -u DISPLAY），不加 @key；不真用打印机/声卡。jtreg 双侧（JDK-8 需 JTREG_JAVA=jdk-17）独立 workDir/reportDir 全绿后提交。

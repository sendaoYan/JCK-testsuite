# javax.print.StreamPrintService 黑盒行为测试设计

## 1. API 功能
StreamPrintService 抽象类 implements PrintService；getOutputStream/getOutputFormat(abstract)/dispose/isDisposed。

## 2. Java 用户如何使用
子类把打印数据写到 OutputStream；用完 dispose。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/StreamPrintService.java。

## 4. 测试用例设计 + 自动化实现
黑盒：内存流往返、getOutputFormat、dispose/isDisposed。白盒：public abstract、implements PrintService。


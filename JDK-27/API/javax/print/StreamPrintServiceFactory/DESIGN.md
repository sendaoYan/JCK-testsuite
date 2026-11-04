# javax.print.StreamPrintServiceFactory 黑盒行为测试设计

## 1. API 功能
StreamPrintServiceFactory 抽象类，lookupStreamPrintServiceFactories 静态查找；getOutputFormat/getSupportedDocFlavors/getPrintService 抽象。

## 2. Java 用户如何使用
lookup 后 getPrintService(outputStream) 创建流打印服务。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/StreamPrintServiceFactory.java。

## 4. 测试用例设计 + 自动化实现
黑盒：lookup 返回非空数组（不真打印）。白盒：public abstract、方法形态。


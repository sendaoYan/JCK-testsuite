# javax.print.attribute.standard.PrinterResolution 黑盒行为测试设计

## 1. API 功能
PrinterResolution extends ResolutionSyntax；getResolution/getCrossFeed/getFeed/toString/lessThanOrEquals。DPI=100, DPCM=254。

## 2. Java 用户如何使用
new PrinterResolution(300,600,DPI) 指定打印分辨率。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/PrinterResolution.java。

## 4. 测试用例设计 + 自动化实现
黑盒：分辨率往返、toString 精确、lessThanOrEquals、equals、零值 IAE。白盒：public final、extends ResolutionSyntax。


# java.awt.datatransfer.SystemFlavorMap 测试设计

## 1. API 功能
系统 flavor 映射表（FlavorMap+FlavorTable 单例），含 Java MIME 编解码工具。

## 2. Java 用户如何使用
getDefaultFlavorMap；getNativesForFlavor/getFlavorsForNative/getNativesForFlavors；encodeJavaMIMEType/decodeJavaMIMEType/isJavaMIMEType/encodeDataFlavor/decodeDataFlavor。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/SystemFlavorMap.java

## 4. 测试用例设计 + 自动化实现
白盒: final、FlavorMap+FlavorTable；黑盒: 默认映射非空、flavor/native 互查、Java MIME 编解码 roundtrip。

自动化实现位于同目录 `SystemFlavorMapTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

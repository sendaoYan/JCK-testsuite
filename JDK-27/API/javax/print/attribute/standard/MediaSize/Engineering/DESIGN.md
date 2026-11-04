# javax.print.attribute.standard.MediaSize.Engineering 黑盒行为测试设计

## 1. API 功能
MediaSize.Engineering 嵌套常量类，提供 MediaSize 静态常量。

## 2. Java 用户如何使用
引用 MediaSize.Engineering.XXX 常量（如 MediaSize.Engineering.A）。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/MediaSize.java。

## 4. 测试用例设计 + 自动化实现
黑盒：常量非空、category/name、精确尺寸、toString 精确。白盒：public final、MediaSize 常量字段。


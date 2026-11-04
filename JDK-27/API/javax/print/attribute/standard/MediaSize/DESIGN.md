# javax.print.attribute.standard.MediaSize 黑盒行为测试设计

## 1. API 功能
MediaSize extends Size2DSyntax implements Attribute；float/int 构造器、getMediaSizeName、getMediaSizeForName、findMedia。

## 2. Java 用户如何使用
new MediaSize(210,297,MM) 或引用 MediaSize.ISO.A4 常量。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/MediaSize.java。

## 4. 测试用例设计 + 自动化实现
黑盒：A4 尺寸/toString/getMediaSizeName/findMedia/getMediaSizeForName、float 构造器、equals、负值 IAE。白盒：extends Size2DSyntax、implements Attribute。


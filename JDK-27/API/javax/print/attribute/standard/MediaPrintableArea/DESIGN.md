# javax.print.attribute.standard.MediaPrintableArea 黑盒行为测试设计

## 1. API 功能
MediaPrintableArea 可打印区域：float/int 构造器、getX/getY/getWidth/getHeight/getPrintableArea/toString。INCH=25400, MM=1000。

## 2. Java 用户如何使用
new MediaPrintableArea(1,1,2,2,INCH) 指定可打印区域。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/MediaPrintableArea.java。

## 4. 测试用例设计 + 自动化实现
黑盒：坐标/宽高往返、toString 精确、equals/hashCode、零宽 IAE。白盒：public final、implements 三接口。


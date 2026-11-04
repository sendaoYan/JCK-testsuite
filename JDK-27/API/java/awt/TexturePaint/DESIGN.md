# java.awt.TexturePaint 测试设计

## 1. API 功能
纹理填充，用 BufferedImage 平铺一块锚定矩形。

## 2. Java 用户如何使用
new TexturePaint(BufferedImage, Rectangle2D)；getImage/getAnchorRect/getTransparency/createContext。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/TexturePaint.java

## 4. 测试用例设计 + 自动化实现
白盒: implements Paint；黑盒: 构造、getImage 同一实例、anchor、transparency、createContext。

自动化实现位于同目录 `TexturePaintTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。

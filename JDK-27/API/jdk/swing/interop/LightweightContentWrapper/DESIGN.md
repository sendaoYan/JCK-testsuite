# jdk.swing.interop.LightweightContentWrapper 设计案

## 1. API 功能
轻量内容抽象类。

## 2. Java 用户如何使用
嵌入场景子类化。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.swing.interop/share/classes/jdk/swing/interop/LightweightContentWrapper.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract；黑盒降级。

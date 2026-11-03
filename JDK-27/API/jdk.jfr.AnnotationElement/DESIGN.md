# jdk.jfr.AnnotationElement 设计案

## 1. API 功能
JFR 注解元素的运行时表示：构造（注解类+值），getTypeName/getValue/hasValue/getValues。

## 2. Java 用户如何使用
new AnnotationElement(Label.class, "x") 后 getValue("value")。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/AnnotationElement.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 构造 + value 回读 + Map 构造。

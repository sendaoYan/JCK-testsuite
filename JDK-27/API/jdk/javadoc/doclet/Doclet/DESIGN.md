# jdk.javadoc.doclet.Doclet 设计案

## 1. API 功能
Doclet 接口：init/getName/getSupportedOptions/run。

## 2. Java 用户如何使用
由 StandardDoclet 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.javadoc/share/classes/jdk/javadoc/doclet/Doclet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 StandardDoclet 实例测接口方法。

# jdk.javadoc.doclet.Taglet 设计案

## 1. API 功能
Taglet 接口：location/isInlineTag/default isBlockTag/init/toString。

## 2. Java 用户如何使用
匿名实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.javadoc/share/classes/jdk/javadoc/doclet/Taglet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒匿名实现+default isBlockTag/init。

# jdk.javadoc.doclet.StandardDoclet 设计案

## 1. API 功能
标准 Doclet：init/getName/getLocale/getReporter/getSupported*。

## 2. Java 用户如何使用
new StandardDoclet()+init。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.javadoc/share/classes/jdk/javadoc/doclet/StandardDoclet.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 init 后查询名称/locale/reporter。

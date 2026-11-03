# jdk.dynalink.beans.StaticClass 设计案

## 1. API 功能
静态类包装：forClass + getRepresentedClass。

## 2. Java 用户如何使用
StaticClass.forClass(String.class)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/beans/StaticClass.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 forClass+equals+hashCode+toString。

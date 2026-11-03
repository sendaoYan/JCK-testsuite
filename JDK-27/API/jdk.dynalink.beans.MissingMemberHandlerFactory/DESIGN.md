# jdk.dynalink.beans.MissingMemberHandlerFactory 设计案

## 1. API 功能
缺失成员处理工厂接口。

## 2. Java 用户如何使用
由 BeansLinker 提供。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/beans/MissingMemberHandlerFactory.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。

# jdk.dynalink.linker.support.SimpleLinkRequest 设计案

## 1. API 功能
简单链接请求实现。

## 2. Java 用户如何使用
new SimpleLinkRequest(csd, unstable, args)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/support/SimpleLinkRequest.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 receiver/arguments/descriptor/stable/replaceArguments。

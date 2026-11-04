# sun.misc.Unsafe 设计案

## 1. API 功能
底层不安全操作类（jdk.unsupported）。

## 2. Java 用户如何使用
常量 + getUnsafe()（受限）。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.unsupported/share/classes/sun/misc/Unsafe.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒常量+getUnsafe 抛 SecurityException。

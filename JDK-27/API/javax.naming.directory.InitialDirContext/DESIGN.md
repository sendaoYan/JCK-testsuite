# javax.naming.directory.InitialDirContext 设计案

## 1. API 功能
初始目录上下文（extends InitialContext）。

## 2. Java 用户如何使用
new InitialDirContext() 或 env。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/InitialDirContext.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造尝试（provider 决定）。

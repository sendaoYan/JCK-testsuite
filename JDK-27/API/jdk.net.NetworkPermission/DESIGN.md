# jdk.net.NetworkPermission 设计案

## 1. API 功能
网络权限（extends BasicPermission）。

## 2. Java 用户如何使用
new NetworkPermission(name) / (name, actions)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.net/share/classes/jdk/net/NetworkPermission.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+getName/getActions/equals。

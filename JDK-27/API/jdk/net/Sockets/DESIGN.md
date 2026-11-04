# jdk.net.Sockets 设计案

## 1. API 功能
Socket 选项工具：setOption/getOption/supportedOptions。

## 2. Java 用户如何使用
Sockets.setOption(socket, option, value)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.net/share/classes/jdk/net/Sockets.java；test/jdk/jdk/net/。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 Socket/ServerSocket set/get option。

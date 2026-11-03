# jdk.net.UnixDomainPrincipal 设计案

## 1. API 功能
Unix 域套接字主体（record）：user/group。

## 2. Java 用户如何使用
new UnixDomainPrincipal(user, group)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.net/share/classes/jdk/net/UnixDomainPrincipal.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+user/group/equals/hashCode/toString。

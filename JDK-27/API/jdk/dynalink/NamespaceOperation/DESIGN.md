# jdk.dynalink.NamespaceOperation 设计案

## 1. API 功能
命名空间操作：baseOperation + 多个 namespace。

## 2. Java 用户如何使用
new NamespaceOperation(op, PROPERTY, ELEMENT) + contains/getNamespaces。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/NamespaceOperation.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+命名空间计数+contains。
## 5. 契约深化（JDK-27）
- 构造校验全表：null base→NPE；base 为 NamedOperation/NamespaceOperation→IAE；空命名空间数组→IAE("Must specify at least one namespace")；null 元素→NPE、null 数组→NPE。
- 数组防御性拷贝双向验证（构造入参与 getNamespaces 返回值）。
- toString 保序拼接 "GET:ELEMENT|PROPERTY"；named 后 "GET:ELEMENT|PROPERTY:q"。
- equals 顺序敏感（ELEMENT,PROPERTY 与 PROPERTY,ELEMENT 不等）；hashCode 与 equals 一致。
- 静态助手 getBaseOperation/getNamespaces/contains 对裸操作（返回原操作/空数组/false）与包装操作的行为。

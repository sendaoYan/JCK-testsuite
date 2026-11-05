# jdk.dynalink.support.SimpleRelinkableCallSite 设计案

## 1. API 功能
简单调用点：relink/resetAndRelink。

## 2. Java 用户如何使用
new SimpleRelinkableCallSite(csd)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/support/SimpleRelinkableCallSite.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+type+getDescriptor。
## 5. 契约深化（JDK-27）
- 真实链接-调用闭环；relink(null, mh)→NPE；resetAndRelink 与 relink 同效（替换目标）；descriptor/type 保真。
- 源码语义：relink=setTarget(gi.compose(relinkAndInvoke))，resetAndRelink 委托 relink。

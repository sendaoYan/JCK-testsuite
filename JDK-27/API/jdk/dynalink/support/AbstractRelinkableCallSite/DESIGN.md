# jdk.dynalink.support.AbstractRelinkableCallSite 设计案

## 1. API 功能
抽象可重链接调用点：getDescriptor/initialize。

## 2. Java 用户如何使用
ChainedCallSite/SimpleRelinkableCallSite 的父类。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/support/AbstractRelinkableCallSite.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒通过子类实例测 getDescriptor/initialize。
## 5. 契约深化（JDK-27）
- 结构：abstract、extends MutableCallSite、implements RelinkableCallSite、protected 构造器。
- MinimalSite 生命周期真实闭环：getDescriptor 恒等、MutableCallSite.type()==descriptor 类型、link() 只 initialize 不 relink、首调恰一次 relink、initialize 原样安装 handle。

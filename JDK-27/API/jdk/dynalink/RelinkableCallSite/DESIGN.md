# jdk.dynalink.RelinkableCallSite 设计案

## 1. API 功能
可重链接调用点接口：initialize/getDescriptor/relink/resetAndRelink。

## 2. Java 用户如何使用
由 ChainedCallSite/SimpleRelinkableCallSite 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/RelinkableCallSite.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 接口面精确集合：{initialize, getDescriptor, relink, resetAndRelink}。
- 手写 RecordingSite（继承 AbstractRelinkableCallSite + setTarget(gi.compose(relink))）驱动真实 linker：link() 返回自身、首调触发一次 relink、initialize 直接安装 bootstrap handle。

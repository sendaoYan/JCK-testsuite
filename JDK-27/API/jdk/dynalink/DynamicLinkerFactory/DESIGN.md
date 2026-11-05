# jdk.dynalink.DynamicLinkerFactory 设计案

## 1. API 功能
链接器工厂：set* 配置 + createLinker()。

## 2. Java 用户如何使用
setPrioritizedLinkers/setSyncOnRelink/setUnstableRelinkThreshold → createLinker。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/DynamicLinkerFactory.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒多步配置后 createLinker 两次。
## 5. 契约深化（JDK-27）
- 默认配置闭环：不设 prioritized 时默认 fallback=BeansLinker，静态属性可链接。
- 优先级/回退顺序计数断言（pri→fb 恰好各一次）；全拒绝→NSDME。
- setPrelinkTransformer 计数恰一次、返回 null→NPE（relink 内 requireNonNull）。
- setAutoConversionStrategy 链接期被调用且目标类型到达 site 类型。
- setInternalObjectsFilter 在动态方法调用建立时生效。
- setUnstableRelinkThreshold(1)+setSyncOnRelink(true)：失败的重链不经过 prelink（计数不变）；createLinker 可重复产出新实例。

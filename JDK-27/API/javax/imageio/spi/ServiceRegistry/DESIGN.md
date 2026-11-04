# javax.imageio.spi.ServiceRegistry — DESIGN

## 1. API 功能
`ServiceRegistry` 是 Image I/O 服务提供者注册表基类（非抽象，构造器公开）。类别限定为 5 个 SPI 类型。方法分组：
- 构造/静态查找：`ServiceRegistry(Iterator<Class<?>>)`、`lookupProviders(Class,ClassLoader)`、`lookupProviders(Class)`。
- 类别：`getCategories()`。
- 注册/注销：`registerServiceProvider(T,Class<T>)`、`registerServiceProvider(Object)`、`registerServiceProviders(Iterator<?>)`、`deregisterServiceProvider(T,Class<T>)`、`deregisterServiceProvider(Object)`、`deregisterAll(Class<?>)`、`deregisterAll()`。
- 查询：`getServiceProviders(Class,boolean)`、`getServiceProviders(Class,Filter,boolean)`、`getServiceProviderByClass(Class)`、`contains(Object)`。
- 排序：`setOrdering(Class,T,T)`、`unsetOrdering(Class,T,T)`。

## 2. Java 用户如何使用
用户构造一个类别限定的 `ServiceRegistry`（如仅 `ImageInputStreamSpi.class`），`registerServiceProvider(provider, ImageInputStreamSpi.class)` 注册自定义 SPI，`contains`/`getServiceProviderByClass` 查询，`setOrdering` 建立优先级，`getServiceProviders(..., true)` 取有序结果，`deregisterServiceProvider`/`deregisterAll` 清理。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/spi/IIORegistry.java`：`IIORegistry` 构造时 `super(initialCategories.iterator())` 传入 5 个类别，再逐个 `registerServiceProvider(...)`。
- `src/java.desktop/share/classes/javax/imageio/spi/ServiceRegistry.java`：内部 `SubRegistry` 维护 `Map<Class<?>,Object>`（每叶类最多一个实例）与 `PartiallyOrderedSet`（偏序）；`registerServiceProvider` 若已有同叶类实例则先注销旧实例；`getServiceProviders(...,false)` 走 map 无序迭代、`true` 走拓扑有序迭代。
- `src/java.desktop/share/classes/javax/imageio/spi/PartiallyOrderedSet.java`：`setOrdering(first, second)` 使 first 在迭代中先于 second。

## 4. 测试用例设计 + 自动化实现（`ServiceRegistryTest.testBlackBox`）
- 用自定义 `TinyInputSpiA`/`TinyInputSpiB`（覆盖 `onRegistration`/`onDeregistration` 计数）走完整注册链。
- 类别查询；注册回调各一次；`contains`/`getServiceProviderByClass` 身份相等；无序迭代包含两者。
- `setOrdering(A,B)` 后有序迭代中 A 先于 B；`unsetOrdering` 首真再假；对未注册提供者排序返回 false；对同一提供者排序抛 `IllegalArgumentException`。
- 同叶类重注册：返回 false、旧实例被注销（`onDeregistration` 一次）、新实例被注册（`onRegistration` 一次）、`contains` 切换。
- `deregisterServiceProvider(T,Class)`/`(Object)`、`registerServiceProvider(Object)`、`registerServiceProviders(Iterator)`、`deregisterAll(Class)`/`deregisterAll()` 的注册/注销往返。
- 边界：构造 null 类别、非法类别（`String.class`）、未知类别 `getServiceProviders`/`deregisterAll`、null provider 注册、`contains(null)` 均抛 `IllegalArgumentException`。
- 静态 `lookupProviders` 返回非 null 迭代器，null 参数抛异常。

## 5. 契约深化补充（本轮，探针实测记录行为）
- 迭代器 remove：providers 迭代器 `remove()` 先于 next 抛 `IllegalStateException`，next 后真删且**绕过 onDeregistration**（回调计数 0）；categories 迭代器 remove 同 ISE、next 后删除类别本身（该类别后续查询 IAE）。
- fail-fast：注册/deregisterAll 修改后旧迭代器 next 抛精确 `ConcurrentModificationException`；未修改可排空。
- Filter 机制：构造即预取（filter 已跑 ≥1 次）；排空后每个提供者恰好一次；null filter `NullPointerException`；filter 异常实例原样中继（`==`）。
- 排序边角：反向 set 替换旧边并翻转迭代；a<b、b<c 传递；never-set unset false；未注册 false；未知类别 set/unset IAE。
- 查询边角：`getServiceProviderByClass(null)` IAE、非类别/未注册类 null；raw 跨类别注册 `ClassCastException` 且状态不变。
- 无类别注册：普通对象无副作用、注销 no-op；**同实例重复注册返回 false 但仍触发 dereg+reg 回调（计数 reg=2/dereg=1）**；空类别表合法；`deregisterAll(category)` 逐提供者回调。
- JDK-27 专属（与 JDK-8 互斥断言）：null 类别元素 IAE；`lookupProviders(String.class)` 两重载 IAE（bug 8068749 收紧）；`finalize()` 已移除（JDK-8 声明）。

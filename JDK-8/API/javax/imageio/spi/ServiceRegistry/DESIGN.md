# javax.imageio.spi.ServiceRegistry — DESIGN（JDK-8）

## 1. API 功能
`ServiceRegistry` 是 Image I/O 服务提供者注册表（**JDK-8 上非抽象、公开构造器、类别不限 Image I/O SPI**；JDK-9+ 因 bug 8068749 收紧）。方法分组：
- 构造/静态查找：`ServiceRegistry(Iterator<Class<?>>)`、`lookupProviders(Class)`、`lookupProviders(Class,ClassLoader)`。
- 类别：`getCategories()`。
- 注册/注销：`registerServiceProvider(T,Class<T>)`、`registerServiceProvider(Object)`、`registerServiceProviders(Iterator<?>)`、`deregisterServiceProvider`（两重载）、`deregisterAll(Class)`、`deregisterAll()`。
- 查询：`getServiceProviders(Class,boolean)`、`getServiceProviders(Class,Filter,boolean)`、`getServiceProviderByClass(Class)`、`contains(Object)`。
- 排序：`setOrdering(Class,T,T)`、`unsetOrdering(Class,T,T)`。

## 2. Java 用户如何使用
构造类别限定的注册表（也可直接用 `IIORegistry` 全局单例），注册自写 SPI，用 `getServiceProviders` 迭代、`setOrdering` 建立偏序、`Filter` 子集选择；`deregisterAll` 批量清理。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/ServiceRegistry.java`：内部 `SubRegistry` 以 `Map<Class<?>,Object>` 按**叶子类**存提供者（每叶类最多一个实例）；`PartiallyOrderedSet` 维护偏序；`ProviderIterator` 预取一个元素（`getServiceProviders` 返回前 filter 已至少跑一次）；迭代器 fail-fast（结构修改后 next 抛 `ConcurrentModificationException`）。
- 同文件：`registerServiceProvider` 对已存在同叶类实例先注销旧再注册新（**同实例重复注册返回 false 但仍触发一次 dereg+reg 回调——实测行为**）；迭代器 `remove()` 直接操作内部集合、**绕过 onDeregistration**；`getCategories()` 迭代器 `remove()` 删除类别本身。
- `jdk/test/javax/imageio/spi/ServiceRegistryRestriction.java`（JDK-9+）：官方回归锁定类别收紧；`OrderingTest.java`：偏序反转/传递语义（bug 4936445）。

## 4. 测试用例设计 + 易错规范细节（`ServiceRegistryTest`）
- 迭代器 remove 语义：providers 迭代器 `remove()` 先于 `next()` 抛 `IllegalStateException`，`next()` 后真删（`contains` 变 false）且**绕过 onDeregistration**（计数 0，记录行为）；categories 迭代器 remove 同 ISE，next 后删类别、后续该类别查询抛 `IllegalArgumentException`。
- fail-fast：注册/`deregisterAll` 修改后旧迭代器 next 抛精确 `ConcurrentModificationException`；未修改可正常排空。
- Filter 机制：构造即预取（filter 已被调用 ≥1 次）；排空后每个提供者恰好见过一次；null filter 抛 `NullPointerException`；filter 抛出的异常实例被原样中继（`==`）。
- 排序边角：反向 setOrdering 返回 true 且翻转迭代（替换旧边）；a<b、b<c 传递成立；未设置对 unset 返回 false；`(a,a)` 自环 set/unset 均 `IllegalArgumentException`；未注册提供者 false；未知类别 set/unset 均 `IllegalArgumentException`；跨类别对返回 false。
- 查询边角：`getServiceProviderByClass(null)` IAE、非类别类返回 null、未注册类返回 null；raw 类型下跨类别注册抛 `ClassCastException` 且状态不变；`register(null,cat)`/未知类别 getServiceProviders/deregisterAll/`contains(null)`/`new ServiceRegistry(null)` 均 IAE。
- 无类别注册：普通对象注册无副作用、注销 no-op；空类别表构造合法；同实例重复注册 false 但回调计数 reg=2/dereg=1；`deregisterAll(category)` 逐提供者回调。
- **JDK-8 专属（与 JDK-27 逐条互斥断言）**：`String.class` 类别被接受、null 类别元素被接受、`lookupProviders(String.class)` 两重载正常返回、`lookupProviders(null)` IAE、`ServiceRegistry` 声明 `finalize()`（JDK-9+ 移除）。
- 易错点：提供者按叶子类去重——测试类必须各自独立叶子类；泛型签名 `<T> registerServiceProvider(T,Class<T>)` 在编译期即拒绝跨类别调用（本身是契约），需 raw 类型绕过验证运行时行为。

# javax.imageio.spi.ServiceRegistry.Filter — DESIGN（JDK-8）

## 1. API 功能
`ServiceRegistry.Filter` 是提供者选择回调接口（`ServiceRegistry` 的嵌套 public 接口），SAM：
- `boolean filter(Object provider)`——单 `Object` 参数、返回 boolean、public abstract、不声明受检异常。

## 2. Java 用户如何使用
`getServiceProviders(Class, Filter, boolean)` 对每个已注册提供者调用一次 `filter`，true 则进入返回的迭代器；典型用法是按格式名（如 `getFormatNames()` 含 "png"）或厂商过滤插件。是函数式接口，可直接用 lambda。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/ServiceRegistry.java`：`ProviderIterator` 构造时立即 `advance()` 预取一个元素——**filter 在 getServiceProviders 返回前已至少运行一次**；filter 抛出的异常在 next/getServiceProviders 调用点原样中继（不包装）。
- 同文件 `getServiceProviders(Class,Filter,boolean)`：`filter == null` 时直接 NPE。
- `jdk/src/share/classes/javax/imageio/ImageIO.java`：`getImageWritersByMIMEType` 等内部匿名 `Filter` 按 MIME 匹配。

## 4. 测试用例设计 + 易错规范细节（`FilterTest`）
- 白盒精确签名：恰好 1 个声明方法；名 `filter`；返回 boolean；单 `Object` 参数；public abstract；无受检异常；lambda 可直接实现（SAM 验证）。
- 调用语义：注册 3 个独立叶子类提供者，计数 filter 实测——getServiceProviders 返回时已见过 ≥1 个（预取）；排空迭代器后 3 个提供者各恰好见过一次（IdentityHashMap 计数）；仅被接受者出现在迭代结果。
- useOrdering 无关性：同一 acceptAll filter 在 useOrdering=false/true 下成员集合相同；true 时尊重已设置的偏序（a 先于 b）。
- 负例（精确异常）：null filter 在 useOrdering 两种状态下均抛 `NullPointerException`；抛异常的 filter 其异常实例被原样中继（`==` 同一实例，从 getServiceProviders 抛出）；rejectAll 得空迭代（不抛）。
- 易错点：filter 每提供者恰好一次依赖"提供者叶子类互不相同"；验证异常中继必须比对实例同一性而非异常类。

# java.beans.Introspector 测试设计案

> 覆盖目标：完整契约（decapitalize 边界、getBeanInfo 属性/事件/绑定/约束自动发现、stopClass 校验、
> 缓存清理）。headless 安全，无网络/文件/墙钟依赖。

## 1. API 功能
- 静态内省入口：`getBeanInfo(Class)`、`getBeanInfo(Class,int)`、`getBeanInfo(Class,Class)`、`getBeanInfo(Class,Class,int)`。
- 工具：`decapitalize(String)`、`getBeanInfoSearchPath()`/`setBeanInfoSearchPath(String[])`、`flushCaches()`、`flushFromCaches(Class)`。
- 标志位：`USE_ALL_BEANINFO` / `IGNORE_IMMEDIATE_BEANINFO` / `IGNORE_ALL_BEANINFO`。

## 2. 使用方式
- 对任意 bean 类调用 `Introspector.getBeanInfo(cls)` 得到 `BeanInfo`，遍历 `getPropertyDescriptors()` / `getEventSetDescriptors()`。
- 用 `decapitalize` 生成属性名；用 `flushCaches`/`flushFromCaches` 清缓存让内省重新分析。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/Introspector.html`）。
- OpenJDK 回归 `~/git/jdk8u-dev` / `~/git/jdk` 的 `test/jdk/java/beans/Introspector`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_beans/Introspector`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | decapitalize 首字母大写/缩写词/空串/单字符/null | testDecapitalize |
| P0 | 属性发现：String/boolean(int)/数组/索引/约束属性 + 读写方法 | testGetBeanInfo |
| P0 | bound 自动发现（addPropertyChangeListener）与 constrained（setter throws PropertyVetoException） | testGetBeanInfo |
| P0 | 事件集发现（自定义 FooListener + propertyChange + vetoableChange） | testGetBeanInfo |
| P0 | stopClass 不是父类（含等于自身）→ IntrospectionException | testFlagsAndStopClass |
| P1 | 三种 flags + 默认 stopClass | testFlagsAndStopClass |
| P1 | flushCaches/flushFromCaches；flushFromCaches(null)→NPE；搜索路径 get/set | testCachesAndSearchPath |

## 关键规范细节
- `decapitalize` 对「连续两个大写字母开头」保留原样：`URL`→`URL`、`URLFoo`→`URLFoo`。
- **constrained 不是靠 addVetoableChangeListener 自动发现**，而是靠 setter 方法 `throws PropertyVetoException`。
- bound 靠 `addPropertyChangeListener(PropertyChangeListener)` 自动发现。
- `getBeanInfo(bean, stopClass)` 中 stopClass 必须位于 bean 的父类链（不含 bean 自身），否则抛 IntrospectionException。
- `flushFromCaches(null)` 抛 NullPointerException。

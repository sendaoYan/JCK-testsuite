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

---

# 深化补充（2026-11-05，batch beansC）

## 新增覆盖维度
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | decapitalize 完整矩阵：`ABc`/`AB`/`URLs`/`AB2`（前两字母大写规则）、`aB`、非 ASCII 大写 `AÜ`/`ÜA`/`ÜU`/`Ünicode`/`Üü`、titlecase 字符 `ǅungla`→`ǆungla`/`ǅǅ`（Lt 类不触发规则）、前导空格、确定性 | testDecapitalizeMatrix |
| P0 | 继承层级 HBase/HSub：全量= {a,b,class}；stop=Base 恰 {b} 且连 Object 的 class 属性一并隐藏、方法描述符只剩 getB/setB；stop=Object 恰 {a,b}；4 参重载与 3 参一致 | testHierarchyAndStopClasses |
| P0 | 缓存身份：同类两次 `getBeanInfo` **返回同一实例**（实测）；PropertyDescriptor 同为共享实例；描述符数组为内部引用（非防御拷贝）；`flushCaches()` 后产出新实例 | testCacheIdentity |
| P0 | 负例：`getBeanInfo(null)` 与 `getBeanInfo(null, Object.class)` → NPE；4 参非法 stopClass / stopClass==beanClass → IntrospectionException | testNegativeAndOddTypes |
| P1 | 奇异类型：接口可内省（getter 变属性、无 Object 的 class 属性、恰 1 个属性）；包私有嵌套 static 类**可**内省（不抛异常，实测记录）；primitive（int.class）零属性；String[]/Integer 只有 class 属性 | testNegativeAndOddTypes |
| P1 | `setBeanInfoSearchPath(null)` 实测存储为**空数组**（不恢复默认值，javadoc 未承诺恢复）；finally 恢复原值并逐字节比对 | testSearchPathNull |
| P1 | 三个 flag 常量精确值 1/2/3；无显式 BeanInfo 的类在 4 种 flag（含 0）下属性名集合完全一致 | testStructure / testFlagEquivalence |

## 易错规范细节（深化）
- **两前导大写规则**的判定用 `Character.isUpperCase`：titlecase 字符（ǅ U+01C5，类别 Lt）不满足 → 首字符被小写化；`Ü` 等非 ASCII 大写字母满足 → 原样返回。规则与 Locale 无关（`Character.toLowerCase(char)`）。
- **stopClass 语义**是「stopClass 自身及其以上的方法全部排除」：`getBeanInfo(Sub, Base)` 连 `Object.getClass()` 的 class 属性也不出现，方法描述符只剩 Sub 自身方法。
- **缓存共享**：JDK-8/JDK-27 实测同类重复 `getBeanInfo` 返回**同一 BeanInfo 实例**、内部数组与 PropertyDescriptor 同为共享引用——修改其一影响后续所有调用方；测试改动后必须恢复 + `flushCaches()`。
- `setBeanInfoSearchPath(null)` 之后搜索路径是空数组而非默认值；默认内容因 JDK 而异（JDK-8/JDK-27 实测均为 `{"sun.beans.infos"}`），测试只断言非空、可恢复，不断言内容。
- 包私有嵌套 static 类在 unnamed package（类路径）下**可正常内省**——`ReflectUtil.isPackageAccessible` 检查的是包可访问性而非类修饰符；想触发 IntrospectionException 要靠非法 stopClass。
- 接口内省结果不含 Object 的 class 属性（接口方法表不含 getClass）。primitive 类无任何方法 → 零属性（不是「只有 class」）。

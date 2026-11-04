# java.util.Map 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。put/get/remove 返回语义、containsKey/containsValue、keySet/values/entrySet、
> 以及 Java 8 默认方法：getOrDefault/putIfAbsent/computeIfAbsent/remove(K,V)/replace(K,V)/
> replace(K,old,new)/computeIfPresent/compute/merge/replaceAll/forEach，含 null 结果移除语义与
> null 参数 NPE。

## 1. API 提供了哪些功能

- **核心**：`put/get/remove`、`containsKey/containsValue`、`keySet/values/entrySet`。
- **默认方法（Java 8）**：`getOrDefault`、`forEach`、`replaceAll`、`putIfAbsent`、`remove(K,V)`、
  `replace(K,V)`、`replace(K,old,new)`、`computeIfAbsent`、`computeIfPresent`、`compute`、`merge`。

## 2. Java 用户如何使用

- `map.getOrDefault(key, d)` 安全读；`map.computeIfAbsent(key, fn)` 惰性初始化；
  `map.merge(key, val, fn)` 原子合并。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Map.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Map.java`（默认方法实现）。

## 4. 测试用例设计（映射到 MapTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 基本 put/get/remove + 视图 | testBasics + testViews |
| P0 | getOrDefault/putIfAbsent/computeIfAbsent/replaceAll/forEach | testDefaults |
| P0 | remove(K,V)/replace/replace(K,old,new)/computeIfPresent/compute/merge | testDefaultMethodContract |
| P0 | replaceAll/forEach 遍历 | testDefaultReplaceAllForEach |

## 关键规范细节（易错，已锁入断言）

- `remove(K,V)` 仅当当前映射等于 V 时移除并返回 true。
- `replace(K,V)` 仅在 key 存在时替换并返回旧值；`replace(K,old,new)` 仅当当前值等于 old 才替换。
- `computeIfPresent` 结果为 null 时**移除**映射；key 不存在时 no-op。
- `compute` 总是执行 fn；fn 结果为 null 时移除映射。
- `merge(key, value, fn)`：key 不存在时直接存 value；存在时 `fn(old, value)`；结果为 null 时移除。
  `merge` 的 value 为 null 或 fn 为 null 抛 **NPE**；`compute/computeIfAbsent/computeIfPresent/replaceAll`
  的 fn 为 null 抛 **NPE**。

## 注意事项

- 用 HashMap（允许 null）为载体；不锁定迭代顺序。


## 5. 契约级深化追加（2026-11，42→151 断言）

新增方法：testEqualsHashCodeFormula / testEntryContract / testDefaultMethodImplementationPaths /
testPresentNullValueBranches / testViewSemantics / testIterationAndInvariants。

| 维度 | 锁定内容 |
|---|---|
| equals/hashCode 公式 | hashCode = Σ entry.hashCode() = Σ (k.hashCode() ^ v.hashCode())；{"a"=1,"b"=2} == 192（97^1 + 98^2，各 96）；null key/value 贡献 0；跨实现 HashMap/TreeMap/LinkedHashMap 相等且同 hash；equals 以 entrySet 相等为判据 |
| Map.Entry 契约 | live entry setValue 返回旧值并反写；entry.hashCode 公式 97^5==100（"k"=2→"k"=5 后 107^2==105 场景）；与 SimpleEntry/SimpleImmutableEntry 跨实现互等；SimpleImmutableEntry.setValue UOE |
| 默认方法实现路径 | ProbeMap 直 implements Map（无父类覆盖），锁定接口 default：putIfAbsent present 不调 put、computeIfAbsent present（含 null 值）跳过函数、computeIfPresent absent/null 值零副作用、merge absent 不调函数、getOrDefault present-null 返回 null（非默认值）、replaceAll/forEach 走 entrySet |
| present-null-value 分支 | putIfAbsent 覆盖 null 返回 null；computeIfAbsent 视 null 为 absent 重新计算；computeIfPresent 忽略 null 值 entry；compute 收到 v==null 且 null 结果删 entry；merge 视 null 为 absent 直取 value；remove(k,null) 删 present-null；replace(k,null,v) 匹配成功；异常原样透传（assertSame）且映射不变 |
| 视图语义 | keySet/values/entrySet size 联动、remove/clear 透传、add UOE、跨实现视图 equals |
| 状态不变量 | clear 后逐视图空、LinkedHashMap 插入序 toString 锁定 "{b=2, a=1, c=3}" |

### 易错规范细节（实测锁定）

- `getOrDefault` 对 **present-null** 返回 null 而非 default（get!=null || containsKey 短路语义）。
- `merge(k, null, fn)` 与 `merge(k, v, null)` 均先抛 NPE（先查参数再查映射）。
- `remove(k, null)` 会删除「现值为 null」的映射（Objects.equals(null,null)=true 且 containsKey 消歧）。
- `computeIfAbsent` 对现值为 null 的 entry **调用**函数并覆盖；`computeIfPresent` 则**忽略**同一 entry——两者对 null 现值语义相反。

# java.util.AbstractSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。`equals` 跨实现（size + containsAll 语义，忽略迭代顺序）、
> `hashCode`（元素 hashCode 之和，顺序无关）、`removeAll` 基于 size 的优化（小者迭代）、
> `add/contains/remove/clear`（含 null 元素、重复 add 返回 false）。

## 1. API 提供了哪些功能

- **抽象方法（继承 AbstractCollection）**：`iterator()`、`size()`。
- **覆写实现**：`equals`、`hashCode`、`removeAll`。

## 2. Java 用户如何使用

- 自定义 Set 只需实现 `iterator()` + `size()`；`equals`/`hashCode` 自动满足 Set 契约。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/AbstractSet.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/AbstractSet.java`
  （`hashCode` 为元素 hashCode 之和；`removeAll` 当 this.size() <= other.size() 迭代 this，
  否则迭代 other）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/AbstractSet`。

## 4. 测试用例设计（映射到 AbstractSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | equals/hashCode 顺序无关 + hashCode 求和 | testEqualsHashCode |
| P0 | removeAll 大小分支优化 | testRemoveAll |
| P0 | 基本 add/contains/remove/clear（含 null） | testBasicOps |

## 关键规范细节（易错，已锁入断言）

- `hashCode()` 等于**元素 hashCode 之和**（顺序无关）；空集合 hashCode 为 0。
- `equals` 跨 Set 实现：只要元素集合相同即相等（不要求同类）。
- `removeAll` 返回是否有变化；小者迭代保证正确移除交集。
- `add` 重复元素返回 false（Set 契约）。

## 注意事项

- 无 GC/图形/网络/文件依赖；用 contains 而非迭代顺序断言。

---

## 契约深化增补（2026-11-05，只追加不改上文）

### 新增测试方法
- `RecSet`（记录 iterator() 创建数与 remove(Object) 委托数的探针）→
  `testRemoveAllOptimization`：**this>arg 时骨架迭代参数、对参数每个元素调 this.remove**
  （委托数==arg.size、this.iterator() 从未创建）；**this<=arg 时走 this 迭代器删**
  （this.remove 零调用、迭代器创建）；等大小走迭代器分支；不相交/空参/空 this 全返回 false。
- `testCrossImplEquals` — 探针与 HashSet/LinkedHashSet/TreeSet 同内容互等（对称），
  hashCode 相等且==Σ元素hash（97+98 锁定）；null 元素贡献 0；空集 0 且互等；Set 永不 equals List。
- `testInheritedOps` — retainAll（继承自 AbstractCollection，走迭代器、从不调 this.remove）、
  toArray 复用+null 终止、clear 经迭代器、contains 扫描。

### 负例精确异常表（本类无新增抛异常路径；行为断言为主）
| 操作 | 行为 |
|---|---|
| removeAll(空参/不相交/空 this) | false，无元素变化 |
| retainAll 经迭代器路径 | this.remove(Object) 不被调用（计数=0） |

### 关键规范细节（易错，已锁入断言）
- AbstractSet.removeAll 的 size 分支选择以 `size() > c.size()` 为界（**等大小时走迭代器分支**）。
- hashCode = Σ元素hash；equals = size 相等且互相 containsAll（跨实现成立）。

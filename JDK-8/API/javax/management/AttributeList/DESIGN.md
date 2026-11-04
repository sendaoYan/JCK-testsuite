# javax.management.AttributeList 黑盒行为测试设计

## 1. API 功能
- 继承 `ArrayList<Object>`。
- 方法：`add(Attribute)`、`add(int, Attribute)`、`set(int, Attribute)`、`addAll(AttributeList)`、`asList()`、`get(int)`。

## 2. Java 用户如何使用
MBeanServer 的 `getAttributes`/`setAttributes` 返回/接收 AttributeList，用于批量读写属性。
```java
AttributeList al = mbs.getAttributes(on, new String[]{"Count", "Name"});
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/AttributeListTypeSafeTest.java`：类型安全的 add/set。
- `test/jdk/javax/management/MBeanServer/MBeanTest.java`：批量属性读写。
- 实现权威：`src/java.management/share/classes/javax/management/AttributeList.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：add(Attribute)/add(int,Attribute)/set(int,Attribute) 生效；asList 返回 List<Attribute>；get 返回对应对象；继承 ArrayList 的 size/clear 行为；addAll(AttributeList) 合并。
- 白盒：public 类、父类 ArrayList、含 asList。

## 深化（2026-11）
追加维度：构造矩阵（含 taint 语义）、typed/untyped 重载解析、ROE 包装 vs 裸 IOOBE、null 元素通道、asList() 活视图与写通道、iterator remove/fail-fast、负容量、serialVersionUID、序列化复活。
- typed 重载（add(int,Attribute)/set(int,Attribute)/addAll(int,AttributeList)）越界 → `RuntimeOperationsException`(包装 IndexOutOfBoundsException)；untyped `add(int,Object)` 越界 → 裸 `IndexOutOfBoundsException`。
- 负容量构造 → `IllegalArgumentException`；`new AttributeList((List<Attribute>) null)` → `IllegalArgumentException("Null parameter")`。
- `asList()` 是活视图（双向写通）；null 元素在所有通道合法。
- serialVersionUID = `-4077085769279709076L`（两侧同值）。
- **版本差异（本目录核心）**：非 Attribute 元素的处置——
  - JDK-8（`adding()` 先查 typeSafe 标志）：List 构造器与全部 untyped 写通道**接受**非 Attribute 元素（仅置 tainted），**延迟**到 `asList()` 才抛 `IllegalArgumentException`；
  - JDK-27（`adding()` 无条件抛）：List 构造器与全部 untyped 写通道**立即**抛 `IllegalArgumentException("Not an Attribute: ...")`，且元素检查先于越界检查（`add(5,(Object)"x")` 抛 IAE 而非 IOOBE）。
  - 两侧各自断言各自真实行为；`asList()` 化的类型安全拒绝语义两侧一致。

## 深化（2026-11）版本差异记录（本目录实测，JDK-8u372 vs JDK-27.0.1）
- 两侧 public API（javap -public）、serialVersionUID、异常类型与消息全部一致（见上文逐项）。
- 与本目录相关的全局版本差异：
  - `MBeanServer.registerMBean(null, name)`：JDK-8 抛 `NullPointerException`；JDK-27 抛 `RuntimeOperationsException`(包装 IllegalArgumentException)。
  - `javax.management.loading.MLet`：JDK-27 已移除（JDK-8 存在）。

# java.util.Objects 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。重点补齐 requireNonNull 的 Supplier 变体（null supplier /
> supplier 返回 null 消息 / 非 null 时短路）、compare 的 null 矩阵与 null comparator、deepEquals
> 的嵌套/多维/数组-vs-标量、hash 的 null 元素与 null varargs 数组、toString 的 toString()-返回-null 边界。

## 1. API 功能

- **null 校验**：`requireNonNull(T)`、`requireNonNull(T,String)`、`requireNonNull(T,Supplier<String>)`。
- **相等**：`equals(Object,Object)`、`deepEquals(Object,Object)`。
- **哈希**：`hashCode(Object)`、`hash(Object...)`。
- **字符串**：`toString(Object)`、`toString(Object,String)`。
- **比较**：`compare(T,T,Comparator)`。
- **判空**：`isNull(Object)`、`nonNull(Object)`。

## 2. 使用方式

参数校验 `Objects.requireNonNull(x, () -> "msg")`；空安全比较 `Objects.equals(a,b)`；
字段哈希 `Objects.hash(a,b,c)`；默认值 `Objects.toString(o, "dflt")`；
比较 `Objects.compare(a, b, cmp)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Objects`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Objects.java`：
  `compare` 用 `(a==b)?0:c.compare(a,b)` 短路；`requireNonNull(obj,supplier)` 在 obj null 时
  `new NullPointerException(messageSupplier.get())`；`toString(o)=String.valueOf(o)`；
  `hash(values)=Arrays.hashCode(values)`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | requireNonNull 三变体 + 消息携带 + null 消息 | testRequireNonNull、testRequireNonNullSupplier |
| P0 | equals 四组合 | testEquals |
| P0 | deepEquals：嵌套/多维/数组-vs-标量/int[]-vs-Integer[]/null 元素 | testDeepEquals |
| P0 | hash/hashCode：null 元素、null varargs 数组、hash()==1 | testHashCode |
| P0 | toString：null 默认、toString()-返回-null 边界 | testToString |
| P0 | compare：null 矩阵、null comparator、相同引用短路、反对称 | testCompare |
| P0 | isNull/nonNull 两态 | testIsNull |

## 关键规范细节

- `requireNonNull(obj, supplier)`：obj 非 null 时 supplier **不调用**；obj null 且 supplier null → NPE；
  supplier 返回 null → NPE 且 message 为 null。
- `compare(a,b,c)`：`a==b`（含 null==null）返回 0 **不调 c**；否则 `c.compare(a,b)`（a 或 b 为 null 时
  是否 NPE 取决于 comparator）；`c==null` 且 `a!=b` → NPE。
- `deepEquals`：int[] vs Integer[]（值相同）返回 false（运行时数组类型不同）；数组 vs 非数组返回 false。
- `hash((Object[]) null)` == 0（`Arrays.hashCode(null)` 返回 0）；`hash(Object...)` 含 null 元素等价 `Arrays.hashCode`。
- `toString(o)` 委托 `String.valueOf`：对象 `toString()` 返回 null 时结果也是 null（不是 "null"）。

## 注意事项

确定性；无墙钟/随机/全局副作用；headless-safe 不加 `@key`。

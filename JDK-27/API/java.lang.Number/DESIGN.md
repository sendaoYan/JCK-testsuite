# java.lang.Number 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **抽象类**：`Number` 是所有数值包装类型（`Integer/Long/Short/Byte/Float/Double`、`BigInteger/BigDecimal`、`AtomicInteger/AtomicLong`）的抽象超类。
- **抽象访问器**：`intValue()`、`longValue()`、`floatValue()`、`doubleValue()`。
- **具体默认**：`byteValue()`（= `(byte) intValue()`）、`shortValue()`（= `(short) intValue()`），JLS 5.1.3 缩窄语义。
- **对象**：继承 `Object.equals/hashCode/toString`（由子类覆写）。

## 2. Java 用户如何使用

- **构造/工厂**：不能直接 `new Number()`；通过任一子类实例 `Number n = Integer.valueOf(1)` 或 `new AtomicLong(1)` 获得多态引用。
- **典型调用**：跨类型读原始值 `n.intValue()/longValue()/doubleValue()`；NaN/Infinity 缩窄饱和 `(int)NaN==0`、`(int)+Inf==Integer.MAX_VALUE`、`(int)-Inf==Integer.MIN_VALUE`。
- **真实组合链路**：`Long.valueOf(0x1234567890abcdefL) → intValue 截断高半 → byteValue 取低字节`；`Double.valueOf(NaN/±Inf) → intValue/longValue/byteValue/shortValue` 饱和。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `java.lang.Integer/Long/Float/Double` 均 `extends Number` 并覆写六个访问器（缩窄与宽化语义在此定义）。
  - `java.util.concurrent.atomic.AtomicInteger/AtomicLong` 亦 `extends Number`，使并发计数器可当作 `Number` 统一读取。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Number/`、`test/jdk/java/lang/Integer/` 覆盖缩窄与 NaN/Infinity 饱和。
- **Guava**（`com.google.common.math`、`com.google.common.primitives`）：大量 API 接受/返回 `Number` 或 `Number` 子类做宽化/缩窄（如 `Ints.saturatedCast(long)` 等价于 JLS 5.1.3 饱和）。
- **Spring Framework**（`NumberUtils.convertNumberToTargetClass`）：把 `Number` 从一种具体子类转换到目标子类，正是 `Number` 多态访问器的典型消费方。

## 4. 测试用例设计 + 自动化实现（→ NumberTest.java）

- **正常**：`Number` 抽象性 + `Object` 超类；`Integer/Long/Short/Byte/Float/Double/BigInteger/BigDecimal/AtomicInteger/AtomicLong` 都是 `Number`。
- **组合**：`Integer(300) → intValue/longValue/floatValue/doubleValue/byteValue/shortValue`；`Long(0x1234567890abcdef) → intValue 截断高半`；`AtomicInteger/AtomicLong` 缩窄；`Double(3.99/-3.99) → intValue` 向零截断。
- **边界**：`Double` NaN/±Inf 缩窄饱和（NaN→0、+Inf→MAX、-Inf→MIN）；`Float` NaN/±Inf 同样饱和。
- **异常**：无（Number 访问器不抛异常）。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；截断/饱和结果确定；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。

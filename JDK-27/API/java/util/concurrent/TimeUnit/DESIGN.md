# java.util.concurrent.TimeUnit 设计案

## 1. API 功能
final 枚举，7 常量（NANOSECONDS..DAYS）。转换 `convert(long, TimeUnit)`/`convert(Duration)`/`toNanos/Micros/Millis/Seconds/Minutes/Hours/Days`；睡眠/等待 `sleep`/`timedJoin`/`timedWait`；`toChronoUnit()`/`of(ChronoUnit)`；`valueOf`/`values`。

## 2. Java 用户如何使用
- 转换：`TimeUnit.SECONDS.toMillis(2)`、`TimeUnit.MILLISECONDS.convert(Duration.ofSeconds(2))`。
- 阻塞：`TimeUnit.MILLISECONDS.sleep(100)`、`timedJoin(thread, timeout)`、`timedWait(lock, timeout)`。
- 枚举映射：`TimeUnit.HOURS.toChronoUnit()`、`TimeUnit.of(ChronoUnit.MILLIS)`。

## 3. 开源代码实际用法调研
- JDK 自用：`TimeUnit.timedWait/timedJoin/sleep` 委托 `Object.wait`/`Thread.join`/`Thread.sleep`；大量并发类以 TimeUnit 表达超时。
- JDK 自带测试：`test/jdk/java/util/concurrent/TimeUnit/`、`tck/TimeUnitTest.java`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：enum + extends Enum + 7 常量 + convert/toXxx/timedWait/timedJoin/sleep/toChronoUnit/of。
2. 黑盒：精确转换（含 Duration 截断）、ChronoUnit 映射、valueOf/values；sleep/timedJoin/timedWait 仅断「不早退」（下限宽松，不断精确耗时）。

实现：`TimeUnitTest.java`（34 断言）。

### 本轮契约深化追加

- values() 顺序（ordinal 0..6）、name==toString、valueOf 往返、compareTo 序、valueOf("BOGUS"/null) 负例（`testValuesOrderAndNames`）。
- **7×7 换算矩阵（每单位 toX(1) 精确值表）**、MINUTES.toSeconds(90)==5400、convert 双向、截断朝零 4 例（`testFullConversionMatrix`）。
- **饱和矩阵**：4 单位 toNanos(MAX/MIN)、多路跨级饱和；不溢出路径精确（SECONDS.toMinutes(MAX)==153722867280912930、NANOS.toMillis(MAX)==9223372036854）（`testSaturation`）。
- **ChronoUnit 全表双向映射 + of(WEEKS) IAE + convert(Duration) 截断/负数/饱和**（`testChronoUnitFullMapping`）。

断言规模：PASS (190 assertions)。

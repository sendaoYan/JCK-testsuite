# java.util.concurrent.TimeUnit 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **枚举常量（7）**：NANOSECONDS/MICROSECONDS/MILLISECONDS/SECONDS/MINUTES/HOURS/DAYS。
- **转换**：`convert(long, TimeUnit)`、`toNanos`/`toMicros`/`toMillis`/`toSeconds`/`toMinutes`/`toHours`/`toDays`。
- **时间等待**：`timedWait(Object, long)`、`timedJoin(Thread, long)`、`sleep(long)`。
- **枚举**：`values()`、`valueOf(String)`。

## 2. Java 用户如何使用

- 单位换算：`TimeUnit.SECONDS.toMillis(2)`。
- 超时参数：`future.get(5, TimeUnit.SECONDS)`、`lock.tryLock(1, TimeUnit.SECONDS)`。
- `sleep`/`timedJoin`/`timedWait` 做有界等待。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/TimeUnit.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（TimeUnit 回归）。
- JCK runtime-8b `api/java_util_concurrent/TimeUnit`。

## 4. 测试用例设计（映射到 TimeUnitTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 各 toXxx 换算；convert 正向/逆向 | testConvert |
| P0 | valueOf/values | testConvert |
| P0 | 溢出饱和（MAX/MIN 不环绕） | testBlackBox |
| P0 | 负值截断朝零 | testBlackBox |
| P0 | valueOf 非法字符串抛 IAE | testBlackBox |
| P0 | 各常量自身单位 convert 恒等 | testBlackBox |
| P0 | sleep 不早退（≥50ms 下界） | testSleep |
| P0 | timedJoin 超时/不早退 | testTimedJoin |
| P0 | timedWait 不早退 | testTimedWait |
| P1 | 白盒：enum/7 常量/关键方法 | testWhiteBox |

## 关键规范细节

- `convert` 溢出饱和（返回 Long.MAX/MIN），不是环绕。
- 负值换算朝零截断。
- JDK-8 无 `toChronoUnit`/`of(ChronoUnit)`/`convert(Duration)`（JDK 9+），不移植。

## 5. 契约深化追加（本轮）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | values() 顺序（ordinal 0..6）、name==toString、valueOf 往返、compareTo 序、valueOf("BOGUS")/null 负例 | testValuesOrderAndNames |
| P0 | **7×7 换算矩阵（每单位 toX(1) 精确值表）** + MINUTES.toSeconds(90)==5400、DAYS.toMillis(1)==86400000 | testFullConversionMatrix |
| P0 | convert 双向 + 截断朝零（NANOS.toSeconds(999999999)==0 等 4 例） | testFullConversionMatrix |
| P0 | **饱和矩阵：SECONDS/MINUTES/HOURS/DAYS 的 toNanos(MAX)==MAX、toNanos(MIN)==MIN、多路跨级饱和**；不溢出路径精确（SECONDS.toMinutes(MAX)==153722867280912930） | testSaturation |
| P1 | JDK-9+ 的 convert(Duration)/toChronoUnit/of(ChronoUnit) 缺席反射断言 | testAbsentJdk9Methods |

断言规模：PASS (167 assertions)。

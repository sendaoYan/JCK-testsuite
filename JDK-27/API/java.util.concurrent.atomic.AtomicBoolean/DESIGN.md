# java.util.concurrent.atomic.AtomicBoolean 测试设计

## 1. API 功能
- 构造器：`AtomicBoolean()`（初始 false）、`AtomicBoolean(boolean)`。
- 读/写：`get()`、`set(boolean)`、`lazySet(boolean)`。
- 原子更新：`getAndSet(boolean)`、`compareAndSet(expect, update)`、`weakCompareAndSet`、
  `weakCompareAndSetPlain`、`weakCompareAndSetVolatile/Acquire/Release`、`compareAndExchange*`。
- 内存序访问器：`getPlain/setPlain`、`getOpaque/setOpaque`、`getAcquire/setRelease`。
- 转换：`toString()`（`Boolean.toString(get())`）。
- 标记接口：`Serializable`。

## 2. Java 用户如何使用
- 一次性初始化标记：`AtomicBoolean initialized = new AtomicBoolean(); if (initialized.compareAndSet(false, true)) { init(); }`
- 开关切换：`flag.set(true)` / `flag.getAndSet(false)`。
- 条件更新失败不改值：`if (!flag.compareAndSet(false, true)) { /* 已被他人设置 */ }`。
- 组合链路：`compareAndSet` 成功 → `get()` 读新值 → `getAndSet` 取旧值。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/io/ObjectInputFilter.java`（jdk25u-dev）：`private static final AtomicBoolean
  filterFactoryNoReplace = new AtomicBoolean();`，用于一次性初始化防替换标记。
- JDK 源码 `java/net/InetAddress.java`：`AtomicLong seq` 用于生成序号（同族原子类，语义一致）。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java` 与 `Lazy.java`：
  验证 weakCompareAndSet/lazySet 语义的参照用例。
- 广泛用法（Guava/Netty 等）：`AtomicBoolean` 常作生命周期开关 `closed.compareAndSet(false, true)`。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicBooleanTest.testBlackBox()`）：
  - 构造默认 false、构造 true、set/get、getAndSet 返回旧值。
  - compareAndSet 成功/失败且失败不改值。
  - weakCompareAndSet 失败确定性返回 false；成功用有界重试（无竞争必成功）。
  - 各内存序访问器 setPlain/getPlain、setOpaque/getOpaque、setRelease/getAcquire。
  - compareAndExchange 返回见证值（成功返回旧值、失败返回当前值）。
  - 并发：8 线程对同一 `compareAndSet(false,true)`，恰好一个赢家（AtomicInteger 计数 ==1）。

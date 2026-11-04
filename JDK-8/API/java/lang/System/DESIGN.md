# java.lang.System 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「println 读一下」的冒烟。重点补：标准流非空、arraycopy
> （全量/部分/同数组重叠右移/左移/长度 0/引用数组 Object[]→String[]/null NPE/越界 IOOBE/元素类型不匹配
> 与 int[]→long[] 的 ASE）、properties（getProperties/getProperty 带缺省/setProperty 返回旧值/
> clearProperty 返回旧值）、环境变量 map 与缺失 null、identityHashCode（稳定/默认 hashCode/ null==0）、
> lineSeparator（与 line.separator 属性一致）、时间 API（currentTimeMillis 合理范围、nanoTime 非负单调）、
> gc/runFinalization 可调用。断言确定，headless 安全，不调用 exit/halt。

## 1. API 提供了哪些功能

- **类**：`java.lang.System`（final、无公开构造器，全是 static 方法）。
- **标准流**：`in`/`out`/`err`。
- **数组**：`arraycopy`（native，处理重叠与类型检查）。
- **属性**：`getProperties`/`getProperty`/`setProperty`/`clearProperty`/`getenv`（map 与单键）。
- **标识/其他**：`identityHashCode`/`lineSeparator`/`gc`/`runFinalization`。
- **时间**：`currentTimeMillis`/`nanoTime`。
- **JDK-8 无**（Java 9+）：`getLogger`/`Logger` 家族（`System.Logger`）。

## 2. Java 用户如何使用

- `System.arraycopy(src, i, dst, j, n)` 拷贝数组（支持同数组重叠）。
- `System.setProperty/clearProperty` 管理 JVM 属性；`System.currentTimeMillis/nanoTime` 计时。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/System.html`）：arraycopy 类型检查、identityHashCode(null)==0、
  lineSeparator 与 line.separator 属性一致。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/System.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：arraycopy int[]→long[] 抛 ArrayStoreException（不拓宽）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/System`。

## 4. 测试用例设计（映射到 SystemTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 标准流非空 | testStreams |
| P0 | arraycopy：全量/部分/重叠右移/左移/长度 0/引用数组/null NPE/越界 IOOBE/类型 ASE | testArraycopy |
| P0 | properties：getProperties/带缺省 getProperty/setProperty 旧值/clearProperty | testProperties |
| P0 | 环境变量：getenv() 非空、缺失 null | testEnv |
| P0 | identityHashCode：稳定/默认 hashCode/null==0 | testIdentityHashCode |
| P0 | lineSeparator 与 line.separator 一致 | testLineSeparator |
| P0 | currentTimeMillis 合理范围、nanoTime 非负单调 | testTime |
| P1 | gc/runFinalization 可调用 | testGC |

## 关键规范细节（易错，已锁入断言）

- **`arraycopy` 元素类型不匹配抛 `ArrayStoreException`**；`int[] → long[]` 不拓宽（仍 ASE）。
- **`arraycopy` 支持同数组重叠**（先拷贝尾部再覆盖，右移/左移结果确定）。
- **`identityHashCode(null)==0`**；`identityHashCode(o)==o.hashCode()`（当 hash code 未被覆写）。
- **`lineSeparator()` 等于 `System.getProperty("line.separator")`**。
- **`setProperty` 首次返回 null、之后返回旧值；`clearProperty` 返回旧值**。
- **`nanoTime` 非负且单调不减**（同一次调用序列内）。

## 注意事项

- 不调用 exit/halt/System.exit；properties 测试用专属键并在末尾 clear 清理，不改全局其它键。
- headless 安全，不加 `@key`；currentTimeMillis 用宽松范围断言（避免墙钟依赖）。

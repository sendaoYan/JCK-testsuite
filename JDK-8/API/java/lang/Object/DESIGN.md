# java.lang.Object 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 个 Object」的冒烟。重点补：白盒结构（无超类、具体类、getClass/
> notify/notifyAll/wait 为 final、hashCode native）、getClass 运行时类、默认 hashCode==identityHashCode、
> equals 自反/对称/null、toString 格式 `className@hexHash`、clone 的 Cloneable 契约 + 浅拷贝 +
> 非 Cloneable 抛 CloneNotSupportedException、wait/notify/notifyAll 监视器协议（未持锁抛 IMSE、
> notify 唤醒、wait(timeout) 超时返回）、protected finalize 可达且 no-op。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Object` 是所有类的根（无超类），具体类（非抽象）。
- **方法**：`getClass`（final）、`hashCode`（native，默认恒等）、`equals`（默认引用相等）、
  `toString`、`clone`（protected）、`finalize`（protected，deprecated）、
  `wait`(×3)/`notify`/`notifyAll`（监视器协议，均 final）。

## 2. Java 用户如何使用

- 覆写 `equals/hashCode/toString` 提供值语义；`clone` 要求类实现 `Cloneable`。
- `synchronized(lock) { lock.wait(); }` 等待通知，`lock.notify()/notifyAll()` 唤醒；
  未持有监视器调用 → `IllegalMonitorStateException`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Object.html`）：各方法契约（clone 需 Cloneable、
  wait 需持锁、hashCode 与 equals 关系）。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Object.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Object` + `Clone`。

## 4. 测试用例设计（映射到 ObjectTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：无超类、具体、getClass/notify/notifyAll/wait final、hashCode native | testWhiteBox |
| P0 | getClass 返回运行时类 | testGetClass |
| P0 | 默认 hashCode==identityHashCode、equals 自反/对称/null | testHashCodeEquals |
| P1 | toString == className@hexHash | testToString |
| P0 | clone：Cloneable 契约 + 浅拷贝 + 非 Cloneable→CNSE | testClone |
| P0 | wait/notify/notifyAll 未持锁→IMSE | testMonitorErrors |
| P0 | wait/notify 唤醒（daemon 子线程 + 有界 join） | testWaitNotify |
| P1 | wait(timeout) 超时返回 | testWaitTimeout |
| P1 | protected finalize 可达且 no-op | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **默认 `hashCode()` == `System.identityHashCode(this)`**（Object 未覆写时）。
- **默认 `equals` 是引用相等**：自反、对称、`equals(null)==false`。
- **`clone` 是浅拷贝**：引用字段共享；仅实现 `Cloneable` 才可调用，否则抛
  `CloneNotSupportedException`。
- **`wait`/`notify`/`notifyAll` 未持锁抛 `IllegalMonitorStateException`**；`getClass`/`wait`/
  `notify`/`notifyAll` 均为 final。
- **`toString` == `getClass().getName() + "@" + Integer.toHexString(hashCode())`**。

## 注意事项

- wait/notify 测试用 daemon 子线程 + 有界 join，不依赖 sleep 时序；`wait(timeout)` 用下界断言。
- headless 安全，不加 `@key`；不调用 `finalize` 触发 GC（仅直接调 protected 方法证明可达）。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 白盒结构面：恰 9 个公共方法（getClass/hashCode/equals/toString/notify/notifyAll/wait×3）；
  clone/finalize 为 protected（不入公共面）；equals/hashCode/toString/clone 均可覆写（非 final）；
  Object 非终态、唯一 public 无参构造器。
- finalize 弃用状态：JDK-8 **未**标注 @Deprecated（JDK-27 为 @Deprecated(since=9, forRemoval=true)，
  测试用 MODERN 分支断言各自实际）。
- wait 参数校验顺序：**负超时/nanos 越界的 IAE 先于监视器检查的 IMSE**（未持锁 + wait(-1) 得
  IAE 而非 IMSE——实测双侧一致）；wait(0,0) 等价 wait(0) 无限等待，测试绝不调用。
- notify 语义：恰唤醒一个等待者（双等待者实测 awakeCount==1）；notifyAll 唤醒全部；
  wait 释放监视器（等待者 TIMED_WAITING 时主线程可入同步块）；中断等待 → InterruptedException
  且中断状态被清空。

### 2. 使用方式（新增）
- equals/hashCode 值语义契约以自造 Pair 全矩阵验证（自反/对称/传递/null/异型/一致性 +
  HashMap/HashSet 联动：equal-but-distinct key 命中、size 不涨、containsKey 走 equals）。
- identityHashCode(null)==0（文档化）；未覆写 hashCode == identityHashCode；toString 后缀
  Integer.toHexString(hashCode) 逐字一致。

### 3. 参考来源
- Oracle Java SE 8 javadoc Object.wait(long,int)（nanos ∈ [0,999999]、millis<0 消息
  "timeout value is negative"、nanos 越界消息 "nanosecond timeout value out of range"）。
- JLS §17.2 wait/notify 语义；§6.6.2 protected 访问。
- OpenJDK 8u-dev Object.wait(long,int) 源码（参数检查在前）。

### 4. 测试用例设计 + 易错规范细节（新增方法）
| 测试点 | 用例方法 |
|---|---|
| 9 公共方法名集 / clone+finalize protected / 可覆写性 / 唯一构造器 | testWhiteBoxStructure |
| finalize 弃用（8 无 / 27 since=9 forRemoval=true，反射读 since/forRemoval） | testFinalizeDeprecation |
| Pair equals 契约矩阵 + HashMap/HashSet 联动 | testEqualsContractValueSemantics |
| identityHashCode(null)==0、稳定、默认==identity | testHashCodeContract |
| getClass() == 身份（两实例/数组长度无关/Class.class/二进制名 $） | testGetClassIdentity |
| toString 形（binaryName@hex 精确、[I@/[J@/[Ljava.lang.String;@ 前缀） | testToStringFormats |
| 未持锁四形态 IMSE + wait(-1) IAE 优先 + 精确消息 | testMonitorErrorsExtended |
| 持锁 wait 参数表（-1/0,-1/1,-1/1,1000000 IAE；0,1/0,999999/1,0 OK） | testWaitArgumentValidation |
| notify 恰一唤醒 + notifyAll 收尾 | testNotifySelectsExactlyOne |
| notifyAll 唤醒全部 3 等待者 | testNotifyAllWakesAll |
| wait 释放监视器（TIMED_WAITING + holdsLock 真值表） | testWaitReleasesLock |
| 中断等待：InterruptedException + 状态清空 | testInterruptDuringWait |
| clone 细节：CNSE 消息==二进制名、三实例互异 | testCloneDetails |
| 任意对象可作监视器 + holdsLock 重入真值 | testAnyObjectAsMonitor |
| 包装类/数组 equals 事实（数组同一性、Integer≠Long） | testEqualsOnWrappersAndArrays |

易错点：
- **wait(0,0) 无限等待**——测试只能用 nanos>0 的 0,1 / 0,999999 与 1,0 等快速返回组合。
- **数组 getClass() 两两 !=** 需先赋给 Class<?>（捕获类型捕获不可直接 !=）。
- **String 常量池当锁**危险（共享），仅自持本地验证。

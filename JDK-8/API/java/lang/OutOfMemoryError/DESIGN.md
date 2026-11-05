# OutOfMemoryError — 设计方案

## 1. API 功能
`java.lang.OutOfMemoryError` 继承自 `VirtualMachineError`，表示堆/GC 无法分配对象（"Thrown when the Java Virtual Machine cannot allocate an object because it is out of memory"）。
JDK-8 javap 构造器面（仅 2 个 public）：`()`、`(String)`——**无 cause 形态构造器**，cause 只能经 `initCause` 补挂。
Serializable（经 Error），`serialVersionUID = 8228564086184010517L`（ObjectStreamClass.lookup 实测锁定）。
继承 Throwable 通用契约。

## 2. Java 用户如何使用
JVM 抛出（常见 message `"Java heap space"`/`"GC overhead limit exceeded"`）；用户侧 catch 后做最小诊断并释放引用：
```java
catch (OutOfMemoryError oom) { log(oom.getMessage()); refs.clear(); }
```

## 3. 开源代码实际用法调研
JDK 内部常在分配点抛出；框架（Netty、缓存库）用它做堆保护分流。无自有成员方法（javap 确认）。

## 4. 测试用例设计 + 易错规范细节
- 白盒：恰 2 构造器且**断言不存在 cause 形态**；直系父 VirtualMachineError；suid 锁定。
- 构造链/cause/suppressed/栈迹/printStackTrace/序列化：同家族模板全矩阵；`initCause(null)` 合法且消耗一次写。
- 与 StackOverflowError 分界（轻路径）：`"Java heap space"` 预设消息透传 + toString 全等；兄弟互不 isInstance——**兄弟类间 `x instanceof Sibling` 是编译期不可转换错误，须用 `Class.isInstance` 表达**；共享父柄但 catch(StackOverflowError) 永不捕获 OOM。
- **真耗尽触发明确放弃**：宿机 1.5TB 内存，默认堆受控耗尽时长不可控（速度红线），按轻路径锁构造/消息/分界契约；DESIGN 记录该取舍。

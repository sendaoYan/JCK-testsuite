# ThreadGroup — 设计方案

## 1. API 功能
`java.lang.ThreadGroup` 组织线程为树状层级，实现 `Thread.UncaughtExceptionHandler`：
- 构造：`ThreadGroup(String)`、`ThreadGroup(ThreadGroup, String)`
- 查询：`getName/getParent/getMaxPriority/isDaemon/isDestroyed/parentOf`
- 管理：`setDaemon/setMaxPriority/interrupt/destroy/list`
- 枚举：`activeCount/enumerate(Thread[])/activeGroupCount/enumerate(ThreadGroup[])`

## 2. Java 用户如何使用
- 把相关线程放入同一组统一 `interrupt()` 或设优先级上限。
```java
ThreadGroup g = new ThreadGroup("workers");
g.setMaxPriority(Thread.NORM_PRIORITY);
new Thread(g, () -> work(), "w1").start();
int n = g.enumerate(arr, true);   // 列出组内活动线程
```

## 3. 开源代码实际用法调研
- 早期 servlet 容器/JVM 用 `ThreadGroup` 分组管理请求线程（现代多被 `ExecutorService` 取代）。
- JDK `test/jdk/java/lang/ThreadGroup` 验证 `destroy`、`enumerate`、优先级继承。
- `Thread.getThreadGroup()` 仍返回当前线程所属组。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ThreadGroup")` 验证父类 `Object`、实现 `Thread.UncaughtExceptionHandler`。
黑盒：当前组属性；父子链接与 `parentOf`；`setMaxPriority`；`enumerate` 列出已启动 daemon 线程；`activeGroupCount`/`list`/`toString` 不抛；`destroy` 为 no-op、`isDestroyed` 恒 false（JDK 16+ 废弃语义）。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- 与 8 侧同套 maxPriority 钳制/enumerate 精确/uncaught 路由/interrupt 选择性/toString 闭环（29→151 断言）。
- **27 独有：destroy() 为 no-op**——不抛 ITSE（即使组非空）、isDestroyed() 恒 false、
  "销毁"后 new Thread/new ThreadGroup 均合法、activeCount/enumerate 继续可用。
- **27 已移除成员**：suspend/resume/stop/allowThreadSuspension getDeclaredMethod→NoSuchMethodException。
- null 名存 null、null 父 NPE（消息文本两侧不同仅锁类型）与 8 一致。

### 使用方式
同 8 侧 latch + 有界轮询；destroy/isDestroyed 带 deprecation 警告（forRemoval）但可编译可调用。

### 参考来源
JDK-27 javadoc（ThreadGroup.destroy 空实现）；探针对照 8/27 全表。

### 测试用例设计 + 易错规范细节
- 新增 testDestroyNoOp、testRemovedMembersAbsent，其余与 8 侧同名方法对齐。
- 易错点：**27 上不要断言任何 destroy 副作用**；弃用方法反射检查方向相反（8 存在/27 缺失）。

- 易错点（jtreg 环境坑）：**default-handler 分支在根组的 ThreadGroup.uncaughtException 里**；jtreg 的
  MainThreadGroup 覆写会拦截组链上的死异常并判负。default-routing 的 worker 锚定真实根组
  （getParent() 链走到头）后行为与独立运行一致。RecordingGroup/CountingGroup 覆写后不再向上委托，安全。

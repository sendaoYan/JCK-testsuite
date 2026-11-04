# java.lang.reflect.InaccessibleObjectException — 测试设计（JDK-27）

## 1. API 功能
`InaccessibleObjectException extends RuntimeException`（非受检，JDK-9+）：
- 构造器：无参、`(String message)`。
- 触发：`setAccessible(true)` 命中模块系统未 open 的包时抛出；`trySetAccessible()` 同因返回 false 不抛。

## 2. Java 用户如何使用
框架打开私有成员前应优先 `trySetAccessible()` 探测；catch IOE 后降级或要求 `--add-opens`。消息指明拒绝模块与包（`module java.base does not "opens java.lang" to unnamed module`）。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/AccessibleObject/` 与模块化测试（`test/hotspot` 邻接）：未 open 包的拒绝路径。
- Spring `ReflectionUtils.makeAccessible` 的 JDK-9 兼容处理与文档建议 `--add-opens java.base/java.lang=ALL-UNNAMED`（同构场景 1）。
- Lombok/Objenesis 遇 IOE 的降级策略（Field setAccessible 失败 → trySetAccessible）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`InaccessibleObjectExceptionTest.testBlackBox`）：
1. 真实触发：`String.class.getDeclaredField("value").setAccessible(true)` → IOE（消息含 "java.base"）；`trySetAccessible()` false 不抛。
2. JDK 私有方法同样拒绝（`Class.getConstantPool`）。
3. 自有类成员从不抛：setAccessible/trySetAccessible 正常。
4. public 成员开关操作无异常（对照）。
5. 构造器面：裸构造 null 消息/cause、消息构造往返、toString 前缀、initCause、按 RuntimeException 捕获。
白盒保留原有层次断言。产出：`InaccessibleObjectExceptionTest.java`（16 断言通过）。

## 深化批次（2026-11-05）
新增 Part 3 三个 test 方法（表面经 javap+探针在 JDK 27.0.1 实测）：
1. `testConstructorAndThrowable`：**公开构造器恰为 () 与 (String)——不存在 (String,Throwable) 重载**（遍历 getConstructors 参数计数实证）；instanceof Serializable；toString 全等 "java.lang.reflect.InaccessibleObjectException: denied"；初始 getSuppressed() 空、addSuppressed 后精确同一元素；initCause 一次成功、二次 → IllegalStateException。
2. `testPublicMembersNeverRefused`：对照面——无条件导出包（java.lang）的 public 成员 setAccessible(true) 永不抛 IOE（String(String) ctor、Integer.valueOf、System.out），canAccess(null) true，关回 false 后 invoke 仍可用。
3. `testCatchHierarchy`：可按 RuntimeException/Exception 捕获同一实例；非 Error；直接父类是 RuntimeException。
产出：`InaccessibleObjectExceptionTest.java` 33 断言通过（旧 16）。

### 易错规范细节
- cause 只能经 initCause 接线（构造器不支持 cause 参数）；二次 initCause 抛 ISE 是 Throwable 通用契约在此类的体现。
- "public 成员 setAccessible OK / 私有成员 IOE" 的分界是**包是否无条件导出 + 成员是否需要深反射**，不是"JDK 内部类一律拒绝"。

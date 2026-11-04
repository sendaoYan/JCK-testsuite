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

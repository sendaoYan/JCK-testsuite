# javax.naming.spi.DirectoryManager — 测试设计

## 1. API 功能
- 继承 `NamingManager`，静态工具类（目录版对象/状态工厂调度）。
- `getObjectInstance(Object, Name, Context, Hashtable, Attributes)`。
- `getStateToBind(Object, Name, Context, Hashtable, Attributes)` 返回 `DirStateFactory.Result`。
- `getContinuationDirContext(CannotProceedException)` 返回 `DirContext`。

## 2. Java 用户如何使用
- 目录服务提供者调用 `DirectoryManager.getObjectInstance` 还原对象、`getStateToBind` 转换绑定状态、`getContinuationDirContext` 处理 `CannotProceedException` 续接。
- 无工厂时 `getObjectInstance` 返回原对象、`getStateToBind` 返回包装原对象/属性的 `Result`。
- 组合链路：`getStateToBind(obj, ..., attrs)` → `Result`；`getContinuationDirContext(cpe)` → 可 `close()` 的 `DirContext`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/spi/DirectoryManager.java`：`getObjectInstance` 委托 `NamingManagerHelper.getDirObjectInstance`；`getStateToBind` 无工厂时 `return new DirStateFactory.Result(obj, attrs)`；`getContinuationDirContext` 构造 `new ContinuationDirContext(cpe, env)`。
- `com/sun/jndi/toolkit/url/GenericURLDirContext.java` 使用 `DirectoryManager.getContinuationDirContext`。

## 4. 测试用例设计
黑盒断言（`DirectoryManagerTest.testBlackBox`）：
- `getObjectInstance` 对普通对象返回原对象、对 null 返回 null。
- `getStateToBind` 返回非空 `Result` 且对象/属性身份一致。
- `getContinuationDirContext` 返回非空 `DirContext` 且可 `close()`。
白盒断言（`testWhiteBox`）：public class、继承 `NamingManager`、三个方法存在。

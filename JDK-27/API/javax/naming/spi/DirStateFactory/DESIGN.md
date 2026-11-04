# javax.naming.spi.DirStateFactory — 测试设计

## 1. API 功能
- 继承 `StateFactory`。
- 目录状态工厂方法：`getStateToBind(Object, Name, Context, Hashtable, Attributes)` 返回 `DirStateFactory.Result`。
- 从 `StateFactory` 继承 4 参 `getStateToBind(Object, Name, Context, Hashtable)` 返回 `Object`。

## 2. Java 用户如何使用
- 绑定前把对象转为可存储状态；`DirectoryManager.getStateToBind` 若工厂是 `DirStateFactory` 调用 5 参方法（返回 `Result`），否则调用 4 参方法并把结果包成 `Result`。
- 组合链路：`getStateToBind(obj, name, ctx, env, attrs)` → 返回 `Result`（含对象 + 属性）→ 服务绑定。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/spi/DirectoryManager.getStateToBind`：`if (factory instanceof DirStateFactory) answer = ((DirStateFactory)factory).getStateToBind(obj, name, nameCtx, environment, attrs);` 否则用 `StateFactory.getStateToBind` 并把结果包为 `new DirStateFactory.Result(objanswer, attrs)`。

## 4. 测试用例设计
黑盒断言（`DirStateFactoryTest.testBlackBox`）：
- 匿名实现验证 5 参（返回 `Result`，对象/属性可断言）与继承的 4 参方法（返回 `Object`）各自独立可调用。
白盒断言（`testWhiteBox`）：public/interface、继承 `StateFactory`、两个重载精确签名。

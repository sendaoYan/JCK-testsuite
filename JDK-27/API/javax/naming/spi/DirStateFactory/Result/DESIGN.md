# javax.naming.spi.DirStateFactory.Result — 测试设计

## 1. API 功能
- 值类，持有绑定前状态转换结果。
- 构造器：`Result(Object obj, Attributes attrs)`。
- 查询：`getObject()`、`getAttributes()`。

## 2. Java 用户如何使用
- `DirStateFactory.getStateToBind` 返回 `new DirStateFactory.Result(obj, attrs)`。
- 服务侧读取 `getObject()`（转换后的对象）与 `getAttributes()`（附加属性）用于绑定。
- 组合链路：`new Result(obj, attrs)` → `getObject()` + `getAttributes()`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/spi/DirectoryManager.getStateToBind` 多处构造 `new DirStateFactory.Result(obj, attrs)`（无工厂时直接包装原对象/属性，有工厂时包装工厂结果）。
- `com/sun/jndi/toolkit/dir` 目录工具读取 `Result` 的对象与属性完成绑定。

## 4. 测试用例设计
黑盒断言（`ResultTest.testBlackBox`）：
- `getObject()/getAttributes()` 返回构造参数（身份一致）。
- 任意类型对象（如 `Integer`）按身份保留。
- `new Result(null, null)` 返回 null/null。
- 多个实例互不影响。
白盒断言（`testWhiteBox`）：public class、继承 `Object`、方法与公开构造器签名。

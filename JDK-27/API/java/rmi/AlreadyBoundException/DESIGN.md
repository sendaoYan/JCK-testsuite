# java.rmi.AlreadyBoundException 行为测试设计

## 1. API 功能
AlreadyBoundException 是 RMI 命名操作中的受检异常，直接继承自 `java.lang.Exception`（不是 RemoteException）。
- 构造器：`AlreadyBoundException()` 与 `AlreadyBoundException(String)`。

## 2. Java 用户如何使用
- `catch (AlreadyBoundException e)` 区分「名称已绑定/未绑定」等命名错误。
- `new AlreadyBoundException("name")` 携带具体名称信息。

## 3. 开源代码实际用法调研
- 类定义见 `src/java.rmi/share/classes/java/rmi/AlreadyBoundException.java`（`~/git/jdk25u-dev`）。
- `java.rmi.registry.Registry.bind/lookup/unbind` 的 throws 声明直接引用该异常，见 `src/java.rmi/share/classes/java/rmi/registry/Registry.java`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（AlreadyBoundExceptionTest.testBlackBox）覆盖：
- 默认构造：`getMessage()==null`、`getCause()==null`。
- String 构造：`getMessage()` 等于入参、`getCause()==null`。
白盒部分（testWhiteBox）验证继承 Exception、非 RemoteException、非抽象/非 final、两个构造器形态。

# javax.security.auth.callback.UnsupportedCallbackException 设计案

## 1. API 功能
- 类 `public class UnsupportedCallbackException extends Exception`。
- 构造器 `UnsupportedCallbackException(Callback callback)`：无 detail message。
- 构造器 `UnsupportedCallbackException(Callback callback, String msg)`：带 detail message。
- 查询：`Callback getCallback()`（返回构造时传入的未识别回调）。
- 语义：`CallbackHandler.handle` 无法识别某个 `Callback` 时抛出，`getCallback()` 返回该回调。

## 2. Java 用户如何使用
- handler 在 `handle` 中遇到不认识的 `Callback` 时：`throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback")`。
- 调用方捕获后可用 `getCallback()` 得知哪个回调不被支持，用 `getMessage()` 获取描述。
- 典型链路：`handle` 遍历 → `else { throw new UnsupportedCallbackException(callbacks[i]); }` → 捕获 → `e.getCallback()`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `src/java.naming/share/classes/com/sun/jndi/ldap/sasl/DefaultCallbackHandler.java`：`} else { throw new UnsupportedCallbackException(callbacks[i]); }`（单参构造）。
- `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java`：`throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");` 与 `throw new UnsupportedCallbackException(confirmation, "Unrecognized message type: " + messageType);`（双参构造）。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、extends Exception、含 `getCallback` 方法、两个构造器。
- 行为（`testBlackBox`）：单参构造后 `getCallback()` 返回同一实例且 `getMessage()` 为 null、`instanceof Exception` 成立；双参构造 `getMessage()` 正确且 `getCallback()` 仍指向回调；`null` 回调也能构造并返回 null（规范未禁止），覆盖主链路与边界。

# org.ietf.jgss.GSSException 测试设计

## 1. API 功能
GSSException 是 GSS-API 抛出的统一异常，携带 major（GSS-API 层）与 minor（机制层）状态码。
- 构造器：`GSSException(int majorCode)`、`GSSException(int majorCode, int minorCode, String minorString)`。
- 查询：`getMajor()`、`getMinor()`、`getMajorString()`、`getMinorString()`、`getMessage()`、`toString()`。
- 设置：`setMinor(int minorCode, String message)`。
- 常量：`BAD_BINDINGS=1` … `GAP_TOKEN=22` 共 22 个 major 码（1..22 连续）。

## 2. Java 用户如何使用
```java
try { ... } catch (GSSException e) {
    int major = e.getMajor();      // 判断 major 码
    String s = e.getMajorString(); // "Invalid name provided" 等
    int minor = e.getMinor();      // 机制层码，0 表示未设置
}
// 抛出方设置 minor：
throw new GSSException(GSSException.FAILURE, 3, "detail");
e.setMinor(7, "new");
```
组合链路：`new GSSException(major, minor, str) → getMajor/getMinor/getMajorString/getMinorString/getMessage → setMinor → 再次读取`。

## 3. 开源代码实际用法调研
- JDK 源码 `org/ietf/jgss/Oid.java`：`throw new GSSException(GSSException.FAILURE, "Improperly formatted ...")`（内部使用 package-private 的 `(int,String)` 构造器）。
- JDK 源码 `sun/security/jgss/krb5/*`：机制层以 `new GSSException(GSSException.BAD_NAME, minorCode, msg)` 抛出并附 KrbException 详情。
- `test/jdk/sun/security/krb5/auto/Context.java` 系列测试依赖 `GSSException` 的 major 码区分失败类型。
（来源：`~/git/jdk25u-dev`。）

## 4. 测试用例设计 + 自动化实现
黑盒断言（`GSSExceptionTest.testBlackBox()`）：
- 22 个常量值 = 1..22 顺序断言。
- `GSSException(int)`：major 正确、`getMajorString()` 精确文本（`BAD_NAME`→"Invalid name provided"、`FAILURE`→"Failure unspecified at GSS-API level"）、minor 默认 0、minorString 默认 null、`getMessage()` 在 minor==0 时等于 majorString。
- 非法 major（0/999）回退到 `FAILURE`。
- `GSSException(int,int,String)`：minor/字符串往返，`getMessage()` 含 "(Mechanism level: ...)"。
- `setMinor` 更新 minor 与 message。
- `toString()` 前缀 `GSSException: `。
白盒（`testWhiteBox()`）：public 类、继承 `java.lang.Exception`、两个 public 构造器 + 七个方法 + 22 个 `public static final int` 字段。

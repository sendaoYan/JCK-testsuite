# FunctionDescriptor — 设计方案

## 1. API 功能
`java.lang.foreign.FunctionDescriptor`（接口）描述下行调用函数签名：返回布局 + 参数布局列表。工厂 `of(ret, args...)`/`ofVoid(args...)`；操作 `appendArgumentLayouts`/`insertArgumentLayouts`/`changeReturnLayout`/`dropReturnLayout`；查询 `returnLayout()`/`argumentLayouts()`/`toMethodType()`。

## 2. Java 用户如何使用
```java
FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
MethodHandle mh = Linker.nativeLinker().downcallHandle(addr, fd);
// fd.toMethodType() == (long)int
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestDowncall` 用 `FunctionDescriptor.of`/`ofVoid` 声明 libc 函数签名。
- jextract 生成的下行调用桩大量组合 `appendArgumentLayouts`/`changeReturnLayout` 描述结构体参数。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 各操作协变返回 `FunctionDescriptor` + `toMethodType` 返回 `MethodType`。
黑盒：`of`/`ofVoid` 构造后 `returnLayout`/`argumentLayouts`/`toMethodType` 精确断言；append/insert/changeReturn/dropReturn 的组合链路及其 `toMethodType`。
对应 `FunctionDescriptorTest.java` 白盒 + 黑盒部分。

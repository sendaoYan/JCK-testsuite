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

## 5. 原生链接对应关系深化（FunctionDescriptorNativeTest）

实证结论（jdk-27.0.1.0.2+1）：
- **fd ↔ handle 类型对应**：bound `downcallHandle(sym, fd)` 的 type() **精确等于** `fd.toMethodType()`；unbound `downcallHandle(fd)` 的 type() 等于前者首部插入 `MemorySegment`（目标地址）。`(ADDRESS,JAVA_INT)→JAVA_LONG` 映射 `(MemorySegment,int)→long`。
- `insertArgumentLayouts` 后布局**按位置**映射到 C 参数（不"移位"既有实参语义）；越界索引（-1、>N）⇒ IAE。
- `changeReturnLayout` 改变 handle 返回类型（C int 返回按描述符加宽为 long，Integer.MAX_VALUE 无损）；`dropReturnLayout`/`ofVoid` 得到 void handle，可调用丢弃 C 返回值；`dropReturnLayout` 保留全部参数布局。
- 实参个数不足在**调用时**抛 `WrongMethodTypeException`（链接不校验 arity）。
- JDK-27 的 FunctionDescriptor **无** `withCallConv` 等属性 API（javap 实证），调用约定差异全部收敛到 `Linker.Option`。

`FunctionDescriptorNativeTest.java`（27 断言）：toMethodType 三向对应 + bound/unbound 真调（含 NULL 实参分支）；append（jck_sum2→jck_sum3 真调精确值）；insert 中部/首部真调 + 越界 IAE；changeReturn 加宽真调；dropReturn/ofVoid 真调；调用 arity WrongMethodTypeException。C 源（`jck_sum2`/`jck_sum3`/`jck_widen`/`jck_void3`/`jck_ptr_and_int`）内嵌运行时编译进 scratch，不入库。

## 5. 契约深化（JDK-27 实测锁定）
- 纯函数族：append/insert/changeReturn/drop 全部不改原描述符；changeReturnLayout 保持参数列表逐项相同。
- insertArgumentLayouts(-1/超界) → **IAE**；changeReturnLayout(null)/of(null,…) → NPE。
- ofVoid() → ()void；值语义 equals/hashCode；ADDRESS 参数映射 MemorySegment.class。
- **argumentLayouts() 不可修改**（add → UOE，27 实测）。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。

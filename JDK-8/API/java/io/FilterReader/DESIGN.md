# java.io.FilterReader 测试方案 (DESIGN)

## 1. API 功能
- 抽象 Reader 装饰器基类；`protected Reader in` 字段与 `protected (Reader)` 构造。
- 继承 Reader 的 read/skip/ready/markSupported/mark/reset/close 均委托给 `in`（子类可覆写拦截）。

## 2. Java 用户如何使用
- 子类化 FilterReader 做字符流过滤/转换（如去元音、大小写转换），覆写 read 委托 + 加工。

## 3. 开源代码实际用法调研
- 委托实现 — `jdk8u-dev/jdk/src/share/classes/java/io/FilterReader.java`。

## 4. 测试用例设计 + 自动化实现（FilterReaderTest.java，纯黑盒）
黑盒场景:
1. 透传 read()/read(char[])/skip/ready/close 均委托并共享位置。
2. mark/reset/markSupported 委托计数。
3. 覆写 read 的子类去元音 "banana"→"bnn"（拦截）。
4. 子类可达 protected `in`。
5. 底层 Reader 抛 IOException("inner") 原样传播。
- 健壮性: 内存 StringReader；无反射白盒。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（行为一致；分方法计数探针）。

### 新增维度
- **精确委托目标（逐方法计数）**：read() → **in.read() 直接**（非 in.read(cbuf,0,1)，探针 singles=1/bulks=0 证实）；read(char[],off,len) → 同名方法；read(char[]) → Reader 默认 → in.read(c,0,len)；skip/ready/mark/reset/close 全部 1:1 委托，无隐藏额外调用。
- 构造 null → NPE（来自基类 Reader 的 lock 非空检查）。
- **关闭底层经装饰器可见**：fr.close() 委托关闭 StringReader 后，read/skip/ready/reset 均透出 IOException("Stream closed")（逐字）。
- 越界传播：fr.read(cbuf,越界) → 底层抛出的 IOOBE 透传；失败后流仍可用。
- 功能性 mark/reset 经装饰器：mark → 读 → reset → 重放；skip+reset 位置正确；ready 透传（StringReader 恒 true）。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| new PassThrough(null) | NullPointerException | 基类 lock 检查 |
| 底层关闭后 read/skip/ready/reset | IOException | "Stream closed"（经装饰器透出） |
| read(cbuf,越界) | IndexOutOfBoundsException | 底层校验透传 |

### 版本差异
- 无。断言 16 → 37。

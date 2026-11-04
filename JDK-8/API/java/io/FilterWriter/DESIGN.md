# java.io.FilterWriter 测试方案 (DESIGN)

## 1. API 功能
- 抽象 Writer 装饰器基类；`protected Writer out` 字段与 `protected (Writer)` 构造。
- write(int)/write(char[],int,int)/write(String,int,int)/flush/close 委托给 `out`。

## 2. Java 用户如何使用
- 子类化 FilterWriter 做字符流过滤/转换，覆写 write 委托 + 加工。

## 3. 开源代码实际用法调研
- 委托实现 — `jdk8u-dev/jdk/src/share/classes/java/io/FilterWriter.java`。

## 4. 测试用例设计 + 自动化实现（FilterWriterTest.java，纯黑盒）
黑盒场景:
1. write(int)/write(char[],off,len)/write(String,off,len) 区域语义委托 → "acdY"。
2. append(char/CharSequence/CharSequence,off,len) 走 Writer 默认 → "QRSU"。
3. flush/close 委托计数。
4. 覆写 write(int) 的转大写装饰器 → "AB"。
5. 子类可达 protected `out`。
6. 底层 Writer 抛 IOException("sink") 原样传播。
- 健壮性: 内存 StringWriter；无反射白盒。

## 深化追加（契约级）

实测环境：JDK 8u372 / JDK 27.0.1（行为一致；分形态计数探针）。

### 新增维度
- **精确委托目标（逐形态计数）**：write(int) → out.write(int) 直接；write(char[],off,len)/write(String,off,len) → 同名方法；**继承的 write(char[]) → out.write(c,0,len)；继承的 write(String) → out.write(s,0,len)**；append(char) 路由 write(int)、append(csq) 路由 write(String)（经 Writer 默认实现最终落到 out.write(String,off,len)）；flush/close 1:1。
- 构造 null → NPE（基类 Writer 的 lock 检查）。
- **关闭底层经装饰器可见**：自定义 ClosedSink 记录 closed，fr.close() 委托后 write(int)/write(c,o,l)/flush → IOException("Stream closed") 透出（StringWriter.close 是 no-op 无法观察，故用自定义 sink）。
- 越界/null 传播：write(char[],越界)/write("abc",0,9) → IOOBE；write((char[])null,0,1) → NPE；失败后仍可用。

### 负例矩阵（双侧实测）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| new PassThrough(null) | NullPointerException | 基类 lock 检查 |
| 底层关闭后 write/flush | IOException | "Stream closed"（自定义 sink 逐字验证） |
| write(越界) 两种形态 | IndexOutOfBoundsException | 8 侧 write(String,o,l) 实际类 StringIndexOutOfBoundsException、27 侧 IndexOutOfBounds（类型级断言 IOOBE） |
| write((char[])null,0,1) | NullPointerException | - |

### 版本差异
- write(String,off,len) 越界的实际异常类：8=StringIndexOutOfBounds / 27=IndexOutOfBounds（其余无差异）。断言 9 → 26。

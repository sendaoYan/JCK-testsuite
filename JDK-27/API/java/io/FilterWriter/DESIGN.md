# java.io.FilterWriter 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象装饰基类：`protected Writer out`；`protected FilterWriter(Writer)`。
- 覆盖 `write(int)`、`write(char[],int,int)`、`write(String,int,int)` 全部透传；`flush()`、`close()` 继承 Writer（同步透传 out）。

## 2. Java 用户如何使用
- 字符输出过滤器骨架：覆盖 write 做转义/加密/审计，flush/close 委托。
- JDK 自身无直接子类（保留 API 完整性），用户库做过滤写。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 三 write 透传实现 — `jdk25u-dev/src/java.base/share/classes/java/io/FilterWriter.java`。

## 4. 测试用例设计 + 自动化实现（FilterWriterTest.testBlackBox）
1. 具体子类包装 StringWriter：write(int)/write(char[])/write(String 区段) 内容精确一致。
2. append(CharSequence) 经 Writer 基类路由到 write(String)。
3. flush/close 透传（StringWriter 均无失败）；close 后 StringWriter 仍可读其缓冲。
- 健壮性: 纯内存。

产出：`FilterWriterTest.java`（白盒 + 黑盒共 17 断言）。

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
- write(String,off,len) 越界的实际异常类：8=StringIndexOutOfBounds / 27=IndexOutOfBounds（其余无差异）。断言 17 → 36。

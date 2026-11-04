# java.io.FilterReader 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象装饰基类：`protected Reader in`；`protected FilterReader(Reader)`。
- 透传 `read()`、`read(char[],int,int)`、`skip`、`ready`、`markSupported`、`mark`、`reset`、`close`。

## 2. Java 用户如何使用
- 字符流过滤器骨架：PushbackReader/BufferedReader 风格扩展；覆盖 read 做大小写转换/脱敏，其余委托。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 透传实现与抽象性 — `jdk25u-dev/src/java.base/share/classes/java/io/FilterReader.java`。
- 官方子类样板 — `jdk25u-dev/src/java.base/share/classes/java/io/PushbackReader.java`（unread 用自身缓冲，其余透传）。

## 4. 测试用例设计 + 自动化实现（FilterReaderTest.testBlackBox）
1. 具体子类包装 StringReader：read/read(char[])/skip/ready 与源一致。
2. mark/reset 透传：mark→读→reset 回退（StringReader 支持 mark）。
3. close 透传后 read 抛 IOException（StringReader close 语义："Stream closed"）。
4. 子类覆盖 read() 后 bulk read 逐字符路由（Tracked 计数）。
- 健壮性: 纯内存。

产出：`FilterReaderTest.java`（白盒 + 黑盒共 25 断言）。

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
- 无。断言 21 → 48。

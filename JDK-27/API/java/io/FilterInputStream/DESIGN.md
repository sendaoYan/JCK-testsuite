# java.io.FilterInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 装饰基类：`protected volatile InputStream in` 为被包装流；构造 `FilterInputStream(InputStream)`（protected）。
- 全部方法透传：`read()`、`read(byte[])`、`read(byte[],int,int)`、`skip`、`available`、`close`、`mark`、`reset`、`markSupported`。
- `read(byte[],int,int)` 不是抽象覆盖，逐字节走子类 `read()`（基类 InputStream 实现）。

## 2. Java 用户如何使用
- 继承并覆盖个别方法（计数、解密、缓冲），其余行为委托底层流：`class Counting extends FilterInputStream { ... }`。
- DataInputStream/PushbackInputStream 等的直接父类。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 透传实现（`return in.read()` 等）— `jdk25u-dev/src/java.base/share/classes/java/io/FilterInputStream.java`。
- 子类化扩展点 — `jdk25u-dev/test/jdk/java/io/FilterInputStream` 系列（CountingInputStream 风格）。

## 4. 测试用例设计 + 自动化实现（FilterInputStreamTest.testBlackBox）
1. 包装 ByteArrayInputStream：read/bulk/skip/available 序列与裸 BAIS 等价（含 available 随消费递减、skip 夹紧到剩余）。
2. mark/reset 透传（BAIS 支持 mark）：mark→读→reset 回退。
3. close 透传：BAIS 容忍 close，未读数据仍可读（字节流 close 无强制失效语义）。
4. 子类只覆盖 read() 时，继承的 read(byte[],int,int) 逐字节调用子类实现（Tracked 子类计数验证调用路由）。
- 健壮性: 固定字节数组，无文件无线程。

产出：`FilterInputStreamTest.java`（白盒 + 黑盒共 26 断言）。

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（22 -> 78）：与 JDK-8 侧逐条镜像（read(byte[]) 路由、"mark/reset not supported"、委托校验、两层 close 传播与二次 close 实测计数 2、skip/available 委托计数、EOF、in 中途换下沉、六操作失败透传 "inner failure"、六操作 null 下沉、无符号读、区域放置）。

### 负例精确异常表（双侧实测）
同 JDK-8 侧："mark/reset not supported" 消息双侧一致；OOB/NPE 类型断言。

### 版本差异
- 本类深化维度双侧行为一致，无新增 API。

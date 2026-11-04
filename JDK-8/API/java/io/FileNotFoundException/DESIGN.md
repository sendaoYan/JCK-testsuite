# java.io.FileNotFoundException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；表示文件路径缺失/目录打开/不可达。

## 2. Java 用户如何使用
- 打开 FileInputStream/FileOutputStream/RandomAccessFile 失败时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/FileNotFoundException.java`。

## 4. 测试用例设计 + 自动化实现（FileNotFoundExceptionTest.java，纯黑盒）
黑盒场景（临时目录隔离）:
1. 两构造器默认 null / 消息保留。
2. 缺失文件 FileInputStream 与 RandomAccessFile("r") 抛异常且消息含路径。
3. 目录当作输入打开抛异常。
4. 父目录不存在的输出路径抛异常。
5. 存在可写文件正常打开（对比）。
6. 作为 IOException 捕获，toString 命名类。
- 健壮性: 无硬编码分隔符；无反射白盒。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 36，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `FileNotFoundException`：双侧同值 `-897856973823710492L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract（suid + 往返）
3. testMessageContent（errno 后缀精确断言）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 不存在路径 → message 含「(No such file or directory)」
- 目录当文件读 → 含「(Is a directory)」
- message 以路径开头；rw 模式可创建成功（对照组不抛）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。

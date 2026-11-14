# java.io.FileNotFoundException 测试方案 (DESIGN)

## 1. API 功能
- `(String reason)`：失败文件系统操作的信号异常（FileInputStream/FileOutputStream/RandomAccessFile 打不开时抛出），message 通常含失败路径与 errno 描述。

## 2. Java 用户如何使用
- 打开不存在/无权限/是目录的路径时捕获，向用户报告路径问题或回退默认位置。

## 3. 开源代码实际用法调研
- FileInputStream/FileOutputStream/RandomAccessFile 构造抛出 — `jdk25u-dev/src/java.base/share/classes/java/io/FileInputStream.java`（open 失败包装为 FNFE）。
- 官方测试覆盖缺失文件/目录路径 — `jdk25u-dev/test/jdk/java/io/FileInputStream/` 与 `FileOutputStream/`。

## 4. 测试用例设计 + 自动化实现（FileNotFoundExceptionTest.java）
- 白盒: extends IOException、两构造。
- 黑盒场景:
  1. 构造器 message。
  2. 读不存在的路径：FIS 抛出且 message 含该路径；RAF("r") 同样抛出。
  3. 打开目录路径读取抛出。
  4. 在不存在的父目录下创建输出文件抛出。
  5. 对照：临时文件可正常打开写入。
  6. 可按 IOException 捕获且 toString 带类名。
- 健壮性: 全部路径用 tmpdir+纳秒唯一名，不依赖权限场景，不产生残留文件。

产出：`FileNotFoundExceptionTest.java`（白盒 3 + 黑盒 9 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 39，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `FileNotFoundException`：双侧同值 `-897856973823710492L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract（suid + 往返）
3. testMessageContent（errno 后缀精确断言）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 不存在路径 → message 含「(平台 OS 原因文本)」（随 OS/locale 本地化）
- 目录当文件读 → 含「(平台 OS 原因文本)」
- message 以路径开头；rw 模式可创建成功（对照组不抛）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。

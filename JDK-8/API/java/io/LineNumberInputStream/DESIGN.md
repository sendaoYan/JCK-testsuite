# java.io.LineNumberInputStream 测试方案 (DESIGN)

## 1. API 功能
- 弃用（@Deprecated）的字节侧行号 FilterInputStream：`getLineNumber()`/`setLineNumber(int)`；行终止符 `\n`/`\r`/`\r\n`（`\r` 交付为 `\n`，`\r\n` 折叠为一个字节）。
- read()/read(byte[],int,int)/skip()/available()/mark()/reset()。

## 2. Java 用户如何使用
- 旧式字节流行号追踪（新代码应使用 LineNumberReader）。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/LineNumberInputStream.java`。

## 4. 测试用例设计 + 自动化实现（LineNumberInputStreamTest.java，纯黑盒）
黑盒场景:
1. 单字节读：`\n`/`\r`/`\r\n` 各计 1 行、`\r`→`\n`、`\r\n` 折叠。
2. 批量读：`\r\n` 折叠为 1 字节、两终止符均交付为 `\n`、计 2 行。
3. available 隐藏 `\r\n` 对中第二个字节（pending `\n`）。
4. setLineNumber 移位后继续计数。
5. mark/reset 恢复行号与位置。
6. skip 跨终止符计数。
7. close 透传（ByteArrayInputStream 容忍 close，数据仍可读）。
- 健壮性: 内存 ByteArrayInputStream；无反射白盒。

## 深化追加（契约级）

新增维度（全部经 JDK-8 实测锁定）：
- **构造 null 不抛 NPE**（FilterInputStream 纯赋值），NPE 延迟到首次使用底层（read/close 实测 NPE）
- available 公式验证：(pushBack==-1) ? 底层avail/2 : 底层avail/2+1 —— "a\rb" 序列 1→1→1、pushback 字节 'b' 随后送达；"ab\r\n" 2→1→0（\r\n 对折半估算）
- **bulk read 吞掉中途 IOException**：LNIS.read(b,off,len) 内部 try-catch，返回部分计数不抛（OneThenBoom 探针返回 1）；单字节 read() 透传 IOException
- read(b,off,len) 参数矩阵：(b,1,3)/(b,-1,0)/(b,0,-1) IOOBE、null NPE、(b,0,0)=0
- skip：负/0 返回 0；跨 \r\n 计 1 行；纯 \r 计 1 行；越过 EOF 夹紧到剩余**合并后**字节数（LNIS 层语义，"1\r\n2\n" 读到 '2' 后 skip(99)=1）
- 行号深分支：setLineNumber(-7) 保持、读非终止符不加行、EOF 不加行、单独 \r 在 EOF 计行
- mark/reset 恢复行号 + 位置 + **pushBack 状态**（"p\rq"：\r 读出后 mark，reset 重新拿到 pending 的 'q'）
- 逐字节 "1\r\n2" → "1\n2"（合并）、空流矩阵（read=-1/available=0/skip=0/行号 0）

负例矩阵：read on null underlying NPE / close on null underlying NPE / read(b,1,3) IOOBE / read(b,-1,0) IOOBE / read(null,0,1) NPE / 底层 IOE 经单字节 read() 透传。

版本差异（8 vs 27）：实现与行为双侧完全一致（JDK-27 未改动该 deprecated 类）；27 侧 read 越界 IOOBE 消息为 Range 风格（来自检查路径变化），8 为 null——均断类型级。

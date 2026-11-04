# java.io.StringBufferInputStream 测试方案 (DESIGN)

## 1. API 功能
- 弃用（@Deprecated）的 InputStream 字节视图：把 String 每字符取低 8 位当字节。
- read()/read(byte[])/read(byte[],int,int)/skip(long)/available()/reset()。

## 2. Java 用户如何使用
- 旧式把字符串当字节流读取（新代码应使用 StringReader）。

## 3. 开源代码实际用法调研
- 实现 — `jdk8u-dev/jdk/src/share/classes/java/io/StringBufferInputStream.java`。

## 4. 测试用例设计 + 自动化实现（StringBufferInputStreamTest.java，纯黑盒）
黑盒场景:
1. 顺序 read() + EOF。
2. read(byte[],off,len) 区域 + read(byte[]) 剩余 + EOF。
3. 非 ASCII 只取低 8 位（'é'→0xE9）。
4. available 全长、skip(2)、skip 截断到剩余。
5. reset 回卷到位置 0（无需 mark）。
6. 精确长度读尽。
7. close 为无操作（close 后仍可读）。
8. 空串 EOF + available 0。
- 健壮性: 内存；无反射白盒。

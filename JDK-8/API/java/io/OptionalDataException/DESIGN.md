# java.io.OptionalDataException 测试方案 (DESIGN)

## 1. API 功能
- final 类、无公共构造（JDK 内部创建）；公共字段 `int length`、`boolean eof`。

## 2. Java 用户如何使用
- readObject 遇到原始块数据时捕获，读取 length/eof 判断剩余。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/OptionalDataException.java`。

## 4. 测试用例设计 + 自动化实现（OptionalDataExceptionTest.java，纯黑盒）
黑盒场景:
1. readObject 遇原始数据 → eof=false 且 length=4（int）。
2. length 随缓冲原始数据量缩放（long+int=12）。
3. 异常后可继续原始读取恢复数据。
4. 先读原始数据再读对象（对比）。
5. 无 readObject 的类跳过自定义附加数据并正确定位。
- 健壮性: 无反射白盒（JDK 8 无强封装 InaccessibleObjectException）。

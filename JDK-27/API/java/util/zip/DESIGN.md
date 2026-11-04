# java.util.zip 包（JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 面加上 JDK-9+ 增量：Deflater/Inflater AutoCloseable+ByteBuffer 通道+close()，ZipEntry setTimeLocal/getTimeLocal，ZipInputStream read()/readAllBytes/readNBytes/skipNBytes/transferTo，ZipFile.toString，CRC32C 深化。

## 2. Java 用户如何使用

ByteBuffer deflate/inflate round-trip；close 后操作 IllegalStateException（与 JDK-8 的 NPE 形成版本差异锁定）；setTimeLocal 毫秒精确往返（2021-03-14T12:30:17 → 1615696217000L）。

## 3. 参考来源

JDK-9~27 javadoc 与 release notes；行为差异经双 JDK 实测锁定。

## 4. 测试用例设计

在 JDK-8 拷贝基础上增加 testJdk27Additions()：Deflater/Inflater/ZipFile/ZipEntry/ZipInputStream/CRC32C。

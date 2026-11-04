# java.util.zip 包 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

校验和（CRC32/CRC32C/Adler32）、压缩解压（Deflater/Inflater）、zip 容器流（ZipInputStream/ZipOutputStream/ZipFile/ZipEntry）及异常类型（ZipException/ZipError/DataFormatException）的契约覆盖。

## 2. Java 用户如何使用

CRC32 已知向量（"123456789"→0xCBF43926、pangram→0x414FA339）；Deflater level-0 存储与压缩 round-trip；ZipOutputStream→ZipFile/ZipInputStream 造包读包闭环；close 后操作 ISE/IOException。

## 3. 参考来源

RFC 1950/1951/1952；PKWARE APPNOTE.txt；OpenJDK test/java/util/zip。

## 4. 测试用例设计

CRC32Test / DeflaterTest / InflaterTest / ZipEntryTest / ZipFileTest / ZipInputStreamTest / ZipOutputStreamTest 各 testContracts；DataFormatException/ZipException/ZipError/DeflaterInputStream/DeflaterOutputStream/InflaterInputStream/InflaterOutputStream 契约块。

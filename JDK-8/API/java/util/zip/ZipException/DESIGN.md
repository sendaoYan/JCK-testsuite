# java.util.zip.ZipException 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

IOException 子类；zip 容器结构损坏（重复条目、END 头缺失、非 zip 数据）时抛出。

## 2. Java 用户如何使用

消息锁定：garbage → "error in opening zip file"、空文件 → "zip file is empty"、重复条目 → "duplicate entry: <name>"、越过流尾写入 → "write beyond end of stream"。

## 3. 参考来源

PKWARE APPNOTE；OpenJDK ZipFile/ZipOutputStream 实现。

## 4. 测试用例设计

ZipExceptionTest.testContracts：异常层级、消息精确断言矩阵。

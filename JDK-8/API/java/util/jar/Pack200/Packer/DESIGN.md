# java.util.jar.Pack200.Packer（JDK-8 专属） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

engine 属性键值接口；EFFORT/SEGMENT_LIMIT/DEFLATE_HINT 等属性。

## 2. Java 用户如何使用

properties() 可变 map；EFFORT 默认 "5"；KNOWN 属性 vs CHANGEABLE。

## 3. 参考来源

JSR-200 Packer javadoc。

## 4. 测试用例设计

PackerTest + Pack200Test 属性面断言。

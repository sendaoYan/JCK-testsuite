# jdk.jshell.execution.impl.RestartableInputStream（内部类，白盒） 设计案

## 1. API 功能
FilterInputStream + AtomicBoolean 关闭标志：close() 置位并委托 super.close()；isClosed() 查询。Util.remoteInputOutput 用它区分 EOF(TAG_EOF，可重启) 与 TAG_CLOSED。

## 2. Java 用户如何使用
包装 System.in 之类的可重启输入。

## 3. 开源代码实际用法调研
来源：RestartableInputStream.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
反射构造(ByteArrayInputStream)：初始未关闭→读透传→EOF 透传→close 置位→重复 close 幂等。


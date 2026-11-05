# jdk.jshell.spi.ExecutionControl.ClassBytecodes 设计案

## 1. API 功能
类名 + 字节码的 Serializable 值类型（serialVersionUID=0xC1A55B47EC0DE5L）。

## 2. Java 用户如何使用
ExecutionControl.load/redefine 的载荷，经 writeObject 走线。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
bytecodes() 共享内部数组引用（无防御拷贝，可变修改外部可见）；序列化往返保 name/bytecodes；svUID 经 ObjectStreamClass.lookup 读取（反射 setAccessible 会被模块 open 检查拒绝）；无 equals 覆盖（同内容不同实例不相等）；null 字节数组构造器不拒绝。


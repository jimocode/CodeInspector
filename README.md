# CodeInspector

## 简介

简化并重写`GadgetInspector`尝试实现一个**自动`Java`代码审计工具**

基本原理是从`Java`的字节码角度入手，使用`ASM`技术解析，模拟JVM的`Operand Stack`和`Local Variables Array`实现数据流分析

最终目标：通过输入一个`SpringBoot`的`Jar`，直接生成漏洞报告

## 原理

JVM在每次方法调用均会创建一个对应的Frame，方法执行完毕或者异常终止，Frame被销毁

而每个Frame的结构如下，主要由本地变量数组（local variables）和操作栈（operand stack）组成

![](https://github.com/EmYiQing/CodeInspector/blob/master/image/1.png)

局部变量表所需的容量大小是在编译期确定下来的，表中的变量只在当前方法调用中有效

JVM把操作数栈作为它的**工作区**——大多数指令都要从这里弹出数据，执行运算，然后把结果压回操作数栈

比如，IADD指令就要从操作数栈中弹出两个整数，执行加法运算，其结果又压回到操作数栈中

之所以介绍JVM Frame，是因为代码模拟了比较完善的Operand Stack和Local Variables交互

例如方法调用会从Stack中弹出参数，方法返回值会压入栈中

根据这样的规则，进而执行数据流的分析

参考代码中的`core/CoreMethodAdapter`，该类构造了`Operand Stack`和`Local Variables Array`并结合ASM技术实现数据流分析

## 进度

目前仅尝试实现了一种简单的SSRF，但可以做到参数可控性判断和数据流追踪分析，参考已有代码可以实现其他的漏洞检测

## 使用

目前仅测试

打包：`mvn clean package`

执行：`java -jar CodeInspector.jar --boot jar/springboot.jar --pack com.inspector.sbdemo`

- boot：指定SpringBoot的Jar包路径
- pack：指定项目的包名，将会分析启动的SpringMVC路径映射，生成自动审计的入口
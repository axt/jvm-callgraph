# jvm-callgraph
Call graph generator for JVM bytecode

## Usage

Get the immediate callers of `com.axt.jvmcallgraph.Callgraph.nextLevel()`
```java
CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
	.addClasspathSource(Main.class.getClassLoader(), "com.axt")
	.addTargetMethod(method -> 
		method.getClassName().equals("com/axt/jvmcallgraph/CallGraph") &&
		method.getName().equals("nextLevel"))
	.build();
	
CallGraph callGraph = new CallGraph(callGraphRequest);
callGraph.build(1);
```


Get the callgraph of all `native` functions in `rt.jar`
```java
CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
	.addJarSource(PATH_TO_RT_JAR)
	.addTargetMethod(method -> method.isNative())
	.stopCondition(method -> method.isPublic())
	.prune(true)
	.build();
	
CallGraph callGraph = new CallGraph(callGraphRequest);
callGraph.build(5);
```


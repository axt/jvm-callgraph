package com.axt.jvmcallgraph;

import java.io.IOException;

class Main {
	
	public static void main(String[] args) throws IOException {
		explicitTargetMethod();
		callsToSystemExit();
	}

	private static void callsToSystemExit() throws IOException {
		CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
				.addJarSource("/usr/lib/jvm/java-8-oracle/jre/lib/rt.jar")
				.addTargetMethod(x -> x.getClassName().equals("java/lang/System") && x.getName().equals("exit"))
				.stopCondition(x -> x.getClassName().startsWith("java/"))
				.prune(true)
				.build();
		
			CallGraph callGraph = new CallGraph(callGraphRequest);
			callGraph.build(3);
			System.out.println(callGraph.dump());
	}

	private static void explicitTargetMethod() throws IOException {
		CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
			.addClasspathSource(Main.class.getClassLoader(), "com.axt")
			.addExplicitTargetMethod(new MethodInfo("com/axt/jvmcallgraph/ClasspathBytecodeSource", "getClassReaders", null), true, true)
			.build();
	
		CallGraph callGraph = new CallGraph(callGraphRequest);
		callGraph.build(5);
		
		System.out.println(callGraph.dump());
	}


}

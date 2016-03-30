package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.Collection;

class Main {
	static final String RTPATH = "../../Java/JDK1.8.0_73/rt";

	public static void main(String[] args) throws IOException {

		CallGraphRequest callGraphRequest = new CallGraphRequest.Builder()
			.addDirectorySource(RTPATH)
			//.addTargetMethod(x -> x.isNative() && x.getClassName().contains("UNIXProcess"))
			.addTargetMethod(x -> x.getClassName().equals("java/lang/System") && x.getName().equals("exit"))
			.build();
	
		CallGraph callGraph = new CallGraph(callGraphRequest);
		callGraph.build(2);
		
		Collection<CallGraphNode> rootNodes = callGraph.getRootNodes();
		for (CallGraphNode rootNode : rootNodes) {
			printCallGraph(rootNode);
		}
		
	}
	
	//NOTE: probably can loop infinitely
	private static void printCallGraph(CallGraphNode rootNode) {
		printNode(rootNode, 0);
	}

	//NOTE: probably can loop infinitely
	private static void printNode(CallGraphNode node, int level) {
		for(int i=0; i < level; i++) {
			System.out.print("\t");
		}
		System.out.println(node.method);
		for (CallGraphNode calleeNode : node.calleeNodes) {
			printNode(calleeNode, level+1);
		}
	}

}

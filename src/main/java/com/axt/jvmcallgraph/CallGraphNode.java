package com.axt.jvmcallgraph;

import java.util.HashSet;
import java.util.Set;

public class CallGraphNode {
	MethodInfo method;
	
	Set<CallGraphNode> calleeNodes;
	CallGraphNode parentNode;
	
	public CallGraphNode(MethodInfo method) {
		this.method = method;
		this.calleeNodes = new HashSet<>();
	}
	
	public void addCallee(CallGraphNode callee) {
		this.calleeNodes.add(callee);
	}
}
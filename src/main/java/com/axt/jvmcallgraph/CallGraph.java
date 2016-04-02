package com.axt.jvmcallgraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;

public class CallGraph {
	
	private CallGraphRequest callGraphRequest;
	
	private List<CallGraphNode> rootNodes;
	private Set<MethodInfo> activeMethods;
	private HashMap<MethodInfo, CallGraphNode> methodToNodeMap;
	
	boolean built = false;
	
	public CallGraph(CallGraphRequest callGraphRequest) {
		this.callGraphRequest = callGraphRequest;
	}

	public void build(int deep) throws IOException {
		this.activeMethods = new HashSet<>(collectTargetMethods());
		this.rootNodes = setRootNodes(this.activeMethods);
		this.methodToNodeMap = new HashMap<>();
		for(CallGraphNode node: rootNodes) {
			this.methodToNodeMap.put(node.method, node);
		}
		for (int i=0; i < deep; i++) {
			boolean workleft = nextLevel();
			if(!workleft) break;
		}
		pruneGraph();
		built = true;
	}

	public CallGraphNode getNode(MethodInfo method) {
		checkIsBuilt();
		return methodToNodeMap.get(method);
	}
	
	public Collection<CallGraphNode> getRootNodes() {
		checkIsBuilt();
		return Collections.unmodifiableList(rootNodes);
	}
	
	private void checkIsBuilt() {
		if (!built)
			throw new IllegalStateException("build() should be called before accessing query functions");
	}

	private List<MethodInfo> collectTargetMethods() throws IOException {
		MethodCollector targetMethodCollector = new MethodCollector(callGraphRequest.getTargetMethods());
		for(ClassReader cr : callGraphRequest.getClassReaders()) {
			targetMethodCollector.setClassName(cr.getClassName());
			cr.accept(targetMethodCollector, 0);
		}
		return targetMethodCollector.getCollectedMethods();
	}

	private List<CallGraphNode> setRootNodes(Collection<MethodInfo> targetMethods) {
		List<CallGraphNode> rootNodes = new ArrayList<>();
		for(MethodInfo method : targetMethods) {
			rootNodes.add(new CallGraphNode(method));
		}
		return rootNodes;
	}
	
	private boolean nextLevel() throws IOException {
		MethodCallCollector methodCallCollector = new MethodCallCollector(activeMethods);
		for(ClassReader cr : callGraphRequest.getClassReaders()) {
			methodCallCollector.setClassName(cr.getClassName());
			cr.accept(methodCallCollector, 0);
		}	
		
		Set<MethodInfo> remainingMethods = new HashSet<>();
		
		for(MethodInfo method: activeMethods) {
			Collection<MethodInfo> callees = methodCallCollector.getCallees(method);
			Predicate<MethodInfo> stopCondition = callGraphRequest.getStopCondition();
			if (stopCondition != null &&  stopCondition.test(method)) {
				continue;
			}
			CallGraphNode methodNode = methodToNodeMap.get(method);
			for (MethodInfo callee : callees) {
				CallGraphNode calleeNode = null;
				
				if (!methodToNodeMap.containsKey(callee)) {
					calleeNode = new CallGraphNode(callee);
					methodToNodeMap.put(callee, calleeNode);
					remainingMethods.add(callee);
				} else {
					calleeNode = methodToNodeMap.get(callee);
				}
				methodNode.addCallee(calleeNode);
			}
		}
	
		this.activeMethods = remainingMethods;
		return activeMethods.size() > 0;
	}

	boolean pruneCalleeNodes(CallGraphNode node, Set<CallGraphNode> visited) {
		if (node.calleeNodes.size() == 0) {
			if(!(callGraphRequest.getStopCondition() != null & callGraphRequest.getStopCondition().test(node.method)))
				return false;
			return true;
		} else {
			boolean keepNode = false;
			Iterator<CallGraphNode> it = node.calleeNodes.iterator();
			while(it.hasNext()) {
				Set<CallGraphNode> visitedCurrent = new HashSet<CallGraphNode>(visited);
				visitedCurrent.add(node);
				CallGraphNode callee = it.next();
				if (visitedCurrent.contains(callee)) {
					it.remove();
					continue;
				};
				boolean keepCallee = pruneCalleeNodes(callee, visitedCurrent);
				if (keepCallee) {
					keepNode |= keepCallee;
				} else {
					it.remove();
				}
			}
			return keepNode;
		}
	}
	
	private void pruneGraph() {
		if (this.callGraphRequest.getPrune()) {
			Iterator<CallGraphNode> it = rootNodes.iterator();
			while(it.hasNext()) {
				if(!pruneCalleeNodes(it.next(), new HashSet<>())) {
					it.remove();
				}
			}
		}
	}
}

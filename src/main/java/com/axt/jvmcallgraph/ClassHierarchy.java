package com.axt.jvmcallgraph;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class ClassHierarchy {
	
	Map<String, String> superNames = new HashMap<>();
	Multimap<String, String> interfaces = HashMultimap.create();
	
	public ClassHierarchy() {
	}

	public void build(List<ClassReader> classReaders) {
		for (ClassReader classReader : classReaders) {
			superNames.put(classReader.getClassName(), classReader.getSuperName());
			interfaces.putAll(classReader.getClassName(), Arrays.asList(classReader.getInterfaces()));
		}
	}

	public String getSuper(String className) {
		return superNames.get(className);
	}
	
	public Collection<String> getInterfaces(String className) {
		return interfaces.get(className);
	}
	
}
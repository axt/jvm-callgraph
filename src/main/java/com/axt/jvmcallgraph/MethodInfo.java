package com.axt.jvmcallgraph;

import org.objectweb.asm.Opcodes;

public class MethodInfo {
	private String name;
	private String description;
	private int access;
	
	public MethodInfo(String name, String description, int access) {
		super();
		this.name = name;
		this.description = description;
		this.access = access;
	}

	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	
	public boolean isPublic() {
		return (access & Opcodes.ACC_PUBLIC) != 0;
	}
	public boolean isPrivate() {
		return (access & Opcodes.ACC_PRIVATE) != 0;
	}
	public boolean isProtected() {
		return (access & Opcodes.ACC_PROTECTED) != 0;
	}
	public boolean isNative() {
		return (access & Opcodes.ACC_NATIVE) != 0;
	}

	@Override
	public String toString() {
		String ret = "";
		if (isPublic())
			ret += "public ";
		if (isProtected())
			ret += "protected ";
		if (isPrivate())
			ret += "private ";
		if (isNative())
			ret += "native ";
		
		ret += name + " " + description;
		return ret;
	}
}

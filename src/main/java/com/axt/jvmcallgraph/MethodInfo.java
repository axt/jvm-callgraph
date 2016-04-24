package com.axt.jvmcallgraph;

import org.objectweb.asm.Opcodes;

public class MethodInfo {
	private String name;
	private String description;
	private String className;
	private Integer access;

	public MethodInfo(String className, String name, String description) {
		super();
		this.className = className;
		this.name = name;
		this.description = description;
	}

	public void setAccess(int access) {
		this.access = access;
	}
	
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getClassName() {
		return className;
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
		if (access != null) {
			if (isPublic())
				ret += "public ";
			if (isProtected())
				ret += "protected ";
			if (isPrivate())
				ret += "private ";
			if (isNative())
				ret += "native ";
		}
		
		ret += className + "::" + name + " " + description;
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodInfo other = (MethodInfo) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


}

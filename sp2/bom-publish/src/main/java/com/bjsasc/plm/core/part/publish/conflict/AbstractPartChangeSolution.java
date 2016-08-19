package com.bjsasc.plm.core.part.publish.conflict;

public abstract class AbstractPartChangeSolution implements PartChangeSolution {
	
	private Object toModify;
	
	public AbstractPartChangeSolution(Object toModify) {
		this.toModify = toModify;
	}

	public Object getToModify() {
		return toModify;
	}

	public void setToModify(Object toModufy) {
		this.toModify = toModufy;
	}

}

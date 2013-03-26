package be.kuleuven.cs.distrinet.jlo.model.expression;

import be.kuleuven.cs.distrinet.chameleon.oo.type.Type;

public class RootTarget extends AbstractTarget {

	@Override
	public Type getTargetDeclaration() {
		return farthestAncestor(Type.class);
	}

	@Override
	public RootTarget clone() {
		return new RootTarget();
	}

}
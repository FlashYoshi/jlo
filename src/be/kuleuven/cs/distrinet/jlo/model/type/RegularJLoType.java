package be.kuleuven.cs.distrinet.jlo.model.type;

import java.util.List;

import be.kuleuven.cs.distrinet.jlo.model.component.ComponentRelation;
import be.kuleuven.cs.distrinet.jlo.model.component.Overrides;
import be.kuleuven.cs.distrinet.jnome.core.type.RegularJavaType;
import be.kuleuven.cs.distrinet.chameleon.core.declaration.SimpleNameSignature;
import be.kuleuven.cs.distrinet.chameleon.core.lookup.LookupException;
import be.kuleuven.cs.distrinet.chameleon.oo.member.Member;
import be.kuleuven.cs.distrinet.chameleon.oo.member.MemberRelationSelector;
import be.kuleuven.cs.distrinet.chameleon.oo.type.Type;
import be.kuleuven.cs.distrinet.chameleon.oo.type.inheritance.InheritanceRelation;

public class RegularJLoType extends RegularJavaType {
	
//	private CreationStackTrace _trace;

	public RegularJLoType(SimpleNameSignature sig) {
		super(sig);
//		sig.parentLink().addListener(new AssociationListener() {
//
//			@Override
//			public void notifyElementAdded(Object element) {
//				
//			}
//
//			@Override
//			public void notifyElementRemoved(Object element) {
//				RegularJLoType.this._trace = new CreationStackTrace();
//			}
//
//			@Override
//			public void notifyElementReplaced(Object oldElement, Object newElement) {
//				RegularJLoType.this._trace = new CreationStackTrace();
//			}
//
//		});
	}

	public RegularJLoType(String name) {
		super(name);
	}
	
	public List<InheritanceRelation> inheritanceRelations() throws LookupException {
		// first take the subtype relations
		List<InheritanceRelation> result = super.inheritanceRelations();
		// then add the component relations
		List<ComponentRelation> components;
			components = body().members(ComponentRelation.class);
			result.addAll(components);
		return result;
	}
	
	protected RegularJLoType cloneThis() {
		return new RegularJLoType(signature().clone());
	}
	
	@Override
	public <D extends Member> List<D> membersDirectlyOverriddenBy(MemberRelationSelector<D> selector) throws LookupException {
		List<D> result = super.membersDirectlyOverriddenBy(selector);
		D declaration = selector.declaration();
		if(declaration.nearestAncestor(Type.class).sameAs(this)) {
			for(Overrides ov: directlyDeclaredElements(Overrides.class)) {
				if(ov.newSignature().sameAs(declaration.signature())) {
					result.add((D)ov.oldDeclaration());
				}
			}
		}
		return result;
	}

}
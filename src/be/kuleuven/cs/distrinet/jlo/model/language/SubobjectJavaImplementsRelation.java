package be.kuleuven.cs.distrinet.jlo.model.language;

import be.kuleuven.cs.distrinet.jlo.model.component.ComponentRelation;
import be.kuleuven.cs.distrinet.jnome.core.language.JavaImplementsRelation;
import be.kuleuven.cs.distrinet.chameleon.core.lookup.LookupException;
import be.kuleuven.cs.distrinet.chameleon.core.relation.StrictPartialOrder;
import be.kuleuven.cs.distrinet.chameleon.oo.member.Member;

public class SubobjectJavaImplementsRelation extends StrictPartialOrder<Member> {

	private JavaImplementsRelation _wrapped = new JavaImplementsRelation();
	
	@Override
	public boolean contains(Member first, Member second) throws LookupException {
		boolean result;
		if(first instanceof ComponentRelation && second instanceof ComponentRelation) {
			boolean defined1 = _wrapped.checkDefined(first);
			if(defined1) {
				boolean defined2 = _wrapped.checkDefined(second);
				return (!defined2) && first.sameSignatureAs(second);
			} else {
			  result = false;
			}
		} else {
			result = _wrapped.contains(first, second);
		}
		return result;
	}

	@Override
	public boolean equal(Member first, Member second) throws LookupException {
		return _wrapped.equal(first, second);
	}


}

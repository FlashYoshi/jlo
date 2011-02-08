package subobjectjava.model.component;

import java.util.ArrayList;
import java.util.List;

import org.rejuse.predicate.UnsafePredicate;

import chameleon.core.declaration.Declaration;
import chameleon.core.declaration.QualifiedName;
import chameleon.core.declaration.Signature;
import chameleon.core.lookup.LookupException;
import chameleon.core.member.Member;
import chameleon.core.member.MemberRelationSelector;
import chameleon.core.validation.BasicProblem;
import chameleon.core.validation.VerificationResult;
import chameleon.oo.type.Type;

public class OverridesClause extends AbstractClause<OverridesClause> {

	public OverridesClause(Signature newSignature, QualifiedName oldFqn) {
		setNewSignature(newSignature);
		setOldFqn(oldFqn);
	}
	
	@Override
	public List<Member> introducedMembers() throws LookupException {
		List<Member> result = new ArrayList<Member>();
		return result;
	}
	
	

	@Override
	public VerificationResult verifySelf() {
		VerificationResult result = super.verifySelf();
		final Signature newSignature = newSignature();
		if(newSignature != null) {
			List<Member> members = nearestAncestor(Type.class).directlyDeclaredMembers();
			boolean overridden;
			try {
			overridden = new UnsafePredicate<Member, LookupException>() {
				@Override
				public boolean eval(Member object) throws LookupException {
					return object.signature().sameAs(newSignature);
				}
			}.exists(members);
			} catch(LookupException exc) {
				overridden = false; 
			}
			if(! overridden) {
				result = result.and(new BasicProblem(this, "There is no local definition of "+newSignature + "."));
			}
		}
		return result;
	}

	@Override
	public OverridesClause clone() {
		return new OverridesClause(newSignature().clone(), oldFqn().clone());
	}

	@Override
	public <D extends Member> List<D> membersDirectlyOverriddenBy(MemberRelationSelector<D> selector) throws LookupException {
		List<D> result = new ArrayList<D>();
		if(selector.selects(newSignature(),oldDeclaration())) {
			result.add((D)oldDeclaration());
		}
		return result;
	}

}

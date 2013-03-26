package be.kuleuven.cs.distrinet.jlo.model.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import be.kuleuven.cs.distrinet.rejuse.property.Property;
import be.kuleuven.cs.distrinet.rejuse.property.PropertyMutex;
import be.kuleuven.cs.distrinet.chameleon.core.declaration.Signature;
import be.kuleuven.cs.distrinet.chameleon.core.declaration.SimpleNameSignature;
import be.kuleuven.cs.distrinet.chameleon.core.lookup.LookupException;
import be.kuleuven.cs.distrinet.chameleon.core.modifier.Modifier;
import be.kuleuven.cs.distrinet.chameleon.exception.ChameleonProgrammerException;
import be.kuleuven.cs.distrinet.chameleon.exception.ModelException;
import be.kuleuven.cs.distrinet.chameleon.oo.language.ObjectOrientedLanguage;
import be.kuleuven.cs.distrinet.chameleon.oo.member.DeclarationComparator;
import be.kuleuven.cs.distrinet.chameleon.oo.member.HidesRelation;
import be.kuleuven.cs.distrinet.chameleon.oo.member.Member;
import be.kuleuven.cs.distrinet.chameleon.oo.member.MemberRelationSelector;
import be.kuleuven.cs.distrinet.chameleon.oo.member.OverridesRelation;
import be.kuleuven.cs.distrinet.chameleon.oo.type.DeclarationWithType;
import be.kuleuven.cs.distrinet.chameleon.oo.type.Parameter;
import be.kuleuven.cs.distrinet.chameleon.oo.type.Type;
import be.kuleuven.cs.distrinet.chameleon.util.Util;

public abstract class ComponentParameter extends Parameter implements DeclarationWithType, Member {

	public abstract ComponentParameter clone();
	
	public ComponentParameter(SimpleNameSignature sig) {
		super(sig);
	}

	@Override
	public List<? extends Member> getIntroducedMembers() throws LookupException {
		return Util.createNonNullList(this);
	}

	@Override
	public List<? extends Member> declaredMembers() {
    try {
			return getIntroducedMembers();
		} catch (LookupException e) {
			throw new ChameleonProgrammerException("This should not happen. Element of class "+this.getClass().getName()+" threw a lookup exception in getIntroducedMembers. This exception ended up in declaredMembers. But if that is the case, then declaredMembers must be overridden to provide a proper definition.", e);
		}
	}

	@Override
	public List<Modifier> modifiers() {
		return new ArrayList<Modifier>();
	}

	@Override
	public void addModifier(Modifier modifier) {
		//throw new ChameleonProgrammerException();
	}

	@Override
	public void removeModifier(Modifier modifier) {
		throw new ChameleonProgrammerException();
	}

	@Override
	public void addModifiers(List<Modifier> modifiers) {
		if(! modifiers.isEmpty()) {throw new ChameleonProgrammerException();};
	}

	@Override
	public List<Modifier> modifiers(PropertyMutex mutex) throws ModelException {
		return new ArrayList<Modifier>();
	}

	@Override
	public List<Modifier> modifiers(Property property) throws ModelException {
		return new ArrayList<Modifier>();
	}

  public MemberRelationSelector<? extends Member> overridesSelector() {
		return new MemberRelationSelector<Member>(Member.class,this,_overridesRelation);
  }
  
  public final boolean overrides(Member other) throws LookupException {
  	return overridesRelation().contains(this,other);
  }

  
  public OverridesRelation<? extends Member> overridesRelation() {
  	return _overridesRelation;
  }
  
  private static OverridesRelation<Member> _overridesRelation = new OverridesRelation<Member>(Member.class) {
		
		public boolean containsBasedOnRest(Member first, Member second) throws LookupException {
			return first.signature().sameAs(second.signature());
		}

		@Override
		public boolean containsBasedOnName(Signature first, Signature second) {
			return first.name().equals(second.name());
		}
	};
  public MemberRelationSelector<? extends Member> aliasSelector() {
		return new MemberRelationSelector<Member>(Member.class,this,_aliasSelector);
  }
	
  private static DeclarationComparator<Member> _aliasSelector = new DeclarationComparator<Member>(Member.class) {
		
		public boolean containsBasedOnRest(Member first, Member second) throws LookupException {
			return first.signature().sameAs(second.signature());
		}

		@Override
		public boolean containsBasedOnName(Signature first, Signature second) {
			return true;
		}
	};

  public final boolean hides(Member other) throws LookupException {
	  return ((HidesRelation)hidesRelation()).contains(this,other);
  }
  public HidesRelation<? extends Member> hidesRelation() {
		return _hidesSelector;
  }
  
  private static HidesRelation<Member> _hidesSelector = new HidesRelation<Member>(Member.class) {
		
		public boolean containsBasedOnRest(Member first, Member second) throws LookupException {
			return true;
		}

	};


  public final boolean canImplement(Member other) throws LookupException {
  	return language(ObjectOrientedLanguage.class).implementsRelation().contains(this,other);
  }

  public List<? extends Member> directlyOverriddenMembers() throws LookupException {
    return nearestAncestor(Type.class).membersDirectlyOverriddenBy(overridesSelector());
  }
  
  public List<? extends Member> directlyAliasedMembers() throws LookupException {
    return nearestAncestor(Type.class).membersDirectlyAliasedBy(aliasSelector());
  }
  
  public List<? extends Member> directlyAliasingMembers() throws LookupException {
    return nearestAncestor(Type.class).membersDirectlyAliasing(aliasSelector());
  }
  
  public Set<? extends Member> overriddenMembers() throws LookupException {
  	List<Member> todo = (List<Member>) directlyOverriddenMembers();
  	Map<Type,List<Member>> visitedTypes = new HashMap<Type,List<Member>>();
  	while(! todo.isEmpty()) {
  		Member m = todo.get(0);
  		todo.remove(0);
  		Type containingType = m.nearestAncestor(Type.class);
		if(! visitedTypes.containsKey(containingType)) {
			visitedTypes.put(containingType, new ArrayList<Member>());
		}
		List<Member> done = visitedTypes.get(containingType);
		boolean contains = false;
		for(Member member:done) {
			if(member.signature().sameAs(m.signature())) {
				contains = true;
				break;
			}
		}
		if(! contains) {
			done.add(m);
			todo.addAll(m.directlyOverriddenMembers());
			todo.addAll(m.aliasedMembers());
			todo.addAll(m.aliasingMembers());
		}
  	}
  	Set<Member> result = new HashSet<Member>();
  	for(List<Member> members: visitedTypes.values()) {
  		result.addAll(members);
  	}
  	return result;
  }
  
  public Set<? extends Member> aliasedMembers() throws LookupException {
	  List<Member> todo = (List<Member>) directlyAliasedMembers();
	  Set<Member> result = new HashSet<Member>();
	  while(! todo.isEmpty()) {
		  Member m = todo.get(0);
		  System.out.println(m.nearestAncestor(Type.class).getFullyQualifiedName()+"."+m.signature().name());
		  todo.remove(0);
		  if(result.add(m)) {
			  todo.addAll(m.directlyAliasedMembers());
		  }
	  }
	  return result;
  }

  public Set<? extends Member> aliasingMembers() throws LookupException {
	  List<Member> todo = (List<Member>) directlyAliasingMembers();
	  Set<Member> result = new HashSet<Member>();
	  while(! todo.isEmpty()) {
		  Member m = todo.get(0);
		  todo.remove(0);
		  if(result.add(m)) {
			  todo.addAll(m.directlyAliasingMembers());
		  }
	  }
	  return result;
  }


}
package jlo.translate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jlo.model.component.ComponentRelation;
import jlo.model.type.RegularJLoType;
import jnome.core.expression.invocation.SuperConstructorDelegation;
import jnome.core.language.Java;
import jnome.core.type.BasicJavaTypeReference;

import org.rejuse.association.Association;
import org.rejuse.logic.ternary.Ternary;

import chameleon.core.declaration.TargetDeclaration;
import chameleon.core.element.Element;
import chameleon.core.lookup.LookupException;
import chameleon.core.modifier.Modifier;
import chameleon.core.namespacedeclaration.NamespaceDeclaration;
import chameleon.core.reference.SimpleReference;
import chameleon.core.tag.TagImpl;
import chameleon.oo.expression.MethodInvocation;
import chameleon.oo.language.ObjectOrientedLanguage;
import chameleon.oo.method.Method;
import chameleon.oo.method.RegularImplementation;
import chameleon.oo.method.SimpleNameMethodHeader;
import chameleon.oo.statement.Block;
import chameleon.oo.type.BasicTypeReference;
import chameleon.oo.type.RegularType;
import chameleon.oo.type.Type;
import chameleon.oo.type.TypeReference;
import chameleon.oo.type.generics.InstantiatedParameterType;
import chameleon.oo.type.generics.ActualTypeArgument;
import chameleon.oo.type.inheritance.SubtypeRelation;
import chameleon.support.member.simplename.method.NormalMethod;
import chameleon.support.statement.StatementExpression;
import chameleon.util.Util;

public class InnerClassCreator extends AbstractTranslator {
	
	public InnerClassCreator(SelectorCreator selectorCreator) {
		_selectorCreator = selectorCreator;
	}
	
	private SelectorCreator _selectorCreator;
	
	public SelectorCreator selectorCreator() {
		return _selectorCreator;
	}

	public Type emptyInnerClassFor(ComponentRelation relationBeingTranslated) throws LookupException {
		incorporateImports(relationBeingTranslated);
		String className = innerClassName(relationBeingTranslated);
		Type result = new RegularJLoType(className);
		for(Modifier mod: relationBeingTranslated.modifiers()) {
			result.addModifier(mod.clone());
		}
		
		for(TypeReference superReference : superClassReferences(relationBeingTranslated,result)) {
			result.addInheritanceRelation(new SubtypeRelation(superReference));
		}
		//JENS
		List<Method> selectors = selectorCreator().selectorsFor(relationBeingTranslated);
		for(Method selector:selectors) {
			result.add(selector);
		}
		processInnerClassMethod(relationBeingTranslated, result);
		return result;
	}
//	Type t = superReference.getElement();
//	if(isJLo(t) && (! splitClass(t))) {
//		transformToImplReference(superReference);
//	}

	private List<TypeReference> superClassReferences(ComponentRelation relation, Type context) throws LookupException {
		Java language = relation.language(Java.class);
		List<TypeReference> result = new ArrayList<TypeReference>();
		TypeReference superReference = relation.componentTypeReference().clone();
		superReference.setUniParent(relation);
		substituteTypeParameters(superReference);
		Type parent = relation.nearestAncestor(Type.class);
		Type type = superReference.getType();
		String superTypeName = type.getFullyQualifiedName();
		boolean toImpl = false;
		if(isJLo(parent) && ! splitClass(parent)) {
			toImpl = true;
		}
		BasicJavaTypeReference expandedSuperTypeReference = language.createTypeReference(superTypeName);
		List<ActualTypeArgument> typeArguments = ((BasicJavaTypeReference)superReference).typeArguments();
		for(ActualTypeArgument arg: typeArguments) {
			TypeReference tref = arg.substitutionReference();
			Type trefType = tref.getElement();
			BasicJavaTypeReference expandedTrefTypeReference = language.createExpandedTypeReference(trefType);

			// Creating the non-local reference will disconnect 'tref' from its parent, so we must store
			// the association end to which it is connected.
			Association parentLink = tref.parentLink().getOtherRelation();
			expandedTrefTypeReference.parentLink().connectTo(parentLink);
			expandedSuperTypeReference.addArgument(arg);
			//		TypeReference nonLocalTref = language.createNonLocalTypeReference(tref, tref.getElement());
			//		nonLocalTref.parentLink().connectTo(parentLink);
		}
		TypeReference nonLocal = language.createNonLocalTypeReference(expandedSuperTypeReference, language.defaultNamespace());
		superReference.setUniParent(null);

		//	result.add(superReference);
		if(toImpl) {
			nonLocal.setMetadata(new TagImpl(), IMPL);
		}
		result.add(nonLocal);
		Set<ComponentRelation> superSubobjects = (Set<ComponentRelation>) relation.overriddenMembers();
		Set<String> doneFQNs = new HashSet<String>();
		for(ComponentRelation superSubobject: superSubobjects) {
			Element origin = superSubobject.origin();
			BasicJavaTypeReference tref;
			String fqn;
			if(origin != superSubobject) {
				ComponentRelation tmp = superSubobject;
				superSubobject = (ComponentRelation) origin; 
				fqn = innerClassFQN(superSubobject);
				tref = language.createTypeReference(fqn);
				copyTypeParametersFromAncestors(superSubobject.componentType(), tref);
				tref.setUniParent(superSubobject);
				substituteTypeParameters(tref);
				tref.setUniParent(null);
			} else {
				fqn = innerClassFQN(superSubobject);
				tref = language.createTypeReference(fqn);
			}
			if(! doneFQNs.contains(fqn)) {
				result.add(language.createNonLocalTypeReference(tref, language.defaultNamespace()));
				doneFQNs.add(fqn);
			}
		}
		return result;
	}

	private String innerClassFQN(ComponentRelation relation) throws LookupException {
		return relation.componentType().getFullyQualifiedName();//innerClassName(relation.signature()); 
	}

private void processInnerClassMethod(ComponentRelation relationBeingTranslated, Type result) throws LookupException {
	Type componentType = relationBeingTranslated.referencedComponentType();
	List<Method> localMethods = componentType.directlyDeclaredMembers(Method.class);
	for(Method method: localMethods) {
		if(method.is(method.language(ObjectOrientedLanguage.class).CONSTRUCTOR) == Ternary.TRUE) {
			NormalMethod clone = (NormalMethod) method.clone();
			clone.setUniParent(method.parent());
			for(BasicTypeReference tref: clone.descendants(BasicTypeReference.class)) {
				if(tref.getTarget() == null) {
				  Type element = tref.getElement();
					Type base = element.baseType();
				  if((! (element instanceof InstantiatedParameterType)) && base instanceof RegularType) {
				  	String fqn = base.getFullyQualifiedName();
				  	String qn = Util.getAllButLastPart(fqn);
				  	if(qn != null && (! qn.isEmpty())) {
				  		tref.setTarget(new SimpleReference<TargetDeclaration>(qn, TargetDeclaration.class));
				  	}
				  }
				}
			}
			clone.setUniParent(null);
			String name = result.signature().name();
			RegularImplementation impl = (RegularImplementation) clone.implementation();
			Block block = new Block();
			impl.setBody(block);
			// substitute parameters before replace the return type, method name, and the body.
			// the types are not known in the component type, and the super class of the component type
			// may not have a constructor with the same signature as the current constructor.
			substituteTypeParameters(method, clone);
			MethodInvocation inv = new SuperConstructorDelegation();
			useParametersInInvocation(clone, inv);
			block.addStatement(new StatementExpression(inv));
			clone.setReturnTypeReference(relationBeingTranslated.language(Java.class).createTypeReference(name));
			((SimpleNameMethodHeader)clone.header()).setName(name);
			result.add(clone);
		}
	}
}

/**
 * Incorporate the imports of the namespace part of the declared type of the component relation to
 * the namespace part of the component relation.
 * @param relationBeingTranslated
 * @throws LookupException
 */
private void incorporateImports(ComponentRelation relationBeingTranslated) throws LookupException {
	incorporateImports(relationBeingTranslated, relationBeingTranslated.farthestAncestor(NamespaceDeclaration.class));
}


}
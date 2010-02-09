package subobjectjava.translate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jnome.core.language.Java;
import jnome.core.type.JavaTypeReference;

import org.apache.log4j.BasicConfigurator;
import org.rejuse.association.Association;
import org.rejuse.association.SingleAssociation;

import subobjectjava.input.SubobjectJavaModelFactory;
import subobjectjava.model.component.ComponentRelation;
import subobjectjava.model.language.SubobjectJava;
import subobjectjava.model.language.SubobjectJavaOverridesRelation;
import chameleon.core.element.Element;
import chameleon.core.expression.ActualArgument;
import chameleon.core.expression.Invocation;
import chameleon.core.expression.NamedTargetExpression;
import chameleon.core.language.Language;
import chameleon.core.lookup.LookupException;
import chameleon.core.member.Member;
import chameleon.core.method.Method;
import chameleon.core.method.RegularImplementation;
import chameleon.core.method.RegularMethod;
import chameleon.core.namespace.Namespace;
import chameleon.core.namespace.RootNamespace;
import chameleon.core.statement.Block;
import chameleon.core.type.Type;
import chameleon.core.variable.FormalParameter;
import chameleon.exception.ChameleonProgrammerException;
import chameleon.input.ParseException;
import chameleon.support.expression.AssignmentExpression;
import chameleon.support.member.simplename.SimpleNameMethodHeader;
import chameleon.support.member.simplename.method.NormalMethod;
import chameleon.support.member.simplename.method.RegularMethodInvocation;
import chameleon.support.member.simplename.variable.MemberVariableDeclarator;
import chameleon.support.modifier.Protected;
import chameleon.support.modifier.Public;
import chameleon.support.statement.ReturnStatement;
import chameleon.support.statement.StatementExpression;
import chameleon.support.variable.VariableDeclaration;
import chameleon.test.provider.BasicDescendantProvider;
import chameleon.test.provider.ElementProvider;

public class JavaTranslator {

	public JavaTranslator(SubobjectJava language, ElementProvider<Namespace> namespaceProvider) throws ParseException, IOException {
		super();
		_language = language;
		_typeProvider = new BasicDescendantProvider<Type>(namespaceProvider, Type.class);
	}
	
	public ElementProvider<Type> typeProvider() {
		return _typeProvider;
	}

	private ElementProvider<Type> _typeProvider;

	public Java translate() throws ParseException, IOException, ChameleonProgrammerException, LookupException {
		RootNamespace clone = language().defaultNamespace().clone();
		Java result = new Java();
		result.cloneConnectorsFrom(language());
		result.cloneProcessorsFrom(language());
		result.setDefaultNamespace(clone);
		for(Type type: typeProvider().elements(result)) {
			translate(type);
		}
		return result;
	}
	
	public Language language() throws ParseException, IOException {
		return _language;
	}
	
	private Language _language;

	public void translate(Type type) throws ChameleonProgrammerException, LookupException {
		List<ComponentRelation> relations = type.directlyDeclaredMembers(ComponentRelation.class);
		for(ComponentRelation relation : relations) {
			MemberVariableDeclarator fieldForComponent = fieldForComponent(relation);
			if(fieldForComponent != null) {
			  type.add(fieldForComponent);
			}
			Method setterForComponent = setterForComponent(relation);
			if(setterForComponent != null) {
				type.add(setterForComponent);
			}
			type.addAll(aliasMethods(relation));
			SingleAssociation<ComponentRelation, Element> parentLink = relation.parentLink();
			Association<? extends Element, ? super ComponentRelation> childLink = parentLink.getOtherRelation();
			Method getterForComponent = getterForComponent(relation);
			if(getterForComponent != null) {
			  childLink.replace(parentLink, getterForComponent.parentLink());
			} else {
				relation.disconnect();
			}
		}
	}
	
	public MemberVariableDeclarator fieldForComponent(ComponentRelation relation) throws LookupException {
		if(! overrides(relation)) {
		MemberVariableDeclarator result = new MemberVariableDeclarator(relation.componentTypeReference().clone());
		result.add(new VariableDeclaration(fieldName(relation)));
		return result;
		} else {
			return null;
		}
	}
	
	public String getterName(ComponentRelation relation) {
		return relation.signature().name()+"__component";
	}
	
	public Method getterForComponent(ComponentRelation relation) throws LookupException {
		if(! overrides(relation)) {
		RegularMethod result = new NormalMethod(new SimpleNameMethodHeader(getterName(relation)), relation.componentTypeReference().clone());
		result.addModifier(new Public());
		Block body = new Block();
		result.setImplementation(new RegularImplementation(body));
		body.addStatement(new ReturnStatement(new NamedTargetExpression(fieldName(relation), null)));
		return result;
		} else {
			return null;
		}
	}
	
	public String setterName(ComponentRelation relation) {
		return "set__component__"+relation.signature().name();
	}
	
	public Method setterForComponent(ComponentRelation relation) throws LookupException {
		if(! overrides(relation)) {
		String name = relation.signature().name();
		RegularMethod result = new NormalMethod(new SimpleNameMethodHeader(setterName(relation)), new JavaTypeReference("void"));
		result.header().addParameter(new FormalParameter(name, relation.componentTypeReference().clone()));
		result.addModifier(new Protected());
		Block body = new Block();
		result.setImplementation(new RegularImplementation(body));
		NamedTargetExpression componentFieldRef = new NamedTargetExpression(fieldName(relation), null);
		body.addStatement(new StatementExpression(new AssignmentExpression(componentFieldRef, new NamedTargetExpression(name, null))));
		return result;
		} else {
			return null;
		}
	}
	
	private boolean overrides(ComponentRelation relation) throws LookupException {
		Type type = relation.nearestAncestor(Type.class);
		for(Type superType: type.getDirectSuperTypes()) {
			List<ComponentRelation> superComponents = superType.members(ComponentRelation.class);
			for(ComponentRelation superComponent: superComponents) {
				if(new SubobjectJavaOverridesRelation().contains(relation, superComponent)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<Method> aliasMethods(ComponentRelation relation) throws LookupException {
		List<Method> result = new ArrayList<Method>();
		List<? extends Member> members = relation.getIntroducedMembers();
		members.remove(relation);
		for(Member member: members) {
			result.add(aliasFor(member, relation));
		}
		return result;
	
	}
	public Method aliasFor(Member member, ComponentRelation relation) throws LookupException{
		if(member instanceof Method) {
			Method<?,?,?,?> method = (Method) member;
			Method origin = (Method) method.origin();
			Method result = new NormalMethod(method.header().clone(), new JavaTypeReference(method.returnType().getFullyQualifiedName()));
			Block body = new Block();
			result.setImplementation(new RegularImplementation(body));
			Invocation invocation = new RegularMethodInvocation(origin.name(), new NamedTargetExpression(fieldName(relation), null));
			// pass parameters.
			for(FormalParameter param: method.formalParameters()) {
				invocation.addArgument(new ActualArgument(new NamedTargetExpression(param.signature().name(), null)));
			}
			if(origin.returnType().getFullyQualifiedName().equals("void")) {
				body.addStatement(new StatementExpression(invocation));
			} else {
				body.addStatement(new ReturnStatement(invocation));
			}
			return result;
		} else {
			throw new ChameleonProgrammerException("Translation of member of type "+member.getClass().getName()+" not supported.");
		}
	}
	
	public String fieldName(ComponentRelation relation) {
		return "__component_" + relation.signature().name();
	}
	
	
  /**
   * args[0] = path for the directory to write output
   * args[1] = path to read input files
   * ...1 or more input paths possible...
   * args[i] = fqn of package to read, let this start with "@" to read the package recursively
   *...1 or more packageFqns possible...
   * args[n] = fqn of package to read, let this start with "#" to NOT read the package recursively.
   *...1 or more packageFqns possible...
   *
   * Example 
   * java Copy outputDir baseInputDir customInputDir1 customInputDir2 @myPackage.subPackage 
   */
  public static void main(String[] args) throws Exception {
    if(args.length < 2) {
      System.out.println("Usage: java .... JavaTranslator outputDir inputDir* @recursivePackageFQN* #packageFQN* $typeFQN*");
    }
    BasicConfigurator.configure();
    ProviderProvider provider = new ProviderProvider(new SubobjectJavaModelFactory(),".java",true,true);
    provider.processArguments(args);
    Java result = new JavaTranslator((SubobjectJava) provider.language(), provider.namespaceProvider()).translate();
    // Output
    File outputDir = provider.outputDir();
    TypeWriter writer = new TypeWriter(result, new BasicDescendantProvider<Type>(provider.namespaceProvider(), Type.class),outputDir);
    writer.write();
  }
}

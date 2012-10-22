package dk.glasius.transformation

import dk.glasius.annotations.EnumMessageSourceResolvable
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*

import static org.codehaus.groovy.ast.expr.MethodCallExpression.NO_ARGUMENTS
import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION

@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class EnumMessageSourceResolvableTransformation extends AbstractASTTransformation {
	static final Class MY_CLASS = EnumMessageSourceResolvable.class;
	static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
	static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
	private static final Object[] EMPTY_OBJECT_ARRAY = [] as Object[]


	void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
		if(!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
			throw new RuntimeException("Internal error: wrong types: ${nodes[0].class} / ${nodes[1].class}")
		}
		AnnotationNode annotationNode = (AnnotationNode) nodes[0]
		AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];

		String prefix = getMemberStringValue(annotationNode, 'prefix')
		String postfix = getMemberStringValue(annotationNode, 'postfix')
		boolean shortName = memberHasValue(annotationNode, 'shortName', true)
		DefaultNameCase defaultNameCase = getEnumAnnotationParam(annotationNode, 'defaultNameCase', DefaultNameCase, DefaultNameCase.UNCHANGED)
		System.err.println()
		System.err.println("Annotation settings. Prefix: ${prefix}, postfix: ${postfix}, shortName: ${shortName}, defaultNameCase: ${defaultNameCase}")

		if(annotatedNode instanceof ClassNode) {
			ClassNode classNode = (ClassNode) annotatedNode;
			addInterface(classNode)
			addGetDefaultMessageMetod(classNode, defaultNameCase)
			addGetCodesMetod(classNode, prefix, postfix, shortName)
			addGetArgumentsMetod(classNode)
		}
	}


	private addInterface(ClassNode classNode) {
		def clazz = ClassHelper.make(org.springframework.context.MessageSourceResolvable)
		classNode.addInterface(clazz)
	}


	private addGetDefaultMessageMetod(ClassNode source, final defaultNameCase) {
		def block = new BlockStatement()
		def nameExpression = new MethodCallExpression(THIS_EXPRESSION, 'name', NO_ARGUMENTS)
		def expression = nameExpression
		switch(defaultNameCase) {
			case DefaultNameCase.CAPITALIZE:
				expression = new MethodCallExpression(nameExpression, 'toLowerCase', NO_ARGUMENTS)
				expression = new MethodCallExpression(expression, 'capitalize', NO_ARGUMENTS)
				break;
			case DefaultNameCase.LOWER_CASE:
				expression = new MethodCallExpression(nameExpression, 'toLowerCase', NO_ARGUMENTS)
				break;
			case DefaultNameCase.UPPER_CASE:
				expression = new MethodCallExpression(nameExpression, 'toUpperCase', NO_ARGUMENTS)
				break;
		}

		block.addStatement(new ReturnStatement(expression))

		def method = new MethodNode("getDefaultMessage", ACC_PUBLIC, ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block)
		System.err.println("${method.name} ${method.returnType.typeClass}")
		source.addMethod(method)
	}


	private addGetArgumentsMetod(ClassNode source) {
		def block = new BlockStatement()
		def arrayExpression = new ArrayExpression(ClassHelper.make(Object), [])
		block.addStatement(new ReturnStatement(arrayExpression))

		def method = new MethodNode("getArguments", ACC_PUBLIC, arrayExpression.type, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block)
		System.err.println("${method.name} ${method.returnType.typeClass}")
		source.addMethod(method)
	}


	private addGetCodesMetod(ClassNode source, String prefix, String postfix, boolean shortName) {
		def block = new BlockStatement()
		def enumName = new MethodCallExpression(THIS_EXPRESSION, 'name', NO_ARGUMENTS)
		def enumNameLowerCase = new MethodCallExpression(enumName, 'toLowerCase', NO_ARGUMENTS)
		def className = new MethodCallExpression(new MethodCallExpression(THIS_EXPRESSION, 'getClass', NO_ARGUMENTS), shortName ? 'getSimpleName' : 'getName', NO_ARGUMENTS)

		def expression = '${_class}.${_name}'
		if(prefix) {
			expression = prefix + '.' + expression
		}
		if(postfix) {
			expression = expression + '.' + postfix
		}
		def upperCase = new GStringExpression('', [new ConstantExpression(''), new ConstantExpression('.')], [className, enumName])
		def lowerCase = new GStringExpression('', [new ConstantExpression(''), new ConstantExpression('.')], [className, enumNameLowerCase])

		def arrayExpression = new ArrayExpression(new ClassNode(String), [upperCase, lowerCase])
		block.addStatement(new ReturnStatement(arrayExpression))

		def method = new MethodNode("getCodes", ACC_PUBLIC, arrayExpression.type, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block)
		System.err.println("${method.name} ${method.returnType.typeClass}")
		source.addMethod(method)
	}


	private getEnumAnnotationParam(AnnotationNode node, String parameterName, Class type, defaultValue) {
		def member = node.getMember(parameterName)
		if(member) {
			if(member instanceof PropertyExpression) {
				try {
					return Enum.valueOf(type, ((PropertyExpression) member).propertyAsString)
				} catch(e) {
					throw new RuntimeException("Expecting ${type.name} value for ${parameterName} annotation parameter. Found $member")
				}
			} else {
				throw new RuntimeException("Expecting ${type.name} value for ${parameterName} annotation parameter. Found $member")
			}
		}
		return defaultValue
	}
}

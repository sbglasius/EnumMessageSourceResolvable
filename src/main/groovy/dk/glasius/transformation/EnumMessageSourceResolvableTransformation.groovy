package dk.glasius.transformation

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.*

@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class EnumMessageSourceResolvableTransformation implements ASTTransformation {

	String prefix
	String postfix
	boolean shortName
	DefaultNameCase defaultNameCase


	void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
		if(!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
			throw new RuntimeException("Internal error: wrong types: ${nodes[0].class} / ${nodes[1].class}")
		}
		AnnotationNode annotationNode = (AnnotationNode) nodes[0]
		AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];

		prefix = getConstantAnnotationParameter(annotationNode, 'prefix', String, '')
		postfix = getConstantAnnotationParameter(annotationNode, 'postfix', String, '')
		shortName = getConstantAnnotationParameter(annotationNode, 'shortName', Boolean, false)
		defaultNameCase = getConstantAnnotationParameter(annotationNode, 'defaultNameCase', DefaultNameCase, DefaultNameCase.UPPER_CASE)
		System.err.println()
		System.err.println("Annotation settings. Prefix: ${prefix}, postfix: ${postfix}, shortName: ${shortName}, defaultNameCase: ${defaultNameCase}")

		if(annotatedNode instanceof ClassNode) {
			ClassNode classNode = (ClassNode) annotatedNode;
			addInterface(classNode)
			addMetodes(classNode)
		}
	}


	private addInterface(ClassNode classNode) {
		def clazz = ClassHelper.make(org.springframework.context.MessageSourceResolvable)
		classNode.addInterface(clazz)
	}


	private addMetodes(ClassNode source) {
		List<ASTNode> methodes = new AstBuilder().buildFromSpec {
			method('getDefaultMessage', Opcodes.ACC_PUBLIC, String) {
				parameters {}
				exceptions {}
				block {
					switch(defaultNameCase) {
						case DefaultNameCase.CAPITALIZE:
							owner.expression.addAll new AstBuilder().buildFromCode {
								return "$name()".toLowerCase().capitalize()
							}
							break
						case DefaultNameCase.LOWER_CASE:

							owner.expression.addAll new AstBuilder().buildFromCode {
								return "$name()".toLowerCase()
							}
							break
						case DefaultNameCase.UPPER_CASE:
							owner.expression.addAll new AstBuilder().buildFromCode {
								return "$name()".toUpperCase()
							}
							break
					}
				}
				annotations {}
			}
				block {
					owner.expression.addAll new AstBuilder().buildFromCode {
						[] as Object


						method('getArguments', Opcodes.ACC_PUBLIC, Object[]) {
							parameters {}
							exceptions {}
					}
				}
				annotations {}
			}
			method('getCodes', Opcodes.ACC_PUBLIC, String[]) {
				parameters {}
				exceptions {}
				expression {
					declaration {
						variable "shortName"
						token "="
						constantExpression { shortName
						}
					}
				}
				block {
					owner.expression.addAll new AstBuilder().buildFromCode {
						def className = "${getClass().name}."
						def enumName = name()
						["$className$enumName", "$className${enumName.toLowerCase()}", shortName] as String[]
					}
				}
				annotations {}
			}
		}
		methodes.each { source.addMethod(it) }
	}


	static def getConstantAnnotationParameter(AnnotationNode node, String parameterName, Class type, defaultValue) {
		def member = node.getMember(parameterName)
		if(member) {
			if(member instanceof ConstantExpression) {
				try {
					return member.value.asType(type)
				} catch(e) {
					throw new RuntimeException("Expecting ${type.name} value for ${parameterName} annotation parameter. Found $member")
				}
			} else if(member instanceof PropertyExpression) {
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

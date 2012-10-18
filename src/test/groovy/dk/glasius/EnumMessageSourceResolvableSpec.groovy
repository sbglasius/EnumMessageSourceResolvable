package dk.glasius

import spock.lang.Specification
import org.springframework.context.MessageSourceResolvable


class EnumMessageSourceResolvableSpec extends Specification {
	def "test that the enum has an interface of MessageSourceResolvable"() {
		expect:
		MessageSourceResolvable in DefaultAnnotatedEnum.class.interfaces
	}

	def "test that the default annotated enum default message returns correct values"() {
		expect:
		DefaultAnnotatedEnum.ONE.defaultMessage == 'ONE'
		DefaultAnnotatedEnum.TWO.defaultMessage == 'TWO'
	}

	def "test that the default annotated enum codes returns correct values"(){
		expect:
		DefaultAnnotatedEnum.ONE.codes == ['dk.glasius.DefaultAnnotatedEnum.ONE','dk.glasius.DefaultAnnotatedEnum.one']
		DefaultAnnotatedEnum.TWO.codes == ['dk.glasius.DefaultAnnotatedEnum.TWO','dk.glasius.DefaultAnnotatedEnum.two']
	}

	def "test that the default annotated enum arguments returns correct values"(){
		expect:
		DefaultAnnotatedEnum.ONE.arguments == []
		DefaultAnnotatedEnum.TWO.arguments == []
	}

	def "test that the defaultNameCase CAPITALIZE annotated enum returns correct values from default message"() {
		expect:
		NameCaseCapitalizedAnnotatedEnum.ONE.defaultMessage == 'One'
		NameCaseCapitalizedAnnotatedEnum.TWO.defaultMessage == 'Two'
	}
	def "test that the defaultNameCase LOWER_CASE annotated enum returns correct values from default message"() {
		expect:
		NameCaseLowerCasedAnnotatedEnum.ONE.defaultMessage == 'one'
		NameCaseLowerCasedAnnotatedEnum.TWO.defaultMessage == 'two'
	}

	def "test that the shortName annotated enum returns correct values from code"() {
		expect:
		ShortNamedAnnotatedEnum.ONE.codes == ['ONE','one']
		ShortNamedAnnotatedEnum.TWO.codes == ['TWO','two']
	}
}

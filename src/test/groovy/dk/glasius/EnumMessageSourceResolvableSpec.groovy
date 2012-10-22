package dk.glasius

import spock.lang.Specification

class EnumMessageSourceResolvableSpec extends Specification {

	def "test that the defaultNameCase CAPITALIZE annotated enum returns correct values from default message"() {
		expect:
		NameCaseAnnotatedEnumSpec.ONE.defaultMessage == 'One'
		NameCaseAnnotatedEnumSpec.TWO.defaultMessage == 'Two'
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

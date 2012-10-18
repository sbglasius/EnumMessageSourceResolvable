package dk.glasius

import dk.glasius.annotations.EnumMessageSourceResolvable
import dk.glasius.transformation.DefaultNameCase

@EnumMessageSourceResolvable(defaultNameCase = DefaultNameCase.CAPITALIZE)
public enum NameCaseCapitalizedAnnotatedEnum {
	ONE,
	TWO
}

package dk.glasius

import dk.glasius.annotations.EnumMessageSourceResolvable
import dk.glasius.transformation.DefaultNameCase

@EnumMessageSourceResolvable(defaultNameCase = DefaultNameCase.LOWER_CASE)
public enum NameCaseLowerCasedAnnotatedEnum {
	ONE,
	TWO
}

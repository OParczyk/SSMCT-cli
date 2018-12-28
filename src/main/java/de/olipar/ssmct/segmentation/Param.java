package de.olipar.ssmct.segmentation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention (RetentionPolicy.RUNTIME)
public @interface Param {
	ParameterType type();
	
	ParameterDisplayType displayType();
	
	String name();

	long max() default Long.MAX_VALUE;

	long min() default Long.MIN_VALUE;

	String regex() default "";
}

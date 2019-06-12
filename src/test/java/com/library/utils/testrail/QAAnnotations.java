package com.library.utils.testrail;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public class QAAnnotations {
	
	/* For Test Case information gathered from java test classes for each test method
	 * 	title = 
	 */
	@Documented
	@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
	@Target(METHOD)
	public @interface TestCaseInfo{
		String testCaseID();
		String title();
		String description() default "";
		String bug() default "";
		boolean isAutomationBug() default false;
	}
	
	@Documented
	@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
	public @interface TestClassInfo{
		String projectName();
		String suiteName();
		String sectionName();
	}
	
}

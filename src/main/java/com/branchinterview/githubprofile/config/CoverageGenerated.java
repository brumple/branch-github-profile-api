package com.branchinterview.githubprofile.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks boilerplate or defensive code that should be ignored by JaCoCo coverage checks.
 * Use only for code that does not represent testable application behavior.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
public @interface CoverageGenerated {
}

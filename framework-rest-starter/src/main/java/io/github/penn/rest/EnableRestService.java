package io.github.penn.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author tangzhongping
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableWebContext
public @interface EnableRestService {

  /**
   * path for scan service
   *
   * @return
   */
  String[] basePackage() default {};

}

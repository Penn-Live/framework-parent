package io.github.penn.rest;

import java.lang.annotation.*;

/**
 * @author tangzhongping
 */

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InjectWebContext {
}

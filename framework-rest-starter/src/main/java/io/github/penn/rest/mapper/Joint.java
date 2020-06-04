package io.github.penn.rest.mapper;

import java.lang.annotation.*;

/**
 * @author tangzhongping
 */
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Joint {

    String domain() default "";

    String using() default "";

    boolean skip() default false;

}

package io.github.penn.rest.call;

import java.lang.annotation.*;

/**
 * @author tangzhongping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GetCall {

    /**
     * service path
     */
    String path() default "";

    /**
     * hits for rest service
     */
    String hint() default "";

}

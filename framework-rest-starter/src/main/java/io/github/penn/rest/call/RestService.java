package io.github.penn.rest.call;

import java.lang.annotation.*;

/**
 * @author tangzhongping
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestService {

    /**
     * what domain you request for,may config in SPEL
     *
     */
    String domain() default "";
}

package io.github.penn.rest;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author tangzhongping
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RestConfigurationSelector.class)
public @interface EnableWebContext {
}

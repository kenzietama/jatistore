package com.indivaragroup.jatistore.audit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    String action();
    String affectedModule();
    String description() default "";
}

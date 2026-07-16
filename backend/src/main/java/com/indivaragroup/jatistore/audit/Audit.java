package com.indivaragroup.jatistore.audit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    String action();           // contoh: "PRODUCT_UPDATE"
    String affectedModule();   // contoh: "PRODUCT"
    String description() default "";
}

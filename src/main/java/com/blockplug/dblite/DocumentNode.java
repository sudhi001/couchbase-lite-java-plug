package com.blockplug.dblite;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DocumentNode {

    String name() default "";
    String title() default "";

}


package io.netlibs.zzz.jersey.cors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessControl {

  String allowOrigin() default "";

  String exposeHeaders() default "";

  Ternary allowCredentials() default Ternary.NEUTRAL;

  public enum Ternary {
    TRUE,
    FALSE,
    NEUTRAL
  }

  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Preflight {

    public static final int UNSET_MAX_AGE = -1;

    int maxAge() default UNSET_MAX_AGE;

    String allowMethods() default "";

    String allowHeaders() default "";

    Ternary allowCredentials() default Ternary.NEUTRAL;

    String allowOrigin() default "";

  }

}

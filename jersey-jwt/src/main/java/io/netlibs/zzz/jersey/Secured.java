package io.netlibs.zzz.jersey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE })
public @interface Secured {

  //
  String[] scopes() default {};

  //
  String[] audience() default {};

  //
  String[] issuer() default {};

}

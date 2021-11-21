package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mark fields you want to save in the database.
 * currently only applies to primitives and strings
 * not reference types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveFieldMMM {
    boolean isPrimaryKey() default false;
    boolean isUnique() default false;
    boolean isNotNull() default false;
    //Object hasDefault() default null;
    //TODO hasCondition //and would somehow have a functional interface here or something IDK
}

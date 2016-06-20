/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author vpc
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
@UpaAware
//@DependsOn("bootstrapService")
@Scope(value = "singleton")
public @interface AppPlugin {

    String version() default "1.0";

    String[] dependsOn() default {};
}
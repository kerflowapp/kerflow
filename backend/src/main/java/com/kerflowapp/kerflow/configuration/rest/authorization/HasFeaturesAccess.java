package com.kerflowapp.kerflow.configuration.rest.authorization;

import com.kerflowapp.kerflow.domain.enums.FeatureFlag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasFeaturesAccess {

    FeatureFlag[] features();
    
}

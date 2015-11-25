package com.pastdev.httpcomponents.annotations;


import com.pastdev.httpcomponents.factory.NullParamValueFactory;
import com.pastdev.httpcomponents.factory.ParamValueFactory;


public @interface Param {
    public String paramName();

    public String paramValue() default "";

    public Class<? extends ParamValueFactory> paramValueFactory() default NullParamValueFactory.class;

    public FactoryParam[] factoryParams() default {};
}

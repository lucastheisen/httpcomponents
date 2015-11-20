package com.pastdev.httpcomponents.factory;

import java.util.Properties;


import com.pastdev.httpcomponents.annotations.Server;

public interface PropertiesFactory {
    Properties properties( Server server );
}

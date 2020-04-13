package com.querydsl.r2dbc.test.env.factory;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;

public class ConfigurationFactory {

    Configuration createConfiguration() {
        return new Configuration(new PostgreSQLTemplates(true));
    }

}

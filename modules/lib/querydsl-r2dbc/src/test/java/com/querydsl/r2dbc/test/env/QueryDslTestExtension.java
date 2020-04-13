package com.querydsl.r2dbc.test.env;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

public class QueryDslTestExtension implements Extension, BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        QueryDslTestEnvironment.getInstance().prepareTest();
    }

}

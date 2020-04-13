package com.querydsl.r2dbc.test.env.runner;

class SchemaAlter {

    private final TestRunner.Environment environment;

    SchemaAlter(TestRunner.Environment environment) {
        this.environment = environment;
    }

    private void executeStatement(String sql) {
        environment.executeStatement(sql);
    }

    void create() {
        executeStatement("\n" +
                "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
        executeStatement("\n" +
                "create table \"Locale\"\n" +
                "(\n" +
                "\t\"LanguageCode\" varchar(2) not null,\n" +
                "\t\"CountryCode\" varchar(2) not null,\n" +
                "\t\"NativeName\" text not null,\n" +
                "\t\"EnglishName\" text not null,\n" +
                "\t\"Description\" varchar(255) null,\n" +
                "\tconstraint \"PK_Locale\"\n" +
                "\t\tprimary key (\"LanguageCode\", \"CountryCode\")\n" +
                ");");
        executeStatement("\n" +
                "create table \"User\"\n" +
                "(\n" +
                "\t\"Id\" bigserial not null\n" +
                "\t\tconstraint \"PK_User\"\n" +
                "\t\t\tprimary key,\n" +
                "\t\"PublicId\" uuid not null\n" +
                "\t\tconstraint \"UK_User_PublicId\"\n" +
                "\t\t  unique,\n" +
                "\t\"CreationTime\" timestamp not null,\n" +
                "\t\"Disabled\" boolean not null,\n" +
                "\t\"PersonName\" text not null,\n" +
                "\t\"PreferredLocaleLanguageCode\" varchar(2) not null,\n" +
                "\t\"PreferredLocaleCountryCode\" varchar(2) not null,\n" +
                "\tconstraint \"FK_User_PreferredLocale\"\n" +
                "\t\tforeign key (\"PreferredLocaleLanguageCode\", \"PreferredLocaleCountryCode\")\n" +
                "\t\t  references \"Locale\"\n" +
                ");");
    }

    void drop() {
        executeStatement("drop table \"User\";");
        executeStatement("drop table \"Locale\";");
    }

}

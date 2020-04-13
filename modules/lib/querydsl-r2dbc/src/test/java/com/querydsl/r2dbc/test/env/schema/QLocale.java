package com.querydsl.r2dbc.test.env.schema;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;
import java.util.Arrays;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QLocale is a Querydsl query type for SLocale
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLocale extends com.querydsl.sql.RelationalPathBase<SLocale> {

    private static final long serialVersionUID = -1502659822;

    public static final QLocale Locale = new QLocale("Locale");

    public final StringPath countryCode = createString("countryCode");

    public final StringPath englishName = createString("englishName");

    public final StringPath languageCode = createString("languageCode");

    public final StringPath nativeName = createString("nativeName");

    public final StringPath description = createString("description");

    public final com.querydsl.sql.PrimaryKey<SLocale> localePK = createPrimaryKey(languageCode, countryCode);

    public final com.querydsl.sql.ForeignKey<SUser> _userPreferredLocaleFK = createInvForeignKey(Arrays.asList(languageCode, countryCode), Arrays.asList("PreferredLocaleLanguageCode", "PreferredLocaleCountryCode"));

    public QLocale(String variable) {
        super(SLocale.class, forVariable(variable), "public", "Locale");
        addMetadata();
    }

    public QLocale(String variable, String schema, String table) {
        super(SLocale.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLocale(String variable, String schema) {
        super(SLocale.class, forVariable(variable), schema, "Locale");
        addMetadata();
    }

    public QLocale(Path<? extends SLocale> path) {
        super(path.getType(), path.getMetadata(), "public", "Locale");
        addMetadata();
    }

    public QLocale(PathMetadata metadata) {
        super(SLocale.class, metadata, "public", "Locale");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(countryCode, ColumnMetadata.named("CountryCode").withIndex(2).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(englishName, ColumnMetadata.named("EnglishName").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(languageCode, ColumnMetadata.named("LanguageCode").withIndex(1).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(nativeName, ColumnMetadata.named("NativeName").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(description, ColumnMetadata.named("Description").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
    }

}


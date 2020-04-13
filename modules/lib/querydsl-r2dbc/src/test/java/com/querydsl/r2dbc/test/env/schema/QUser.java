package com.querydsl.r2dbc.test.env.schema;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QUser is a Querydsl query type for SUser
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUser extends com.querydsl.sql.RelationalPathBase<SUser> {

    private static final long serialVersionUID = -10230141;

    public static final QUser User = new QUser("User");

    public final DateTimePath<LocalDateTime> creationTime = createDateTime("creationTime", LocalDateTime.class);

    public final BooleanPath disabled = createBoolean("disabled");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath personName = createString("personName");

    public final StringPath preferredLocaleCountryCode = createString("preferredLocaleCountryCode");

    public final StringPath preferredLocaleLanguageCode = createString("preferredLocaleLanguageCode");

    public final SimplePath<UUID> publicId = createSimple("publicId", UUID.class);

    public final com.querydsl.sql.PrimaryKey<SUser> userPK = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<SLocale> userPreferredLocaleFK = createForeignKey(Arrays.asList(preferredLocaleLanguageCode, preferredLocaleCountryCode), Arrays.asList("LanguageCode", "CountryCode"));

    public QUser(String variable) {
        super(SUser.class, forVariable(variable), "public", "User");
        addMetadata();
    }

    public QUser(String variable, String schema, String table) {
        super(SUser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUser(String variable, String schema) {
        super(SUser.class, forVariable(variable), schema, "User");
        addMetadata();
    }

    public QUser(Path<? extends SUser> path) {
        super(path.getType(), path.getMetadata(), "public", "User");
        addMetadata();
    }

    public QUser(PathMetadata metadata) {
        super(SUser.class, metadata, "public", "User");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(creationTime, ColumnMetadata.named("CreationTime").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(disabled, ColumnMetadata.named("Disabled").withIndex(9).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("Id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(personName, ColumnMetadata.named("PersonName").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(preferredLocaleCountryCode, ColumnMetadata.named("PreferredLocaleCountryCode").withIndex(14).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(preferredLocaleLanguageCode, ColumnMetadata.named("PreferredLocaleLanguageCode").withIndex(13).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(publicId, ColumnMetadata.named("PublicId").withIndex(2).ofType(Types.OTHER).withSize(2147483647).notNull());
    }

}


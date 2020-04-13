package com.querydsl.r2dbc.test;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.corereactive.types.dsl.OptionalExpression;
import com.querydsl.r2dbc.test.env.QueryDslTest;
import com.querydsl.r2dbc.test.env.schema.QLocale;
import com.querydsl.r2dbc.test.env.schema.QUser;
import com.querydsl.r2dbc.test.env.schema.SUser;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.querydsl.r2dbc.test.env.QueryDslTestEnvironment.run;

@QueryDslTest
public class R2dbcQueryTest {

    private static final QLocale locale$ = QLocale.Locale;
    private static final QUser user$ = QUser.User;
    private static final Path<Long> constantNumber$ = Expressions.numberPath(Long.class, "ConstantNumber");
    private static final Path<Long> calculatedNumber$ = Expressions.numberPath(Long.class, "CalculatedNumber");

    @Test
    public void selectNull() {
        run(env ->
            Assertions.assertThrows(NullPointerException.class, () ->
                    env.query()
                            .select(Expressions.nullExpression())
                            .fetchOne()
                            .block()
            )
        );
    }

    @Test
    public void selectNullAsOptional() {
        run(env -> {
            Optional<Object> result = env.query()
                    .select(OptionalExpression.of(Expressions.nullExpression()))
                    .fetchOne()
                    .block();
            Assertions.assertNotNull(result);
            Assertions.assertFalse(result.isPresent(), "Should not present");
        });
    }

    @Test
    public void selectNotNullAsOptional() {
        run(env -> {
            Optional<Long> result = env.query()
                    .select(OptionalExpression.of(Expressions.asNumber(15L)))
                    .fetchOne()
                    .block();
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isPresent(), "Should present");
            Assertions.assertEquals(15L, result.get());
        });
    }

    @Test
    public void selectUnion() {
        run(env -> {
            // Given
            env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .execute();
            // When
            SubQueryExpression<Tuple> selectUK = env.query()
                    .from(locale$)
                    .select(locale$.countryCode, locale$.languageCode)
                    .where(locale$.countryCode.eq("UK"));
            SubQueryExpression<Tuple> selectUS = env.query()
                    .from(locale$)
                    .select(locale$.languageCode, locale$.countryCode)
                    .where(locale$.countryCode.eq("US"));
            List<Tuple> result = env.query()
                    .union(ImmutableList.of(selectUK, selectUS))
                    .fetch()
                    .collectList()
                    .block();
            // Then
            Assertions.assertNotNull(result);
            Assertions.assertEquals(2, result.size());
            Assert.assertEquals("en", result.get(0).get(0, String.class));
            Assert.assertEquals("US", result.get(0).get(1, String.class));
            Assert.assertEquals("UK", result.get(1).get(0, String.class));
            Assert.assertEquals("en", result.get(1).get(1, String.class));
        });
    }

    @Test
    public void selectUsersTuple() {
        run(env -> {
            // Given
            env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .execute();
            for (long id = 1; id <= 20; id++) {
                env.jdbcInsert(user$)
                        .set(user$.publicId, new UUID(id, 1))
                        .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                        .set(user$.disabled, false)
                        .set(user$.personName, "Person name " + id)
                        .set(user$.preferredLocaleLanguageCode, "en")
                        .set(user$.preferredLocaleCountryCode, "US")
                        .execute();
            }
            // When
            List<Tuple> list = env.query()
                    .select(
                            user$.id,
                            user$.publicId,
                            user$.creationTime,
                            user$.disabled,
                            user$.personName,
                            user$.preferredLocaleCountryCode,
                            user$.preferredLocaleLanguageCode,
                            locale$.englishName,
                            env.query()
                                    .select(Expressions.constant(50L))
                                    .as(constantNumber$),
                            user$.id.add(1)
                                    .as(calculatedNumber$)
                    )
                    .from(user$)
                    .leftJoin(locale$)
                    .on(user$.preferredLocaleCountryCode.eq(locale$.countryCode)
                            .and(user$.preferredLocaleLanguageCode.eq(locale$.languageCode)))
                    .where(user$.id.gt(10L))
                    .orderBy(user$.id.asc())
                    .limit(3)
                    .fetch()
                    .collectList()
                    .block();
            // Then
            Assertions.assertNotNull(list);
            Assertions.assertEquals(3, list.size());
            int index = 0;
            for (long id = 11; id <= 13; id++) {
                Tuple t = list.get(index);
                Assertions.assertEquals(id, t.get(user$.id));
                Assertions.assertEquals(new UUID(id, 1), t.get(user$.publicId));
                Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 12, 30, 20), t.get(user$.creationTime));
                Assertions.assertEquals(false, t.get(user$.disabled));
                Assertions.assertEquals("Person name " + id, t.get(user$.personName));
                Assertions.assertEquals("en", t.get(user$.preferredLocaleLanguageCode));
                Assertions.assertEquals("US", t.get(user$.preferredLocaleCountryCode));
                Assertions.assertEquals("English (US)", t.get(locale$.englishName));
                Assertions.assertEquals(50L, t.get(constantNumber$));
                Assertions.assertEquals(id + 1, t.get(calculatedNumber$));
                index++;
            }
        });
    }

    @Test
    public void selectUsersTupleWithUseLiterals() {
        run(env -> {
            // Given
            env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .execute();
            for (long id = 1; id <= 20; id++) {
                env.jdbcInsert(user$)
                        .set(user$.publicId, new UUID(id, 1))
                        .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                        .set(user$.disabled, false)
                        .set(user$.personName, "Person name " + id)
                        .set(user$.preferredLocaleLanguageCode, "en")
                        .set(user$.preferredLocaleCountryCode, "US")
                        .execute();
            }
            // When
            List<Tuple> list = env.query()
                    .withUseLiterals()
                    .select(
                            user$.id,
                            user$.publicId,
                            user$.creationTime,
                            user$.disabled,
                            user$.personName,
                            user$.preferredLocaleCountryCode,
                            user$.preferredLocaleLanguageCode,
                            locale$.englishName,
                            env.query()
                                    .select(Expressions.constant(50L))
                                    .as(constantNumber$),
                            user$.id.add(1)
                                    .as(calculatedNumber$)
                    )
                    .from(user$)
                    .leftJoin(locale$)
                    .on(user$.preferredLocaleCountryCode.eq(locale$.countryCode)
                            .and(user$.preferredLocaleLanguageCode.eq(locale$.languageCode)))
                    .where(user$.id.gt(10L))
                    .orderBy(user$.id.asc())
                    .limit(3)
                    .fetch()
                    .collectList()
                    .block();
            // Then
            Assertions.assertNotNull(list);
            Assertions.assertEquals(3, list.size());
            int index = 0;
            for (long id = 11; id <= 13; id++) {
                Tuple t = list.get(index);
                Assertions.assertEquals(id, t.get(user$.id));
                Assertions.assertEquals(new UUID(id, 1), t.get(user$.publicId));
                Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 12, 30, 20), t.get(user$.creationTime));
                Assertions.assertEquals(false, t.get(user$.disabled));
                Assertions.assertEquals("Person name " + id, t.get(user$.personName));
                Assertions.assertEquals("en", t.get(user$.preferredLocaleLanguageCode));
                Assertions.assertEquals("US", t.get(user$.preferredLocaleCountryCode));
                Assertions.assertEquals("English (US)", t.get(locale$.englishName));
                Assertions.assertEquals(50L, t.get(constantNumber$));
                Assertions.assertEquals(id + 1, t.get(calculatedNumber$));
                index++;
            }
        });
    }

    @Test
    public void selectUsersPojo() {
        run(env -> {
            // Given
            env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .execute();
            for (long id = 1; id <= 20; id++) {
                env.jdbcInsert(user$)
                        .set(user$.publicId, new UUID(id, 1))
                        .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                        .set(user$.disabled, false)
                        .set(user$.personName, "Person name " + id)
                        .set(user$.preferredLocaleLanguageCode, "en")
                        .set(user$.preferredLocaleCountryCode, "US")
                        .execute();
            }
            // When
            List<SUser> list = env.query()
                    .select(user$)
                    .from(user$)
                    .where(user$.id.gt(10L))
                    .orderBy(user$.id.asc())
                    .limit(3)
                    .fetch()
                    .collectList()
                    .block();
            // Then
            Assertions.assertNotNull(list);
            Assertions.assertEquals(3, list.size());
            int index = 0;
            for (long id = 11; id <= 13; id++) {
                SUser user = list.get(index);
                Assertions.assertEquals(id, user.getId());
                Assertions.assertEquals(new UUID(id, 1), user.getPublicId());
                Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 12, 30, 20), user.getCreationTime());
                Assertions.assertEquals(false, user.getDisabled());
                Assertions.assertEquals("Person name " + id, user.getPersonName());
                Assertions.assertEquals("en", user.getPreferredLocaleLanguageCode());
                Assertions.assertEquals("US", user.getPreferredLocaleCountryCode());
                index++;
            }
        });
    }

    @Test
    public void selectUsersWildcard() {
        run(env -> {
            // Given
            env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .execute();
            for (long id = 1; id <= 20; id++) {
                env.jdbcInsert(user$)
                        .set(user$.publicId, new UUID(id, 1))
                        .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                        .set(user$.disabled, false)
                        .set(user$.personName, "Person name " + id)
                        .set(user$.preferredLocaleLanguageCode, "en")
                        .set(user$.preferredLocaleCountryCode, "US")
                        .execute();
            }
            // When
            List<Object[]> list = env.query()
                    .select(Wildcard.all)
                    .from(user$)
                    .where(user$.id.gt(10L))
                    .orderBy(user$.id.asc())
                    .limit(3)
                    .fetch()
                    .collectList()
                    .block();
            // Then
            Assertions.assertNotNull(list);
            Assertions.assertEquals(3, list.size());
            int index = 0;
            for (long id = 11; id <= 13; id++) {
                Object[] user = list.get(index);
                Assertions.assertEquals(id, user[0]);
                Assertions.assertEquals(new UUID(id, 1), user[1]);
                Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 12, 30, 20), user[2]);
                Assertions.assertEquals(false, user[3]);
                Assertions.assertEquals("Person name " + id, user[4]);
                Assertions.assertEquals("en", user[5]);
                Assertions.assertEquals("US", user[6]);
                index++;
            }
        });
    }

    @Test
    public void selectUsersId() {
        run(env -> {
            // Given
            env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .addBatch()
                    .execute();
            for (long id = 1; id <= 20; id++) {
                env.jdbcInsert(user$)
                        .set(user$.publicId, new UUID(id, 1))
                        .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                        .set(user$.disabled, false)
                        .set(user$.personName, "Person name " + id)
                        .set(user$.preferredLocaleLanguageCode, "en")
                        .set(user$.preferredLocaleCountryCode, "US")
                        .execute();
            }
            // When
            List<Long> list = env.query()
                    .select(user$.id)
                    .from(user$)
                    .where(user$.id.gt(10L))
                    .orderBy(user$.id.asc())
                    .limit(3)
                    .fetch()
                    .collectList()
                    .block();
            // Then
            Assertions.assertNotNull(list);
            Assertions.assertEquals(3, list.size());
            int index = 0;
            for (long id = 11; id <= 13; id++) {
                Assertions.assertEquals(id, list.get(index));
                index++;
            }
        });
    }

}

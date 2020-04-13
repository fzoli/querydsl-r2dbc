package com.querydsl.r2dbc.test;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.r2dbc.test.env.QueryDslTest;
import com.querydsl.r2dbc.test.env.schema.QLocale;
import com.querydsl.r2dbc.test.env.schema.QUser;
import com.querydsl.r2dbc.test.env.schema.SLocale;
import com.querydsl.r2dbc.test.env.schema.SUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.querydsl.r2dbc.test.env.QueryDslTestEnvironment.run;

@QueryDslTest
public class R2dbcInsertTest {

    private static final QLocale locale$ = QLocale.Locale;
    private static final QUser user$ = QUser.User;

    @Test
    public void insert() {
        run(env -> {
            Long numRowsInserted = env.insert(locale$) // execute() test
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .setNull(locale$.description)
                    .execute()
                    .block();
            Assertions.assertEquals(1L, numRowsInserted);
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(1L, locales.size());
            SLocale locale = locales.get(0);
            Assertions.assertEquals("US", locale.getCountryCode());
            Assertions.assertEquals("en", locale.getLanguageCode());
            Assertions.assertEquals("English (US)", locale.getEnglishName());
            Assertions.assertEquals("English", locale.getNativeName());
            Assertions.assertNull(locale.getDescription());
            UUID generatedId = UUID.randomUUID();
            Long generatedUserId = env.insert(user$) // executeWithKey() test
                    .set(user$.publicId, generatedId)
                    .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                    .set(user$.disabled, false)
                    .set(user$.personName, "Person name")
                    .set(user$.preferredLocaleLanguageCode, "en")
                    .set(user$.preferredLocaleCountryCode, "US")
                    .executeWithKey(user$.id)
                    .block();
            Assertions.assertNotNull(generatedUserId);
            SUser user = env.jdbcQuery()
                    .select(user$)
                    .from(user$)
                    .where(user$.id.eq(generatedUserId))
                    .fetchOne();
            Assertions.assertEquals(generatedUserId, user.getId());
            Assertions.assertEquals(generatedId, user.getPublicId());
            Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 12, 30, 20), user.getCreationTime());
            Assertions.assertEquals(false, user.getDisabled());
            Assertions.assertEquals("Person name", user.getPersonName());
            Assertions.assertEquals("en", user.getPreferredLocaleLanguageCode());
            Assertions.assertEquals("US", user.getPreferredLocaleCountryCode());
        });
    }

    @Test
    public void insertWithUseLiterals() {
        run(env -> {
            Long numRowsInserted = env.insert(locale$)
                    .withUseLiterals()
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .setNull(locale$.description)
                    .execute()
                    .block();
            Assertions.assertEquals(1L, numRowsInserted);
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(1L, locales.size());
            SLocale locale = locales.get(0);
            Assertions.assertEquals("US", locale.getCountryCode());
            Assertions.assertEquals("en", locale.getLanguageCode());
            Assertions.assertEquals("English (US)", locale.getEnglishName());
            Assertions.assertEquals("English", locale.getNativeName());
            Assertions.assertNull(locale.getDescription());
        });
    }

    @Test
    public void insertExpression() {
        run(env -> {
            Long numRowsInserted = env.insert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, Expressions.stringTemplate("'English (US)'::text"))
                    .set(locale$.nativeName, "English")
                    .setNull(locale$.description)
                    .execute()
                    .block();
            Assertions.assertEquals(1L, numRowsInserted);
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(1L, locales.size());
            SLocale locale = locales.get(0);
            Assertions.assertEquals("US", locale.getCountryCode());
            Assertions.assertEquals("en", locale.getLanguageCode());
            Assertions.assertEquals("English (US)", locale.getEnglishName());
            Assertions.assertEquals("English", locale.getNativeName());
            Assertions.assertNull(locale.getDescription());
        });
    }

    @Test
    public void batchInsert() {
        run(env -> {
            Long numRowsInserted = env.insert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .set(locale$.description, "USA")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .set(locale$.description, "United Kingdom")
                    .addBatch()
                    .execute()
                    .block();
            Assertions.assertEquals(2L, numRowsInserted);
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(2L, locales.size());
            SLocale locale0 = locales.get(0);
            Assertions.assertEquals("US", locale0.getCountryCode());
            Assertions.assertEquals("en", locale0.getLanguageCode());
            Assertions.assertEquals("English (US)", locale0.getEnglishName());
            Assertions.assertEquals("English", locale0.getNativeName());
            Assertions.assertEquals("USA", locale0.getDescription());
            SLocale locale1 = locales.get(1);
            Assertions.assertEquals("UK", locale1.getCountryCode());
            Assertions.assertEquals("en", locale1.getLanguageCode());
            Assertions.assertEquals("English (UK)", locale1.getEnglishName());
            Assertions.assertEquals("English", locale1.getNativeName());
            Assertions.assertEquals("United Kingdom", locale1.getDescription());
        });
    }

    @Test
    public void batchBulkInsert() {
        run(env -> {
            Long numRowsInserted = env.insert(locale$)
                    .withBatchToBulk()
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .set(locale$.description, "USA")
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .set(locale$.description, "United Kingdom")
                    .addBatch()
                    .execute()
                    .block();
            Assertions.assertEquals(2L, numRowsInserted);
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(2L, locales.size());
            SLocale locale0 = locales.get(0);
            Assertions.assertEquals("US", locale0.getCountryCode());
            Assertions.assertEquals("en", locale0.getLanguageCode());
            Assertions.assertEquals("English (US)", locale0.getEnglishName());
            Assertions.assertEquals("English", locale0.getNativeName());
            Assertions.assertEquals("USA", locale0.getDescription());
            SLocale locale1 = locales.get(1);
            Assertions.assertEquals("UK", locale1.getCountryCode());
            Assertions.assertEquals("en", locale1.getLanguageCode());
            Assertions.assertEquals("English (UK)", locale1.getEnglishName());
            Assertions.assertEquals("English", locale1.getNativeName());
            Assertions.assertEquals("United Kingdom", locale1.getDescription());
        });
    }

    @Test
    public void batchInsertWithKeys() {
        run(env -> {
            Long numRowsInserted = env.insert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .set(locale$.description, "USA")
                    .execute()
                    .block();
            Assertions.assertEquals(1L, numRowsInserted);
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(1, locales.size());
            SLocale locale0 = locales.get(0);
            Assertions.assertEquals("US", locale0.getCountryCode());
            Assertions.assertEquals("en", locale0.getLanguageCode());
            Assertions.assertEquals("English (US)", locale0.getEnglishName());
            Assertions.assertEquals("English", locale0.getNativeName());
            Assertions.assertEquals("USA", locale0.getDescription());
            R2dbcInsertClause insert = env.insert(user$);
            LongStream.rangeClosed(1L, 20L).forEach(id -> insert
                    .set(user$.publicId, new UUID(id, 1))
                    .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                    .set(user$.disabled, false)
                    .set(user$.personName, "Person name " + id)
                    .set(user$.preferredLocaleLanguageCode, "en")
                    .set(user$.preferredLocaleCountryCode, "US")
                    .addBatch()
            );
            List<Long> ids = insert
                    .executeWithKeys(user$.id)
                    .collectList()
                    .block();
            Assertions.assertNotNull(ids);
            Assertions.assertEquals(20, ids.size());
            List<SUser> users = env.jdbcQuery()
                    .select(user$)
                    .from(user$)
                    .where(user$.id.in(ids))
                    .fetch();
            Assertions.assertEquals(20, users.size());
            IntStream.range(0, 20).forEach(i -> {
                SUser user = users.get(i);
                long id = i + 1;
                UUID publicId = new UUID(id, 1);
                Assertions.assertEquals(id, user.getId());
                Assertions.assertEquals(publicId, user.getPublicId());
                Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 12, 30, 20), user.getCreationTime());
                Assertions.assertEquals(false, user.getDisabled());
                Assertions.assertEquals("Person name " + id, user.getPersonName());
                Assertions.assertEquals("en", user.getPreferredLocaleLanguageCode());
                Assertions.assertEquals("US", user.getPreferredLocaleCountryCode());
            });
        });
    }

    @Test
    public void batchInsertWithKey() {
        run(env -> {
            Long numRowsInserted = env.insert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .set(locale$.description, "USA")
                    .execute()
                    .block();
            Assertions.assertEquals(1L, numRowsInserted);
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(1, locales.size());
            SLocale locale0 = locales.get(0);
            Assertions.assertEquals("US", locale0.getCountryCode());
            Assertions.assertEquals("en", locale0.getLanguageCode());
            Assertions.assertEquals("English (US)", locale0.getEnglishName());
            Assertions.assertEquals("English", locale0.getNativeName());
            Assertions.assertEquals("USA", locale0.getDescription());
            R2dbcInsertClause insert = env.insert(user$);
            LongStream.rangeClosed(1L, 20L).forEach(id -> insert
                    .set(user$.publicId, new UUID(id, 1))
                    .set(user$.creationTime, LocalDateTime.of(2010, 1, 1, 12, 30, 20))
                    .set(user$.disabled, false)
                    .set(user$.personName, "Person name " + id)
                    .set(user$.preferredLocaleLanguageCode, "en")
                    .set(user$.preferredLocaleCountryCode, "US")
                    .addBatch()
            );
            Long firstId = insert
                    .executeWithKey(user$.id)
                    .block();
            Assertions.assertNotNull(firstId);
            Assertions.assertEquals(1L, firstId);
            List<SUser> users = env.jdbcQuery()
                    .select(user$)
                    .from(user$)
                    .fetch();
            Assertions.assertEquals(20, users.size());
            IntStream.range(0, 20).forEach(i -> {
                SUser user = users.get(i);
                long id = i + 1;
                UUID publicId = new UUID(id, 1);
                Assertions.assertEquals(id, user.getId());
                Assertions.assertEquals(publicId, user.getPublicId());
                Assertions.assertEquals(LocalDateTime.of(2010, 1, 1, 12, 30, 20), user.getCreationTime());
                Assertions.assertEquals(false, user.getDisabled());
                Assertions.assertEquals("Person name " + id, user.getPersonName());
                Assertions.assertEquals("en", user.getPreferredLocaleLanguageCode());
                Assertions.assertEquals("US", user.getPreferredLocaleCountryCode());
            });
        });
    }

}

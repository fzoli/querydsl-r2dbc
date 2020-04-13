package com.querydsl.r2dbc.test;

import com.querydsl.r2dbc.test.env.QueryDslTest;
import com.querydsl.r2dbc.test.env.schema.QLocale;
import com.querydsl.r2dbc.test.env.schema.SLocale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.querydsl.r2dbc.test.env.QueryDslTestEnvironment.run;

@QueryDslTest
public class R2dbcDeleteTest {

    private static final QLocale locale$ = QLocale.Locale;

    @Test
    public void delete() {
        run(env -> {
            Long numRowsInserted = env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .setNull(locale$.description)
                    .execute();
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
            Long numRowsUpdated = env.delete(locale$)
                    .where(locale$.countryCode.eq(locale.getCountryCode())
                            .and(locale$.languageCode.eq(locale.getLanguageCode())))
                    .execute()
                    .block();
            Assertions.assertEquals(1L, numRowsUpdated);
            List<SLocale> locales2 = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(0L, locales2.size());
        });
    }

    @Test
    public void deleteBatch() {
        run(env -> {
            Long numRowsInserted = env.jdbcInsert(locale$)
                    .set(locale$.countryCode, "US")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (US)")
                    .set(locale$.nativeName, "English")
                    .setNull(locale$.description)
                    .addBatch()
                    .set(locale$.countryCode, "UK")
                    .set(locale$.languageCode, "en")
                    .set(locale$.englishName, "English (UK)")
                    .set(locale$.nativeName, "English")
                    .setNull(locale$.description)
                    .addBatch()
                    .execute();
            Assertions.assertEquals(2L, numRowsInserted);
            Long numRowsUpdated = env.delete(locale$)
                    .where(locale$.countryCode.eq("US"))
                    .addBatch()
                    .where(locale$.countryCode.eq("UK"))
                    .addBatch()
                    .execute()
                    .block();
            Assertions.assertEquals(2L, numRowsUpdated);
            List<SLocale> locales2 = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(0L, locales2.size());
        });
    }

}

package com.querydsl.r2dbc.test;

import com.querydsl.r2dbc.test.env.QueryDslTest;
import com.querydsl.r2dbc.test.env.schema.QLocale;
import com.querydsl.r2dbc.test.env.schema.SLocale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.querydsl.r2dbc.test.env.QueryDslTestEnvironment.run;

@QueryDslTest
public class R2dbcUpdateTest {

    private static final QLocale locale$ = QLocale.Locale;

    @Test
    public void update() {
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
            Long numRowsUpdated = env.update(locale$)
                    .set(locale$.description, "USA")
                    .execute()
                    .block();
            Assertions.assertEquals(1L, numRowsUpdated);
            List<SLocale> locales2 = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(1L, locales2.size());
            SLocale locale2 = locales2.get(0);
            Assertions.assertEquals("US", locale2.getCountryCode());
            Assertions.assertEquals("en", locale2.getLanguageCode());
            Assertions.assertEquals("English (US)", locale2.getEnglishName());
            Assertions.assertEquals("English", locale2.getNativeName());
            Assertions.assertEquals("USA", locale2.getDescription());
        });
    }

    @Test
    public void updateBatch() {
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
            List<SLocale> locales = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .fetch();
            Assertions.assertEquals(2L, locales.size());
            Long numRowsUpdated = env.update(locale$)
                    .set(locale$.description, "US")
                    .where(locale$.countryCode.eq("US"))
                    .addBatch()
                    .set(locale$.description, "UK")
                    .where(locale$.countryCode.eq("UK"))
                    .addBatch()
                    .execute()
                    .block();
            Assertions.assertEquals(2L, numRowsUpdated);
            List<SLocale> locales2 = env.jdbcQuery()
                    .select(locale$)
                    .from(locale$)
                    .orderBy(locale$.countryCode.asc())
                    .fetch();
            Assertions.assertEquals(2L, locales2.size());
            Assertions.assertEquals("UK", locales2.get(0).getDescription());
            Assertions.assertEquals("US", locales2.get(1).getDescription());
        });
    }

}

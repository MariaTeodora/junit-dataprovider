package com.tngtech.test.junit.dataprovider;

import static com.tngtech.junit.dataprovider.DataProviders.$;
import static com.tngtech.junit.dataprovider.DataProviders.$$;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.UseDataProvider;

class NameConventionAcceptanceTest {

    @DataProvider
    static Object[][] testIsEmptyString() {
        // @formatter:off
        return new Object[][] {
                { null },
                { "" },
        };
        // @formatter:on
    }

    @ParameterizedTest
    @UseDataProvider
    void testIsEmptyString(String str) {
        // Given:

        // When:
        boolean isEmpty = (str == null) || str.isEmpty();

        // Then:
        assertThat(isEmpty).isTrue();
    }

    @DataProvider
    static Object[][] dataProviderAdd() {
        // @formatter:off
        return $$(
                $( -1, -1, -2 ),
                $( -1,  0, -1 ),
                $(  0, -1, -1 ),
                $(  0,  0,  0 ),
                $(  0,  1,  1 ),
                $(  1,  0,  1 ),
                $(  1,  1,  2 )
        );
        // @formatter:on
    }

    @ParameterizedTest
    @UseDataProvider
    void testAdd(int a, int b, int expected) {
        // Given:

        // When:
        int result = a + b;

        // Then:
        assertThat(result).isEqualTo(expected);
    }
}

package com.tngtech.junit.dataprovider.internal.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviders;

public class StringConverterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private StringConverter underTest;

    @Mock
    private DataProvider dataProvider;

    @Test
    public void testConvertShouldReturnNullObjectWrappedInObjectArray() {
        // Given:
        String data = null;
        Class<?>[] parameterTypes = new Class<?>[] { Integer.class };

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 1);

        // Then:
        assertThat(result).isEqualTo(new Object[] { null });
    }

    @Test
    public void testConvertShouldThrowExceptionIfNumberOfArgumentsIsNotEqualToNumberOfParameterTypes() {
        // Given:
        String data = "";
        Class<?>[] parameterTypes = new Class<?>[] { String.class, int.class };

        doReturn(",").when(dataProvider).splitBy();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Test method expected 2 parameters but got 1 arguments in row 2");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 2);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldThrowExceptionIfVarargsAndNumberOfArgumentsIsTwoLessComparedToNumberOfParameterTypes() {
        // Given:
        String data = "";
        Class<?>[] parameterTypes = new Class<?>[] { long.class, boolean.class, int[].class };

        doReturn(",").when(dataProvider).splitBy();

        expectedException.expect(IllegalArgumentException.class);
        expectedException
                .expectMessage("Test method expected at least 2 parameters but got 1 arguments in row 3");

        // When:
        underTest.convert(data, true, parameterTypes, dataProvider, 3);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldCorrectlyParseAllPrimitiveTypes() {
        // Given:
        String data = "true,1,c,2,3,4,5.5,6.6";
        Class<?>[] parameterTypes = new Class[] { boolean.class, byte.class, char.class, short.class, int.class,
                long.class, float.class, double.class };

        doReturn(",").when(dataProvider).splitBy();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 10);

        // Then:
        assertThat(result).containsExactly(true, (byte) 1, 'c', (short) 2, 3, 4L, 5.5f, 6.6d);
    }

    @Test
    public void testConvertShouldCorrectlyParseAllPrimitiveTypesAsJavaString() {
        // Given:
        String data = "-5;2014l;-1.234567f;-901e-3";
        Class<?>[] parameterTypes = new Class[] { int.class, long.class, float.class, double.class };

        doReturn(";").when(dataProvider).splitBy();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 11);

        // Then:
        assertThat(result).containsExactly(-5, 2014l, -1.234567f, -0.901d);
    }

    @Test
    public void testConvertShouldNotTrimValuesIfSettingsTrimIsFalse() {
        // Given:
        String data = " foo|  bar   |baz    ";
        Class<?>[] parameterTypes = new Class[] { String.class, String.class, String.class };

        doReturn("\\|").when(dataProvider).splitBy();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 12);

        // Then:
        assertThat(result).containsExactly(" foo", "  bar   ", "baz    ");
    }

    @Test
    public void testConvertShouldTrimAndParseAllPrimitiveTypesIfSettingsTrimIsTrue() {
        // Given:
        String data = "   false   ;    11    ;    z    ;  22       ;   33   ;44      ;   55.55     ;  66.66     ";
        Class<?>[] parameterTypes = new Class[] { boolean.class, byte.class, char.class, short.class, int.class,
                long.class, float.class, double.class };

        doReturn(";").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 13);

        // Then:
        assertThat(result).containsExactly(false, (byte) 11, 'z', (short) 22, 33, 44L, 55.55f, 66.66d);
    }

    @Test
    public void testConvertShouldTrimNonSpaceWhitespaceCharsIfSettingsTrimIsTrue() {
        // Given:
        String data = "\n-1f\n,\r-2\r,\t3.0d\t";
        Class<?>[] parameterTypes = new Class[] { float.class, int.class, double.class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 20);

        // Then:
        assertThat(result).containsExactly(-1f, -2, 3d);
    }

    @Test
    public void testConvertShouldNotTrimNonBreakingSpaceEvenIfSettingsTrimIsTrue() {
        // Given:
        String data = "\u00A0test\u00A0";
        Class<?>[] parameterTypes = new Class[] { String.class };

        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 21);

        // Then:
        assertThat(result).containsExactly("\u00A0test\u00A0");
    }

    @Test
    public void testConvertShouldCorrectlyHandleLeadingEmptyString() {
        // Given:
        String data = "/true";
        Class<?>[] parameterTypes = new Class[] { String.class, boolean.class };

        doReturn("/").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 30);

        // Then:
        assertThat(result).containsExactly("", true);
    }

    @Test
    public void testConvertShouldCorrectlyHandleTrailingEmptyString() {
        // Given:
        String data = "1 ";
        Class<?>[] parameterTypes = new Class[] { int.class, String.class };

        doReturn(" ").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 31);

        // Then:
        assertThat(result).containsExactly(1, "");
    }

    @Test
    public void testConvertShouldThrowIllegalArgumentExceptionIfCharHasNotLengthOne() {
        // Given:
        String data = "noChar";
        Class<?>[] parameterTypes = new Class[] { char.class };

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("'noChar' cannot be converted to type 'char'");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 40);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldThrowIllegalArgumentExceptionIfValueOfThrowsNumberFormatException() {
        // Given:
        String data = "noInt";
        Class<?>[] parameterTypes = new Class[] { int.class };

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot convert 'noInt' to type 'int'");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 41);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldThrowIllegalArgumentExceptionForTargetTypeConstructorWithStringArgWhichThrowsException() {
        // Given:
        String data = "noInt";
        Class<?>[] parameterTypes = new Class[] { BigInteger.class };

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "Tried to invoke 'public java.math.BigInteger(java.lang.String)' for argument 'noInt'. Exception was: null");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 42);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldThrowIllegalArgumentExceptionForUnsupportedTargetType() {
        // Given:
        String data = "noObject";
        Class<?>[] parameterTypes = new Class[] { Object.class };

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "Type 'Object' is not supported as parameter type of test methods. Supported types are primitive types and their wrappers, 'Enum' values, 'String's, and types having a single 'String' parameter constructor");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 43);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldCorrectlyParseEnum() {
        // Given:
        String data = " VAL1,  VAL2 ";
        Class<?>[] parameterTypes = new Class[] { TestEnum.class, TestEnum.class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 50);

        // Then:
        assertThat(result).containsExactly(TestEnum.VAL1, TestEnum.VAL2);
    }

    @Test
    public void testConvertShouldThrowIllegalArgumentExceptionIfEnumValuesIsInvalid() {
        // Given:
        String data = "Val1";
        Class<?>[] parameterTypes = new Class[] { TestEnum.class };

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "'Val1' is not a valid value of enum 'TestEnum'. Please be aware of case sensitivity or use 'ignoreEnumCase'");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 51);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldCorrectlyParseEnumIgnoringCase() {
        // Given:
        String data = "Val1,val2";
        Class<?>[] parameterTypes = new Class[] { TestEnum.class, TestEnum.class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).ignoreEnumCase();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 50);

        // Then:
        assertThat(result).containsExactly(TestEnum.VAL1, TestEnum.VAL2);
    }

    @Test
    public void testConvertShouldThrowIllegalArgumentExceptionIfEnumValueIsInvalid() {
        // Given:
        String data = "UNKNOW_ENUM_VALUE";
        Class<?>[] parameterTypes = new Class[] { TestEnum.class };

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "'UNKNOW_ENUM_VALUE' is not a valid value of enum 'TestEnum'. Please be aware of case sensitivity or use 'ignoreEnumCase'.");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 51);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldCorrectlyParseClass() {
        // Given:
        String data = " java.lang.Thread, com.tngtech.junit.dataprovider.DataProviders ";
        Class<?>[] parameterTypes = new Class[] { Class.class, Class.class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 50);

        // Then:
        assertThat(result).containsExactly(Thread.class, DataProviders.class);
    }

    @Test
    public void testConvertShouldThrowIllegalArgumentExceptionIfClassNameIsInvalid() {
        // Given:
        String data = "String";
        Class<?>[] parameterTypes = new Class[] { Class.class };

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unable to instantiate 'Class' for 'String'");

        // When:
        underTest.convert(data, false, parameterTypes, dataProvider, 55);

        // Then: expect exception
    }

    @Test
    public void testConvertShouldCorrectlyParseAllPrimitiveWrapperTypes() {
        // Given:
        String data = "true,1,c,2,3,4,5.5,6.6";
        Class<?>[] parameterTypes = new Class[] { Boolean.class, Byte.class, Character.class, Short.class,
                Integer.class, Long.class, Float.class, Double.class };

        doReturn(",").when(dataProvider).splitBy();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 60);

        // Then:
        assertThat(result).containsExactly(
                Boolean.TRUE, Byte.valueOf((byte) 1), Character.valueOf('c'), Short.valueOf((short) 2),
                Integer.valueOf(3), Long.valueOf(4L), Float.valueOf(5.5f), Double.valueOf(6.6d));
    }

    @Test
    public void testConvertShouldParseNullValuesAsStringIfSettingsConvertNullsIsFalse() {
        // Given:
        String data = "null#null";
        Class<?>[] parameterTypes = new Class[] { String.class, String.class };

        doReturn("#").when(dataProvider).splitBy();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 70);

        // Then:
        assertThat(result).containsExactly("null", "null");
    }

    @Test
    public void testConvertShouldParseNullValuesAsNullObjectIfSettingsConvertNullsIsTrue() {
        // Given:
        String data = "null,null,foo";
        Class<?>[] parameterTypes = new Class[] { String.class, String.class, String.class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).convertNulls();

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 71);

        // Then:
        assertThat(result).containsExactly(null, null, "foo");
    }

    @Test
    public void testConvertShouldCallCustomConvertAndNotReturnValueIfObjectNoConversion() {
        // Given:
        String data = "2016-03-11";
        Class<?>[] parameterTypes = new Class<?>[] { Date.class };

        StringConverter underTest = new StringConverter() {
            @Override
            protected Object customConvertValue(String str, Class<?> targetType, DataProvider dataProvider) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(str);
                } catch (ParseException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        };

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 75);

        // Then:
        assertThat(result).hasSize(1);
        assertThat(result[0]).isInstanceOf(Date.class).isEqualTo(date(2016, Calendar.MARCH, 11));
    }

    @Test
    public void testConvertShouldCallCustomConvertAndReturnValueIfNotObjectNoConversion() {
        // Given:
        String data = "2016-03-11";
        Class<?>[] parameterTypes = new Class<?>[] { Date.class };

        StringConverter underTest = new StringConverter() {
            @Override
            protected Object customConvertValue(String str, Class<?> targetType, DataProvider dataProvider) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(str);
                } catch (ParseException e) {
                    fail("Unexpected exception: " + e);
                    return null; // fool compiler
                }
            }
        };

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 76);

        // Then:
        GregorianCalendar expectedDate = new GregorianCalendar();
        expectedDate.set(2016, Calendar.MARCH, 11, 0, 0, 0);
        expectedDate.set(Calendar.MILLISECOND, 0);
        assertThat(result).containsExactly(expectedDate.getTime());
    }

    @Test
    public void testConvertShouldCorrectlyUseConstructorWithSingleStringArgForBigInteger() {
        // Given:
        String data = "1";
        Class<?>[] parameterTypes = new Class<?>[] { BigInteger.class };

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 80);

        // Then:
        assertThat(result).containsExactly(BigInteger.ONE);
    }

    @Test
    public void testConvertShouldCorrectlyUseConstructorWithSingleStringArgForFile() {
        // Given:
        String data = "home/schmida";
        Class<?>[] parameterTypes = new Class<?>[] { File.class };

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 80);

        // Then:
        assertThat(result).containsExactly(new File("home/schmida"));
    }

    @Test
    public void testConvertShouldCreateEmptyVarargsArrayForMissingOnlyVarargsArgument() {
        // Given:
        String data = "";
        Class<?>[] parameterTypes = new Class<?>[] { int[].class };

        // When:
        Object[] result = underTest.convert(data, true, parameterTypes, dataProvider, 90);

        // Then:
        assertThat(result).containsExactly(new int[0]);
    }

    @Test
    public void testConvertShouldCreateEmptyVarargsArrayForLastMissingVarargsArgument() {
        // Given:
        String data = "test";
        Class<?>[] parameterTypes = new Class<?>[] { String.class, int[].class };

        doReturn(",").when(dataProvider).splitBy();

        // When:
        Object[] result = underTest.convert(data, true, parameterTypes, dataProvider, 91);

        // Then:
        assertThat(result).containsExactly("test", new int[0]);
    }

    @Test
    public void testConvertShouldCreateVarargsArrayForOneOnlyVarargsArguments() {
        // Given:
        String data = "1.0";
        Class<?>[] parameterTypes = new Class<?>[] { double[].class };

        doReturn(",").when(dataProvider).splitBy();

        // When:
        Object[] result = underTest.convert(data, true, parameterTypes, dataProvider, 92);

        // Then:
        assertThat(result).containsExactly(new double[] { 1.0 });
    }

    @Test
    public void testConvertShouldCreateVarargsArrayForOneLastVarargsArguments() {
        // Given:
        String data = "a,2,1.0,null";
        Class<?>[] parameterTypes = new Class<?>[] { char.class, byte.class, Double[].class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).convertNulls();

        // When:
        Object[] result = underTest.convert(data, true, parameterTypes, dataProvider, 93);

        // Then:
        assertThat(result).containsExactly('a', (byte) 2, new Double[] { 1.0, null });
    }

    @Test
    public void testConvertShouldCreateVarargsArrayForMultipleOnlyVarargsArguments() {
        // Given:
        String data = "1, 2, 3";
        Class<?>[] parameterTypes = new Class<?>[] { long[].class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, true, parameterTypes, dataProvider, 94);

        // Then:
        assertThat(result).containsExactly(new long[] { 1, 2, 3 });
    }

    @Test
    public void testConvertShouldCreateVarargsArrayForMultipleLastVarargsArguments() {
        // Given:
        String data = "foobar, 1, 2, 3";
        Class<?>[] parameterTypes = new Class<?>[] { String.class, long[].class };

        doReturn(",").when(dataProvider).splitBy();
        doReturn(true).when(dataProvider).trimValues();

        // When:
        Object[] result = underTest.convert(data, true, parameterTypes, dataProvider, 95);

        // Then:
        assertThat(result).containsExactly("foobar", new long[] { 1, 2, 3 });
    }

    @Test
    public void testConvertShouldNotSplitIfSingleNonVarargArgumentIsRequired() {
        // Given:
        String data = "VAL1";
        Class<?>[] parameterTypes = new Class<?>[] { TestEnum.class };

        // When:
        Object[] result = underTest.convert(data, false, parameterTypes, dataProvider, 100);

        // Then:
        assertThat(result).containsExactly(TestEnum.VAL1);
    }

    @Test
    public void testCustomConvertShouldByDefaultReturnObjectNoConversion() {
        // Given:
        String data = "2016-03-11";
        Class<?> parameterType = Date.class;

        // When:
        Object result = underTest.customConvertValue(data, parameterType, dataProvider);

        // Then:
        assertThat(result).isEqualTo(StringConverter.OBJECT_NO_CONVERSION);
    }

    // -- test data ----------------------------------------------------------------------------------------------------

    protected static enum TestEnum {
        VAL1, VAL2, VAL3
    }

    // -- helper methods -----------------------------------------------------------------------------------------------

    private Date date(int year, int month, int day) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(0);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }
}

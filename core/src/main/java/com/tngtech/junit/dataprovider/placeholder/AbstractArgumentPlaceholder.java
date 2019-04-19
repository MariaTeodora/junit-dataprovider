package com.tngtech.junit.dataprovider.placeholder;

import java.util.Arrays;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This abstract placeholder is able to format arguments of a dataprovider test as comma-separated {@link String}
 * according to the given index or range subscript. Furthermore the following arguments are treated specially:
 * <table>
 * <caption>Special {@link String} treatment</caption>
 * <tr>
 * <th>Argument value</th>
 * <th>target {@link String}</th>
 * </tr>
 * <tr>
 * <td>null</td>
 * <td>&lt;null&gt;</td>
 * </tr>
 * <tr>
 * <td>&quot;&quot; (= empty string)</td>
 * <td>&lt;empty string&gt;</td>
 * </tr>
 * <tr>
 * <td>array (e.g. String[])</td>
 * <td>{@code "[" + formatPattern(array) + "]"}</td>
 * </tr>
 * <tr>
 * <td>other</td>
 * <td>{@link Object#toString()}</td>
 * </tr>
 * </table>
 */
abstract class AbstractArgumentPlaceholder extends BasePlaceholder {

    protected static final class FromAndTo {
        protected final int from;
        protected final int to;
        protected FromAndTo(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * {@link String} representation of {@code null}
     */
    protected static final String STRING_NULL = "<null>";

    /**
     * {@link String} representation of {@code ""}
     */
    protected static final String STRING_EMPTY = "<empty string>";

    /**
     * {@link String} representation of an non-printable character
     */
    protected static final String STRING_NON_PRINTABLE = "<np>";

    AbstractArgumentPlaceholder(String placeholderRegex) {
        super(placeholderRegex);
    }

    /**
     * @param placeholder containing the subscript
     * @param subscriptStartIndex starting index of first subscript inner digit (to parse part within {@code []})
     * @param argumentCount used to apply the parsed subscript values and derive real {@code from} and {@code to}
     * @return the wrapped {@link FromAndTo} for the subscript contained in given {@code placeholder} starting at given
     *         position {@code subscriptStartIndex} and applied on the given {@code argumentCount}
     */
    FromAndTo calcFromAndToForSubscriptAndArguments(String placeholder, int subscriptStartIndex,
            int argumentCount) {
        String subscript = placeholder.substring(subscriptStartIndex, placeholder.length() - 1);

        int from = Integer.MAX_VALUE;
        int to = Integer.MIN_VALUE;
        if (subscript.contains("..")) {
            String[] split = subscript.split("\\.\\.");

            from = Integer.parseInt(split[0]);
            to = Integer.parseInt(split[1]);
        } else {
            from = Integer.parseInt(subscript);
            to = from;
        }

        from = (from >= 0) ? from : argumentCount + from;
        to = (to >= 0) ? to + 1 : argumentCount + to + 1;
        return new FromAndTo(from, to);
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "false positive if 'param.toString()' returns 'null'")
    protected String format(Object param) {
        if (param == null) {
            return STRING_NULL;

        } else if (param.getClass().isArray()) {
            if (param.getClass().getComponentType().isPrimitive()) {
                return formatPrimitiveArray(param);
            }
            return "[" + formatArray((Object[]) param) + "]";

        } else if (param instanceof String && ((String) param).isEmpty()) {
            return STRING_EMPTY;

        }

        String result;
        if (param instanceof String) {
            result = (String) param;
        } else {
            result = param.toString();
        }
        if (result == null) { // maybe null if "param.toString()" returns null
            return STRING_NULL;
        }
        result = result.replaceAll("\0", "\\\\0").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        return replaceNonPrintableChars(result, STRING_NON_PRINTABLE);
    }

    private String formatPrimitiveArray(Object primitiveArray) {
        Class<?> componentType = primitiveArray.getClass().getComponentType();

        if (boolean.class.equals(componentType)) {
            return Arrays.toString((boolean[]) primitiveArray);

        } else if (byte.class.equals(componentType)) {
            return Arrays.toString((byte[]) primitiveArray);

        } else if (char.class.equals(componentType)) {
            return Arrays.toString((char[]) primitiveArray);

        } else if (short.class.equals(componentType)) {
            return Arrays.toString((short[]) primitiveArray);

        } else if (int.class.equals(componentType)) {
            return Arrays.toString((int[]) primitiveArray);

        } else if (long.class.equals(componentType)) {
            return Arrays.toString((long[]) primitiveArray);

        } else if (float.class.equals(componentType)) {
            return Arrays.toString((float[]) primitiveArray);

        } else if (double.class.equals(componentType)) {
            return Arrays.toString((double[]) primitiveArray);
        }
        throw new IllegalStateException("Called 'formatPrimitiveArray' on non-primitive array");
    }

    private String formatArray(Object[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            stringBuilder.append(format(array[i]));
            if (i < array.length - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    private String replaceNonPrintableChars(String input, String replacement) {
        StringBuilder result = new StringBuilder();
        for (int offset = 0; offset < input.length();) {
            int codePoint = input.codePointAt(offset);
            offset += Character.charCount(codePoint);

            // Replace invisible control characters and unused code points
            switch (Character.getType(codePoint)) {
                case Character.CONTROL: // \p{Cc}
                case Character.FORMAT: // \p{Cf}
                case Character.PRIVATE_USE: // \p{Co}
                case Character.SURROGATE: // \p{Cs}
                case Character.UNASSIGNED: // \p{Cn}
                    result.append(replacement);
                    break;

                default:
                    result.append(Character.toChars(codePoint));
                    break;
            }
        }
        return result.toString();
    }
}

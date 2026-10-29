package seedu.address.commons.util;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Case-insensitive ordering with arbitrarily large ASCII numeric runs compared numerically. */
public final class NaturalOrderComparator implements Comparator<String> {
    public static final NaturalOrderComparator INSTANCE = new NaturalOrderComparator();
    private static final Pattern TOKEN = Pattern.compile("[0-9]+|[^0-9]+");

    private NaturalOrderComparator() {
    }

    @Override
    public int compare(String first, String second) {
        Matcher left = TOKEN.matcher(first);
        Matcher right = TOKEN.matcher(second);
        while (left.find()) {
            if (!right.find()) {
                return 1;
            }
            String leftToken = left.group();
            String rightToken = right.group();
            boolean bothNumeric = leftToken.charAt(0) >= '0' && leftToken.charAt(0) <= '9'
                    && rightToken.charAt(0) >= '0' && rightToken.charAt(0) <= '9';
            int comparison = bothNumeric
                    ? new BigInteger(leftToken).compareTo(new BigInteger(rightToken))
                    : String.CASE_INSENSITIVE_ORDER.compare(leftToken, rightToken);
            if (comparison != 0) {
                return comparison;
            }
        }
        return right.find() ? -1 : 0;
    }
}

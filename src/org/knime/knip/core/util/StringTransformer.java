package org.knime.knip.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.util.ValuePair;

public class StringTransformer {

        private final List<ValuePair<String, Boolean>> m_parsedList;

        public StringTransformer(final String expression, final String delim)
                        throws IllegalArgumentException {
                m_parsedList = parse(expression, delim);
        }

        /*
         * Pre-calculates the parsing of the expression, which can be used later
         * on
         */
        private List<ValuePair<String, Boolean>> parse(final String expression,
                        final String delim) throws IllegalArgumentException {
                int current = 0;
                final List<ValuePair<String, Boolean>> res = new ArrayList<ValuePair<String, Boolean>>();

                while (current < expression.length()) {

                        final int start = expression.indexOf(delim, current);

                        if (start == -1) {
                                res.add(new ValuePair<String, Boolean>(
                                                expression
                                                .substring(current, expression
                                                                .length()),
                                                true));
                                break;
                        }

                        if (start != current) {
                                res.add(new ValuePair<String, Boolean>(
                                                expression
                                                .substring(current, start),
                                                true));
                                current = start;
                                continue;
                        }

                        final int end = expression.indexOf(delim, start + 1);

                        if (end < start) {
                                throw new IllegalArgumentException(
                                                "No closing $ for: \""
                                                                + expression.substring(
                                                                                start,
                                                                                Math.max(expression
                                                                                                .length(),
                                                                                                start + 10))
                                                                + "\"");
                        }

                        current = end + 1;

                        res.add(new ValuePair<String, Boolean>(expression
                                        .substring(
                                        start + 1, end), false));
                }
                return res;
        }

        /**
         * Given a map from String to Object, the resulting String is created,
         * given the expression set in the constructor.
         *
         * @param input
         * @return
         * @throws InvalidSettingsException
         */
        public String transform(final Map<String, Object> input)
                        throws IllegalArgumentException {
                final StringBuffer bf = new StringBuffer();
                for (final ValuePair<String, Boolean> ValuePair : m_parsedList) {
                        if (ValuePair.b) {
                                bf.append(ValuePair.a);
                        } else {
                                bf.append(input.get(ValuePair.a).toString());
                        }
                }

                return bf.toString();
        }

        public static void main(final String[] args) throws IllegalArgumentException {
                final Map<String, Object> map = new HashMap<String, Object>();

                map.put("name", "Name");
                map.put("label", "myLabel");

                System.out.println(new StringTransformer("$name$#_chrome", "$")
                                .transform(map).toString());
        }

}

package org.knime.knip.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.util.Pair;

public class StringTransformer {

        private final List<Pair<String, Boolean>> m_parsedList;

        public StringTransformer(String expression, String delim)
                        throws IllegalArgumentException {
                m_parsedList = parse(expression, delim);
        }

        /*
         * Pre-calculates the parsing of the expression, which can be used later
         * on
         */
        private List<Pair<String, Boolean>> parse(String expression,
                        String delim) throws IllegalArgumentException {
                int current = 0;
                List<Pair<String, Boolean>> res = new ArrayList<Pair<String, Boolean>>();

                while (current < expression.length()) {

                        int start = expression.indexOf(delim, current);

                        if (start == -1) {
                                res.add(new Pair<String, Boolean>(expression,
                                                true));
                                break;
                        }

                        if (start != current) {
                                res.add(new Pair<String, Boolean>(expression
                                                .substring(current, start),
                                                true));
                                current = start;
                                continue;
                        }

                        int end = expression.indexOf(delim, start + 1);

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

                        res.add(new Pair<String, Boolean>(expression.substring(
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
        public String transform(Map<String, Object> input)
                        throws IllegalArgumentException {
                StringBuffer bf = new StringBuffer();
                for (Pair<String, Boolean> pair : m_parsedList) {
                        if (pair.b) {
                                bf.append(pair.a);
                        } else {
                                bf.append(input.get(pair.a).toString());
                        }
                }

                return bf.toString();
        }

        public static void main(String[] args) throws IllegalArgumentException {
                Map<String, Object> map = new HashMap<String, Object>();

                map.put("name", "Name");
                map.put("label", "myLabel");

                System.out.println(new StringTransformer("$name$#$label$", "$")
                                .transform(map).toString());
        }

}

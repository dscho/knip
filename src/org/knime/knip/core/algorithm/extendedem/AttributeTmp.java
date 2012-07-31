package org.knime.knip.core.algorithm.extendedem;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

public class AttributeTmp {
        private final int m_Type;

        public static final int NUMERIC = 0;

        public static final int NOMINAL = 1;

        public static final int STRING = 2;

        public static final int DATE = 3;

        public static final int RELATIONAL = 4;

        public static final int ORDERING_ORDERED = 1;

        private int m_Index;

        private ProtectedProperties m_Metadata;

        private int m_Ordering;

        private boolean m_IsRegular;

        private boolean m_IsAveragable;

        private boolean m_HasZeropoint;

        public static final int ORDERING_SYMBOLIC = 0;

        public static final int ORDERING_MODULO = 2;

        public final boolean isNumeric() {

                return ((m_Type == NUMERIC) || (m_Type == DATE));
        }

        private final ArrayList<Object> m_Values;

        public final boolean isNominal() {

                return (m_Type == NOMINAL);
        }

        public final int index() {

                return m_Index;
        }

        final void setIndex(final int index) {

                m_Index = index;
        }

        public final/* @ pure @ */int numValues() {

                if (!isNominal() && !isString() && !isRelationValued()) {
                        return 0;
                } else {
                        return m_Values.size();
                }
        }

        public final boolean isRelationValued() {

                return (m_Type == RELATIONAL);
        }

        public final boolean isString() {

                return (m_Type == STRING);
        }

        public AttributeTmp(final String attributeName, final int index) {

                this(attributeName);
                m_Index = index;
        }

        public AttributeTmp(final String attributeName) {

                this(attributeName, new ProtectedProperties(new Properties()));
        }

        public AttributeTmp(final String attributeName,
                        final ProtectedProperties metadata) {

                m_Index = -1;
                m_Values = null;
                m_Type = NUMERIC;
                setMetadata(metadata);
        }

        private void setMetadata(final ProtectedProperties metadata) {

                m_Metadata = metadata;

                if (m_Type == DATE) {
                        m_Ordering = ORDERING_ORDERED;
                        m_IsRegular = true;
                        m_IsAveragable = false;
                        m_HasZeropoint = false;
                } else {

                        // get ordering
                        final String orderString = m_Metadata.getProperty(
                                        "ordering", "");

                        // numeric ordered attributes are averagable and
                        // zeropoint by
                        // default
                        String def;
                        if (m_Type == NUMERIC
                                        && orderString.compareTo("modulo") != 0
                                        && orderString.compareTo("symbolic") != 0)
                                def = "true";
                        else
                                def = "false";

                        // determine boolean states
                        m_IsAveragable = (m_Metadata.getProperty("averageable",
                                        def).compareTo("true") == 0);
                        m_HasZeropoint = (m_Metadata.getProperty("zeropoint",
                                        def).compareTo("true") == 0);
                        // averagable or zeropoint implies regular
                        if (m_IsAveragable || m_HasZeropoint)
                                def = "true";
                        m_IsRegular = (m_Metadata.getProperty("regular", def)
                                        .compareTo("true") == 0);

                        // determine ordering
                        if (orderString.compareTo("symbolic") == 0)
                                m_Ordering = ORDERING_SYMBOLIC;
                        else if (orderString.compareTo("ordered") == 0)
                                m_Ordering = ORDERING_ORDERED;
                        else if (orderString.compareTo("modulo") == 0)
                                m_Ordering = ORDERING_MODULO;
                        else {
                                if (m_Type == NUMERIC || m_IsAveragable
                                                || m_HasZeropoint)
                                        m_Ordering = ORDERING_ORDERED;
                                else
                                        m_Ordering = ORDERING_SYMBOLIC;
                        }
                }

                // consistency checks
                if (m_IsAveragable && !m_IsRegular)
                        throw new IllegalArgumentException(
                                        "An averagable attribute must be"
                                                        + " regular");
                if (m_HasZeropoint && !m_IsRegular)
                        throw new IllegalArgumentException(
                                        "A zeropoint attribute must be"
                                                        + " regular");
                if (m_IsRegular && m_Ordering == ORDERING_SYMBOLIC)
                        throw new IllegalArgumentException(
                                        "A symbolic attribute cannot be"
                                                        + " regular");
                if (m_IsAveragable && m_Ordering != ORDERING_ORDERED)
                        throw new IllegalArgumentException(
                                        "An averagable attribute must be"
                                                        + " ordered");
                if (m_HasZeropoint && m_Ordering != ORDERING_ORDERED)
                        throw new IllegalArgumentException(
                                        "A zeropoint attribute must be"
                                                        + " ordered");

                // determine numeric range
                if (m_Type == NUMERIC)
                        setNumericRange(m_Metadata.getProperty("range"));
        }

        private double m_UpperBound;

        private double m_LowerBound;

        private void setNumericRange(final String rangeString) {
                // set defaults
                m_LowerBound = Double.NEGATIVE_INFINITY;
                m_UpperBound = Double.POSITIVE_INFINITY;

                if (rangeString == null)
                        return;

                // set up a tokenzier to parse the string
                final StreamTokenizer tokenizer = new StreamTokenizer(
                                new StringReader(rangeString));
                tokenizer.resetSyntax();
                tokenizer.whitespaceChars(0, ' ');
                tokenizer.wordChars(' ' + 1, '\u00FF');
                tokenizer.ordinaryChar('[');
                tokenizer.ordinaryChar('(');
                tokenizer.ordinaryChar(',');
                tokenizer.ordinaryChar(']');
                tokenizer.ordinaryChar(')');

                try {

                        // get opening brace
                        tokenizer.nextToken();

                        // get lower bound
                        tokenizer.nextToken();
                        if (tokenizer.ttype != tokenizer.TT_WORD)
                                throw new IllegalArgumentException(
                                                "Expected lower bound in range,"
                                                                + " found: "
                                                                + tokenizer.toString());
                        if (tokenizer.sval.compareToIgnoreCase("-inf") == 0)
                                m_LowerBound = Double.NEGATIVE_INFINITY;
                        else if (tokenizer.sval.compareToIgnoreCase("+inf") == 0)
                                m_LowerBound = Double.POSITIVE_INFINITY;
                        else if (tokenizer.sval.compareToIgnoreCase("inf") == 0)
                                m_LowerBound = Double.NEGATIVE_INFINITY;
                        else
                                try {
                                        m_LowerBound = Double.valueOf(
                                                        tokenizer.sval)
                                                        .doubleValue();
                                } catch (final NumberFormatException e) {
                                        throw new IllegalArgumentException(
                                                        "Expected lower bound in range,"
                                                                        + " found: '"
                                                                        + tokenizer.sval
                                                                        + "'");
                                }

                        // get separating comma
                        if (tokenizer.nextToken() != ',')
                                throw new IllegalArgumentException(
                                                "Expected comma in range,"
                                                                + " found: "
                                                                + tokenizer.toString());

                        // get upper bound
                        tokenizer.nextToken();
                        if (tokenizer.ttype != tokenizer.TT_WORD)
                                throw new IllegalArgumentException(
                                                "Expected upper bound in range,"
                                                                + " found: "
                                                                + tokenizer.toString());
                        if (tokenizer.sval.compareToIgnoreCase("-inf") == 0)
                                m_UpperBound = Double.NEGATIVE_INFINITY;
                        else if (tokenizer.sval.compareToIgnoreCase("+inf") == 0)
                                m_UpperBound = Double.POSITIVE_INFINITY;
                        else if (tokenizer.sval.compareToIgnoreCase("inf") == 0)
                                m_UpperBound = Double.POSITIVE_INFINITY;
                        else
                                try {
                                        m_UpperBound = Double.valueOf(
                                                        tokenizer.sval)
                                                        .doubleValue();
                                } catch (final NumberFormatException e) {
                                        throw new IllegalArgumentException(
                                                        "Expected upper bound in range,"
                                                                        + " found: '"
                                                                        + tokenizer.sval
                                                                        + "'");
                                }

                        // get closing brace
                        tokenizer.nextToken();

                        // check for rubbish on end
                        if (tokenizer.nextToken() != tokenizer.TT_EOF)
                                throw new IllegalArgumentException(
                                                "Expected end of range string,"
                                                                + " found: "
                                                                + tokenizer.toString());

                } catch (final IOException e) {
                        throw new IllegalArgumentException(
                                        "IOException reading attribute range"
                                                        + " string: "
                                                        + e.getMessage());
                }

                if (m_UpperBound < m_LowerBound)
                        throw new IllegalArgumentException("Upper bound ("
                                        + m_UpperBound
                                        + ") on numeric range is"
                                        + " less than lower bound ("
                                        + m_LowerBound + ")!");
        }
}

class ProtectedProperties extends Properties {
        // private boolean closed = false;

        public ProtectedProperties(final Properties props) {

                final Enumeration propEnum = props.propertyNames();
                while (propEnum.hasMoreElements()) {
                        final String propName = (String) propEnum.nextElement();
                        final String propValue = props.getProperty(propName);
                        super.setProperty(propName, propValue);
                }
                // closed = true; // no modifications allowed from now on
        }
}

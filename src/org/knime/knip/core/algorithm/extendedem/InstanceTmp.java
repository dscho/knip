package org.knime.knip.core.algorithm.extendedem;

public class InstanceTmp {
    protected InstancesTmp m_dataset;

    protected double[] m_attValues;

    protected double m_weight;

    public InstanceTmp(final double weight, final double[] attValues) {

        m_attValues = attValues;
        m_weight = weight;
        m_dataset = null;
    }

    public boolean isMissingValue(final double val) {

        return Double.isNaN(val);
    }

    public boolean isMissing(final int attIndex) {

        if (isMissingValue(value(attIndex))) {
            return true;
        }
        return false;
    }

    public final double weight() {
        return m_weight;
    }

    public double value(final int attIndex) {
        return m_attValues[attIndex];
    }

    public AttributeTmp attribute(final int index) {
        return m_dataset.attribute(index);
    }

    public void setDataset(final InstancesTmp instances) {
        m_dataset = instances;

    }

    public double missingValue() {

        return Double.NaN;
    }

    public InstanceTmp(final int numAttributes) {

        m_attValues = new double[numAttributes];
        for (int i = 0; i < m_attValues.length; i++) {
            m_attValues[i] = missingValue();
        }
        m_weight = 1;
        m_dataset = null;
    }

    public void setValue(final AttributeTmp att, final double value) {
        setValue(att.index(), value);

    }

    public void setWeight(final double weight) {
        m_weight = weight;

    }

    private void freshAttributeVector() {

        m_attValues = toDoubleArray();
    }

    public void setValue(final int attIndex, final double value) {
        freshAttributeVector();
        m_attValues[attIndex] = value;
    }

    public double[] toDoubleArray() {
        final double[] newValues = new double[m_attValues.length];
        System.arraycopy(m_attValues, 0, newValues, 0, m_attValues.length);
        return newValues;
    }
}
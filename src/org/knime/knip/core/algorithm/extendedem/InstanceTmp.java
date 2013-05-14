package org.knime.knip.core.algorithm.extendedem;

public class InstanceTmp {
    protected InstancesTmp m_Dataset;
    protected double[] m_AttValues;
    protected double m_Weight;

    public InstanceTmp(final double weight, final double[] attValues) {

        m_AttValues = attValues;
        m_Weight = weight;
        m_Dataset = null;
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
        return m_Weight;
    }

    public double value(final int attIndex) {
        return m_AttValues[attIndex];
    }

    public AttributeTmp attribute(final int index) {
        return m_Dataset.attribute(index);
    }

    public void setDataset(final InstancesTmp instances) {
        m_Dataset = instances;

    }

    public double missingValue() {

        return Double.NaN;
    }

    public InstanceTmp(final int numAttributes) {

        m_AttValues = new double[numAttributes];
        for (int i = 0; i < m_AttValues.length; i++) {
            m_AttValues[i] = missingValue();
        }
        m_Weight = 1;
        m_Dataset = null;
    }

    public void setValue(final AttributeTmp att, final double value) {
        setValue(att.index(), value);

    }

    public void setWeight(final double weight) {
        m_Weight = weight;

    }

    private void freshAttributeVector() {

        m_AttValues = toDoubleArray();
    }

    public void setValue(final int attIndex, final double value) {
        freshAttributeVector();
        m_AttValues[attIndex] = value;
    }

    public double[] toDoubleArray() {
        final double[] newValues = new double[m_AttValues.length];
        System.arraycopy(m_AttValues, 0, newValues, 0,
                         m_AttValues.length);
        return newValues;
    }
}
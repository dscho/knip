package org.knime.knip.core.algorithm.extendedem;

class StatsTmp {

    private double m_count = 0;

    private double m_sum = 0;

    private double m_sumSq = 0;

    private double m_stdDev = Double.NaN;

    private double m_mean = Double.NaN;

    private double m_min = Double.NaN;

    private double m_max = Double.NaN;

    public void add(final double value, final double n) {

        m_sum += value * n;
        m_sumSq += value * value * n;
        m_count += n;
        if (Double.isNaN(m_min)) {
            m_min = m_max = value;
        } else if (value < m_min) {
            m_min = value;
        } else if (value > m_max) {
            m_max = value;
        }
    }

    public void calculateDerived() {

        m_mean = Double.NaN;
        setStdDev(Double.NaN);
        if (m_count > 0) {
            m_mean = m_sum / m_count;
            setStdDev(Double.POSITIVE_INFINITY);
            if (m_count > 1) {
                setStdDev(m_sumSq - ((m_sum * m_sum) / m_count));
                setStdDev(getStdDev() / (m_count - 1));
                if (getStdDev() < 0) {
                    setStdDev(0);
                }
                setStdDev(Math.sqrt(getStdDev()));
            }
        }
    }

    /**
     * @return the m_stdDev
     */
    public double getStdDev() {
        return m_stdDev;
    }

    /**
     * @param m_stdDev the m_stdDev to set
     */
    public void setStdDev(double m_stdDev) {
        this.m_stdDev = m_stdDev;
    }
}
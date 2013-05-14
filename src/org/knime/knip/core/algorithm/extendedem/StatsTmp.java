package org.knime.knip.core.algorithm.extendedem;

class StatsTmp {
    public double m_count = 0;

    public double m_sum = 0;

    public double m_sumSq = 0;

    public double m_stdDev = Double.NaN;

    public double m_mean = Double.NaN;

    public double m_min = Double.NaN;

    public double m_max = Double.NaN;

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
        m_stdDev = Double.NaN;
        if (m_count > 0) {
            m_mean = m_sum / m_count;
            m_stdDev = Double.POSITIVE_INFINITY;
            if (m_count > 1) {
                m_stdDev = m_sumSq - ((m_sum * m_sum) / m_count);
                m_stdDev /= (m_count - 1);
                if (m_stdDev < 0) {
                    // System.err.println("Warning: stdDev value = "
                    // + stdDev
                    // + " -- rounded to zero.");
                    m_stdDev = 0;
                }
                m_stdDev = Math.sqrt(m_stdDev);
            }
        }
    }
}
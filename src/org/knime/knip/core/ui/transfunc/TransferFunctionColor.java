package org.knime.knip.core.ui.transfunc;

import java.awt.Color;

public enum TransferFunctionColor {

    RED(new Color(1.0f, 0.0f, 0.0f, 0.8f)),

    GREEN(new Color(0.0f, 1.0f, 0.0f, 0.8f)), 

    BLUE(new Color(0.0f, 0.0f, 1.0f, 0.8f)), 
    
    ALPHA(new Color(1.0f, 1.0f, 1.0f, 0.8f)), 
    
    GREY(new Color(0.25f, 0.25f, 0.25f, 0.8f));

    private final Color m_color;

    private TransferFunctionColor(final Color color) {
        m_color = color;
    }
    
    public Color getColor() {
        return m_color;
    }

}

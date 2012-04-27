package org.kniplib.types;

public enum ImgConversionTypes {
        DIRECT("Direct"), DIRECTCLIP("Direct(clipped)"), SCALE("Scale"), NORMALIZESCALE(
                        "Normalize and scale"), NORMALIZEDIRECT(
                        "Normalize and direct"), NORMALIZEDIRECTCLIP(
                        "Normalize and direct(clipped)");

        private String m_name;

        private ImgConversionTypes(String name) {

                m_name = name;
        }

        @Override
        public String toString() {
                return m_name;
        }
}

package org.kniplib.types;

public enum ImgConversionTypes {
        DIRECT("Direct"), DIRECTCLIP("Direct(clipped)"), SCALE("Scale"), NORMALIZESCALE(
                        "Normalize and scale"), NORMALIZEDIRECT(
                        "Normalize and direct"), NORMALIZEDIRECTCLIP(
                        "Normalize and direct(clipped)");

        private ImgConversionTypes(String name) {
        }
}

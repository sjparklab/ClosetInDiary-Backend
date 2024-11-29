package me.parkseongjong.springbootdeveloper.domain;

public enum OutfitCategory {
    TOPS, DRESSES, PANTS, SKIRTS, OUTERWEAR, SHOES, BAGS, ACCESSORIES;

    public static boolean contains(String category) {
        for (OutfitCategory outfitCategory : OutfitCategory.values()) {
            if (outfitCategory.name().equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }
}

package tn.esprit.mybudget.util;

import tn.esprit.mybudget.R;

/**
 * Utility class to map category names to their corresponding icon resources
 */
public class CategoryIconMapper {

    /**
     * Returns the drawable resource ID for a given category name
     * 
     * @param categoryName The name of the category
     * @return The resource ID of the corresponding icon
     */
    public static int getIconResource(String categoryName) {
        if (categoryName == null) {
            return R.drawable.ic_menu_myplaces; // Default icon
        }

        // Normalize the category name to lowercase for case-insensitive matching
        String normalizedName = categoryName.toLowerCase().trim();

        // Map category names to icons
        switch (normalizedName) {
            case "alimentation":
            case "food":
            case "restaurant":
                return R.drawable.ic_category_food;

            case "quotidien":
            case "daily":
            case "shopping":
                return R.drawable.ic_category_daily;

            case "transport":
            case "transportation":
                return R.drawable.ic_category_transport;

            case "social":
                return R.drawable.ic_category_social;

            case "résidentiel":
            case "residentiel":
            case "home":
            case "housing":
            case "maison":
                return R.drawable.ic_category_home;

            case "cadeaux":
            case "cadeau":
            case "gift":
            case "gifts":
                return R.drawable.ic_category_gift;

            case "communication":
            case "phone":
            case "téléphone":
            case "telephone":
                return R.drawable.ic_category_communication;

            case "vêtements":
            case "vetements":
            case "clothing":
            case "clothes":
                return R.drawable.ic_category_clothing;

            case "loisirs":
            case "leisure":
            case "entertainment":
            case "divertissement":
                return R.drawable.ic_category_leisure;

            case "beauté":
            case "beaute":
            case "beauty":
            case "cosmetics":
                return R.drawable.ic_category_beauty;

            case "médical":
            case "medical":
            case "health":
            case "santé":
            case "sante":
                return R.drawable.ic_category_medical;

            case "impôt":
            case "impot":
            case "tax":
            case "taxes":
                return R.drawable.ic_category_tax;

            case "éducation":
            case "education":
            case "école":
            case "ecole":
            case "school":
                return R.drawable.ic_category_education;

            case "bébé":
            case "bebe":
            case "baby":
            case "enfant":
            case "child":
                return R.drawable.ic_category_baby;

            case "animal de compagnie":
            case "animal":
            case "pet":
            case "pets":
            case "animaux":
                return R.drawable.ic_category_pet;

            case "voyage":
            case "voyages":
            case "travel":
            case "trip":
                return R.drawable.ic_category_travel;

            case "salaire":
            case "salary":
            case "wage":
            case "paie":
                return R.drawable.ic_category_salary;

            case "primes":
            case "prime":
            case "bonus":
            case "bonuses":
                return R.drawable.ic_category_bonus;

            case "ventes":
            case "vente":
            case "sales":
            case "sale":
                return R.drawable.ic_category_sales;

            default:
                // Return a default icon for unknown categories
                return R.drawable.ic_menu_myplaces;
        }
    }

    /**
     * Returns a color tint for the icon based on the category type
     * 
     * @param type The category type ("INCOME" or "EXPENSE")
     * @return The color resource ID
     */
    public static int getIconColor(String type) {
        if ("INCOME".equalsIgnoreCase(type)) {
            return android.R.color.holo_green_dark;
        } else {
            return android.R.color.holo_red_dark;
        }
    }
}

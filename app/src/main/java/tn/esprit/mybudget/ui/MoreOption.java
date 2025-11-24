package tn.esprit.mybudget.ui;

public class MoreOption {
    private String title;
    private int iconResId;

    public MoreOption(String title, int iconResId) {
        this.title = title;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }
}

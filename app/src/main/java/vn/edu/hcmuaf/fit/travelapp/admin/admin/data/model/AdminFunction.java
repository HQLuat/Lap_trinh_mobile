package vn.edu.hcmuaf.fit.travelapp.admin.admin.data.model;

public class AdminFunction {
    private String title;
    private int iconResId;
    private int cardColorResId;

    public AdminFunction(String title, int iconResId, int cardColorResId) {
        this.title = title;
        this.iconResId = iconResId;
        this.cardColorResId = cardColorResId;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getCardColorResId() {
        return cardColorResId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setCardColorResId(int cardColorResId) {
        this.cardColorResId = cardColorResId;
    }
}

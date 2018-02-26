package com.ctp.bakeit.models;

/**
 * Created by clinton on 2/24/18.
 */

public class Ingredient {

    private float quantity;

    private String measure;

    private String ingredient;


    public Ingredient(int quantity, String measure, String ingredient) {
        this.quantity = quantity;
        this.measure = measure;
        this.ingredient = ingredient;
    }

    public Ingredient() {
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "quantity=" + quantity + "\n"+
                ", measure='" + measure + "\n" +
                ", ingredient='" + ingredient + "\n" +
                '}';
    }
}

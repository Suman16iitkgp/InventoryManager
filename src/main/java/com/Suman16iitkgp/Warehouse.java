package com.Suman16iitkgp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Warehouse {
    private final String id;
    private final Map<String, Integer> inventory;
    private final Map<String, List<AlertConfig>> alertConfigs;


    public Warehouse(String warehouseId) {
        this.id = warehouseId;
        inventory = new HashMap<>();
        alertConfigs = new HashMap<>();
    }

    public String getId(){
        return id;
    }

    public void addStock(String productId, int quantity) {
        if( quantity <= 0 ){
            throw new RuntimeException("Invalid Input");
        }
        synchronized (this){
            int currentQuantity = inventory.getOrDefault(productId, 0);
            inventory.put(productId, currentQuantity+quantity);
        }

    }

    public boolean removeStock(String productId, int quantity) {

        List<AlertToFire> alertToFires;

        synchronized (this){
            if( quantity <= 0 ){
                return false;
            }

            int currentQuantity = inventory.getOrDefault(productId, 0);
            if( currentQuantity < quantity ){
                return false;
            }

            int newQuantity = currentQuantity-quantity;
            inventory.put(productId, newQuantity);

            alertToFires = getAlertsToFire(productId, newQuantity);
        }

        if( alertToFires != null ) fireAlert(productId, alertToFires);

        return true;

    }

    private void fireAlert(String productId, List<AlertToFire> fireAlerts) {
        for(AlertToFire alertToFire: fireAlerts){
            AlertListener listener = alertToFire.listener;
            listener.onLowStock(id, alertToFire.productId, alertToFire.productId);
        }
    }

    private List<AlertToFire> getAlertsToFire(String productId, int newQuantity) {


        if( alertConfigs.get(productId) == null ){
            return null;
        }

        List<AlertToFire> validAlertConfigs = new ArrayList<>();

        for( AlertConfig alertConfig : alertConfigs.get(productId) ){
            if( newQuantity <= alertConfig.threshold() ){
                validAlertConfigs.add( new AlertToFire(alertConfig.alertListener(),productId, newQuantity ) );
            }
        }

        return validAlertConfigs;
    }

    public void addLowStockAlert(String productId, int threshold, AlertListener listener) {
        AlertConfig alertConfig = new AlertConfig(threshold, listener);

        if(!alertConfigs.containsKey(productId)){
            alertConfigs.put(productId, new ArrayList<>());
        }

        alertConfigs.get(productId).add(alertConfig);

    }

    private static class AlertToFire {
        final AlertListener listener;
        final String productId;
        final int quantity;


        private AlertToFire(AlertListener listener, String productId, int quantity) {
            this.listener = listener;
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}

package com.Suman16iitkgp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    private Map<String, Warehouse> warehouseMap;

    public InventoryManager(List<String> warehouseIds){
        this.warehouseMap = new HashMap<>();
        for(String warehouseId : warehouseIds) {
            warehouseMap.put(warehouseId, new Warehouse(warehouseId));
        }
    }

    public boolean addStock(String warehouseId, String productId, int quantity){

        if( !warehouseMap.containsKey(warehouseId) ){
            throw new RuntimeException("Warehouse " + warehouseId + " is not present");
        }

        Warehouse warehouse = warehouseMap.get(warehouseId);
        warehouse.addStock(productId, quantity);

        return true;

    }

    public boolean removeStock(String warehouseId, String productId, int quantity){
        if( !warehouseMap.containsKey(warehouseId) ){
            throw new RuntimeException("Warehouse " + warehouseId + " is not present");
        }

        Warehouse warehouse = warehouseMap.get(warehouseId);
        return warehouse.removeStock(productId, quantity);

    }

    public boolean transfer(String sourceWarehouseId, String destWarehouseId, String productId, int quantity){
        if( quantity < 0 || sourceWarehouseId.equals(destWarehouseId) ){
            return false;
        }

        if( !warehouseMap.containsKey(sourceWarehouseId) || !warehouseMap.containsKey(destWarehouseId) ){
            return false;
        }

        Warehouse sourceWarehouse = warehouseMap.get(sourceWarehouseId);
        Warehouse destWarehouse = warehouseMap.get(destWarehouseId);

        String firstId, secondId;

        if(sourceWarehouseId.compareTo(destWarehouseId) < 0){
            firstId = sourceWarehouseId;
            secondId = destWarehouseId;
        }else{
            firstId = destWarehouseId;
            secondId = sourceWarehouseId;
        }

        Warehouse firstWarehouse = warehouseMap.get(firstId);
        Warehouse secondWarehouse = warehouseMap.get(secondId);

        synchronized (secondWarehouse){
            synchronized (firstWarehouse){
                if( !sourceWarehouse.removeStock(productId, quantity) ){
                    return false;
                }
                destWarehouse.addStock(productId,quantity);
            }
        }

        return true;

    }

    public void setLowStockAlert(String warehouseId, String productId, int threshold, AlertListener listener){

        if(!warehouseMap.containsKey(warehouseId)){
            throw new RuntimeException("Invalid Warehouse");
        }

        Warehouse warehouse = warehouseMap.get(warehouseId);
        warehouse.addLowStockAlert(productId, threshold, listener);
    }



}

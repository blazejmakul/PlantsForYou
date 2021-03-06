package com.example.plantsforyou.plant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "management/api/v1/plants")
public class PlantManagementController {
    private final PlantService plantService;

    @Autowired
    public PlantManagementController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping
    public List<Plant> getAllPlants(){
        return plantService.getAllPlants();
    }

    @PostMapping
    public ResponseEntity<Object> addNewPlant(@RequestBody Plant plant){
        plantService.addPlant(plant);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(path = "{plantID}")
    public ResponseEntity<Object> deletePlant(@PathVariable("plantID") Long plantID){
        plantService.delete(plantID);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(path = "{plantID}/inStock")
    public void updatePlantInStock(@PathVariable("plantID") Long plantID, @RequestParam() boolean inStock){
        plantService.updateInStock(plantID, inStock);
    }

    @PutMapping(path = "{plantID}/price")
    public void updatePlantPrice(@PathVariable("plantID") Long plantID, @RequestParam() Double price){
        plantService.updatePrice(plantID, price);
    }
}

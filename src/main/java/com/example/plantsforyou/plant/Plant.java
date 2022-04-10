package com.example.plantsforyou.plant;

import com.example.plantsforyou.plant.enums.PlantCareDifficulty;
import com.example.plantsforyou.plant.enums.PlantSize;
import com.example.plantsforyou.plant.enums.PlantTypeOfLight;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    @Column(columnDefinition="TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    private PlantTypeOfLight typeOfLight;
    @Enumerated(EnumType.STRING)
    private PlantCareDifficulty difficulty;
    @Enumerated(EnumType.STRING)
    private PlantSize size;
    boolean inStock;

    public Plant(String name,
                 double price,
                 String description,
                 PlantTypeOfLight typeOfLight,
                 PlantCareDifficulty difficulty,
                 PlantSize size, boolean inStock) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.typeOfLight = typeOfLight;
        this.difficulty = difficulty;
        this.size = size;
        this.inStock = inStock;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
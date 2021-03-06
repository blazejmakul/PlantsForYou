package com.example.plantsforyou.dto;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class AddToCartDto {

    /*
    * products: "[{\"id\":1,
    * \"name\":\"Monstera deliciosa\",
    * \"price\":25.12,
    * \"quantity\":3,
    * \"description\":\"Najbardziej pożądana roślina we wnętrzach ostatnich lat, czule nazwana przez nas Grzegorzem, zawdzięcza swoją popularność nie tylko wyjątkowej urodzie, ale też swojej bezproblemowości. Monstera jest jedną z tych roślin, z którymi będziesz żyć długo i szczęśliwie.\",
    * \"typeOfLight\":\"diffused\",
    * \"difficulty\":\"EASY\",
    * \"size\":\"MEDIUM\",
    * \"inStock\":true,
    * \"amountInBasket\":2}]"
    * */
    private @NotNull Long plantId;
    private @NotNull Integer amountInBasket;

    public AddToCartDto(Long plantId, Integer amountInBasket) {
        this.plantId = plantId;
        this.amountInBasket = amountInBasket;
    }

}

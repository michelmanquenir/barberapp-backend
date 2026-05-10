package barberiapp.dto;

import lombok.Data;

@Data
public class CreateShopRequest {
    private String name;
    private String description;
    private String slug;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean homeServiceEnabled;
    private Integer pricePerKm;
    private String categoryId;
    private String transferAccountHolder;
    private String transferRut;
    private String transferEmail;
    private String transferAccountType;
    private String transferAccountNumber;
    private String transferBankName;
}

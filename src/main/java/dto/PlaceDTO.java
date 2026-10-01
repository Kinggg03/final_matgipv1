package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class PlaceDTO {
    private Long pno;
    private String guName;
    private String restaurantNameKo;
    private String restaurantNameEn;
    private String category;
    private String summaryKo;
    private String summaryEn;
    private String imageUrl1;
    private String imageUrl2;
    private String address;
    private String phone;
    private String operatingHours;
    private String breakTime;
    private String offDays;
    private String mainMenu;
    private String priceRange;
    private String parking;
    private Integer pageNum;

    private LocalDateTime regDate;
    private LocalDateTime modDate;
}

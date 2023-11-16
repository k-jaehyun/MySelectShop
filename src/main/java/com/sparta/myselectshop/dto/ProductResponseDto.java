package com.sparta.myselectshop.dto;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.ProductFolder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String title;
    private String link;
    private String image;
    private int lprice;
    private int myprice;

    private List<FolderResponseDto> productFolderList = new ArrayList<>();  //하나의 상품에 여러 폴더가 연결됨

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.link = product.getLink();
        this.image = product.getImage();
        this.lprice = product.getLprice();
        this.myprice = product.getMyprice();
        for (ProductFolder productFolder : product.getProductFolderList()) {   //product.getProductFolderList() 할 때 지연로딩을 사용하는 것이므로 Service에서 사용 할 때 @Transactional을 걸어줘야함
            productFolderList.add(new FolderResponseDto(productFolder.getFolder()));
        }
    }
}
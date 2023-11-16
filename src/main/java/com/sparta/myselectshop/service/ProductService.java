package com.sparta.myselectshop.service;


import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepositoty;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FolderRepositoty folderRepositoty;
    private final ProductFolderRepository productFolderRepository;

    public static final int MIN_MY_PRICE = 100;

    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        Product product = productRepository.save(new Product(requestDto, user));
        return new ProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice();
        if (myprice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소" + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
        }

        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품을 찾을 수 없습니다.")
        );

        product.update(requestDto);

        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true)  //Product의 List<ProductFolder> 필드가 지연로딩(@Many로 끝남)이기 때문에 걸어줘야함.
    // Products의 정보를 가져올 때 폴더의 정보도 클라이언트에게 전달해줘야함. -> 그래서 Product entity에 ProductFolder와 양방향으로 걸어줬음.
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        //관리자로 로그인 했을 때 get 해오는거 따로 만들지 않고 여기에 한번에 (관리자인지 판단하여)
        UserRoleEnum userRoleEnum = user.getRole();  //현재 로그인 한 유저의 정보가 담겨짐

        Page<Product> productList;

        if (userRoleEnum == UserRoleEnum.USER) {
            productList = productRepository.findAllByUser(user, pageable);
        } else {
            productList = productRepository.findAll(pageable);
        }

        //Page 클래스는 ctl 들어가서 보면 map을 이용한 convert 기능이 있는 것을 알 수 있다. -> 이렇게 그냥 갖다 쓰면 됨.
        return productList.map(ProductResponseDto::new);
    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품은 존재하지 않습니다.")
        );
        product.updateByItemDto(itemDto);
    }

    public void addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        Folder folder = folderRepositoty.findById(folderId).orElseThrow(
                () -> new NullPointerException("해당 폴더가 존재하지 않습니다.")
        );

        if (!product.getUser().getId().equals(user.getId())
        || !folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다.");
        }

        // 지금 등록하는 관심상품이 폴더에 이미 중복되는지 확인 -> ProductFolder에서 확인해보면 됨
        Optional<ProductFolder> overlapFolder = productFolderRepository.findByProductAndFolder(product,folder);
        if(overlapFolder.isPresent()) {
            throw new IllegalArgumentException("중복된 폴더입니다.");
        }

        productFolderRepository.save(new ProductFolder(product,folder));
    }

    public Page<ProductResponseDto> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
        //페이징 처리(정렬, 페이지에 개수 제한, 페이지 넘기고, 등등)하는거 아래 3줄
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        //특정 폴더에 속해있는 특정 리스트가 필요한 상황. -> user와 (Product entity의)ProductFolderList의 folderID로 찾아옴
        //쿼리 메서드로는 ResponseDto 타입으로 반환하지 못함. -> Product로 받아옴
        Page<Product> productList = productRepository.findAllByUserAndProductFolderList_FolderId(user,folderId, pageable); //페이징 처리를 위해 pageable 넣어줌

        Page<ProductResponseDto> responseDtoList = productList.map(ProductResponseDto::new);

        return responseDtoList;
    }
}

package com.vuhien.application.controller.shop;

import static com.vuhien.application.config.Contant.SIZE_CM;
import static com.vuhien.application.config.Contant.SIZE_US;
import static com.vuhien.application.config.Contant.SIZE_VN;

import com.vuhien.application.entity.Brand;
import com.vuhien.application.entity.Category;
import com.vuhien.application.entity.Order;
import com.vuhien.application.entity.Post;
import com.vuhien.application.entity.Promotion;
import com.vuhien.application.entity.User;
import com.vuhien.application.exception.BadRequestException;
import com.vuhien.application.exception.NotFoundException;
import com.vuhien.application.model.dto.CheckPromotion;
import com.vuhien.application.model.dto.DetailProductInfoDTO;
import com.vuhien.application.model.dto.PageableDTO;
import com.vuhien.application.model.dto.ProductInfoDTO;
import com.vuhien.application.model.request.CreateOrderRequest;
import com.vuhien.application.model.request.FilterProductRequest;
import com.vuhien.application.security.CustomUserDetails;
import com.vuhien.application.service.BrandService;
import com.vuhien.application.service.CategoryService;
import com.vuhien.application.service.OrderService;
import com.vuhien.application.service.PostService;
import com.vuhien.application.service.ProductService;
import com.vuhien.application.service.PromotionService;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

  private final ProductService productService;

  private final BrandService brandService;

  private final PostService postService;

  private final OrderService orderService;

  private final CategoryService categoryService;

  private final PromotionService promotionService;

  public HomeController(ProductService productService, BrandService brandService, PostService postService,
      OrderService orderService, CategoryService categoryService, PromotionService promotionService) {
    this.productService = productService;
    this.brandService = brandService;
    this.postService = postService;
    this.orderService = orderService;
    this.categoryService = categoryService;
    this.promotionService = promotionService;
  }

  @GetMapping
  public String homePage(Model model) {

    //L???y 5 s???n ph???m m???i nh???t
    List<ProductInfoDTO> newProducts = productService.getListNewProducts();
    model.addAttribute("newProducts", newProducts);

    //L???y 5 s???n ph???m b??n ch???y nh???t
    List<ProductInfoDTO> bestSellerProducts = productService.getListBestSellProducts();
    model.addAttribute("bestSellerProducts", bestSellerProducts);

    //L???y 5 s???n ph???m c?? l?????t xem nhi???u
    List<ProductInfoDTO> viewProducts = productService.getListViewProducts();
    model.addAttribute("viewProducts", viewProducts);

    //L???y danh s??ch nh??n hi???u
    List<Brand> brands = brandService.getListBrand();
    model.addAttribute("brands", brands);

    //L???y 5 b??i vi???t m???i nh???t
    List<Post> posts = postService.getLatesPost();
    model.addAttribute("posts", posts);

    return "shop/index";
  }

  @GetMapping("/{slug}/{id}")
  public String getProductDetail(Model model, @PathVariable String id) {

    //L???y th??ng tin s???n ph???m
    DetailProductInfoDTO product;
    try {
      product = productService.getDetailProductById(id);
    } catch (NotFoundException ex) {
      return "error/404";
    } catch (Exception ex) {
      return "error/500";
    }
    model.addAttribute("product", product);

    //L???y s???n ph???m li??n quan
    List<ProductInfoDTO> relatedProducts = productService.getRelatedProducts(id);
    model.addAttribute("relatedProducts", relatedProducts);

    //L???y danh s??ch nh??n hi???u
    List<Brand> brands = brandService.getListBrand();
    model.addAttribute("brands", brands);

    // L???y size c?? s???n
    List<Integer> availableSizes = productService.getListAvailableSize(id);
    model.addAttribute("availableSizes", availableSizes);
    if (!availableSizes.isEmpty()) {
      model.addAttribute("canBuy", true);
    } else {
      model.addAttribute("canBuy", false);
    }

    //L???y danh s??ch size gi???y
    model.addAttribute("sizeVn", SIZE_VN);
    model.addAttribute("sizeUs", SIZE_US);
    model.addAttribute("sizeCm", SIZE_CM);

    return "shop/detail";
  }

  @GetMapping("/dat-hang")
  public String getCartPage(Model model, @RequestParam String id, @RequestParam int size) {

    //L???y chi ti???t s???n ph???m
    DetailProductInfoDTO product;
    try {
      product = productService.getDetailProductById(id);
    } catch (NotFoundException ex) {
      return "error/404";
    } catch (Exception ex) {
      return "error/500";
    }
    model.addAttribute("product", product);

    //Validate size
    if (size < 35 || size > 42) {
      return "error/404";
    }

    //L???y danh s??ch size c?? s???n
    List<Integer> availableSizes = productService.getListAvailableSize(id);
    model.addAttribute("availableSizes", availableSizes);
    boolean notFoundSize = true;
    for (Integer availableSize : availableSizes) {
      if (availableSize == size) {
        notFoundSize = false;
        break;
      }
    }
    model.addAttribute("notFoundSize", notFoundSize);

    //L???y danh s??ch size
    model.addAttribute("sizeVn", SIZE_VN);
    model.addAttribute("sizeUs", SIZE_US);
    model.addAttribute("sizeCm", SIZE_CM);
    model.addAttribute("size", size);

    return "shop/payment";
  }

  @PostMapping("/api/orders")
  public ResponseEntity<Object> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
    User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    Order order = orderService.createOrder(createOrderRequest, user.getId());

    return ResponseEntity.ok(order.getId());
  }

  @GetMapping("/products")
  public ResponseEntity<Object> getListBestSellProducts() {
    List<ProductInfoDTO> productInfoDTOS = productService.getListBestSellProducts();
    return ResponseEntity.ok(productInfoDTOS);
  }

  @GetMapping("/san-pham")
  public String getProductShopPages(Model model) {

    //L???y danh s??ch nh??n hi???u
    List<Brand> brands = brandService.getListBrand();
    model.addAttribute("brands", brands);
    List<Long> brandIds = new ArrayList<>();
    for (Brand brand : brands) {
      brandIds.add(brand.getId());
    }
    model.addAttribute("brandIds", brandIds);

    //L???y danh s??ch danh m???c
    List<Category> categories = categoryService.getListCategories();
    model.addAttribute("categories", categories);
    List<Long> categoryIds = new ArrayList<>();
    for (Category category : categories) {
      categoryIds.add(category.getId());
    }
    model.addAttribute("categoryIds", categoryIds);

    //Danh s??ch size c???a s???n ph???m
    model.addAttribute("sizeVn", SIZE_VN);

    //L???y danh s??ch s???n ph???m
    FilterProductRequest req = new FilterProductRequest(brandIds, categoryIds, new ArrayList<>(), (long) 0, Long.MAX_VALUE, 1);
    PageableDTO result = productService.filterProduct(req);
    model.addAttribute("totalPages", result.getTotalPages());
    model.addAttribute("currentPage", result.getCurrentPage());
    model.addAttribute("listProduct", result.getItems());

    return "shop/product";
  }

  @PostMapping("/api/san-pham/loc")
  public ResponseEntity<?> filterProduct(@RequestBody FilterProductRequest req) {
    // Validate
    if (req.getMinPrice() == null) {
      req.setMinPrice((long) 0);
    } else {
      if (req.getMinPrice() < 0) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("M???c gi?? ph???i l???n h??n 0");
      }
    }
    if (req.getMaxPrice() == null) {
      req.setMaxPrice(Long.MAX_VALUE);
    } else {
      if (req.getMaxPrice() < 0) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("M???c gi?? ph???i l???n h??n 0");
      }
    }

    PageableDTO result = productService.filterProduct(req);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/api/tim-kiem")
  public String searchProduct(Model model, @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer page) {

    PageableDTO result = productService.searchProductByKeyword(keyword, page);

    model.addAttribute("totalPages", result.getTotalPages());
    model.addAttribute("currentPage", result.getCurrentPage());
    model.addAttribute("listProduct", result.getItems());
    model.addAttribute("keyword", keyword);
    if (((List<?>) result.getItems()).isEmpty()) {
      model.addAttribute("hasResult", false);
    } else {
      model.addAttribute("hasResult", true);
    }

    return "shop/search";
  }

  @GetMapping("/api/check-hidden-promotion")
  public ResponseEntity<Object> checkPromotion(@RequestParam String code) {
    if (code == null || code == "") {
      throw new BadRequestException("M?? code tr???ng");
    }

    Promotion promotion = promotionService.checkPromotion(code);
    if (promotion == null) {
      throw new BadRequestException("M?? code kh??ng h???p l???");
    }
    CheckPromotion checkPromotion = new CheckPromotion();
    checkPromotion.setDiscountType(promotion.getDiscountType());
    checkPromotion.setDiscountValue(promotion.getDiscountValue());
    checkPromotion.setMaximumDiscountValue(promotion.getMaximumDiscountValue());
    return ResponseEntity.ok(checkPromotion);
  }

  @GetMapping("lien-he")
  public String contact() {
    return "shop/lien-he";
  }

  @GetMapping("huong-dan")
  public String buyGuide() {
    return "shop/buy-guide";
  }

  @GetMapping("doi-hang")
  public String doiHang() {
    return "shop/doi-hang";
  }

}

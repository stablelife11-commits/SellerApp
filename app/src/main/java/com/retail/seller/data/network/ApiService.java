package com.retail.seller.data.network;

import com.retail.seller.data.model.AuthResponseDto;
import com.retail.seller.data.model.LoginRequestDto;
import com.retail.seller.data.model.ProductRequestDto;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.model.ReturnRequestDto;
import com.retail.seller.data.model.ReturnResponseDto;
import com.retail.seller.data.model.SaleRequestDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.model.StockUpdateRequestDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto request);

    @GET("products")
    Call<List<ProductResponseDto>> getProducts();

    @GET("products/{id}")
    Call<ProductResponseDto> getProductById(@Path("id") Long id);

    @POST("products")
    Call<ProductResponseDto> createProduct(@Body ProductRequestDto request);

    @PATCH("products/variants/{variantId}/stock")
    Call<ProductVariantDto> updateVariantStock(@Path("variantId") Long variantId, @Body StockUpdateRequestDto request);

    @GET("sales")
    Call<List<SaleResponseDto>> getSales();

    @POST("sales")
    Call<SaleResponseDto> createSale(@Body SaleRequestDto request);

    @POST("returns")
    Call<ReturnResponseDto> processReturn(@Body ReturnRequestDto request);
}
package com.retail.seller.data.network;

import com.retail.seller.data.model.AuthResponseDto;
import com.retail.seller.data.model.DashboardSummaryDto;
import com.retail.seller.data.model.InventoryTransactionDto;
import com.retail.seller.data.model.LoginRequestDto;
import com.retail.seller.data.model.PaymentRequestDto;
import com.retail.seller.data.model.PaymentResponseDto;
import com.retail.seller.data.model.ProductRequestDto;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.model.ReturnRequestDto;
import com.retail.seller.data.model.ReturnResponseDto;
import com.retail.seller.data.model.SaleRequestDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.model.SellerOrderUpdateRequestDto;
import com.retail.seller.data.model.StockUpdateRequestDto;
import com.retail.seller.data.model.UserProfileDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto request);

    @GET("users/me")
    Call<UserProfileDto> getMyProfile();

    @PUT("users/me/profile")
    Call<UserProfileDto> updateMyProfile(@Body UserProfileDto request);

    @GET("admin/users")
    Call<List<UserProfileDto>> getAllUsersForAdmin();

    @PATCH("admin/users/{id}/role")
    Call<UserProfileDto> updateUserRole(
            @Path("id") Long id,
            @Query("role") String role
    );

    @PATCH("admin/users/{id}/status")
    Call<UserProfileDto> updateUserStatus(
            @Path("id") Long id,
            @Query("active") boolean active
    );

    @GET("reports/dashboard-summary")
    Call<DashboardSummaryDto> getDashboardSummary();

    @GET("products/my-products")
    Call<List<ProductResponseDto>> getMyProducts(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("products")
    Call<List<ProductResponseDto>> getProducts();

    @GET("products/{id}")
    Call<ProductResponseDto> getProductById(@Path("id") Long id);

    @POST("products")
    Call<ProductResponseDto> createProduct(@Body ProductRequestDto request);

    @PUT("products/{id}")
    Call<ProductResponseDto> updateProduct(
            @Path("id") Long id,
            @Body ProductRequestDto request
    );

    @PATCH("products/{id}/status")
    Call<ProductResponseDto> updateProductStatus(
            @Path("id") Long id,
            @Query("active") boolean active
    );

    @PATCH("products/variants/{variantId}/stock")
    Call<ProductVariantDto> updateVariantStock(
            @Path("variantId") Long variantId,
            @Body StockUpdateRequestDto request
    );

    @GET("inventory/transactions")
    Call<List<InventoryTransactionDto>> getInventoryTransactions(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("orders/seller/{sellerId}")
    Call<List<SaleResponseDto>> getSellerOrders(
            @Path("sellerId") Long sellerId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("orders")
    Call<List<SaleResponseDto>> getAllOrdersForAdmin(
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("orders/{id}")
    Call<SaleResponseDto> modifyOrder(
            @Path("id") Long id,
            @Body SellerOrderUpdateRequestDto request
    );

    @PATCH("orders/{id}/status")
    Call<SaleResponseDto> updateOrderStatus(
            @Path("id") Long id,
            @Query("status") String status
    );

    @POST("payments")
    Call<PaymentResponseDto> recordPayment(@Body PaymentRequestDto request);

    @GET("payments")
    Call<List<PaymentResponseDto>> getPayments();

    @GET("sales")
    Call<List<SaleResponseDto>> getSales();

    @POST("sales")
    Call<SaleResponseDto> createSale(@Body SaleRequestDto request);

    @POST("returns")
    Call<ReturnResponseDto> processReturn(@Body ReturnRequestDto request);

    @GET("returns")
    Call<List<ReturnResponseDto>> getReturns();
}
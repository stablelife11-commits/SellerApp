package com.retail.seller;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.retail.seller.data.network.TokenManager;
import com.retail.seller.ui.auth.LoginActivity;
import com.retail.seller.ui.dashboard.DashboardFragment;
import com.retail.seller.ui.inventory.InventoryFragment;
import com.retail.seller.ui.products.ProductsFragment;
import com.retail.seller.ui.profile.ProfileFragment;
import com.retail.seller.ui.sales.SalesFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_DASHBOARD = "tag_dashboard";
    private static final String TAG_SALES = "tag_sales";
    private static final String TAG_PRODUCTS = "tag_products";
    private static final String TAG_INVENTORY = "tag_inventory";
    private static final String TAG_PROFILE = "tag_profile";

    private BottomNavigationView bottomNav;
    private Fragment dashboardFragment;
    private Fragment salesFragment;
    private Fragment productsFragment;
    private Fragment inventoryFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TokenManager tokenManager = TokenManager.getInstance(this);
        AppCompatDelegate.setDefaultNightMode(
                tokenManager.isDarkModeEnabled()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        if (!tokenManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            dashboardFragment = new DashboardFragment();
            salesFragment = new SalesFragment();
            productsFragment = new ProductsFragment();
            inventoryFragment = new InventoryFragment();
            profileFragment = new ProfileFragment();

            fm.beginTransaction()
                    .add(R.id.fragment_container, profileFragment, TAG_PROFILE).hide(profileFragment)
                    .add(R.id.fragment_container, inventoryFragment, TAG_INVENTORY).hide(inventoryFragment)
                    .add(R.id.fragment_container, productsFragment, TAG_PRODUCTS).hide(productsFragment)
                    .add(R.id.fragment_container, salesFragment, TAG_SALES).hide(salesFragment)
                    .add(R.id.fragment_container, dashboardFragment, TAG_DASHBOARD)
                    .commit();
            activeFragment = dashboardFragment;
        } else {
            dashboardFragment = fm.findFragmentByTag(TAG_DASHBOARD);
            salesFragment = fm.findFragmentByTag(TAG_SALES);
            productsFragment = fm.findFragmentByTag(TAG_PRODUCTS);
            inventoryFragment = fm.findFragmentByTag(TAG_INVENTORY);
            profileFragment = fm.findFragmentByTag(TAG_PROFILE);

            int selectedId = bottomNav.getSelectedItemId();
            activeFragment = resolveFragmentForMenuId(selectedId);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment targetFragment = resolveFragmentForMenuId(item.getItemId());
            if (targetFragment != null && targetFragment != activeFragment) {
                fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(activeFragment)
                        .show(targetFragment)
                        .commit();
                activeFragment = targetFragment;
                return true;
            }
            return targetFragment != null;
        });
    }

    private Fragment resolveFragmentForMenuId(int itemId) {
        if (itemId == R.id.nav_dashboard) {
            return dashboardFragment;
        } else if (itemId == R.id.nav_sales) {
            return salesFragment;
        } else if (itemId == R.id.nav_products) {
            return productsFragment;
        } else if (itemId == R.id.nav_inventory) {
            return inventoryFragment;
        } else if (itemId == R.id.nav_profile) {
            return profileFragment;
        }
        return dashboardFragment;
    }

    public void switchToTab(int menuItemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(menuItemId);
        }
    }
}
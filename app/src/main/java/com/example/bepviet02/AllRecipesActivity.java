package com.example.bepviet02;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bepviet02.adapters.RecipeAdapter;
import com.example.bepviet02.databinding.ActivityAllRecipesBinding;
import com.example.bepviet02.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllRecipesActivity extends AppCompatActivity {
    private ActivityAllRecipesBinding binding;
    private String typeFilter;
    private DatabaseReference dbRef;
    private List<Recipe> mRecipeList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUi();
    }
    private void initUi() {
        dbRef = FirebaseDatabase.getInstance().getReference("Recipes");
        binding.btnGoBack.setOnClickListener(view -> finish());
        binding.rvAllRecipe.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvAllRecipe.setAdapter(new RecipeAdapter());
        typeFilter = getIntent().getStringExtra("type");
        if(TextUtils.isEmpty(typeFilter)){
            Toast.makeText(this, "Lỗi không lấy được dữ liệu", Toast.LENGTH_SHORT).show();
            finish();
        }
        Log.e("AllRecipesActivity", "onCreate: " + typeFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (typeFilter.equalsIgnoreCase("search")) {
            filterByRecipe();
        }
        else if (typeFilter.equalsIgnoreCase("category")) {
            filterByCategory();
        }
        else loadAllRecipes();

    }

    private void loadAllRecipes() {
        dbRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChildren()) {
                    mRecipeList.clear();
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                        mRecipeList.add(recipe);
                    }
                    setRecipeListToAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AllRecipesActivity", "loadAllRecipes - onCancelled: " + error.getMessage());
            }
        });
    }

    private void filterByRecipe() {
        String search_query = getIntent().getStringExtra("search_query");
        binding.tvAllRecipeTitle.setText("Tìm kiếm");
        binding.tvAllRecipeFilter.setText(search_query);
        dbRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChildren()) {
                    mRecipeList.clear();
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                        if (recipe.getName().toLowerCase().contains(search_query.toLowerCase())){
                            mRecipeList.add(recipe);
                        }
                    }
                    setRecipeListToAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AllRecipesActivity", "filterByRecipe - onCancelled: " + error.getMessage());
            }
        });
    }

    private void filterByCategory() {
        binding.tvAllRecipeTitle.setText("Danh mục");
        String category_name = getIntent().getStringExtra("category_name");
        Log.e("AllRecipesActivity", "filterByCategory: " + category_name);
        binding.tvAllRecipeFilter.setText(category_name);
        // recipe.getCategory().equals(category_name)
        dbRef.orderByChild("category").equalTo(category_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChildren()){
                    mRecipeList.clear();
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                        mRecipeList.add(recipe);
                    }
                    setRecipeListToAdapter();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AllRecipesActivity", "filterByCategory - onCancelled: " + error.getMessage());
            }
        });
    }

    private void setRecipeListToAdapter() {
        RecipeAdapter recipeAdapter = (RecipeAdapter) binding.rvAllRecipe.getAdapter();
        if (recipeAdapter != null) {
            recipeAdapter.setRecipeList(mRecipeList);
        }
    }


}
package com.example.bepviet02;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.example.bepviet02.databinding.ActivityRecipeDetailsBinding;
import com.example.bepviet02.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetailsActivity extends AppCompatActivity {
    private ActivityRecipeDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWidget();
    }

    private void getWidget() {
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (recipe == null) {
            finish();
            return;
        }
        binding.btnGoBack.setOnClickListener(view -> finish());
        Glide.with(this).load(recipe.getImageUrl()).into(binding.ivRecipeDetails);
        binding.tvRecipeDetailsName.setText(recipe.getName());
        binding.tvRecipeDetailsCategory.setText(recipe.getCategory());
        binding.tvRecipeDetailsTime.setText(recipe.getTime() + " phút");
        binding.tvRecipeDetailsIngredients.setText(recipe.getIngredients());
        binding.tvRecipeDetailsDescription.setText(recipe.getDescription());
        fetchAuthorName(recipe.getAuthorId());
        // Make Edit Button visible
        if (!isCurrentUserAuthor(recipe)){
            binding.btnRecipeDetailEdit.setVisibility(View.GONE);
            binding.btnRecipeDetailDelete.setVisibility(View.GONE);
        }
        else {
            binding.btnRecipeDetailEdit.setVisibility(View.VISIBLE);
            binding.btnRecipeDetailDelete.setVisibility(View.VISIBLE);
            binding.btnRecipeDetailEdit.setOnClickListener(view -> {
                new AlertDialog.Builder(this)
                        .setTitle("Cập nhật món ăn")
                        .setMessage("Bạn có muốn cập nhật món ăn này không?")
                        .setPositiveButton("không", (updateDialog, i) -> updateDialog.dismiss())
                        .setNegativeButton("Có", (updateDialog, i) -> upDateRecipe(recipe))
                        .show();
            });
            binding.btnRecipeDetailDelete.setOnClickListener(view ->{
                new AlertDialog.Builder(this)
                        .setTitle("Xóa món ăn")
                        .setMessage("Bạn có muốn xóa món ăn này không?")
                        .setPositiveButton("không", (deleteDialog, i) -> deleteDialog.dismiss())
                        .setNegativeButton("Có", (deleteDialog, i) -> deleteRecipe(recipe))
                        .show();
            });
        }
    }



    private boolean isCurrentUserAuthor(Recipe recipe){
        if(FirebaseAuth.getInstance().getCurrentUser() == null) return false;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return currentUserId.equals(recipe.getAuthorId());
    }

    private void fetchAuthorName(String recipeAuthorId) {
        // Check if `authorId` is not empty
        Log.d("RecipeDetailsActivity", "bindingAuthorNameById: " + recipeAuthorId);
        binding.tvRecipeDetailsAuthor.setText("Không rõ");
        if (!TextUtils.isEmpty(recipeAuthorId)) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users/" + recipeAuthorId);
            db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String authorName = snapshot.child("name").getValue(String.class);
                    if (!TextUtils.isEmpty(authorName)) {
                        binding.tvRecipeDetailsAuthor.setText(authorName);
                    } else {
                        binding.tvRecipeDetailsAuthor.setText("Không rõ");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RecipeDetailsActivity.this,
                            "Không lấy được thông tin người sở hữu",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private final ActivityResultLauncher<Intent> startForEditRecipeResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Recipe updatedRecipe = (Recipe) result.getData().getSerializableExtra("updated_recipe");
                    if (updatedRecipe != null) {
                        updateUI(updatedRecipe);
                    }
                }
            });

    private void upDateRecipe(Recipe recipe) {
        Intent intent = new Intent(this, AddRecipeActivity.class);
        intent.putExtra("recipe", recipe);
        intent.putExtra("isEdit", true);
        startForEditRecipeResult.launch(intent);
    }

    private void updateUI(Recipe updatedRecipe) {
        Glide.with(this).load(updatedRecipe.getImageUrl()).into(binding.ivRecipeDetails);
        binding.tvRecipeDetailsName.setText(updatedRecipe.getName());
        binding.tvRecipeDetailsCategory.setText(updatedRecipe.getCategory());
        binding.tvRecipeDetailsTime.setText(updatedRecipe.getTime() + " phút");
        binding.tvRecipeDetailsIngredients.setText(updatedRecipe.getIngredients());
        binding.tvRecipeDetailsDescription.setText(updatedRecipe.getDescription());
    }

    private void deleteRecipe(Recipe recipe) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Recipes/" + recipe.getId());
        db.removeValue().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Xóa món ăn thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Xóa món ăn thất bại", Toast.LENGTH_SHORT).show();
                });
        finish();
    }

}

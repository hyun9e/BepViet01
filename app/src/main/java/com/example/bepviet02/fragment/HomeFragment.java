package com.example.bepviet02.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bepviet02.AllRecipesActivity;
import com.example.bepviet02.SettingsActivity;
import com.example.bepviet02.adapters.RecipeAdapter;
import com.example.bepviet02.databinding.FragmentHomeBinding;
import com.example.bepviet02.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment's layout and return the root view
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUi();

    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipes();
    }



    private void initUi() {
        binding.btnHomeSettings.setOnClickListener(view -> startActivity(new Intent(binding.getRoot().getContext(), SettingsActivity.class)));
        // TODO: Search recipes
        binding.etSearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sendSearchQuery();
                return true;
            }
            return false;
        });
        binding.rvPopulars.setAdapter(new RecipeAdapter());
        binding.rvFavs.setAdapter(new RecipeAdapter());
    }

    private void sendSearchQuery() {
        String searchQuery = binding.etSearch.getText().toString();
        Intent intent = new Intent(binding.getRoot().getContext(), AllRecipesActivity.class);
        intent.putExtra("type", "search");
        intent.putExtra("search_query", searchQuery);
        binding.getRoot().getContext().startActivity(intent);
    }



    private void loadRecipes() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Recipes");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipes.add(recipe);
                    }
                    loadFavRecipes(recipes);
                    loadPopularRecipes(recipes);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "onCancelled: " + error.getMessage());
            }
        });
    }
    // Load and display favorite recipes
    private void loadFavRecipes(List<Recipe> recipes) {
        List<Recipe> favRecipes = new ArrayList<>();
        List<Recipe> availableRecipes = new ArrayList<>(recipes); // Make temp lít
        // Add sample popular recipes
        for (int i = 0; i < 5 && !availableRecipes.isEmpty(); i++) {
            int randomIndex = (int) (Math.random() * availableRecipes.size());
            favRecipes.add(availableRecipes.get(randomIndex));
            availableRecipes.remove(randomIndex); // Loại bỏ công thức đã chọn
        }
        // Set up RecyclerView adapter
        RecipeAdapter adapter = (RecipeAdapter) binding.rvFavs.getAdapter();
        if (adapter != null) {
            adapter.setRecipeList(favRecipes);
        }
    }

    // Load and display popular recipes
    private void loadPopularRecipes(List<Recipe> recipes) {
        List<Recipe> popularRecipes  = new ArrayList<>();
        List<Recipe> availableRecipes = new ArrayList<>(recipes); // Make temp lít
        // Add sample popular recipes
        for (int i = 0; i < 5 && !availableRecipes.isEmpty(); i++) {
            int randomIndex = (int) (Math.random() * availableRecipes.size());
            popularRecipes .add(availableRecipes.get(randomIndex));
            availableRecipes.remove(randomIndex); // Loại bỏ công thức đã chọn
        }
        // Set up RecyclerView adapter
        RecipeAdapter adapter = (RecipeAdapter) binding.rvPopulars.getAdapter();
        if (adapter != null) {
            adapter.setRecipeList(popularRecipes );
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
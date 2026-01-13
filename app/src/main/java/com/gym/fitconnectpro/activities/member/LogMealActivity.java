package com.gym.fitconnectpro.activities.member;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.dao.MealLogDAO;
import com.gym.fitconnectpro.dao.MemberDashboardDAO;
import com.gym.fitconnectpro.models.Food;
import com.gym.fitconnectpro.models.MealPlanFood;
import com.gym.fitconnectpro.services.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogMealActivity extends AppCompatActivity {

    private MealLogDAO mealLogDAO;
    private Session session;
    private int memberId;
    
    private TextView tvDate;
    private Spinner spinnerMealType;
    private EditText etSearchFood;
    private ImageButton btnSearch;
    private RecyclerView rvSearchResults;
    private RecyclerView rvAddedItems;
    private TextInputEditText etNotes;
    private TextView tvTotalCalories, tvTotalMacros;
    private Button btnSaveMeal;
    
    private List<MealPlanFood> addedItems = new ArrayList<>();
    private AddedItemsAdapter addedItemsAdapter;
    private SearchResultsAdapter searchResultsAdapter;
    
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_meal);
        
        session = Session.getInstance(this);
        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        // Fetch Member ID using User ID
        MemberDashboardDAO dashboardDAO = new MemberDashboardDAO(this);
        int userId = session.getUserId();
        java.util.Map<String, String> memberInfo = dashboardDAO.getMemberHeaderInfo(userId);
        
        if (memberInfo != null && memberInfo.containsKey("member_id")) {
            memberId = Integer.parseInt(memberInfo.get("member_id"));
        } else {
            Toast.makeText(this, "Error loading member profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        mealLogDAO = new MealLogDAO(this);
        
        initViews();
        setupListeners();
        
        // Pass meal type from intent if available
        String preSelectedType = getIntent().getStringExtra("MEAL_TYPE");
        if (preSelectedType != null) {
            String[] types = getResources().getStringArray(R.array.meal_types);
            for (int i = 0; i < types.length; i++) {
                if (types[i].equalsIgnoreCase(preSelectedType)) {
                    spinnerMealType.setSelection(i);
                    break;
                }
            }
        }
        
        updateDateDisplay();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        tvDate = findViewById(R.id.tvDate);
        spinnerMealType = findViewById(R.id.spinnerMealType);
        etSearchFood = findViewById(R.id.etSearchFood);
        btnSearch = findViewById(R.id.btnSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        rvAddedItems = findViewById(R.id.rvAddedItems);
        etNotes = findViewById(R.id.etNotes);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalMacros = findViewById(R.id.tvTotalMacros);
        btnSaveMeal = findViewById(R.id.btnSaveMeal);
        
        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.meal_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);
        
        // Setup RecyclerViews
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvAddedItems.setLayoutManager(new LinearLayoutManager(this));
        
        searchResultsAdapter = new SearchResultsAdapter();
        rvSearchResults.setAdapter(searchResultsAdapter);
        
        addedItemsAdapter = new AddedItemsAdapter();
        rvAddedItems.setAdapter(addedItemsAdapter);
    }
    
    private void setupListeners() {
        btnSearch.setOnClickListener(v -> performSearch());
        
        etSearchFood.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
        
        btnSaveMeal.setOnClickListener(v -> saveMeal());
    }
    
    private void performSearch() {
        String query = etSearchFood.getText().toString().trim();
        if (!query.isEmpty()) {
            List<Food> results = mealLogDAO.searchFoods(query);
            searchResultsAdapter.setFoods(results);
            if (results.isEmpty()) {
                Toast.makeText(this, "No foods found", Toast.LENGTH_SHORT).show();
                rvSearchResults.setVisibility(View.GONE);
            } else {
                rvSearchResults.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void addFoodItem(Food food) {
        // Create new item with default quantity 1.0
        MealPlanFood item = new MealPlanFood();
        item.setFood(food);
        item.setFoodId(food.getId());
        item.setQuantity(1.0);
        
        addedItems.add(item);
        addedItemsAdapter.notifyItemInserted(addedItems.size() - 1);
        updateTotals();
        
        // Clear search
        etSearchFood.setText("");
        rvSearchResults.setVisibility(View.GONE);
        Toast.makeText(this, food.getName() + " added", Toast.LENGTH_SHORT).show();
    }
    
    private void removeFoodItem(int position) {
        addedItems.remove(position);
        addedItemsAdapter.notifyItemRemoved(position);
        updateTotals();
    }
    
    private void updateTotals() {
        int cals = 0;
        double prot = 0, carbs = 0, fats = 0;
        
        for (MealPlanFood item : addedItems) {
            double qty = item.getQuantity();
            Food f = item.getFood();
            
            cals += (int)(f.getCalories() * qty);
            prot += f.getProtein() * qty;
            carbs += f.getCarbs() * qty;
            fats += f.getFats() * qty;
        }
        
        tvTotalCalories.setText(cals + " cal");
        tvTotalMacros.setText(String.format(Locale.getDefault(), 
                "P: %.1fg  C: %.1fg  F: %.1fg", prot, carbs, fats));
    }
    
    private void saveMeal() {
        if (addedItems.isEmpty()) {
            Toast.makeText(this, "Add at least one food item", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String date = dbDateFormat.format(selectedDate.getTime());
        String type = spinnerMealType.getSelectedItem().toString();
        String notes = etNotes.getText() != null ? etNotes.getText().toString() : "";
        
        boolean success = mealLogDAO.logMeal(memberId, date, type, addedItems, notes);
        
        if (success) {
            Toast.makeText(this, "Meal logged successfully!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to log meal", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateDisplay() {
        tvDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // --- Adapters ---
    
    // Search Results Adapter
    class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
        private List<Food> foods = new ArrayList<>();

        void setFoods(List<Food> foods) {
            this.foods = foods;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_meal_log_food, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Food food = foods.get(position);
            holder.tvName.setText(food.getName());
            holder.tvUnit.setText(food.getServingUnit());
            holder.tvCalories.setText(food.getCalories() + " cal");
            holder.layoutQuantity.setVisibility(View.GONE);
            
            holder.btnAdd.setOnClickListener(v -> addFoodItem(food));
        }

        @Override
        public int getItemCount() {
            return foods.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvUnit, tvCalories;
            ImageButton btnAdd;
            LinearLayout layoutQuantity;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvFoodName);
                tvUnit = itemView.findViewById(R.id.tvServingUnit);
                tvCalories = itemView.findViewById(R.id.tvCalories);
                btnAdd = itemView.findViewById(R.id.btnAddOrRemove);
                layoutQuantity = itemView.findViewById(R.id.layoutQuantity);
            }
        }
    }
    
    // Added Items Adapter (with Quantity Edit)
    class AddedItemsAdapter extends RecyclerView.Adapter<AddedItemsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_meal_log_food, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MealPlanFood item = addedItems.get(position);
            Food food = item.getFood();
            
            holder.tvName.setText(food.getName());
            holder.tvUnit.setText(food.getServingUnit());
            holder.tvCalories.setText((int)(food.getCalories() * item.getQuantity()) + " cal");
            
            // Change Add button to Remove button look
            holder.btnAdd.setImageResource(android.R.drawable.ic_delete);
            holder.btnAdd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFCDD2)); // Light Red
            holder.btnAdd.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFD32F2F)); // Red
            
            holder.btnAdd.setOnClickListener(v -> removeFoodItem(holder.getAdapterPosition()));
            
            // Show Quantity Input
            holder.layoutQuantity.setVisibility(View.VISIBLE);
            holder.etQuantity.setText(String.valueOf(item.getQuantity()));
            
            // Handle Quantity Change
            holder.etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        double qty = Double.parseDouble(holder.etQuantity.getText().toString());
                        if (qty > 0) {
                            item.setQuantity(qty);
                            notifyItemChanged(holder.getAdapterPosition());
                            updateTotals();
                        }
                    } catch (NumberFormatException e) {
                        holder.etQuantity.setText(String.valueOf(item.getQuantity()));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return addedItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvUnit, tvCalories;
            ImageButton btnAdd;
            LinearLayout layoutQuantity;
            EditText etQuantity;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvFoodName);
                tvUnit = itemView.findViewById(R.id.tvServingUnit);
                tvCalories = itemView.findViewById(R.id.tvCalories);
                btnAdd = itemView.findViewById(R.id.btnAddOrRemove);
                layoutQuantity = itemView.findViewById(R.id.layoutQuantity);
                etQuantity = itemView.findViewById(R.id.etQuantity);
            }
        }
    }
}

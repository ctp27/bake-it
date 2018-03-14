package com.ctp.bakeit.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctp.bakeit.R;
import com.ctp.bakeit.provider.BakeItContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 2/25/18.
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Cursor cursor;
    private RecipeAdapterCallback callback;


    public RecipeAdapter(Cursor cursor, RecipeAdapterCallback callback) {
        this.cursor = cursor;
        this.callback = callback;
    }

    public interface RecipeAdapterCallback{
        void onRecipeClicked(int recipeId);
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.recipe_list_row_item,parent,false);
        return new RecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {

        cursor.moveToPosition(position);

        int ingredientCount = cursor.getInt(cursor.getColumnIndex(BakeItContract.RecipeEntry.COLUMN_INGREDIENT_COUNT));
        String recipeName = cursor.getString(cursor.getColumnIndex(BakeItContract.RecipeEntry.COLUMN_NAME));
        int recipeId = cursor.getInt(cursor.getColumnIndex(BakeItContract.RecipeEntry._ID));

        holder.itemView.setTag(recipeId);
        holder.recipeNameView.setText(recipeName);
        holder.ingredientCountView.setText(Integer.toString(ingredientCount));
    }

    @Override
    public int getItemCount() {
        if(cursor == null){
            return 0;
        }
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor){

        if(cursor!=null){
            cursor.close();
            cursor=null;
        }
        cursor = newCursor;
        notifyDataSetChanged();

    }



    public class RecipeViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener{

        @BindView(R.id.ingredient_count)  TextView ingredientCountView;
        @BindView(R.id.recipe_name_view) TextView recipeNameView;
        @BindView(R.id.view_recipe_text_view)  TextView viewRecipeView;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int id = (int)itemView.getTag();
            callback.onRecipeClicked(id);
        }
    }
}

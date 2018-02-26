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
import com.ctp.bakeit.utils.BakeItUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by clinton on 2/25/18.
 */

public class RecipeStepsAdapter extends RecyclerView.Adapter<RecipeStepsAdapter.RecipeStepsViewHolder> {

    private Cursor cursor;
    private RecipeStepsAdapterCallback callback;

    public RecipeStepsAdapter(Cursor cursor, RecipeStepsAdapterCallback callback) {
        this.cursor = cursor;
        this.callback = callback;
    }

    public interface RecipeStepsAdapterCallback{
        void onRecipeStepClicked(int stepId);
    }

    @Override
    public RecipeStepsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.steps_list_item_view,parent,false);

        return new RecipeStepsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecipeStepsViewHolder holder, int position) {

        cursor.moveToPosition(position);
        long id = cursor.getLong(cursor.getColumnIndex(BakeItContract.StepEntry._ID));
        String shortDescription = cursor.getString(
                cursor.getColumnIndex(BakeItContract.StepEntry.COLUMN_SHORT_DESC));
        String description = cursor.getString(
                cursor.getColumnIndex(BakeItContract.StepEntry.COLUMN_DESC));
        shortDescription = position+1 + ".\t" + shortDescription;
        holder.shortDescription.setText(shortDescription);
        holder.description.setText(BakeItUtils.getFormattedDescriptionString(description,position));

        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        if(cursor == null)
            return 0;
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


    public class RecipeStepsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        @BindView(R.id.recipe_step_short_descripton)
        TextView shortDescription;

        @BindView(R.id.recipe_step_description)
        TextView description;

        public RecipeStepsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            long id = (long)itemView.getTag();
            callback.onRecipeStepClicked((int)id);
        }
    }
}
